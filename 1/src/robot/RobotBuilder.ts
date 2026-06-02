import * as THREE from 'three'
import { MaterialSystem } from '../core/MaterialSystem'
import { RobotPart } from '../types'

export class RobotBuilder {
  private group: THREE.Group
  private materialSystem: MaterialSystem
  private parts: Map<string, RobotPart> = new Map()
  private originalPositions: Map<string, THREE.Vector3> = new Map()
  private originalRotations: Map<string, THREE.Euler> = new Map()
  private sensorVisualizations: THREE.Group | null = null

  constructor(materialSystem: MaterialSystem) {
    this.group = new THREE.Group()
    this.group.name = 'warehouseRobot'
    this.materialSystem = materialSystem
  }

  build(): THREE.Group {
    this.createChassis()
    this.createWheelAssembly()
    this.createLiftMechanism()
    this.createSensors()
    this.createLidar()
    this.createCameras()
    this.createIndicatorLights()
    this.createBatteryCompartment()
    this.createChargingContacts()
    this.createShellAndSeams()
    this.createPayloadTray()
    this.createCargo()
    this.createSensorVisualizations()

    return this.group
  }

  getParts(): Map<string, RobotPart> {
    return this.parts
  }

  getOriginalPositions(): Map<string, THREE.Vector3> {
    return this.originalPositions
  }

  getOriginalRotations(): Map<string, THREE.Euler> {
    return this.originalRotations
  }

  getSensorVisualizations(): THREE.Group | null {
    return this.sensorVisualizations
  }

