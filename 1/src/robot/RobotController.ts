import * as THREE from 'three'
import { RobotBuilder } from './RobotBuilder'
import { MaterialSystem } from '../core/MaterialSystem'
import { AnimationStateMachine } from '../core/AnimationStateMachine'
import { RobotPart, RobotState, RobotAnimationState, PathPoint, Task } from '../types'

export class RobotController {
  public robotGroup: THREE.Group
  public parts: Map<string, RobotPart>
  private materialSystem: MaterialSystem
  private robotBuilder: RobotBuilder
  private animationStateMachine: AnimationStateMachine
  private originalPositions: Map<string, THREE.Vector3> = new Map()
  private originalRotations: Map<string, THREE.Euler> = new Map()
  private robotState: RobotState
  private isExploded: boolean = false
  private isTransparent: boolean = false
  private isMaintenance: boolean = false
  private showSensorViz: boolean = false
  private trajectoryHistory: THREE.Vector3[] = []
  private maxTrajectoryLength: number = 500
  private trajectoryLine: THREE.Line | null = null
  private currentPath: PathPoint[] = []
  private pathProgress: number = 0
  private moveSpeed: number = 3
  private rotationSpeed: number = 2
  private highlightedParts: Set<string> = new Set()
  private obstacleList: THREE.Object3D[] = []
  private robotBoundingBox: THREE.Box3 = new THREE.Box3()

  constructor(materialSystem: MaterialSystem) {
    this.materialSystem = materialSystem
    this.robotBuilder = new RobotBuilder(materialSystem)
    this.robotGroup = this.robotBuilder.build()
    this.parts = this.robotBuilder.getParts()
    
    this.animationStateMachine = new AnimationStateMachine()
    
    this.parts.forEach((part, id) => {
      this.originalPositions.set(id, part.originalPosition.clone())
    })

    this.robotState = {
      position: new THREE.Vector3(-8, 0, 0),
      rotation: 0,
      targetRotation: 0,
      batteryLevel: 85,
      liftHeight: 0,
      targetLiftHeight: 0,
      isCharging: false,
      isMoving: false,
      isAvoiding: false,
      hasPayload: false,
      currentAnimation: 'idle',
      previousAnimation: 'idle',
      speed: 1,
      pathIndex: 0
    }

    this.setupAnimations()
    this.createTrajectoryLine()
  }

  private setupAnimations() {
    this.animationStateMachine.onStateAnimate('moving', (progress) => {
      this.animateWheels(progress)
      this.animateLidar(progress)
      this.animateLeds(progress)
      this.updateMovement()
      this.consumeBattery()
    })

    this.animationStateMachine.onStateAnimate('turning', (progress) => {
      this.animateWheels(progress * 2)
      this.updateRotation()
    })

    this.animationStateMachine.onStateAnimate('lifting', (progress) => {
      this.animateLift(progress, true)
    })

    this.animationStateMachine.onStateAnimate('lowering', (progress) => {
      this.animateLift(progress, false)
    })

    this.animationStateMachine.onStateAnimate('charging', (progress) => {
      this.animateCharging(progress)
    })

    this.animationStateMachine.onStateAnimate('avoiding', (progress) => {
      this.animateAvoiding(progress)
    })

    this.animationStateMachine.onStateAnimate('pickingUp', (progress) => {
      this.animatePickup(progress)
    })

    this.animationStateMachine.onStateAnimate('droppingOff', (progress) => {
      this.animateDropoff(progress)
    })

    this.animationStateMachine.onStateAnimate('returning', (progress) => {
      this.animateWheels(progress)
      this.animateLidar(progress)
      this.updateMovement()
      this.consumeBattery()
    })

    this.animationStateMachine.onStateAnimate('fault', (progress) => {
      this.animateFault(progress)
    })

    this.animationStateMachine.onStateAnimate('paused', () => {
      this.animateLidar(this.animationStateMachine.getProgress())
    })

    this.animationStateMachine.onStateAnimate('idle', (progress) => {
      this.animateLidar(progress * 0.5)
      this.animateBreathing(progress)
      this.updateTrajectory()
    })

    this.animationStateMachine.onStateEnter('charging', () => {
      this.robotState.isCharging = true
    })

    this.animationStateMachine.onStateExit('charging', () => {
      this.robotState.isCharging = false
    })
  }

