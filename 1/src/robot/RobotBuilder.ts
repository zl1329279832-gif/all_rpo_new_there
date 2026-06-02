import * as THREE from 'three'
import { MaterialSystem } from '../core/MaterialSystem'
import { RobotPart } from '../types'

export class RobotBuilder {
  private group: THREE.Group
  private materialSystem: MaterialSystem
  private parts: Map<string, RobotPart> = new Map()
  private originalPositions: Map<string, THREE.Vector3> = new Map()

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

    return this.group
  }

  getParts(): Map<string, RobotPart> {
    return this.parts
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
    this.group.add(mesh)
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

    const bottomPlate = new THREE.Mesh(
      new THREE.BoxGeometry(1.85, 0.06, 1.45),
      this.materialSystem.clone('metalSteel')
    )
    bottomPlate.position.y = 0.05
    chassisGroup.add(bottomPlate)

    const cornerPositions = [
      [0.85, 0.3, 0.65], [-0.85, 0.3, 0.65],
      [0.85, 0.3, -0.65], [-0.85, 0.3, -0.65]
    ]
    cornerPositions.forEach(pos => {
      const corner = new THREE.Mesh(
        new THREE.SphereGeometry(0.12, 16, 16, 0, Math.PI * 2, 0, Math.PI / 2),
        this.materialSystem.clone('plasticDark')
      )
      corner.position.set(pos[0], pos[1], pos[2])
      chassisGroup.add(corner)
    })

    this.registerPart('chassis', '机器人底盘', '高强度铝合金底盘，承载所有机械和电子部件', chassisGroup, 'structure')
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

      const tire = new THREE.Mesh(
        new THREE.TorusGeometry(0.18, 0.06, 12, 24),
        this.materialSystem.clone('rubberBlack')
      )
      tire.rotation.x = Math.PI / 2
      wheelGroup.add(tire)

      const wheelHub = new THREE.Mesh(
        new THREE.CylinderGeometry(0.1, 0.1, 0.04, 16),
        this.materialSystem.clone('metalSteel')
      )
      wheelHub.rotation.x = Math.PI / 2
      wheelGroup.add(wheelHub)

      const hubCap = new THREE.Mesh(
        new THREE.CircleGeometry(0.08, 16),
        this.materialSystem.clone('plasticBlue')
      )
      hubCap.position.z = 0.021
      wheelGroup.add(hubCap)

      const suspension = new THREE.Mesh(
        new THREE.BoxGeometry(0.15, 0.15, 0.1),
        this.materialSystem.clone('metalSteel')
      )
      suspension.position.y = 0.2
      wheelGroup.add(suspension)

      this.registerPart(
        `wheel_${index}`,
        `驱动轮 ${index + 1}`,
        '高性能麦克纳姆轮，支持全向移动',
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

    const scissorPositions = [-0.5, 0.5]
    scissorPositions.forEach(x => {
      const arm1 = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.6, 0.06),
        this.materialSystem.clone('metalAluminum')
      )
      arm1.position.set(x, 0.35, 0)
      arm1.rotation.z = 0.2
      liftGroup.add(arm1)

      const arm2 = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 0.6, 0.06),
        this.materialSystem.clone('metalAluminum')
      )
      arm2.position.set(x, 0.35, 0)
      arm2.rotation.z = -0.2
      liftGroup.add(arm2)

      const pivot = new THREE.Mesh(
        new THREE.SphereGeometry(0.05, 12, 12),
        this.materialSystem.clone('metalBrass')
      )
      pivot.position.set(x, 0.35, 0)
      liftGroup.add(pivot)
    })

    const hydraulicCylinder = new THREE.Mesh(
      new THREE.CylinderGeometry(0.06, 0.08, 0.4, 12),
      this.materialSystem.clone('metalSteel')
    )
    hydraulicCylinder.position.set(0, 0.3, 0.15)
    hydraulicCylinder.rotation.x = -0.3
    liftGroup.add(hydraulicCylinder)

    const hydraulicRod = new THREE.Mesh(
      new THREE.CylinderGeometry(0.03, 0.03, 0.35, 12),
      this.materialSystem.clone('metalAluminum')
    )
    hydraulicRod.position.set(0, 0.5, 0.2)
    hydraulicRod.rotation.x = -0.3
    liftGroup.add(hydraulicRod)

    const topPlatform = new THREE.Mesh(
      new THREE.BoxGeometry(1.7, 0.1, 1.3),
      this.materialSystem.clone('metalAluminum')
    )
    topPlatform.position.y = 0.65
    liftGroup.add(topPlatform)

    const antiSlipPad = new THREE.Mesh(
      new THREE.BoxGeometry(1.6, 0.03, 1.2),
      this.materialSystem.clone('rubberBlack')
    )
    antiSlipPad.position.y = 0.72
    liftGroup.add(antiSlipPad)

    this.registerPart(
      'liftMechanism',
      '升降机构',
      '剪叉式液压升降系统，最大举升高度 1.2 米',
      liftGroup,
      'mechanism'
    )
  }

  private createSensors() {
    const sensorPositions = [
      { x: 0.95, z: 0, rot: 0 },
      { x: -0.95, z: 0, rot: Math.PI },
      { x: 0, z: 0.75, rot: Math.PI / 2 },
      { x: 0, z: -0.75, rot: -Math.PI / 2 }
    ]

    sensorPositions.forEach((pos, index) => {
      const sensorGroup = new THREE.Group()
      sensorGroup.name = `proximitySensor_${index}`
      sensorGroup.position.set(pos.x, 0.35, pos.z)
      sensorGroup.rotation.y = pos.rot

      const housing = new THREE.Mesh(
        new THREE.BoxGeometry(0.1, 0.12, 0.06),
        this.materialSystem.clone('plasticDark')
      )
      sensorGroup.add(housing)

      const lens = new THREE.Mesh(
        new THREE.CircleGeometry(0.035, 16),
        this.materialSystem.clone('sensor')
      )
      lens.position.x = 0.051
      sensorGroup.add(lens)

      const irEmitter = new THREE.Mesh(
        new THREE.CircleGeometry(0.015, 8),
        this.materialSystem.clone('ledRed')
      )
      irEmitter.position.set(0.051, 0.025, 0)
      sensorGroup.add(irEmitter)

      this.registerPart(
        `proximitySensor_${index}`,
        `红外传感器 ${index + 1}`,
        '近距离红外传感器，探测范围 0-30cm',
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

    const housing = new THREE.Mesh(
      new THREE.CylinderGeometry(0.11, 0.11, 0.15, 24),
      this.materialSystem.clone('plasticDark')
    )
    housing.position.y = 0.1
    lidarGroup.add(housing)

    const scanRing = new THREE.Mesh(
      new THREE.TorusGeometry(0.1, 0.025, 12, 36),
      this.materialSystem.clone('lidar')
    )
    scanRing.position.y = 0.1
    scanRing.rotation.x = Math.PI / 2
    scanRing.name = 'lidarScanRing'
    lidarGroup.add(scanRing)

    const topCover = new THREE.Mesh(
      new THREE.ConeGeometry(0.1, 0.06, 24),
      this.materialSystem.clone('metalAluminum')
    )
    topCover.position.y = 0.2
    topCover.rotation.x = Math.PI
    lidarGroup.add(topCover)

    this.registerPart(
      'lidar',
      '激光雷达',
      '360 度激光测距传感器，扫描频率 10Hz，探测距离 0.1-10m',
      lidarGroup,
      'sensor'
    )
  }

  private createCameras() {
    const frontCameraGroup = new THREE.Group()
    frontCameraGroup.name = 'frontCamera'
    frontCameraGroup.position.set(0, 0.8, 0.75)

    const cameraBody = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.12, 0.12),
      this.materialSystem.clone('plasticDark')
    )
    frontCameraGroup.add(cameraBody)

    const lens = new THREE.Mesh(
      new THREE.CylinderGeometry(0.04, 0.05, 0.06, 16),
      this.materialSystem.clone('glassClear')
    )
    lens.position.z = 0.09
    lens.rotation.x = Math.PI / 2
    frontCameraGroup.add(lens)

    const lensRing = new THREE.Mesh(
      new THREE.TorusGeometry(0.045, 0.01, 8, 16),
      this.materialSystem.clone('metalBrass')
    )
    lensRing.position.z = 0.09
    lensRing.rotation.x = Math.PI / 2
    frontCameraGroup.add(lensRing)

    const statusLed = new THREE.Mesh(
      new THREE.CircleGeometry(0.015, 8),
      this.materialSystem.clone('ledGreen')
    )
    statusLed.position.set(0.04, 0.03, 0.061)
    frontCameraGroup.add(statusLed)

    this.registerPart(
      'frontCamera',
      '前置摄像头',
      '1080P 高清视觉摄像头，用于视觉导航和二维码识别',
      frontCameraGroup,
      'sensor'
    )

    const downCameraGroup = new THREE.Group()
    downCameraGroup.name = 'downCamera'
    downCameraGroup.position.set(0, 0.4, 0)

    const downCameraBody = new THREE.Mesh(
      new THREE.CylinderGeometry(0.05, 0.06, 0.06, 16),
      this.materialSystem.clone('plasticDark')
    )
    downCameraGroup.add(downCameraBody)

    const downLens = new THREE.Mesh(
      new THREE.CircleGeometry(0.04, 16),
      this.materialSystem.clone('glassClear')
    )
    downLens.position.y = -0.031
    downLens.rotation.x = -Math.PI / 2
    downCameraGroup.add(downLens)

    this.registerPart(
      'downCamera',
      '底部摄像头',
      '视觉定位摄像头，用于地面二维码识别和精确定位',
      downCameraGroup,
      'sensor'
    )
  }

  private createIndicatorLights() {
    const lightStripPositions = [
      { x: 0, z: 0.76, rot: 0, count: 8 },
      { x: 0, z: -0.76, rot: Math.PI, count: 8 },
      { x: 0.91, z: 0, rot: Math.PI / 2, count: 6 },
      { x: -0.91, z: 0, rot: -Math.PI / 2, count: 6 }
    ]

    lightStripPositions.forEach((strip, stripIndex) => {
      const stripGroup = new THREE.Group()
      stripGroup.name = `lightStrip_${stripIndex}`
      stripGroup.position.set(strip.x, 0.5, strip.z)
      stripGroup.rotation.y = strip.rot

      const housing = new THREE.Mesh(
        new THREE.BoxGeometry(0.8, 0.08, 0.04),
        this.materialSystem.clone('plasticDark')
      )
      stripGroup.add(housing)

      for (let i = 0; i < strip.count; i++) {
        const led = new THREE.Mesh(
          new THREE.BoxGeometry(0.08, 0.04, 0.02),
          this.materialSystem.clone('ledBlue')
        )
        led.position.set(-0.35 + i * 0.1, 0, 0.021)
        led.name = `led_${stripIndex}_${i}`
        stripGroup.add(led)
      }

      this.registerPart(
        `lightStrip_${stripIndex}`,
        `状态灯条 ${stripIndex + 1}`,
        'RGB LED 状态指示灯，显示机器人运行状态',
        stripGroup,
        'indicator'
      )
    })

    const batteryLedGroup = new THREE.Group()
    batteryLedGroup.name = 'batteryIndicator'
    batteryLedGroup.position.set(0.7, 0.35, 0.55)

    for (let i = 0; i < 4; i++) {
      const led = new THREE.Mesh(
        new THREE.BoxGeometry(0.04, 0.06, 0.02),
        i < 2 ? this.materialSystem.clone('ledGreen') : this.materialSystem.clone('ledOrange')
      )
      led.position.set(i * 0.05, 0, 0)
      batteryLedGroup.add(led)
    }

    this.registerPart(
      'batteryIndicator',
      '电量指示灯',
      '4 段 LED 电量显示',
      batteryLedGroup,
      'indicator'
    )
  }

  private createBatteryCompartment() {
    const batteryGroup = new THREE.Group()
    batteryGroup.name = 'batteryCompartment'
    batteryGroup.position.set(0, 0.3, -0.4)

    const compartmentFrame = new THREE.Mesh(
      new THREE.BoxGeometry(0.6, 0.35, 0.5),
      this.materialSystem.clone('metalSteel')
    )
    batteryGroup.add(compartmentFrame)

    const batteryPack = new THREE.Mesh(
      new THREE.BoxGeometry(0.5, 0.25, 0.4),
      this.materialSystem.clone('battery')
    )
    batteryPack.position.y = 0
    batteryGroup.add(batteryPack)

    const batteryLabel = new THREE.Mesh(
      new THREE.BoxGeometry(0.25, 0.08, 0.01),
      this.materialSystem.clone('plasticWhite')
    )
    batteryLabel.position.set(0, 0.05, 0.201)
    batteryGroup.add(batteryLabel)

    const terminals = [
      { x: 0.15, color: 'ledRed' },
      { x: -0.15, color: 'metalSteel' }
    ]
    terminals.forEach(t => {
      const terminal = new THREE.Mesh(
        new THREE.BoxGeometry(0.06, 0.04, 0.06),
        this.materialSystem.clone(t.color as string)
      )
      terminal.position.set(t.x, 0.12, 0.2)
      batteryGroup.add(terminal)
    })

    this.registerPart(
      'batteryCompartment',
      '电池仓',
      '48V 20Ah 磷酸铁锂电池组，续航 8 小时',
      batteryGroup,
      'power'
    )
  }

  private createChargingContacts() {
    const contactGroup = new THREE.Group()
    contactGroup.name = 'chargingContacts'
    contactGroup.position.set(0, 0.25, -0.75)

    const contactPlate = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.15, 0.05),
      this.materialSystem.clone('plasticDark')
    )
    contactGroup.add(contactPlate)

    const positiveContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.1, 0.08, 0.04),
      this.materialSystem.clone('metalBrass')
    )
    positiveContact.position.set(0.1, 0, 0.045)
    contactGroup.add(positiveContact)

    const negativeContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.1, 0.08, 0.04),
      this.materialSystem.clone('metalBrass')
    )
    negativeContact.position.set(-0.1, 0, 0.045)
    contactGroup.add(negativeContact)

    const guidePin1 = new THREE.Mesh(
      new THREE.ConeGeometry(0.03, 0.08, 12),
      this.materialSystem.clone('metalSteel')
    )
    guidePin1.position.set(0.15, 0, 0.08)
    contactGroup.add(guidePin1)

    const guidePin2 = new THREE.Mesh(
      new THREE.ConeGeometry(0.03, 0.08, 12),
      this.materialSystem.clone('metalSteel')
    )
    guidePin2.position.set(-0.15, 0, 0.08)
    contactGroup.add(guidePin2)

    this.registerPart(
      'chargingContacts',
      '充电触点',
      '自动充电对接触点，支持快充',
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

    const shellTop = new THREE.Mesh(
      new THREE.BoxGeometry(1.8, 0.1, 1.2),
      this.materialSystem.clone('plasticWhite')
    )
    shellTop.position.y = 0.25
    shellGroup.add(shellTop)

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

    const ventSlots = [-0.4, -0.2, 0, 0.2, 0.4]
    ventSlots.forEach(x => {
      const vent = new THREE.Mesh(
        new THREE.BoxGeometry(0.02, 0.15, 0.02),
        this.materialSystem.clone('plasticDark')
      )
      vent.position.set(x, 0, 0.33)
      shellGroup.add(vent)
    })

    this.registerPart(
      'outerShell',
      '外壳',
      'ABS 工程塑料外壳，防护等级 IP54',
      shellGroup,
      'structure'
    )
  }

  private createPayloadTray() {
    const trayGroup = new THREE.Group()
    trayGroup.name = 'payloadTray'
    trayGroup.position.y = 2.0

    const trayBase = new THREE.Mesh(
      new THREE.BoxGeometry(2.0, 0.08, 1.5),
      this.materialSystem.clone('metalSteel')
    )
    trayGroup.add(trayBase)

    const sideRails = [
      { x: 0.95, w: 0.06, d: 1.5 },
      { x: -0.95, w: 0.06, d: 1.5 },
      { z: 0.7, w: 2.0, d: 0.06 },
      { z: -0.7, w: 2.0, d: 0.06 }
    ]

    sideRails.forEach(rail => {
      const railMesh = new THREE.Mesh(
        new THREE.BoxGeometry(rail.w || 0, 0.15, rail.d || 0),
        this.materialSystem.clone('metalAluminum')
      )
      railMesh.position.set(rail.x || 0, 0.05, rail.z || 0)
      trayGroup.add(railMesh)
    })

    const rubberMat = new THREE.Mesh(
      new THREE.BoxGeometry(1.85, 0.03, 1.35),
      this.materialSystem.clone('rubberBlack')
    )
    rubberMat.position.y = 0.055
    trayGroup.add(rubberMat)

    this.registerPart(
      'payloadTray',
      '货架托盘',
      '标准货架托盘，最大承重 500kg',
      trayGroup,
      'payload'
    )
  }

  getOriginalPositions(): Map<string, THREE.Vector3> {
    return this.originalPositions
  }
}