  private registerPart(id: string, name: string, description: string, mesh: THREE.Object3D, category: string) {
    mesh.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        child.castShadow = true
        child.receiveShadow = true
        child.userData.partId = id
      }
    })
    mesh.userData.partId = id
    
    const part: RobotPart = {
      id,
      name,
      description,
      mesh,
      category,
      originalPosition: mesh.position.clone()
    }
    this.parts.set(id, part)
    this.originalPositions.set(id, mesh.position.clone())
    this.originalRotations.set(id, mesh.rotation.clone())
    this.group.add(mesh)
  }

  private createScrewHole(parent: THREE.Group, x: number, y: number, z: number, size: number = 0.025) {
    const screwHead = new THREE.Mesh(
      new THREE.CylinderGeometry(size, size * 1.3, size * 0.4, 8),
      this.materialSystem.clone('metalSteelBrushed')
    )
    screwHead.position.set(x, y, z)
    screwHead.rotation.x = Math.PI / 2
    parent.add(screwHead)

    const screwSlot = new THREE.Mesh(
      new THREE.BoxGeometry(size * 1.2, size * 0.1, size * 0.25),
      this.materialSystem.clone('plasticDark')
    )
    screwSlot.position.set(x, y, z)
    parent.add(screwSlot)
  }

  private createChassis() {
    const chassisGroup = new THREE.Group()
    chassisGroup.name = 'chassis'

    const mainBody = new THREE.Mesh(
      new THREE.BoxGeometry(1.8, 0.4, 1.4),
      this.materialSystem.clone('plasticDark')
    )
    mainBody.position.y = 0.3
    chassisGroup.add(mainBody)

    const topPlate = new THREE.Mesh(
      new THREE.BoxGeometry(1.9, 0.08, 1.5),
      this.materialSystem.clone('metalAluminum')
    )
    topPlate.position.y = 0.55
    chassisGroup.add(topPlate)

    const screwPositions = [
      [0.8, 0.59, 0.6], [-0.8, 0.59, 0.6],
      [0.8, 0.59, -0.6], [-0.8, 0.59, -0.6],
      [0, 0.59, 0.65], [0, 0.59, -0.65],
      [0.4, 0.59, 0], [-0.4, 0.59, 0]
    ]
    screwPositions.forEach(pos => {
      this.createScrewHole(chassisGroup, pos[0], pos[1], pos[2], 0.02)
    })

    const bottomPlate = new THREE.Mesh(
      new THREE.BoxGeometry(1.85, 0.06, 1.45),
      this.materialSystem.clone('metalSteel')
    )
    bottomPlate.position.y = 0.05
    chassisGroup.add(bottomPlate)

    for (let i = 0; i < 4; i++) {
      const rib = new THREE.Mesh(
        new THREE.BoxGeometry(0.04, 0.3, 1.3),
        this.materialSystem.clone('metalSteelBrushed')
      )
      rib.position.set(-0.6 + i * 0.4, 0.3, 0)
      chassisGroup.add(rib)
    }

    const cornerRadius = 0.12
    const cornerPositions = [
      [0.85, 0.3, 0.65], [-0.85, 0.3, 0.65],
      [0.85, 0.3, -0.65], [-0.85, 0.3, -0.65]
    ]
    cornerPositions.forEach(pos => {
      const corner = new THREE.Mesh(
        new THREE.SphereGeometry(cornerRadius, 16, 16, 0, Math.PI * 2, 0, Math.PI / 2),
        this.materialSystem.clone('plasticDark')
      )
      corner.position.set(pos[0], pos[1], pos[2])
      chassisGroup.add(corner)
    })

    const frameLabel = new THREE.Mesh(
      new THREE.BoxGeometry(0.25, 0.08, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    frameLabel.position.set(-0.7, 0.45, 0.701)
    chassisGroup.add(frameLabel)

    this.registerPart('chassis', '机器人底盘', '高强度铝合金底盘，承载所有机械和电子部件，含加强筋结构', chassisGroup, 'structure')
  }

  private createWheelAssembly() {
    const wheelPositions = [
      { x: 0.75, z: 0.55, rot: 0 },
      { x: -0.75, z: 0.55, rot: 0 },
      { x: 0.75, z: -0.55, rot: Math.PI },
      { x: -0.75, z: -0.55, rot: Math.PI }
    ]

    wheelPositions.forEach((pos, index) => {
      const wheelGroup = new THREE.Group()
      wheelGroup.name = `wheel_${index}`
      wheelGroup.position.set(pos.x, 0.18, pos.z)
      wheelGroup.rotation.y = pos.rot

      const wheelRotor = new THREE.Group()
      wheelRotor.name = `wheelRotor_${index}`

      const tire = new THREE.Mesh(
        new THREE.TorusGeometry(0.18, 0.06, 16, 36),
        this.materialSystem.clone('rubberBlack')
      )
      tire.rotation.x = Math.PI / 2
      wheelRotor.add(tire)

      for (let i = 0; i < 24; i++) {
        const tread = new THREE.Mesh(
          new THREE.BoxGeometry(0.015, 0.02, 0.08),
          this.materialSystem.clone('rubberGray')
        )
        const angle = (i / 24) * Math.PI * 2
        tread.position.set(
          Math.cos(angle) * 0.18,
          Math.sin(angle) * 0.18,
          0
        )
        tread.rotation.z = angle + Math.PI / 2
        wheelRotor.add(tread)
      }

      const wheelHub = new THREE.Mesh(
        new THREE.CylinderGeometry(0.1, 0.1, 0.04, 16),
        this.materialSystem.clone('metalSteel')
      )
      wheelHub.rotation.x = Math.PI / 2
      wheelRotor.add(wheelHub)

      const hubDetail = new THREE.Mesh(
        new THREE.TorusGeometry(0.07, 0.008, 8, 24),
        this.materialSystem.clone('metalAluminum')
      )
      hubDetail.rotation.x = Math.PI / 2
      wheelRotor.add(hubDetail)

      for (let i = 0; i < 6; i++) {
        const spoke = new THREE.Mesh(
          new THREE.BoxGeometry(0.015, 0.01, 0.06),
          this.materialSystem.clone('metalSteelBrushed')
        )
        const angle = (i / 6) * Math.PI * 2
        spoke.position.set(
          Math.cos(angle) * 0.04,
          Math.sin(angle) * 0.04,
          0
        )
        spoke.rotation.z = angle
        wheelRotor.add(spoke)
      }

      const hubCap = new THREE.Mesh(
        new THREE.CircleGeometry(0.08, 16),
        this.materialSystem.clone('plasticBlue')
      )
      hubCap.position.z = 0.021
      wheelRotor.add(hubCap)

      const capLogo = new THREE.Mesh(
        new THREE.RingGeometry(0.02, 0.05, 6),
        this.materialSystem.clone('plasticWhite')
      )
      capLogo.position.z = 0.022
      wheelRotor.add(capLogo)

      wheelGroup.add(wheelRotor)

      const suspension = new THREE.Mesh(
        new THREE.BoxGeometry(0.15, 0.15, 0.1),
        this.materialSystem.clone('metalSteel')
      )
      suspension.position.y = 0.2
      wheelGroup.add(suspension)

      const spring = new THREE.Mesh(
        new THREE.CylinderGeometry(0.03, 0.03, 0.12, 8),
        this.materialSystem.clone('metalSteelBrushed')
      )
      spring.position.y = 0.25
      wheelGroup.add(spring)

      const motorHousing = new THREE.Mesh(
        new THREE.CylinderGeometry(0.08, 0.09, 0.15, 16),
        this.materialSystem.clone('plasticDark')
      )
      motorHousing.position.y = 0.15
      motorHousing.rotation.x = Math.PI / 2
      motorHousing.position.z = -0.1
      wheelGroup.add(motorHousing)

      this.registerPart(
        `wheel_${index}`,
        `驱动轮 ${index + 1}`,
        '高性能麦克纳姆轮，带花纹轮胎和内置伺服电机，支持全向移动',
        wheelGroup,
        'motion'
      )
    })
  }

  private createLiftMechanism() {
    const liftGroup = new THREE.Group()
    liftGroup.name = 'liftMechanism'
    liftGroup.position.y = 0.6

    const baseFrame = new THREE.Mesh(
      new THREE.BoxGeometry(1.6, 0.15, 0.2),
      this.materialSystem.clone('metalSteel')
    )
    baseFrame.position.z = 0
    liftGroup.add(baseFrame)

    const baseFrame2 = new THREE.Mesh(
      new THREE.BoxGeometry(1.6, 0.15, 0.2),
      this.materialSystem.clone('metalSteel')
    )
    baseFrame2.position.z = -0.5
    liftGroup.add(baseFrame2)

    const crossBeam = new THREE.Mesh(
      new THREE.BoxGeometry(0.1, 0.1, 0.7),
      this.materialSystem.clone('metalSteelBrushed')
    )
    crossBeam.position.set(-0.7, 0, -0.25)
    liftGroup.add(crossBeam)

    const crossBeam2 = crossBeam.clone()
    crossBeam2.position.x = 0.7
    liftGroup.add(crossBeam2)

    const scissorPositions = [-0.5, 0.5]
    scissorPositions.forEach((x, idx) => {
      const arm1 = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.6, 0.06),
        this.materialSystem.clone('metalAluminum')
      )
      arm1.position.set(x, 0.35, -0.1)
      arm1.rotation.z = 0.2
      arm1.userData.scissorIdx = idx
      arm1.userData.armIdx = 0
      liftGroup.add(arm1)

      const arm2 = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.6, 0.06),
        this.materialSystem.clone('metalAluminum')
      )
      arm2.position.set(x, 0.35, -0.4)
      arm2.rotation.z = -0.2
      arm2.userData.scissorIdx = idx
      arm2.userData.armIdx = 1
      liftGroup.add(arm2)

      const arm3 = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.6, 0.06),
        this.materialSystem.clone('metalAluminum')
      )
      arm3.position.set(x, 0.5, -0.1)
      arm3.rotation.z = -0.2
      liftGroup.add(arm3)

      const arm4 = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.6, 0.06),
        this.materialSystem.clone('metalAluminum')
      )
      arm4.position.set(x, 0.5, -0.4)
      arm4.rotation.z = 0.2
      liftGroup.add(arm4)

      const pivot = new THREE.Mesh(
        new THREE.SphereGeometry(0.05, 12, 12),
        this.materialSystem.clone('metalBrass')
      )
      pivot.position.set(x, 0.42, -0.25)
      liftGroup.add(pivot)

      const pin1 = new THREE.Mesh(
        new THREE.CylinderGeometry(0.025, 0.025, 0.12, 8),
        this.materialSystem.clone('metalSteel')
      )
      pin1.position.set(x, 0.35, -0.25)
      pin1.rotation.x = Math.PI / 2
      liftGroup.add(pin1)
    })

    const hydraulicCylinder = new THREE.Mesh(
      new THREE.CylinderGeometry(0.06, 0.08, 0.4, 12),
      this.materialSystem.clone('metalSteel')
    )
    hydraulicCylinder.position.set(0, 0.3, 0.15)
    hydraulicCylinder.rotation.x = -0.3
    liftGroup.add(hydraulicCylinder)

    const cylinderDetail = new THREE.Mesh(
      new THREE.TorusGeometry(0.07, 0.01, 8, 16),
      this.materialSystem.clone('metalSteelBrushed')
    )
    cylinderDetail.position.set(0, 0.3, 0.15)
    cylinderDetail.rotation.x = -0.3
    liftGroup.add(cylinderDetail)

    const hydraulicRod = new THREE.Mesh(
      new THREE.CylinderGeometry(0.03, 0.03, 0.35, 12),
      this.materialSystem.clone('metalStainless')
    )
    hydraulicRod.position.set(0, 0.5, 0.2)
    hydraulicRod.rotation.x = -0.3
    hydraulicRod.name = 'hydraulicRod'
    liftGroup.add(hydraulicRod)

    const hydraulicPivot = new THREE.Mesh(
      new THREE.SphereGeometry(0.035, 12, 12),
      this.materialSystem.clone('metalBrass')
    )
    hydraulicPivot.position.set(0, 0.65, 0.25)
    liftGroup.add(hydraulicPivot)

    const topPlatform = new THREE.Mesh(
      new THREE.BoxGeometry(1.7, 0.1, 1.3),
      this.materialSystem.clone('metalAluminum')
    )
    topPlatform.position.y = 0.65
    liftGroup.add(topPlatform)

    const platformFrame = new THREE.Mesh(
      new THREE.BoxGeometry(1.75, 0.06, 1.35),
      this.materialSystem.clone('metalSteel')
    )
    platformFrame.position.y = 0.6
    liftGroup.add(platformFrame)

    const antiSlipPad = new THREE.Mesh(
      new THREE.BoxGeometry(1.6, 0.03, 1.2),
      this.materialSystem.clone('rubberBlack')
    )
    antiSlipPad.position.y = 0.72
    liftGroup.add(antiSlipPad)

    for (let i = 0; i < 8; i++) {
      for (let j = 0; j < 6; j++) {
        const gripDot = new THREE.Mesh(
          new THREE.CylinderGeometry(0.015, 0.015, 0.01, 8),
          this.materialSystem.clone('rubberGray')
        )
        gripDot.position.set(
          -0.7 + i * 0.2,
          0.74,
          -0.5 + j * 0.2
        )
        liftGroup.add(gripDot)
      }
    }

    const alignmentPin1 = new THREE.Mesh(
      new THREE.ConeGeometry(0.03, 0.08, 12),
      this.materialSystem.clone('metalStainless')
    )
    alignmentPin1.position.set(0.5, 0.72, 0.4)
    liftGroup.add(alignmentPin1)

    const alignmentPin2 = alignmentPin1.clone()
    alignmentPin2.position.set(-0.5, 0.72, 0.4)
    liftGroup.add(alignmentPin2)

    const alignmentPin3 = alignmentPin1.clone()
    alignmentPin3.position.set(0.5, 0.72, -0.4)
    liftGroup.add(alignmentPin3)

    const alignmentPin4 = alignmentPin1.clone()
    alignmentPin4.position.set(-0.5, 0.72, -0.4)
    liftGroup.add(alignmentPin4)

    this.registerPart(
      'liftMechanism',
      '升降机构',
      '双剪叉式液压升降系统，带导向销和防滑纹理托盘，最大举升高度 1.2 米',
      liftGroup,
      'mechanism'
    )
  }

  private createSensors() {
    const sensorPositions = [
      { x: 0.95, z: 0, rot: 0, name: 'front' },
      { x: -0.95, z: 0, rot: Math.PI, name: 'back' },
      { x: 0, z: 0.75, rot: Math.PI / 2, name: 'left' },
      { x: 0, z: -0.75, rot: -Math.PI / 2, name: 'right' }
    ]

    sensorPositions.forEach((pos, index) => {
      const sensorGroup = new THREE.Group()
      sensorGroup.name = `proximitySensor_${index}`
      sensorGroup.position.set(pos.x, 0.35, pos.z)
      sensorGroup.rotation.y = pos.rot

      const housing = new THREE.Mesh(
        new THREE.BoxGeometry(0.12, 0.15, 0.08),
        this.materialSystem.clone('plasticDark')
      )
      sensorGroup.add(housing)

      const housingBevel = new THREE.Mesh(
        new THREE.BoxGeometry(0.1, 0.13, 0.01),
        this.materialSystem.clone('plasticGray')
      )
      housingBevel.position.x = 0.055
      sensorGroup.add(housingBevel)

      const lens = new THREE.Mesh(
        new THREE.CircleGeometry(0.04, 16),
        this.materialSystem.clone('sensor')
      )
      lens.position.x = 0.061
      sensorGroup.add(lens)

      const lensRing = new THREE.Mesh(
        new THREE.TorusGeometry(0.045, 0.006, 8, 16),
        this.materialSystem.clone('metalBrass')
      )
      lensRing.position.x = 0.062
      lensRing.rotation.y = Math.PI / 2
      sensorGroup.add(lensRing)

      const irEmitter = new THREE.Mesh(
        new THREE.CircleGeometry(0.018, 8),
        this.materialSystem.clone('ledRed')
      )
      irEmitter.position.set(0.061, 0.03, 0)
      sensorGroup.add(irEmitter)

      const irReceiver = new THREE.Mesh(
        new THREE.CircleGeometry(0.018, 8),
        this.materialSystem.clone('sensor')
      )
      irReceiver.position.set(0.061, -0.03, 0)
      sensorGroup.add(irReceiver)

      const statusLed = new THREE.Mesh(
        new THREE.CircleGeometry(0.01, 8),
        this.materialSystem.clone('ledGreen')
      )
      statusLed.position.set(0, 0.05, 0.041)
      statusLed.rotation.y = Math.PI / 2
      sensorGroup.add(statusLed)

      const cable = new THREE.Mesh(
        new THREE.CylinderGeometry(0.008, 0.008, 0.1, 6),
        this.materialSystem.clone('plasticDark')
      )
      cable.position.set(0, -0.08, 0)
      cable.rotation.x = Math.PI / 2
      sensorGroup.add(cable)

      this.registerPart(
        `proximitySensor_${index}`,
        `红外传感器 ${index + 1} (${pos.name})`,
        '高精度红外测距传感器，带状态指示灯和收发分离镜头，探测范围 0-50cm',
        sensorGroup,
        'sensor'
      )
    })
  }

  private createLidar() {
    const lidarGroup = new THREE.Group()
    lidarGroup.name = 'lidar'
    lidarGroup.position.set(0, 1.5, 0)

    const baseMount = new THREE.Mesh(
      new THREE.CylinderGeometry(0.12, 0.15, 0.08, 24),
      this.materialSystem.clone('metalAluminum')
    )
    lidarGroup.add(baseMount)

    const mountDetail = new THREE.Mesh(
      new THREE.TorusGeometry(0.135, 0.008, 8, 24),
      this.materialSystem.clone('metalSteelBrushed')
    )
    mountDetail.position.y = 0.04
    mountDetail.rotation.x = Math.PI / 2
    lidarGroup.add(mountDetail)

    for (let i = 0; i < 6; i++) {
      const mountScrew = new THREE.Mesh(
        new THREE.CylinderGeometry(0.012, 0.015, 0.02, 6),
        this.materialSystem.clone('metalSteel')
      )
      const angle = (i / 6) * Math.PI * 2
      mountScrew.position.set(
        Math.cos(angle) * 0.11,
        0.07,
        Math.sin(angle) * 0.11
      )
      lidarGroup.add(mountScrew)
    }

    const housing = new THREE.Mesh(
      new THREE.CylinderGeometry(0.11, 0.11, 0.15, 24),
      this.materialSystem.clone('plasticDark')
    )
    housing.position.y = 0.1
    lidarGroup.add(housing)

    const housingTop = new THREE.Mesh(
      new THREE.CylinderGeometry(0.115, 0.11, 0.03, 24),
      this.materialSystem.clone('plasticGray')
    )
    housingTop.position.y = 0.19
    lidarGroup.add(housingTop)

    const glassWindow = new THREE.Mesh(
      new THREE.CylinderGeometry(0.105, 0.105, 0.08, 24, 1, true),
      this.materialSystem.clone('glassCover')
    )
    glassWindow.position.y = 0.1
    lidarGroup.add(glassWindow)

    const rotorAssembly = new THREE.Group()
    rotorAssembly.name = 'rotorAssembly'
    rotorAssembly.position.y = 0.1

    const scanRing = new THREE.Mesh(
      new THREE.TorusGeometry(0.1, 0.025, 12, 36),
      this.materialSystem.clone('lidar')
    )
    scanRing.rotation.x = Math.PI / 2
    scanRing.name = 'lidarScanRing'
    rotorAssembly.add(scanRing)

    const laserEmitter = new THREE.Mesh(
      new THREE.BoxGeometry(0.03, 0.02, 0.12),
      this.materialSystem.clone('ledBlue')
    )
    laserEmitter.rotation.y = Math.PI / 2
    rotorAssembly.add(laserEmitter)

    const mirror = new THREE.Mesh(
      new THREE.BoxGeometry(0.02, 0.04, 0.02),
      this.materialSystem.clone('metalStainless')
    )
    mirror.position.set(0, 0.01, 0)
    mirror.rotation.y = Math.PI / 4
    rotorAssembly.add(mirror)

    const motorRotor = new THREE.Mesh(
      new THREE.CylinderGeometry(0.025, 0.025, 0.05, 12),
      this.materialSystem.clone('metalSteel')
    )
    rotorAssembly.add(motorRotor)

    lidarGroup.add(rotorAssembly)

    const topCover = new THREE.Mesh(
      new THREE.ConeGeometry(0.1, 0.06, 24),
      this.materialSystem.clone('metalAluminum')
    )
    topCover.position.y = 0.2
    topCover.rotation.x = Math.PI
    lidarGroup.add(topCover)

    const topLabel = new THREE.Mesh(
      new THREE.CircleGeometry(0.04, 16),
      this.materialSystem.clone('plasticWhite')
    )
    topLabel.position.y = 0.26
    lidarGroup.add(topLabel)

    const cableGland = new THREE.Mesh(
      new THREE.CylinderGeometry(0.02, 0.025, 0.08, 8),
      this.materialSystem.clone('plasticDark')
    )
    cableGland.position.set(0.08, -0.04, 0)
    cableGland.rotation.z = Math.PI / 2
    lidarGroup.add(cableGland)

    this.registerPart(
      'lidar',
      '激光雷达',
      '360 度激光测距传感器，高速旋转扫描头，带玻璃保护罩，探测距离 0.1-15m',
      lidarGroup,
      'sensor'
    )
  }

  private createCameras() {
    const frontCameraGroup = new THREE.Group()
    frontCameraGroup.name = 'frontCamera'
    frontCameraGroup.position.set(0, 0.8, 0.75)

    const cameraBody = new THREE.Mesh(
      new THREE.BoxGeometry(0.18, 0.15, 0.15),
      this.materialSystem.clone('plasticDark')
    )
    frontCameraGroup.add(cameraBody)

    const bodyBevel = new THREE.Mesh(
      new THREE.BoxGeometry(0.16, 0.13, 0.02),
      this.materialSystem.clone('plasticGray')
    )
    bodyBevel.position.z = 0.085
    frontCameraGroup.add(bodyBevel)

    const lensMount = new THREE.Mesh(
      new THREE.CylinderGeometry(0.06, 0.055, 0.04, 16),
      this.materialSystem.clone('metalSteelBrushed')
    )
    lensMount.position.z = 0.1
    lensMount.rotation.x = Math.PI / 2
    frontCameraGroup.add(lensMount)

    const lensBarrel = new THREE.Mesh(
      new THREE.CylinderGeometry(0.045, 0.045, 0.08, 16),
      this.materialSystem.clone('metalSteel')
    )
    lensBarrel.position.z = 0.14
    lensBarrel.rotation.x = Math.PI / 2
    frontCameraGroup.add(lensBarrel)

    for (let i = 0; i < 3; i++) {
      const focusRing = new THREE.Mesh(
        new THREE.TorusGeometry(0.05, 0.006, 8, 16),
        this.materialSystem.clone('metalStainless')
      )
      focusRing.position.z = 0.11 + i * 0.02
      focusRing.rotation.x = Math.PI / 2
      frontCameraGroup.add(focusRing)
    }

    const lens = new THREE.Mesh(
      new THREE.SphereGeometry(0.035, 16, 16, 0, Math.PI * 2, 0, Math.PI / 2),
      this.materialSystem.clone('glassLens')
    )
    lens.position.z = 0.18
    frontCameraGroup.add(lens)

    const lensReflection = new THREE.Mesh(
      new THREE.CircleGeometry(0.015, 8),
      this.materialSystem.clone('ledWhite')
    )
    lensReflection.position.set(0.01, 0.01, 0.2)
    frontCameraGroup.add(lensReflection)

    const statusLed = new THREE.Mesh(
      new THREE.CircleGeometry(0.015, 8),
      this.materialSystem.clone('ledGreen')
    )
    statusLed.position.set(0.05, 0.04, 0.076)
    frontCameraGroup.add(statusLed)

    const micHole1 = new THREE.Mesh(
      new THREE.CircleGeometry(0.008, 8),
      this.materialSystem.clone('plasticDark')
    )
    micHole1.position.set(-0.05, 0.04, 0.076)
    frontCameraGroup.add(micHole1)

    const micHole2 = micHole1.clone()
    micHole2.position.x = -0.03
    frontCameraGroup.add(micHole2)

    const mountingBracket = new THREE.Mesh(
      new THREE.BoxGeometry(0.22, 0.04, 0.06),
      this.materialSystem.clone('metalSteel')
    )
    mountingBracket.position.y = -0.09
    frontCameraGroup.add(mountingBracket)

    const supportArm = new THREE.Mesh(
      new THREE.BoxGeometry(0.06, 0.35, 0.05),
      this.materialSystem.clone('metalSteelBrushed')
    )
    supportArm.position.set(0, -0.28, 0)
    frontCameraGroup.add(supportArm)

    const armReinforcement1 = new THREE.Mesh(
      new THREE.BoxGeometry(0.04, 0.02, 0.12),
      this.materialSystem.clone('metalAluminum')
    )
    armReinforcement1.position.set(0.04, -0.15, 0.03)
    armReinforcement1.rotation.x = 0.3
    frontCameraGroup.add(armReinforcement1)

    const armReinforcement2 = new THREE.Mesh(
      new THREE.BoxGeometry(0.04, 0.02, 0.12),
      this.materialSystem.clone('metalAluminum')
    )
    armReinforcement2.position.set(-0.04, -0.15, 0.03)
    armReinforcement2.rotation.x = 0.3
    frontCameraGroup.add(armReinforcement2)

    const basePlate = new THREE.Mesh(
      new THREE.BoxGeometry(0.12, 0.03, 0.08),
      this.materialSystem.clone('metalSteel')
    )
    basePlate.position.set(0, -0.46, 0)
    frontCameraGroup.add(basePlate)

    this.createScrewHole(frontCameraGroup, 0.04, -0.45, 0.02, 0.012)
    this.createScrewHole(frontCameraGroup, -0.04, -0.45, 0.02, 0.012)

    const cableConduit = new THREE.Mesh(
      new THREE.CylinderGeometry(0.012, 0.012, 0.25, 6),
      this.materialSystem.clone('plasticDark')
    )
    cableConduit.position.set(0.04, -0.25, -0.04)
    frontCameraGroup.add(cableConduit)

    this.registerPart(
      'frontCamera',
      '前置摄像头',
      '1080P 高清视觉摄像头，带可调光圈、补光灯和麦克风，用于视觉导航',
      frontCameraGroup,
      'sensor'
    )

    const downCameraGroup = new THREE.Group()
    downCameraGroup.name = 'downCamera'
    downCameraGroup.position.set(0, 0.4, 0)

    const downCameraBody = new THREE.Mesh(
      new THREE.CylinderGeometry(0.06, 0.07, 0.08, 16),
      this.materialSystem.clone('plasticDark')
    )
    downCameraGroup.add(downCameraBody)

    const downLensRing = new THREE.Mesh(
      new THREE.TorusGeometry(0.045, 0.008, 8, 16),
      this.materialSystem.clone('metalBrass')
    )
    downLensRing.position.y = -0.04
    downLensRing.rotation.x = Math.PI / 2
    downCameraGroup.add(downLensRing)

    const downLens = new THREE.Mesh(
      new THREE.CircleGeometry(0.04, 16),
      this.materialSystem.clone('glassLens')
    )
    downLens.position.y = -0.041
    downLens.rotation.x = -Math.PI / 2
    downCameraGroup.add(downLens)

    const ledRing = new THREE.Mesh(
      new THREE.TorusGeometry(0.065, 0.006, 8, 24),
      this.materialSystem.clone('ledWhite')
    )
    ledRing.position.y = -0.04
    ledRing.rotation.x = Math.PI / 2
    downCameraGroup.add(ledRing)

    for (let i = 0; i < 4; i++) {
      const fillLight = new THREE.Mesh(
        new THREE.CircleGeometry(0.01, 8),
        this.materialSystem.clone('ledWhite')
      )
      const angle = (i / 4) * Math.PI * 2
      fillLight.position.set(
        Math.cos(angle) * 0.065,
        -0.041,
        Math.sin(angle) * 0.065
      )
      fillLight.rotation.x = -Math.PI / 2
      downCameraGroup.add(fillLight)
    }

    this.registerPart(
      'downCamera',
      '底部摄像头',
      '视觉定位摄像头，带环形补光灯，用于地面二维码识别和 ±1mm 精确定位',
      downCameraGroup,
      'sensor'
    )
  }

  private createIndicatorLights() {
    const lightStripPositions = [
      { x: 0, z: 0.76, rot: 0, count: 8, side: 'front' },
      { x: 0, z: -0.76, rot: Math.PI, count: 8, side: 'back' },
      { x: 0.91, z: 0, rot: Math.PI / 2, count: 6, side: 'right' },
      { x: -0.91, z: 0, rot: -Math.PI / 2, count: 6, side: 'left' }
    ]

    lightStripPositions.forEach((strip, stripIndex) => {
      const stripGroup = new THREE.Group()
      stripGroup.name = `lightStrip_${stripIndex}`
      stripGroup.position.set(strip.x, 0.5, strip.z)
      stripGroup.rotation.y = strip.rot

      const housing = new THREE.Mesh(
        new THREE.BoxGeometry(0.85, 0.1, 0.06),
        this.materialSystem.clone('plasticDark')
      )
      stripGroup.add(housing)

      const diffuser = new THREE.Mesh(
        new THREE.BoxGeometry(0.8, 0.08, 0.01),
        this.materialSystem.clone('glassCover')
      )
      diffuser.position.z = 0.031
      stripGroup.add(diffuser)

      for (let i = 0; i < strip.count; i++) {
        const led = new THREE.Mesh(
          new THREE.BoxGeometry(0.06, 0.04, 0.02),
          this.materialSystem.clone('ledBlue')
        )
        led.position.set(-0.35 + i * 0.1, 0, 0.02)
        led.name = `led_${stripIndex}_${i}`
        stripGroup.add(led)

        const ledReflector = new THREE.Mesh(
          new THREE.BoxGeometry(0.08, 0.06, 0.01),
          this.materialSystem.clone('metalAluminum')
        )
        ledReflector.position.set(-0.35 + i * 0.1, 0, 0.005)
        stripGroup.add(ledReflector)
      }

      const endCap1 = new THREE.Mesh(
        new THREE.BoxGeometry(0.02, 0.1, 0.06),
        this.materialSystem.clone('plasticGray')
      )
      endCap1.position.x = 0.425
      stripGroup.add(endCap1)

      const endCap2 = endCap1.clone()
      endCap2.position.x = -0.425
      stripGroup.add(endCap2)

      this.registerPart(
        `lightStrip_${stripIndex}`,
        `状态灯条 ${stripIndex + 1} (${strip.side})`,
        'RGB LED 状态指示灯条，带磨砂扩散罩，显示运行、错误、充电等状态',
        stripGroup,
        'indicator'
      )
    })

    const batteryLedGroup = new THREE.Group()
    batteryLedGroup.name = 'batteryIndicator'
    batteryLedGroup.position.set(0.7, 0.35, 0.55)

    const batteryHousing = new THREE.Mesh(
      new THREE.BoxGeometry(0.22, 0.1, 0.04),
      this.materialSystem.clone('plasticDark')
    )
    batteryLedGroup.add(batteryHousing)

    const batteryIcon = new THREE.Mesh(
      new THREE.BoxGeometry(0.04, 0.06, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    batteryIcon.position.set(-0.1, 0, 0.021)
    batteryLedGroup.add(batteryIcon)

    const batteryTip = new THREE.Mesh(
      new THREE.BoxGeometry(0.02, 0.03, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    batteryTip.position.set(-0.13, 0.015, 0.021)
    batteryLedGroup.add(batteryTip)

    for (let i = 0; i < 4; i++) {
      const led = new THREE.Mesh(
        new THREE.BoxGeometry(0.04, 0.06, 0.02),
        i < 2 ? this.materialSystem.clone('ledGreen') : this.materialSystem.clone('ledOrange')
      )
      led.position.set(-0.05 + i * 0.05, 0, 0.02)
      led.name = `batteryLed_${i}`
      batteryLedGroup.add(led)
    }

    this.registerPart(
      'batteryIndicator',
      '电量指示灯',
      '4 段 LED 电量显示，绿-橙-红渐变指示剩余电量',
      batteryLedGroup,
      'indicator'
    )
  }

  private createBatteryCompartment() {
    const batteryGroup = new THREE.Group()
    batteryGroup.name = 'batteryCompartment'
    batteryGroup.position.set(0, 0.3, -0.4)

    const compartmentFrame = new THREE.Mesh(
      new THREE.BoxGeometry(0.65, 0.4, 0.55),
      this.materialSystem.clone('metalSteel')
    )
    batteryGroup.add(compartmentFrame)

    const frameReinforcement = new THREE.Mesh(
      new THREE.BoxGeometry(0.68, 0.05, 0.58),
      this.materialSystem.clone('metalSteelBrushed')
    )
    frameReinforcement.position.y = 0.175
    batteryGroup.add(frameReinforcement)

    const batteryPack = new THREE.Mesh(
      new THREE.BoxGeometry(0.55, 0.3, 0.45),
      this.materialSystem.clone('battery')
    )
    batteryPack.position.y = 0
    batteryGroup.add(batteryPack)

    const cellDivisions = []
    for (let i = 0; i < 3; i++) {
      const division = new THREE.Mesh(
        new THREE.BoxGeometry(0.01, 0.28, 0.43),
        this.materialSystem.clone('plasticDark')
      )
      division.position.set(-0.18 + i * 0.18, 0, 0)
      cellDivisions.push(division)
      batteryGroup.add(division)
    }

    for (let i = 0; i < 4; i++) {
      const division = new THREE.Mesh(
        new THREE.BoxGeometry(0.53, 0.28, 0.01),
        this.materialSystem.clone('plasticDark')
      )
      division.position.set(0, 0, -0.15 + i * 0.1)
      batteryGroup.add(division)
    }

    const batteryLabel = new THREE.Mesh(
      new THREE.BoxGeometry(0.3, 0.12, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    batteryLabel.position.set(0, 0.05, 0.226)
    batteryGroup.add(batteryLabel)

    const terminals = [
      { x: 0.18, color: 'ledRed', label: '+' },
      { x: -0.18, color: 'metalSteel', label: '-' }
    ]
    terminals.forEach(t => {
      const terminalBase = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.06, 0.08),
        this.materialSystem.clone('metalCopper')
      )
      terminalBase.position.set(t.x, 0.15, 0.2)
      batteryGroup.add(terminalBase)

      const terminalPost = new THREE.Mesh(
        new THREE.CylinderGeometry(0.025, 0.025, 0.06, 8),
        this.materialSystem.clone('metalBrass')
      )
      terminalPost.position.set(t.x, 0.2, 0.2)
      batteryGroup.add(terminalPost)
    })

    const batteryCover = new THREE.Mesh(
      new THREE.BoxGeometry(0.6, 0.04, 0.5),
      this.materialSystem.clone('metalSteelBrushed')
    )
    batteryCover.position.y = 0.2
    batteryCover.name = 'batteryCover'
    batteryGroup.add(batteryCover)

    const coverHandle = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.04, 0.03),
      this.materialSystem.clone('plasticDark')
    )
    coverHandle.position.set(0, 0.23, 0)
    batteryGroup.add(coverHandle)

    const latch1 = new THREE.Mesh(
      new THREE.BoxGeometry(0.04, 0.03, 0.06),
      this.materialSystem.clone('metalSteel')
    )
    latch1.position.set(0.25, 0.22, 0.15)
    batteryGroup.add(latch1)

    const latch2 = latch1.clone()
    latch2.position.x = -0.25
    batteryGroup.add(latch2)

    for (let i = 0; i < 4; i++) {
      const px = i < 2 ? 0.25 : -0.25
      const pz = i % 2 === 0 ? 0.2 : -0.2
      this.createScrewHole(batteryGroup, px, 0.22, pz, 0.015)
    }

    const ventSlot = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.02, 0.02),
      this.materialSystem.clone('plasticDark')
    )
    ventSlot.position.set(0, 0, 0.276)
    batteryGroup.add(ventSlot)

    const bmsLed = new THREE.Mesh(
      new THREE.CircleGeometry(0.015, 8),
      this.materialSystem.clone('ledGreen')
    )
    bmsLed.position.set(0.2, 0.1, 0.276)
    batteryGroup.add(bmsLed)

    this.registerPart(
      'batteryCompartment',
      '电池仓',
      '48V 20Ah 磷酸铁锂电池组，16 电芯设计，带 BMS 管理系统和散热通风',
      batteryGroup,
      'power'
    )
  }

  private createChargingContacts() {
    const contactGroup = new THREE.Group()
    contactGroup.name = 'chargingContacts'
    contactGroup.position.set(0, 0.25, -0.75)

    const contactPlate = new THREE.Mesh(
      new THREE.BoxGeometry(0.45, 0.2, 0.08),
      this.materialSystem.clone('plasticDark')
    )
    contactGroup.add(contactPlate)

    const plateFrame = new THREE.Mesh(
      new THREE.BoxGeometry(0.48, 0.23, 0.03),
      this.materialSystem.clone('metalSteelBrushed')
    )
    plateFrame.position.z = -0.025
    contactGroup.add(plateFrame)

    const positiveContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.12, 0.1, 0.06),
      this.materialSystem.clone('metalBrass')
    )
    positiveContact.position.set(0.12, 0, 0.07)
    contactGroup.add(positiveContact)

    const positiveSpring = new THREE.Mesh(
      new THREE.CylinderGeometry(0.02, 0.02, 0.08, 8),
      this.materialSystem.clone('metalSteel')
    )
    positiveSpring.position.set(0.12, 0, 0.03)
    positiveSpring.rotation.x = Math.PI / 2
    contactGroup.add(positiveSpring)

    const negativeContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.12, 0.1, 0.06),
      this.materialSystem.clone('metalBrass')
    )
    negativeContact.position.set(-0.12, 0, 0.07)
    contactGroup.add(negativeContact)

    const negativeSpring = new THREE.Mesh(
      new THREE.CylinderGeometry(0.02, 0.02, 0.08, 8),
      this.materialSystem.clone('metalSteel')
    )
    negativeSpring.position.set(-0.12, 0, 0.03)
    negativeSpring.rotation.x = Math.PI / 2
    contactGroup.add(negativeSpring)

    const contactCover = new THREE.Mesh(
      new THREE.BoxGeometry(0.1, 0.03, 0.02),
      this.materialSystem.clone('metalStainless')
    )
    contactCover.position.set(0.12, 0.06, 0.101)
    contactGroup.add(contactCover)

    const contactCover2 = contactCover.clone()
    contactCover2.position.x = -0.12
    contactGroup.add(contactCover2)

    const guidePin1 = new THREE.Mesh(
      new THREE.ConeGeometry(0.04, 0.12, 12),
      this.materialSystem.clone('metalSteel')
    )
    guidePin1.position.set(0.18, 0, 0.12)
    contactGroup.add(guidePin1)

    const guidePin2 = new THREE.Mesh(
      new THREE.ConeGeometry(0.04, 0.12, 12),
      this.materialSystem.clone('metalSteel')
    )
    guidePin2.position.set(-0.18, 0, 0.12)
    contactGroup.add(guidePin2)

    const pinBase1 = new THREE.Mesh(
      new THREE.CylinderGeometry(0.05, 0.06, 0.04, 12),
      this.materialSystem.clone('metalSteelBrushed')
    )
    pinBase1.position.set(0.18, -0.04, 0.1)
    contactGroup.add(pinBase1)

    const pinBase2 = pinBase1.clone()
    pinBase2.position.x = -0.18
    contactGroup.add(pinBase2)

    const irSensor = new THREE.Mesh(
      new THREE.CircleGeometry(0.02, 8),
      this.materialSystem.clone('ledRed')
    )
    irSensor.position.set(0, 0.06, 0.041)
    contactGroup.add(irSensor)

    const alignmentMark = new THREE.Mesh(
      new THREE.RingGeometry(0.015, 0.025, 8),
      this.materialSystem.clone('ledOrange')
    )
    alignmentMark.position.set(0, -0.06, 0.041)
    contactGroup.add(alignmentMark)

    this.registerPart(
      'chargingContacts',
      '充电触点',
      '自动充电对接触点，弹簧浮动设计，铜合金导电，支持 50A 快充',
      contactGroup,
      'power'
    )
  }

  private createShellAndSeams() {
    const shellGroup = new THREE.Group()
    shellGroup.name = 'outerShell'
    shellGroup.position.y = 0.85

    const frontShell = new THREE.Mesh(
      new THREE.BoxGeometry(1.85, 0.5, 0.65),
      this.materialSystem.clone('plasticWhite')
    )
    frontShell.position.z = 0
    shellGroup.add(frontShell)

    const frontBevel = new THREE.Mesh(
      new THREE.BoxGeometry(1.8, 0.45, 0.05),
      this.materialSystem.clone('plasticGray')
    )
    frontBevel.position.z = 0.35
    shellGroup.add(frontBevel)

    const shellTop = new THREE.Mesh(
      new THREE.BoxGeometry(1.8, 0.12, 1.2),
      this.materialSystem.clone('plasticWhite')
    )
    shellTop.position.y = 0.25
    shellGroup.add(shellTop)

    const topBevel = new THREE.Mesh(
      new THREE.BoxGeometry(1.75, 0.03, 1.15),
      this.materialSystem.clone('plasticGray')
    )
    topBevel.position.y = 0.32
    shellGroup.add(topBevel)

    const sideShellL = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.5, 0.55),
      this.materialSystem.clone('plasticWhite')
    )
    sideShellL.position.set(0.88, 0, -0.05)
    shellGroup.add(sideShellL)

    const sideShellR = sideShellL.clone()
    sideShellR.position.x = -0.88
    shellGroup.add(sideShellR)

    const seamPositions = [
      { x: 0, y: 0, z: 0.326, rx: 0, ry: 0, rz: 0, w: 1.85, h: 0.5, d: 0.008 },
      { x: 0, y: 0, z: -0.326, rx: 0, ry: 0, rz: 0, w: 1.85, h: 0.5, d: 0.008 },
      { x: 0.926, y: 0, z: 0, rx: 0, ry: 0, rz: 0, w: 0.008, h: 0.5, d: 0.65 },
      { x: -0.926, y: 0, z: 0, rx: 0, ry: 0, rz: 0, w: 0.008, h: 0.5, d: 0.65 },
      { x: 0, y: 0.301, z: 0, rx: 0, ry: 0, rz: 0, w: 1.85, h: 0.008, d: 0.65 }
    ]

    seamPositions.forEach(seam => {
      const seamMesh = new THREE.Mesh(
        new THREE.BoxGeometry(seam.w, seam.h, seam.d),
        this.materialSystem.clone('plasticDark')
      )
      seamMesh.position.set(seam.x, seam.y, seam.z)
      shellGroup.add(seamMesh)
    })

    const ventSlotsZ = [-0.4, -0.2, 0, 0.2, 0.4]
    ventSlotsZ.forEach(z => {
      const vent = new THREE.Mesh(
        new THREE.BoxGeometry(0.02, 0.15, 0.02),
        this.materialSystem.clone('plasticDark')
      )
      vent.position.set(0, 0, 0.33 + z * 0.001)
      shellGroup.add(vent)
    })

    const ventSlotsX = [-0.7, -0.5, -0.3, -0.1, 0.1, 0.3, 0.5, 0.7]
    ventSlotsX.forEach(x => {
      const vent = new THREE.Mesh(
        new THREE.BoxGeometry(0.02, 0.15, 0.02),
        this.materialSystem.clone('plasticDark')
      )
      vent.position.set(x, 0, 0.33)
      shellGroup.add(vent)
    })

    for (let i = 0; i < 6; i++) {
      const vent = new THREE.Mesh(
        new THREE.BoxGeometry(0.15, 0.02, 0.02),
        this.materialSystem.clone('plasticDark')
      )
      vent.position.set(-0.37 + i * 0.15, 0, 0.33)
      shellGroup.add(vent)
    }

    const sideVents = [-0.5, 0, 0.5]
    sideVents.forEach(y => {
      for (let i = 0; i < 5; i++) {
        const vent = new THREE.Mesh(
          new THREE.BoxGeometry(0.02, 0.015, 0.1),
          this.materialSystem.clone('plasticDark')
        )
        vent.position.set(0.927, y, -0.2 + i * 0.1)
        shellGroup.add(vent)
      }
    })

    const brandLogo = new THREE.Mesh(
      new THREE.BoxGeometry(0.3, 0.12, 0.015),
      this.materialSystem.clone('plasticBlue')
    )
    brandLogo.position.set(0, 0.05, 0.336)
    shellGroup.add(brandLogo)

    const logoText = new THREE.Mesh(
      new THREE.BoxGeometry(0.2, 0.04, 0.002),
      this.materialSystem.clone('plasticWhite')
    )
    logoText.position.set(0, 0.05, 0.344)
    shellGroup.add(logoText)

    const statusDisplay = new THREE.Mesh(
      new THREE.BoxGeometry(0.25, 0.08, 0.01),
      this.materialSystem.clone('sensor')
    )
    statusDisplay.position.set(0.6, 0.1, 0.331)
    shellGroup.add(statusDisplay)

    const displayLed = new THREE.Mesh(
      new THREE.CircleGeometry(0.01, 8),
      this.materialSystem.clone('ledGreen')
    )
    displayLed.position.set(0.7, 0.1, 0.337)
    shellGroup.add(displayLed)

    const shellScrews = [
      [0.7, 0.2, 0.3], [-0.7, 0.2, 0.3],
      [0.7, -0.2, 0.3], [-0.7, -0.2, 0.3],
      [0.7, 0.2, -0.3], [-0.7, 0.2, -0.3],
      [0.7, -0.2, -0.3], [-0.7, -0.2, -0.3]
    ]
    shellScrews.forEach(pos => {
      this.createScrewHole(shellGroup, pos[0], pos[1], pos[2], 0.018)
    })

    const topHandle = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.08, 0.1),
      this.materialSystem.clone('plasticDark')
    )
    topHandle.position.set(0, 0.38, 0)
    shellGroup.add(topHandle)

    const handleGrip = new THREE.Mesh(
      new THREE.BoxGeometry(0.35, 0.04, 0.08),
      this.materialSystem.clone('rubberGray')
    )
    handleGrip.position.set(0, 0.4, 0)
    shellGroup.add(handleGrip)

    this.registerPart(
      'outerShell',
      '外壳',
      'ABS 工程塑料外壳，多段拼接设计，大面积散热格栅，防护等级 IP54',
      shellGroup,
      'structure'
    )
  }

  private createPayloadTray() {
    const trayGroup = new THREE.Group()
    trayGroup.name = 'payloadTray'
    trayGroup.position.y = 2.0

    const trayBase = new THREE.Mesh(
      new THREE.BoxGeometry(2.1, 0.1, 1.6),
      this.materialSystem.clone('metalSteel')
    )
    trayGroup.add(trayBase)

    const trayFrame = new THREE.Mesh(
      new THREE.BoxGeometry(2.15, 0.06, 1.65),
      this.materialSystem.clone('metalSteelBrushed')
    )
    trayFrame.position.y = -0.03
    trayGroup.add(trayFrame)

    const sideRails = [
      { x: 1.0, w: 0.08, d: 1.65 },
      { x: -1.0, w: 0.08, d: 1.65 },
      { z: 0.775, w: 2.15, d: 0.08 },
      { z: -0.775, w: 2.15, d: 0.08 }
    ]

    sideRails.forEach(rail => {
      const railMesh = new THREE.Mesh(
        new THREE.BoxGeometry(rail.w, 0.2, rail.d),
        this.materialSystem.clone('metalAluminum')
      )
      railMesh.position.set(rail.x || 0, 0.08, rail.z || 0)
      trayGroup.add(railMesh)
    })

    const railCaps = [
      [0.96, 0.16, 0.735], [-0.96, 0.16, 0.735],
      [0.96, 0.16, -0.735], [-0.96, 0.16, -0.735]
    ]
    railCaps.forEach(pos => {
      const cap = new THREE.Mesh(
        new THREE.SphereGeometry(0.04, 8, 8, 0, Math.PI * 2, 0, Math.PI / 2),
        this.materialSystem.clone('metalStainless')
      )
      cap.position.set(pos[0], pos[1], pos[2])
      trayGroup.add(cap)
    })

    const crossBeams = []
    for (let i = 0; i < 5; i++) {
      const beam = new THREE.Mesh(
        new THREE.BoxGeometry(2.0, 0.06, 0.08),
        this.materialSystem.clone('metalSteel')
      )
      beam.position.set(0, 0.02, -0.6 + i * 0.3)
      crossBeams.push(beam)
      trayGroup.add(beam)
    }

    for (let i = 0; i < 3; i++) {
      const beam = new THREE.Mesh(
        new THREE.BoxGeometry(0.06, 0.06, 1.5),
        this.materialSystem.clone('metalSteelBrushed')
      )
      beam.position.set(-0.9 + i * 0.9, 0.02, 0)
      trayGroup.add(beam)
    }

    const rubberMat = new THREE.Mesh(
      new THREE.BoxGeometry(1.95, 0.04, 1.45),
      this.materialSystem.clone('rubberBlack')
    )
    rubberMat.position.y = 0.07
    trayGroup.add(rubberMat)

    for (let i = 0; i < 20; i++) {
      for (let j = 0; j < 15; j++) {
        const gripDot = new THREE.Mesh(
          new THREE.CylinderGeometry(0.012, 0.012, 0.015, 8),
          this.materialSystem.clone('rubberGray')
        )
        gripDot.position.set(
          -0.9 + i * 0.1,
          0.095,
          -0.65 + j * 0.1
        )
        trayGroup.add(gripDot)
      }
    }

    const locatorHoles = [
      [0.7, 0.5], [-0.7, 0.5],
      [0.7, -0.5], [-0.7, -0.5],
      [0, 0.6], [0, -0.6]
    ]
    locatorHoles.forEach(pos => {
      const hole = new THREE.Mesh(
        new THREE.CylinderGeometry(0.03, 0.03, 0.05, 12),
        this.materialSystem.clone('metalSteel')
      )
      hole.position.set(pos[0], 0.09, pos[1])
      trayGroup.add(hole)

      const holeRing = new THREE.Mesh(
        new THREE.TorusGeometry(0.035, 0.006, 8, 16),
        this.materialSystem.clone('metalStainless')
      )
      holeRing.position.set(pos[0], 0.11, pos[1])
      holeRing.rotation.x = Math.PI / 2
      trayGroup.add(holeRing)
    })

    const connectorSlots = [
      { x: 0, z: 0.65, rot: 0 },
      { x: 0, z: -0.65, rot: Math.PI }
    ]
    connectorSlots.forEach(slot => {
      const connector = new THREE.Mesh(
        new THREE.BoxGeometry(0.3, 0.15, 0.05),
        this.materialSystem.clone('metalSteelBrushed')
      )
      connector.position.set(slot.x, 0.15, slot.z)
      trayGroup.add(connector)

      const pin1 = new THREE.Mesh(
        new THREE.CylinderGeometry(0.015, 0.015, 0.1, 8),
        this.materialSystem.clone('metalBrass')
      )
      pin1.position.set(slot.x + 0.08, 0.2, slot.z)
      pin1.rotation.x = Math.PI / 2
      trayGroup.add(pin1)

      const pin2 = pin1.clone()
      pin2.position.x = slot.x - 0.08
      trayGroup.add(pin2)
    })

    const rfidTag = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.02, 0.05),
      this.materialSystem.clone('plasticWhite')
    )
    rfidTag.position.set(-0.8, 0.09, 0)
    trayGroup.add(rfidTag)

    this.registerPart(
      'payloadTray',
      '货架托盘',
      '标准货架托盘，防滑橡胶垫，定位销孔，电气连接器，RFID 标签，最大承重 500kg',
      trayGroup,
      'payload'
    )
  }

  private cargoGroup: THREE.Group | null = null

  private createCargo() {
    const cargoGroup = new THREE.Group()
    cargoGroup.name = 'cargo'
    cargoGroup.visible = false
    cargoGroup.position.y = 0.12

    const palletBase = new THREE.Mesh(
      new THREE.BoxGeometry(1.2, 0.15, 1.0),
      this.materialSystem.clone('plasticWood')
    )
    cargoGroup.add(palletBase)

    for (let i = 0; i < 3; i++) {
      const skid = new THREE.Mesh(
        new THREE.BoxGeometry(0.1, 0.15, 1.0),
        this.materialSystem.clone('plasticWood')
      )
      skid.position.set(-0.4 + i * 0.4, -0.08, 0)
      cargoGroup.add(skid)
    }

    const box1 = new THREE.Mesh(
      new THREE.BoxGeometry(1.1, 0.8, 0.9),
      this.materialSystem.clone('plasticWood')
    )
    box1.position.y = 0.55
    cargoGroup.add(box1)

    const box2 = new THREE.Mesh(
      new THREE.BoxGeometry(1.05, 0.7, 0.85),
      this.materialSystem.clone('plasticWood')
    )
    box2.position.y = 1.3
    cargoGroup.add(box2)

    const strap1 = new THREE.Mesh(
      new THREE.BoxGeometry(1.12, 0.06, 0.06),
      this.materialSystem.clone('plasticBlue')
    )
    strap1.position.set(0, 0.55, 0)
    cargoGroup.add(strap1)

    const strap2 = new THREE.Mesh(
      new THREE.BoxGeometry(0.06, 0.82, 0.06),
      this.materialSystem.clone('plasticBlue')
    )
    strap2.position.set(-0.4, 0.55, 0.35)
    cargoGroup.add(strap2)

    const strap3 = strap2.clone()
    strap3.position.x = 0.4
    cargoGroup.add(strap3)

    const strap4 = strap2.clone()
    strap4.position.z = -0.35
    cargoGroup.add(strap4)

    const strap5 = strap2.clone()
    strap5.position.set(0.4, 0.55, -0.35)
    cargoGroup.add(strap5)

    const label1 = new THREE.Mesh(
      new THREE.BoxGeometry(0.25, 0.15, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    label1.position.set(0.3, 0.6, 0.455)
    cargoGroup.add(label1)

    const label2 = new THREE.Mesh(
      new THREE.BoxGeometry(0.2, 0.12, 0.01),
      this.materialSystem.clone('plasticGreen')
    )
    label2.position.set(-0.35, 0.3, 0.455)
    cargoGroup.add(label2)

    const warningStripe1 = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.6, 0.01),
      this.materialSystem.clone('safetyOrange')
    )
    warningStripe1.position.set(0, 0.6, 0.455)
    cargoGroup.add(warningStripe1)

    for (let i = 0; i < 4; i++) {
      const box = new THREE.Mesh(
        new THREE.BoxGeometry(0.35, 0.5, 0.4),
        this.materialSystem.clone('plasticWhite')
      )
      box.position.set(-0.35 + (i % 2) * 0.7, 1.6, -0.2 + Math.floor(i / 2) * 0.4)
      cargoGroup.add(box)

      const boxLabel = new THREE.Mesh(
        new THREE.BoxGeometry(0.15, 0.08, 0.01),
        this.materialSystem.clone('plasticGray')
      )
      boxLabel.position.copy(box.position)
      boxLabel.position.z += 0.205
      cargoGroup.add(boxLabel)
    }

    const fragileSticker = new THREE.Mesh(
      new THREE.BoxGeometry(0.12, 0.12, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    fragileSticker.position.set(-0.45, 1.3, 0.425)
    cargoGroup.add(fragileSticker)

    this.cargoGroup = cargoGroup
    const liftGroup = this.group.getObjectByName('liftMechanism')
    if (liftGroup) {
      liftGroup.add(cargoGroup)
    }
  }

  setCargoVisible(visible: boolean) {
    if (this.cargoGroup) {
      this.cargoGroup.visible = visible
    }
  }

  hasCargo(): boolean {
    return this.cargoGroup ? this.cargoGroup.visible : false
  }

  private createSensorVisualizations() {
    const vizGroup = new THREE.Group()
    vizGroup.name = 'sensorVisualizations'
    vizGroup.visible = false

    const lidarScan = new THREE.Mesh(
      new THREE.RingGeometry(0.1, 10, 64, 1, 0, Math.PI * 2),
      this.materialSystem.clone('scanLine')
    )
    lidarScan.position.y = 1.5
    lidarScan.rotation.x = -Math.PI / 2
    lidarScan.name = 'lidarScan'
    vizGroup.add(lidarScan)

    const sensorAngles = [
      { x: 1, z: 0, rotY: 0, name: 'front' },
      { x: -1, z: 0, rotY: Math.PI, name: 'back' },
      { x: 0, z: 1, rotY: Math.PI / 2, name: 'left' },
      { x: 0, z: -1, rotY: -Math.PI / 2, name: 'right' }
    ]

    sensorAngles.forEach((angle, idx) => {
      const cone = new THREE.Mesh(
        new THREE.ConeGeometry(0.5, 1, 32, 1, true),
        this.materialSystem.clone('scanLine')
      )
      cone.position.set(angle.x * 1.5, 0.35, angle.z * 1.5)
      cone.rotation.x = -Math.PI / 2
      cone.rotation.z = -angle.rotY
      cone.name = `sensorCone_${idx}`
      vizGroup.add(cone)
    })

    const cameraFrustum = new THREE.Mesh(
      new THREE.ConeGeometry(0.8, 3, 4, 1, true),
      this.materialSystem.clone('scanLine')
    )
    cameraFrustum.position.set(0, 0.8, 2)
    cameraFrustum.rotation.x = -Math.PI / 2
    cameraFrustum.name = 'cameraFrustum'
    vizGroup.add(cameraFrustum)

    const downCameraFrustum = new THREE.Mesh(
      new THREE.ConeGeometry(0.3, 0.5, 4, 1, true),
      this.materialSystem.clone('scanLine')
    )
    downCameraFrustum.position.set(0, 0.15, 0)
    downCameraFrustum.rotation.x = Math.PI
    downCameraFrustum.name = 'downCameraFrustum'
    vizGroup.add(downCameraFrustum)

    const pathTrailGeometry = new THREE.BufferGeometry()
    const trailPoints = new Float32Array(200 * 3)
    pathTrailGeometry.setAttribute('position', new THREE.BufferAttribute(trailPoints, 3))
    const pathTrail = new THREE.Line(
      pathTrailGeometry,
      new THREE.LineBasicMaterial({ color: 0x00ff00, transparent: true, opacity: 0.5 })
    )
    pathTrail.name = 'pathTrail'
    vizGroup.add(pathTrail)

    this.sensorVisualizations = vizGroup
    this.group.add(vizGroup)
  }

  toggleSensorVisualizations(visible: boolean) {
    if (this.sensorVisualizations) {
      this.sensorVisualizations.visible = visible
    }
  }

  updateLidarScan(angle: number) {
    const lidarScan = this.group.getObjectByName('lidarScan')
    if (lidarScan) {
      ;(lidarScan as THREE.Mesh).rotation.z = angle
    }
  }
}