  private createTrajectoryLine() {
    const geometry = new THREE.BufferGeometry()
    const positions = new Float32Array(this.maxTrajectoryLength * 3)
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
    geometry.setDrawRange(0, 0)

    const material = new THREE.LineBasicMaterial({
      color: 0x00ff00,
      transparent: true,
      opacity: 0.6,
      linewidth: 2
    })

    this.trajectoryLine = new THREE.Line(geometry, material)
    this.trajectoryLine.name = 'trajectoryLine'
    this.trajectoryLine.visible = false
    this.robotGroup.parent?.add(this.trajectoryLine)
  }

  private animateWheels(progress: number) {
    for (let i = 0; i < 4; i++) {
      const wheel = this.parts.get(`wheel_${i}`)?.mesh
      if (wheel) {
        const rotor = wheel.getObjectByName(`wheelRotor_${i}`)
        if (rotor) {
          rotor.rotation.y = progress * 8 * this.robotState.speed
        }
      }
    }
  }

  private animateLidar(progress: number) {
    const lidar = this.parts.get('lidar')?.mesh
    if (lidar) {
      const scanRing = lidar.getObjectByName('lidarScanRing')
      if (scanRing) {
        scanRing.rotation.z = progress * 6
      }
    }

    const builder = this.parts.get('lidar')?.mesh.parent as any
    if (builder && builder.userData && typeof builder.userData.updateLidarScan === 'function') {
      builder.userData.updateLidarScan(progress * 10)
    }
  }

