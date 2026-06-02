import * as THREE from 'three'
import { RobotBuilder } from './RobotBuilder'
import { MaterialSystem } from '../core/MaterialSystem'
import { AnimationStateMachine } from '../core/AnimationStateMachine'
import { RobotPart, RobotState } from '../types'

export class RobotController {
  public robotGroup: THREE.Group
  public parts: Map<string, RobotPart>
  private materialSystem: MaterialSystem
  private animationStateMachine: AnimationStateMachine
  private originalPositions: Map<string, THREE.Vector3> = new Map()
  private robotState: RobotState
  private isExploded: boolean = false
  private isTransparent: boolean = false
  private isMaintenance: boolean = false

  constructor(materialSystem: MaterialSystem) {
    this.materialSystem = materialSystem
    const builder = new RobotBuilder(materialSystem)
    this.robotGroup = builder.build()
    this.parts = builder.getParts()
    
    this.animationStateMachine = new AnimationStateMachine()
    
    this.parts.forEach((part, id) => {
      this.originalPositions.set(id, part.originalPosition.clone())
    })

    this.robotState = {
      position: new THREE.Vector3(0, 0, 0),
      rotation: 0,
      batteryLevel: 85,
      liftHeight: 0,
      isCharging: false,
      isMoving: false,
      isAvoiding: false,
      hasPayload: false,
      currentAnimation: 'idle'
    }

    this.setupAnimations()
  }

  private setupAnimations() {
    this.animationStateMachine.onStateAnimate('moving', (progress) => {
      this.animateWheels(progress)
      this.animateLidar(progress)
      this.animateLeds(progress)
    })

    this.animationStateMachine.onStateAnimate('turning', (progress) => {
      this.animateWheels(progress * 2)
      this.robotGroup.rotation.y = Math.sin(progress * 0.5) * 0.3
    })

    this.animationStateMachine.onStateAnimate('lifting', (progress) => {
      this.animateLift(progress)
    })

    this.animationStateMachine.onStateAnimate('charging', (progress) => {
      this.animateCharging(progress)
    })

    this.animationStateMachine.onStateAnimate('avoiding', (progress) => {
      this.animateAvoiding(progress)
    })

    this.animationStateMachine.onStateAnimate('idle', (progress) => {
      this.animateLidar(progress * 0.5)
      this.animateBreathing(progress)
    })
  }

  private animateWheels(progress: number) {
    for (let i = 0; i < 4; i++) {
      const wheel = this.parts.get(`wheel_${i}`)?.mesh
      if (wheel) {
        wheel.children.forEach(child => {
          if (child instanceof THREE.Mesh && child.geometry instanceof THREE.TorusGeometry) {
            child.rotation.x = progress * 10
          }
        })
      }
    }
  }

  private animateLidar(progress: number) {
    const lidar = this.parts.get('lidar')?.mesh
    if (lidar) {
      const scanRing = lidar.getObjectByName('lidarScanRing')
      if (scanRing) {
        scanRing.rotation.z = progress * 5
      }
    }
  }

  private animateLeds(progress: number) {
    for (let i = 0; i < 4; i++) {
      const strip = this.parts.get(`lightStrip_${i}`)?.mesh
      if (strip) {
        strip.children.forEach((child, idx) => {
          if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
            const mat = child.material as THREE.MeshStandardMaterial
            mat.emissiveIntensity = 0.3 + Math.sin(progress * 3 + idx * 0.5) * 0.7
          }
        })
      }
    }
  }

  private animateLift(progress: number) {
    const lift = this.parts.get('liftMechanism')?.mesh
    const tray = this.parts.get('payloadTray')?.mesh
    
    const liftAmount = Math.sin(progress) * 0.5 + 0.5
    
    if (lift) {
      lift.position.y = 0.6 + liftAmount * 0.8
      this.robotState.liftHeight = liftAmount
    }
    if (tray) {
      tray.position.y = 2.0 + liftAmount * 0.8
    }
  }

  private animateCharging(progress: number) {
    const batteryIndicator = this.parts.get('batteryIndicator')?.mesh
    if (batteryIndicator) {
      batteryIndicator.children.forEach((child, idx) => {
        if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
          const mat = child.material as THREE.MeshStandardMaterial
          const active = (Math.floor(progress * 2) + idx) % 4 < Math.ceil(this.robotState.batteryLevel / 25)
          mat.emissiveIntensity = active ? 0.8 + Math.sin(progress * 10) * 0.2 : 0.1
        }
      })
    }

    if (this.robotState.batteryLevel < 100) {
      this.robotState.batteryLevel = Math.min(100, this.robotState.batteryLevel + 0.05)
    }
  }

  private animateAvoiding(progress: number) {
    this.robotGroup.position.x = Math.sin(progress * 2) * 0.5
    this.animateWheels(progress)
    this.animateLeds(progress)
  }

  private animateBreathing(progress: number) {
    for (let i = 0; i < 4; i++) {
      const strip = this.parts.get(`lightStrip_${i}`)?.mesh
      if (strip) {
        strip.children.forEach((child) => {
          if (child instanceof THREE.Mesh && 'emissiveIntensity' in child.material) {
            const mat = child.material as THREE.MeshStandardMaterial
            mat.emissiveIntensity = 0.2 + Math.sin(progress * 2) * 0.2
          }
        })
      }
    }
  }

  update(deltaTime: number) {
    this.animationStateMachine.update(deltaTime)
    this.robotState.currentAnimation = this.animationStateMachine.getState()
  }

  playAnimation(animation: string) {
    const validStates: RobotAnimationState[] = ['idle', 'moving', 'turning', 'lifting', 'charging', 'avoiding']
    if (validStates.includes(animation as RobotAnimationState)) {
      this.animationStateMachine.setState(animation as RobotAnimationState)
    }
  }

  toggleExplodedView() {
    this.isExploded = !this.isExploded
    const explodeFactor = this.isExploded ? 1.5 : 0

    this.parts.forEach((part, id) => {
      const originalPos = this.originalPositions.get(id)
      if (originalPos) {
        const direction = part.mesh.position.clone().normalize()
        part.mesh.position.lerp(
          originalPos.clone().add(direction.multiplyScalar(explodeFactor)),
          0.1
        )
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
            mat.opacity = this.isTransparent ? 0.3 : 1
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
        shell.position.y += 0.5
        shell.rotation.x = -0.3
      }
    } else {
      const shell = this.parts.get('outerShell')?.mesh
      const originalPos = this.originalPositions.get('outerShell')
      if (shell && originalPos) {
        shell.position.copy(originalPos)
        shell.rotation.set(0, 0, 0)
      }
    }
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

  dispose() {
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