  private animateLeds(progress: number) {
    for (let i = 0; i < 4; i++) {
      const strip = this.parts.get(`lightStrip_${i}`)?.mesh
      if (strip) {
        strip.children.forEach((child, idx) => {
          if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
            const mat = child.material as THREE.MeshStandardMaterial
            mat.emissiveIntensity = 0.3 + Math.sin(progress * 4 + idx * 0.3) * 0.7
          }
        })
      }
    }
  }

  private animateLift(progress: number, isLifting: boolean) {
    const lift = this.parts.get('liftMechanism')?.mesh
    const tray = this.parts.get('payloadTray')?.mesh
    
    const targetHeight = isLifting ? 1 : 0
    const currentProgress = Math.min(1, progress / 2)
    const liftAmount = isLifting ? currentProgress : 1 - currentProgress
    
    if (lift) {
      lift.position.y = 0.6 + liftAmount * 0.8
      this.robotState.liftHeight = liftAmount
    }
    if (tray) {
      tray.position.y = 2.0 + liftAmount * 0.8
    }

    if (currentProgress >= 1) {
      if (isLifting) {
        this.robotState.hasPayload = true
        this.playAnimation('moving')
      } else {
        this.robotState.hasPayload = false
        this.playAnimation('idle')
      }
    }
  }

  private animateCharging(progress: number) {
    const batteryIndicator = this.parts.get('batteryIndicator')?.mesh
    if (batteryIndicator) {
      batteryIndicator.children.forEach((child, idx) => {
        if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
          const mat = child.material as THREE.MeshStandardMaterial
          const active = (Math.floor(progress * 3) + idx) % 4 < Math.ceil(this.robotState.batteryLevel / 25)
          mat.emissiveIntensity = active ? 0.6 + Math.sin(progress * 8) * 0.4 : 0.1
        }
      })
    }

    if (this.robotState.batteryLevel < 100) {
      this.robotState.batteryLevel = Math.min(100, this.robotState.batteryLevel + 0.1)
    } else {
      this.playAnimation('idle')
    }

    this.animateLids(progress)
  }

  private avoidPathPhase: number = 0
  private avoidStartPos: THREE.Vector3 = new THREE.Vector3()
  private avoidOffset: number = 0

  private animateAvoiding(progress: number) {
    if (this.avoidPathPhase === 0) {
      this.avoidStartPos.copy(this.robotGroup.position)
      this.avoidPathPhase = 1
      this.avoidOffset = 0
    }

    const totalDuration = 6
    const normalizedProgress = (progress % totalDuration) / totalDuration

    let lateralOffset = 0
    let forwardOffset = 0

    if (normalizedProgress < 0.15) {
      const t = normalizedProgress / 0.15
      const ease = t * t * (3 - 2 * t)
      lateralOffset = 0
      forwardOffset = ease * 0.3
    } else if (normalizedProgress < 0.35) {
      const t = (normalizedProgress - 0.15) / 0.2
      const ease = t * t * (3 - 2 * t)
      lateralOffset = ease * 1.8
      forwardOffset = 0.3 + t * 0.5
    } else if (normalizedProgress < 0.65) {
      const t = (normalizedProgress - 0.35) / 0.3
      lateralOffset = 1.8
      forwardOffset = 0.8 + t * 2.0
    } else if (normalizedProgress < 0.85) {
      const t = (normalizedProgress - 0.65) / 0.2
      const ease = t * t * (3 - 2 * t)
      lateralOffset = 1.8 * (1 - ease)
      forwardOffset = 2.8 + t * 0.5
    } else {
      const t = (normalizedProgress - 0.85) / 0.15
      const ease = t * t * (3 - 2 * t)
      lateralOffset = 0
      forwardOffset = 3.3 + ease * 0.2
    }

    const robotForward = new THREE.Vector3(
      Math.sin(this.robotState.rotation),
      0,
      Math.cos(this.robotState.rotation)
    )
    const robotRight = new THREE.Vector3(
      Math.cos(this.robotState.rotation),
      0,
      -Math.sin(this.robotState.rotation)
    )

    let nextPosition = this.avoidStartPos.clone()
      .add(robotForward.clone().multiplyScalar(forwardOffset))
      .add(robotRight.clone().multiplyScalar(lateralOffset))

    if (!this.checkCollision(nextPosition)) {
      this.robotGroup.position.copy(nextPosition)
      this.robotState.position.copy(this.robotGroup.position)
    }

    if (normalizedProgress > 0.15 && normalizedProgress < 0.35) {
      const t = (normalizedProgress - 0.15) / 0.2
      this.robotState.targetRotation = this.robotState.rotation + t * 0.4
      this.updateRotation()
    } else if (normalizedProgress > 0.65 && normalizedProgress < 0.85) {
      const t = (normalizedProgress - 0.65) / 0.2
      this.robotState.targetRotation = this.robotState.rotation - t * 0.4
      this.updateRotation()
    }

    this.animateWheels(progress)
    this.animateLeds(progress)
    this.consumeBattery()
    this.updateTrajectory()

    if (normalizedProgress >= 1.0) {
      this.avoidPathPhase = 0
      this.robotGroup.position.copy(this.avoidStartPos).add(
        new THREE.Vector3(Math.sin(this.robotState.rotation), 0, Math.cos(this.robotState.rotation)).multiplyScalar(3.5)
      )
      this.robotState.position.copy(this.robotGroup.position)
      this.playAnimation('idle')
    }
  }

  private pickupPhase: number = 0
  private pickupStartPos: THREE.Vector3 = new THREE.Vector3()

  private animatePickup(progress: number) {
    if (this.pickupPhase === 0) {
      this.pickupStartPos.copy(this.robotGroup.position)
      this.pickupPhase = 1
      this.robotState.taskProgress = 0
    }

    const totalDuration = 8
    const normalizedProgress = (progress % totalDuration) / totalDuration

    this.robotState.taskProgress = normalizedProgress * 100

    const ease = (t: number) => t * t * (3 - 2 * t)

    if (normalizedProgress < 0.2) {
      const t = normalizedProgress / 0.2
      const forwardOffset = ease(t) * 1.5
      const robotForward = new THREE.Vector3(
        Math.sin(this.robotState.rotation),
        0,
        Math.cos(this.robotState.rotation)
      )
      const nextPosition = this.pickupStartPos.clone()
        .add(robotForward.multiplyScalar(forwardOffset))
      
      if (!this.checkCollision(nextPosition)) {
        this.robotGroup.position.copy(nextPosition)
        this.robotState.position.copy(this.robotGroup.position)
      }
      this.animateWheels(progress)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.35) {
      const t = (normalizedProgress - 0.2) / 0.15
      this.animateLift(t * 0.15, true)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.4) {
      if (!this.robotBuilder.hasCargo()) {
        this.robotBuilder.setCargoVisible(true)
        this.robotState.hasPayload = true
      }
      this.animateLift(0.15, true)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.55) {
      const t = (normalizedProgress - 0.4) / 0.15
      this.animateLift(0.15 + t * 0.15, true)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.85) {
      const t = (normalizedProgress - 0.55) / 0.3
      const forwardOffset = 1.5 + ease(t) * 2.0
      const robotForward = new THREE.Vector3(
        Math.sin(this.robotState.rotation),
        0,
        Math.cos(this.robotState.rotation)
      )
      const nextPosition = this.pickupStartPos.clone()
        .add(robotForward.multiplyScalar(forwardOffset))
      
      if (!this.checkCollision(nextPosition)) {
        this.robotGroup.position.copy(nextPosition)
        this.robotState.position.copy(this.robotGroup.position)
      }
      this.animateLift(0.3, true)
      this.animateWheels(progress)
      this.animateLidar(progress)
    } else {
      if (normalizedProgress >= 1.0) {
        this.pickupPhase = 0
        this.robotState.taskProgress = 100
        this.playAnimation('moving')
      }
    }

    this.animateLeds(progress)
    this.consumeBattery()
    this.updateTrajectory()
  }

  private dropoffPhase: number = 0
  private dropoffStartPos: THREE.Vector3 = new THREE.Vector3()

  private animateDropoff(progress: number) {
    if (this.dropoffPhase === 0) {
      this.dropoffStartPos.copy(this.robotGroup.position)
      this.dropoffPhase = 1
      this.robotState.taskProgress = 0
    }

    const totalDuration = 8
    const normalizedProgress = (progress % totalDuration) / totalDuration

    this.robotState.taskProgress = normalizedProgress * 100

    const ease = (t: number) => t * t * (3 - 2 * t)

    if (normalizedProgress < 0.2) {
      const t = normalizedProgress / 0.2
      const forwardOffset = ease(t) * 1.5
      const robotForward = new THREE.Vector3(
        Math.sin(this.robotState.rotation),
        0,
        Math.cos(this.robotState.rotation)
      )
      const nextPosition = this.dropoffStartPos.clone()
        .add(robotForward.multiplyScalar(forwardOffset))
      
      if (!this.checkCollision(nextPosition)) {
        this.robotGroup.position.copy(nextPosition)
        this.robotState.position.copy(this.robotGroup.position)
      }
      this.animateLift(0.3, true)
      this.animateWheels(progress)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.35) {
      const t = (normalizedProgress - 0.2) / 0.15
      this.animateLift(0.3 - t * 0.15, true)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.4) {
      if (this.robotBuilder.hasCargo()) {
        this.robotBuilder.setCargoVisible(false)
        this.robotState.hasPayload = false
      }
      this.animateLift(0.15, true)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.55) {
      const t = (normalizedProgress - 0.4) / 0.15
      this.animateLift(0.15 * (1 - t), true)
      this.animateLidar(progress)
    } else if (normalizedProgress < 0.85) {
      const t = (normalizedProgress - 0.55) / 0.3
      const forwardOffset = 1.5 - ease(t) * 2.0
      const robotForward = new THREE.Vector3(
        Math.sin(this.robotState.rotation),
        0,
        Math.cos(this.robotState.rotation)
      )
      const nextPosition = this.dropoffStartPos.clone()
        .add(robotForward.multiplyScalar(Math.max(forwardOffset, -0.5)))
      
      if (!this.checkCollision(nextPosition)) {
        this.robotGroup.position.copy(nextPosition)
        this.robotState.position.copy(this.robotGroup.position)
      }
      this.animateWheels(progress)
      this.animateLidar(progress)
    } else {
      if (normalizedProgress >= 1.0) {
        this.dropoffPhase = 0
        this.robotState.taskProgress = 100
        this.playAnimation('idle')
      }
    }

    this.animateLeds(progress)
    this.consumeBattery()
    this.updateTrajectory()
  }

  private animateFault(progress: number) {
    for (let i = 0; i < 4; i++) {
      const strip = this.parts.get(`lightStrip_${i}`)?.mesh
      if (strip) {
        strip.children.forEach((child) => {
          if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
            const mat = child.material as THREE.MeshStandardMaterial
            mat.color.setHex(0xff0000)
            mat.emissive.setHex(0xff0000)
            mat.emissiveIntensity = Math.abs(Math.sin(progress * 10))
          }
        })
      }
    }
  }

  private animateBreathing(progress: number) {
    for (let i = 0; i < 4; i++) {
      const strip = this.parts.get(`lightStrip_${i}`)?.mesh
      if (strip) {
        strip.children.forEach((child) => {
          if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
            const mat = child.material as THREE.MeshStandardMaterial
            mat.emissiveIntensity = 0.15 + Math.sin(progress * 1.5) * 0.15
          }
        })
      }
    }
  }

  private updateMovement() {
    if (this.currentPath.length === 0) return

    const currentPoint = this.currentPath[this.robotState.pathIndex]
    if (!currentPoint) {
      this.playAnimation('idle')
      return
    }

    const targetPos = currentPoint.position
    const direction = new THREE.Vector3()
      .subVectors(targetPos, this.robotState.position)
      .normalize()

    const distance = this.robotState.position.distanceTo(targetPos)
    
    if (distance < 0.1) {
      this.robotState.position.copy(targetPos)
      
      if (currentPoint.action) {
        switch (currentPoint.action) {
          case 'lift':
            this.playAnimation('lifting')
            break
          case 'lower':
            this.playAnimation('lowering')
            break
          case 'wait':
            setTimeout(() => {
              this.advancePath()
            }, (currentPoint.duration || 1) * 1000)
            return
        }
      }
      
      this.advancePath()
    } else {
      const moveAmount = this.moveSpeed * 0.016 * this.robotState.speed
      let nextPosition = this.robotState.position.clone().add(
        direction.clone().multiplyScalar(moveAmount)
      )

      if (this.checkCollision(nextPosition)) {
        const avoidDir = this.findAvoidanceDirection(
          this.robotState.position,
          direction
        )

        if (avoidDir) {
          nextPosition = this.robotState.position.clone().add(
            avoidDir.clone().multiplyScalar(moveAmount * 0.5)
          )
          if (!this.checkCollision(nextPosition)) {
            this.robotState.position.copy(nextPosition)
            this.robotState.targetRotation = Math.atan2(avoidDir.x, avoidDir.z)
            this.updateRotation()
          }
        }
      } else {
        this.robotState.position.copy(nextPosition)
        this.robotState.targetRotation = Math.atan2(direction.x, direction.z)
        this.updateRotation()
      }
    }

    this.robotGroup.position.copy(this.robotState.position)
    this.robotGroup.rotation.y = this.robotState.rotation
    this.updateTrajectory()
  }

  private updateRotation() {
    const rotationDiff = this.robotState.targetRotation - this.robotState.rotation
    const normalizedDiff = Math.atan2(Math.sin(rotationDiff), Math.cos(rotationDiff))
    
    if (Math.abs(normalizedDiff) > 0.01) {
      this.robotState.rotation += normalizedDiff * this.rotationSpeed * 0.1
      this.robotGroup.rotation.y = this.robotState.rotation
    }
  }

  private advancePath() {
    if (this.robotState.pathIndex < this.currentPath.length - 1) {
      this.robotState.pathIndex++
    } else {
      this.currentPath = []
      this.robotState.pathIndex = 0
      this.playAnimation('idle')
    }
  }

  private consumeBattery() {
    if (this.robotState.batteryLevel > 0) {
      this.robotState.batteryLevel -= 0.002 * this.robotState.speed
      
      if (this.robotState.batteryLevel < 20 && !this.robotState.isCharging) {
        if (this.robotState.batteryLevel < 5) {
          this.animationStateMachine.triggerFault({
            code: 'E001',
            message: '电池严重不足',
            severity: 'critical',
            affectedParts: ['batteryCompartment']
          })
        }
      }
    }
  }

  private updateTrajectory() {
    this.trajectoryHistory.push(this.robotState.position.clone())
    
    if (this.trajectoryHistory.length > this.maxTrajectoryLength) {
      this.trajectoryHistory.shift()
    }

    if (this.trajectoryLine) {
      const positions = this.trajectoryLine.geometry.attributes.position.array as Float32Array
      this.trajectoryHistory.forEach((point, i) => {
        positions[i * 3] = point.x
        positions[i * 3 + 1] = point.y + 0.05
        positions[i * 3 + 2] = point.z
      })
      this.trajectoryLine.geometry.attributes.position.needsUpdate = true
      this.trajectoryLine.geometry.setDrawRange(0, this.trajectoryHistory.length)
    }
  }

  private animateLids(progress: number) {
    const contactPlate = this.parts.get('chargingContacts')?.mesh
    if (contactPlate) {
      contactPlate.children.forEach((child) => {
        if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
          const mat = child.material as THREE.MeshStandardMaterial
          mat.emissiveIntensity = 0.3 + Math.sin(progress * 5) * 0.7
        }
      })
    }
  }

  update(deltaTime: number) {
    this.animationStateMachine.update(deltaTime)
    this.robotState.currentAnimation = this.animationStateMachine.getState()
    this.robotState.previousAnimation = this.animationStateMachine.getPreviousState()
    this.robotState.isMoving = this.animationStateMachine.getState() === 'moving' || 
                               this.animationStateMachine.getState() === 'returning'
  }

  playAnimation(animation: string) {
    const validStates: RobotAnimationState[] = [
      'idle', 'moving', 'turning', 'lifting', 'lowering', 
      'charging', 'avoiding', 'pickingUp', 'droppingOff', 
      'returning', 'fault', 'paused'
    ]
    if (validStates.includes(animation as RobotAnimationState)) {
      this.animationStateMachine.setState(animation as RobotAnimationState)
    }
  }

  setPath(path: PathPoint[]) {
    this.currentPath = path
    this.robotState.pathIndex = 0
    this.pathProgress = 0
  }

  getPath(): PathPoint[] {
    return [...this.currentPath]
  }

  moveTo(target: THREE.Vector3) {
    this.currentPath = [{ position: target }]
    this.robotState.pathIndex = 0
    this.playAnimation('moving')
  }

  followPath(path: PathPoint[]) {
    this.setPath(path)
    this.playAnimation('moving')
  }

  pauseAnimation() {
    this.animationStateMachine.setPaused(true)
  }

  resumeAnimation() {
    this.animationStateMachine.setPaused(false)
  }

  setSpeed(speed: number) {
    this.robotState.speed = Math.max(0.1, Math.min(3, speed))
    this.animationStateMachine.setSpeed(speed)
  }

  triggerFault(code: string, message: string, severity: 'warning' | 'error' | 'critical', affectedParts: string[]) {
    this.animationStateMachine.triggerFault({ code, message, severity, affectedParts })
    affectedParts.forEach(partId => this.highlightPart(partId, true))
  }

  clearFault() {
    this.highlightedParts.forEach(partId => this.highlightPart(partId, false))
    this.highlightedParts.clear()
    this.animationStateMachine.clearFault()
  }

  toggleExplodedView() {
    this.isExploded = !this.isExploded
    const explodeFactor = this.isExploded ? 2 : 0

    this.parts.forEach((part, id) => {
      const originalPos = this.originalPositions.get(id)
      if (originalPos) {
        const direction = new THREE.Vector3()
          .subVectors(part.mesh.position, new THREE.Vector3(0, 1, 0))
          .normalize()
        
        const targetPos = originalPos.clone().add(direction.multiplyScalar(explodeFactor))
        part.mesh.position.lerp(targetPos, 0.1)
      }
    })
  }

  toggleTransparentShell() {
    this.isTransparent = !this.isTransparent
    const shell = this.parts.get('outerShell')?.mesh
    
    if (shell) {
      shell.traverse((child) => {
        if (child instanceof THREE.Mesh && child.material) {
          const materials = Array.isArray(child.material) ? child.material : [child.material]
          materials.forEach(mat => {
            mat.transparent = this.isTransparent
            mat.opacity = this.isTransparent ? 0.25 : 1
          })
        }
      })
    }
  }

  toggleMaintenanceMode() {
    this.isMaintenance = !this.isMaintenance
    
    if (this.isMaintenance) {
      const shell = this.parts.get('outerShell')?.mesh
      if (shell) {
        shell.position.y += 0.8
        shell.rotation.x = -0.4
      }
      
      const batteryCover = this.parts.get('batteryCompartment')?.mesh?.getObjectByName('batteryCover')
      if (batteryCover) {
        batteryCover.position.y += 0.3
        batteryCover.rotation.x = 0.5
      }
    } else {
      this.resetPartPosition('outerShell')
      this.resetPartPosition('batteryCompartment')
    }
  }

  private resetPartPosition(partId: string) {
    const part = this.parts.get(partId)?.mesh
    const originalPos = this.originalPositions.get(partId)
    if (part && originalPos) {
      part.position.copy(originalPos)
      part.rotation.set(0, 0, 0)
    }
  }

  toggleSensorVisualizations() {
    this.showSensorViz = !this.showSensorViz
    const vizGroup = this.robotGroup.getObjectByName('sensorVisualizations')
    if (vizGroup) {
      vizGroup.visible = this.showSensorViz
    }
  }

  toggleTrajectory(show: boolean) {
    if (this.trajectoryLine) {
      this.trajectoryLine.visible = show
    }
  }

  clearTrajectory() {
    this.trajectoryHistory = []
    if (this.trajectoryLine) {
      this.trajectoryLine.geometry.setDrawRange(0, 0)
    }
  }

  highlightPart(partId: string, highlight: boolean) {
    const part = this.parts.get(partId)?.mesh
    if (part) {
      part.traverse((child) => {
        if (child instanceof THREE.Mesh && child.material) {
          const materials = Array.isArray(child.material) ? child.material : [child.material]
          materials.forEach(mat => {
            if ('emissive' in mat) {
              const stdMat = mat as THREE.MeshStandardMaterial
              if (highlight) {
                stdMat.emissive.setHex(0xff0000)
                stdMat.emissiveIntensity = 0.5
              } else {
                stdMat.emissive.setHex(0x000000)
                stdMat.emissiveIntensity = 0
              }
            }
          })
        }
      })
    }
  }

  getHighlightedParts(): string[] {
    return [...this.highlightedParts]
  }

  getState(): RobotState {
    return { ...this.robotState }
  }

  getPartById(id: string): RobotPart | undefined {
    return this.parts.get(id)
  }

  setBatteryLevel(level: number) {
    this.robotState.batteryLevel = Math.max(0, Math.min(100, level))
  }

  getAnimationStateMachine(): AnimationStateMachine {
    return this.animationStateMachine
  }

  getTrajectoryHistory(): THREE.Vector3[] {
    return [...this.trajectoryHistory]
  }

  registerObstacles(obstacles: THREE.Object3D[]) {
    this.obstacleList = obstacles
  }

  addObstacle(obstacle: THREE.Object3D) {
    if (!this.obstacleList.includes(obstacle)) {
      this.obstacleList.push(obstacle)
    }
  }

  removeObstacle(obstacle: THREE.Object3D) {
    const index = this.obstacleList.indexOf(obstacle)
    if (index > -1) {
      this.obstacleList.splice(index, 1)
    }
  }

  clearObstacles() {
    this.obstacleList = []
  }

  private checkCollision(nextPosition: THREE.Vector3): boolean {
    const robotHalfSize = new THREE.Vector3(1.1, 1.2, 0.9)
    const robotBox = new THREE.Box3(
      new THREE.Vector3(
        nextPosition.x - robotHalfSize.x,
        0,
        nextPosition.z - robotHalfSize.z
      ),
      new THREE.Vector3(
        nextPosition.x + robotHalfSize.x,
        robotHalfSize.y,
        nextPosition.z + robotHalfSize.z
      )
    )

    const safetyMargin = 0.3

    for (const obstacle of this.obstacleList) {
      if (!obstacle.visible) continue

      const obstacleBox = new THREE.Box3().setFromObject(obstacle)

      obstacleBox.min.x -= safetyMargin
      obstacleBox.min.z -= safetyMargin
      obstacleBox.max.x += safetyMargin
      obstacleBox.max.z += safetyMargin

      if (robotBox.intersectsBox(obstacleBox)) {
        return true
      }
    }

    return false
  }

  private findAvoidanceDirection(
    currentPos: THREE.Vector3,
    desiredDir: THREE.Vector3
  ): THREE.Vector3 | null {
    const testDirections = [
      new THREE.Vector3(desiredDir.z, 0, -desiredDir.x),
      new THREE.Vector3(-desiredDir.z, 0, desiredDir.x),
      new THREE.Vector3(desiredDir.x + desiredDir.z * 0.5, 0, desiredDir.z - desiredDir.x * 0.5).normalize(),
      new THREE.Vector3(desiredDir.x - desiredDir.z * 0.5, 0, desiredDir.z + desiredDir.x * 0.5).normalize()
    ]

    for (const dir of testDirections) {
      const testPos = currentPos.clone().add(dir.clone().multiplyScalar(0.5))
      if (!this.checkCollision(testPos)) {
        return dir
      }
    }

    return null
  }

  dispose() {
    if (this.trajectoryLine) {
      this.trajectoryLine.geometry.dispose()
      ;(this.trajectoryLine.material as THREE.Material).dispose()
    }

    this.robotGroup.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        child.geometry.dispose()
        if (Array.isArray(child.material)) {
          child.material.forEach(m => m.dispose())
        } else {
          child.material.dispose()
        }
      }
    })
  }
}
