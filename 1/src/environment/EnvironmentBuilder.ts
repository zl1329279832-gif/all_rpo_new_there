import * as THREE from 'three'
import { MaterialSystem } from '../core/MaterialSystem'

export interface ShelfData {
  id: string
  position: THREE.Vector3
  hasPayload: boolean
  payloadType?: string
}

export interface QRCodeData {
  id: string
  position: THREE.Vector3
  code: string
  type: 'location' | 'charging' | 'pickup' | 'dropoff'
}

export class EnvironmentBuilder {
  private group: THREE.Group
  private materialSystem: MaterialSystem
  private shelves: ShelfData[] = []
  private qrCodes: QRCodeData[] = []
  private pathPoints: THREE.Vector3[] = []

  constructor(materialSystem: MaterialSystem) {
    this.group = new THREE.Group()
    this.group.name = 'environment'
    this.materialSystem = materialSystem
  }

  build(): THREE.Group {
    this.createWarehouseFloor()
    this.createPathMarkings()
    this.createQRCodes()
    this.createShelvingSystem()
    this.createChargingStation()
    this.createObstacleCones()
    this.createWorkZones()
    this.createPalletRack()
    return this.group
  }

  getShelves(): ShelfData[] {
    return this.shelves
  }

  getQRCodes(): QRCodeData[] {
    return this.qrCodes
  }

  getPathPoints(): THREE.Vector3[] {
    return this.pathPoints
  }

  private createWarehouseFloor() {
    const floorGroup = new THREE.Group()
    floorGroup.name = 'warehouseFloor'

    const mainFloor = new THREE.Mesh(
      new THREE.PlaneGeometry(40, 30),
      this.materialSystem.clone('floor')
    )
    mainFloor.rotation.x = -Math.PI / 2
    mainFloor.receiveShadow = true
    floorGroup.add(mainFloor)

    const floorLines = new THREE.Group()
    floorLines.name = 'floorGrid'

    for (let i = -20; i <= 20; i += 2) {
      const lineX = new THREE.Mesh(
        new THREE.PlaneGeometry(0.02, 30),
        new THREE.MeshBasicMaterial({ color: 0x333333, transparent: true, opacity: 0.2 })
      )
      lineX.rotation.x = -Math.PI / 2
      lineX.position.set(i, 0.001, 0)
      floorLines.add(lineX)
    }

    for (let i = -15; i <= 15; i += 2) {
      const lineZ = new THREE.Mesh(
        new THREE.PlaneGeometry(40, 0.02),
        new THREE.MeshBasicMaterial({ color: 0x333333, transparent: true, opacity: 0.2 })
      )
      lineZ.rotation.x = -Math.PI / 2
      lineZ.position.set(0, 0.001, i)
      floorLines.add(lineZ)
    }

    floorGroup.add(floorLines)
    this.group.add(floorGroup)
  }

  private createPathMarkings() {
    const pathGroup = new THREE.Group()
    pathGroup.name = 'pathMarkings'

    const pathMaterial = this.materialSystem.clone('safetyYellow')

    const pathRoutes = [
      { start: new THREE.Vector3(-8, 0.002, 0), end: new THREE.Vector3(8, 0.002, 0), width: 0.8 },
      { start: new THREE.Vector3(0, 0.002, -10), end: new THREE.Vector3(0, 0.002, 10), width: 0.8 },
      { start: new THREE.Vector3(5, 0.002, 0), end: new THREE.Vector3(5, 0.002, 8), width: 0.8 },
      { start: new THREE.Vector3(-5, 0.002, 0), end: new THREE.Vector3(-5, 0.002, -8), width: 0.8 },
      { start: new THREE.Vector3(8, 0.002, 5), end: new THREE.Vector3(-8, 0.002, 5), width: 0.6 },
      { start: new THREE.Vector3(8, 0.002, -5), end: new THREE.Vector3(-8, 0.002, -5), width: 0.6 }
    ]

    pathRoutes.forEach((route, idx) => {
      const dx = route.end.x - route.start.x
      const dz = route.end.z - route.start.z
      const length = Math.sqrt(dx * dx + dz * dz)
      const angle = Math.atan2(dz, dx)

      const path = new THREE.Mesh(
        new THREE.PlaneGeometry(length, route.width),
        pathMaterial
      )
      path.rotation.x = -Math.PI / 2
      path.rotation.z = -angle
      path.position.set(
        (route.start.x + route.end.x) / 2,
        0.002,
        (route.start.z + route.end.z) / 2
      )
      pathGroup.add(path)

      const edgeMaterial = new THREE.MeshBasicMaterial({ color: 0x000000 })
      const edge1 = new THREE.Mesh(
        new THREE.PlaneGeometry(length, 0.03),
        edgeMaterial
      )
      edge1.rotation.x = -Math.PI / 2
      edge1.rotation.z = -angle
      edge1.position.set(
        (route.start.x + route.end.x) / 2,
        0.003,
        (route.start.z + route.end.z) / 2 + route.width / 2
      )
      pathGroup.add(edge1)

      const edge2 = edge1.clone()
      edge2.position.set(
        (route.start.x + route.end.x) / 2,
        0.003,
        (route.start.z + route.end.z) / 2 - route.width / 2
      )
      pathGroup.add(edge2)

      const steps = Math.floor(length / 0.5)
      for (let i = 0; i < steps; i++) {
        const t = (i + 0.5) / steps
        this.pathPoints.push(new THREE.Vector3(
          route.start.x + dx * t,
          0,
          route.start.z + dz * t
        ))
      }
    })

    const arrowPositions = [
      { x: 0, z: 5, rot: 0 },
      { x: 0, z: -5, rot: Math.PI },
      { x: 5, z: 0, rot: Math.PI / 2 },
      { x: -5, z: 0, rot: -Math.PI / 2 }
    ]

    arrowPositions.forEach(pos => {
      const arrowShape = new THREE.Shape()
      arrowShape.moveTo(-0.3, -0.1)
      arrowShape.lineTo(0, 0.3)
      arrowShape.lineTo(0.3, -0.1)
      arrowShape.lineTo(0.15, -0.1)
      arrowShape.lineTo(0.15, -0.3)
      arrowShape.lineTo(-0.15, -0.3)
      arrowShape.lineTo(-0.15, -0.1)
      arrowShape.lineTo(-0.3, -0.1)

      const arrow = new THREE.Mesh(
        new THREE.ShapeGeometry(arrowShape),
        new THREE.MeshBasicMaterial({ color: 0x000000, side: THREE.DoubleSide })
      )
      arrow.rotation.x = -Math.PI / 2
      arrow.rotation.z = -pos.rot
      arrow.position.set(pos.x, 0.004, pos.z)
      pathGroup.add(arrow)
    })

    this.group.add(pathGroup)
  }

  private createQRCodes() {
    const qrGroup = new THREE.Group()
    qrGroup.name = 'qrCodes'

    const qrPositions = [
      { x: -8, z: 0, code: 'CHG-001', type: 'charging' as const },
      { x: 8, z: 5, code: 'PICK-001', type: 'pickup' as const },
      { x: 8, z: -5, code: 'DROP-001', type: 'dropoff' as const },
      { x: 0, z: 8, code: 'LOC-A01', type: 'location' as const },
      { x: 0, z: -8, code: 'LOC-B01', type: 'location' as const },
      { x: 5, z: 8, code: 'LOC-C01', type: 'location' as const },
      { x: -5, z: -8, code: 'LOC-D01', type: 'location' as const }
    ]

    qrPositions.forEach((pos, idx) => {
      const qrGroupSingle = new THREE.Group()
      qrGroupSingle.name = `qrCode_${idx}`
      qrGroupSingle.position.set(pos.x, 0.005, pos.z)

      const qrBase = new THREE.Mesh(
        new THREE.PlaneGeometry(0.4, 0.4),
        this.materialSystem.clone('plasticWhite')
      )
      qrBase.rotation.x = -Math.PI / 2
      qrGroupSingle.add(qrBase)

      const qrPattern = new THREE.Group()
      for (let i = 0; i < 5; i++) {
        for (let j = 0; j < 5; j++) {
          if (Math.random() > 0.4 || (i < 2 && j < 2) || (i < 2 && j > 2) || (i > 2 && j < 2)) {
            const dot = new THREE.Mesh(
              new THREE.PlaneGeometry(0.06, 0.06),
              new THREE.MeshBasicMaterial({ color: 0x000000 })
            )
            dot.rotation.x = -Math.PI / 2
            dot.position.set(-0.12 + i * 0.06, 0.001, -0.12 + j * 0.06)
            qrPattern.add(dot)
          }
        }
      }
      qrGroupSingle.add(qrPattern)

      const finderPatterns = [
        { x: -0.13, z: -0.13 },
        { x: 0.13, z: -0.13 },
        { x: -0.13, z: 0.13 }
      ]
      finderPatterns.forEach(fp => {
        const outer = new THREE.Mesh(
          new THREE.PlaneGeometry(0.1, 0.1),
          new THREE.MeshBasicMaterial({ color: 0x000000 })
        )
        outer.rotation.x = -Math.PI / 2
        outer.position.set(fp.x, 0.001, fp.z)
        qrGroupSingle.add(outer)

        const inner = new THREE.Mesh(
          new THREE.PlaneGeometry(0.06, 0.06),
          new THREE.MeshBasicMaterial({ color: 0xffffff })
        )
        inner.rotation.x = -Math.PI / 2
        inner.position.set(fp.x, 0.002, fp.z)
        qrGroupSingle.add(inner)

        const center = new THREE.Mesh(
          new THREE.PlaneGeometry(0.03, 0.03),
          new THREE.MeshBasicMaterial({ color: 0x000000 })
        )
        center.rotation.x = -Math.PI / 2
        center.position.set(fp.x, 0.003, fp.z)
        qrGroupSingle.add(center)
      })

      const label = new THREE.Mesh(
        new THREE.PlaneGeometry(0.35, 0.08),
        this.materialSystem.clone('plasticWhite')
      )
      label.rotation.x = -Math.PI / 2
      label.position.set(0, 0.001, 0.25)
      qrGroupSingle.add(label)

      qrGroup.add(qrGroupSingle)

      this.qrCodes.push({
        id: `qr_${idx}`,
        position: new THREE.Vector3(pos.x, 0, pos.z),
        code: pos.code,
        type: pos.type
      })
    })

    this.group.add(qrGroup)
  }

  private createShelvingSystem() {
    const shelfGroup = new THREE.Group()
    shelfGroup.name = 'shelvingSystem'

    const shelfRows = 2
    const shelvesPerRow = 4
    const shelfSpacing = 3.5
    const rowSpacing = 8

    for (let row = 0; row < shelfRows; row++) {
      for (let col = 0; col < shelvesPerRow; col++) {
        const x = 10 + col * shelfSpacing
        const z = -8 + row * rowSpacing

        const singleShelf = this.createSingleShelf(`${row}_${col}`, x, z)
        shelfGroup.add(singleShelf)

        this.shelves.push({
          id: `shelf_${row}_${col}`,
          position: new THREE.Vector3(x, 0, z),
          hasPayload: Math.random() > 0.3,
          payloadType: ['box', 'container', 'pallet'][Math.floor(Math.random() * 3)]
        })
      }
    }

    this.group.add(shelfGroup)
  }

  private createSingleShelf(id: string, x: number, z: number): THREE.Group {
    const shelf = new THREE.Group()
    shelf.name = `shelf_${id}`
    shelf.position.set(x, 0, z)

    const supportPositions = [
      { x: -1, z: -0.6 },
      { x: 1, z: -0.6 },
      { x: -1, z: 0.6 },
      { x: 1, z: 0.6 }
    ]

    supportPositions.forEach(pos => {
      const verticalSupport = new THREE.Mesh(
        new THREE.BoxGeometry(0.08, 3.5, 0.08),
        this.materialSystem.clone('metalSteel')
      )
      verticalSupport.position.set(pos.x, 1.75, pos.z)
      verticalSupport.castShadow = true
      verticalSupport.receiveShadow = true
      shelf.add(verticalSupport)

      const basePlate = new THREE.Mesh(
        new THREE.BoxGeometry(0.15, 0.05, 0.15),
        this.materialSystem.clone('metalSteelBrushed')
      )
      basePlate.position.set(pos.x, 0.025, pos.z)
      shelf.add(basePlate)
    })

    for (let level = 0; level < 5; level++) {
      const y = 0.3 + level * 0.7

      const shelfBoard = new THREE.Mesh(
        new THREE.BoxGeometry(2.2, 0.06, 1.3),
        this.materialSystem.clone('metalAluminum')
      )
      shelfBoard.position.set(0, y, 0)
      shelfBoard.castShadow = true
      shelfBoard.receiveShadow = true
      shelf.add(shelfBoard)

      const shelfFrame = new THREE.Mesh(
        new THREE.BoxGeometry(2.3, 0.04, 1.4),
        this.materialSystem.clone('metalSteelBrushed')
      )
      shelfFrame.position.set(0, y - 0.03, 0)
      shelf.add(shelfFrame)

      const beamFront = new THREE.Mesh(
        new THREE.BoxGeometry(2.3, 0.08, 0.05),
        this.materialSystem.clone('metalSteel')
      )
      beamFront.position.set(0, y - 0.02, 0.68)
      shelf.add(beamFront)

      const beamBack = beamFront.clone()
      beamBack.position.z = -0.68
      shelf.add(beamBack)

      if (level < 3 && Math.random() > 0.4) {
        this.addBoxesToShelf(shelf, y + 0.05)
      }
    }

    const labelPlate = new THREE.Mesh(
      new THREE.BoxGeometry(0.3, 0.15, 0.02),
      this.materialSystem.clone('plasticWhite')
    )
    labelPlate.position.set(0, 0.2, 0.71)
    shelf.add(labelPlate)

    return shelf
  }

  private addBoxesToShelf(parent: THREE.Group, y: number) {
    const boxTypes = [
      { size: [0.4, 0.35, 0.4], color: 'plasticBlue' },
      { size: [0.35, 0.3, 0.35], color: 'plasticWhite' },
      { size: [0.5, 0.4, 0.5], color: 'plasticDark' }
    ]

    const numBoxes = Math.floor(Math.random() * 3) + 1
    const positions = [
      { x: -0.5, z: 0 },
      { x: 0.5, z: 0.2 },
      { x: 0, z: -0.2 }
    ]

    for (let i = 0; i < numBoxes && i < 3; i++) {
      const boxType = boxTypes[Math.floor(Math.random() * boxTypes.length)]
      const box = new THREE.Mesh(
        new THREE.BoxGeometry(boxType.size[0], boxType.size[1], boxType.size[2]),
        this.materialSystem.clone(boxType.color as keyof typeof this.materialSystem['materials'])
      )
      box.position.set(positions[i].x, y + boxType.size[1] / 2, positions[i].z)
      box.castShadow = true
      box.receiveShadow = true
      parent.add(box)

      const tape = new THREE.Mesh(
        new THREE.BoxGeometry(boxType.size[0] + 0.02, 0.04, boxType.size[2] + 0.02),
        this.materialSystem.clone('plasticWhite')
      )
      tape.position.set(positions[i].x, y + boxType.size[1] / 2, positions[i].z)
      parent.add(tape)
    }
  }

  private createChargingStation() {
    const stationGroup = new THREE.Group()
    stationGroup.name = 'chargingStation'
    stationGroup.position.set(-10, 0, 0)

    const basePlatform = new THREE.Mesh(
      new THREE.BoxGeometry(3, 0.15, 2.5),
      this.materialSystem.clone('metalSteel')
    )
    basePlatform.position.y = 0.075
    basePlatform.receiveShadow = true
    stationGroup.add(basePlatform)

    const platformFrame = new THREE.Mesh(
      new THREE.BoxGeometry(3.2, 0.05, 2.7),
      this.materialSystem.clone('metalSteelBrushed')
    )
    platformFrame.position.y = 0.175
    stationGroup.add(platformFrame)

    const guideRailL = new THREE.Mesh(
      new THREE.BoxGeometry(3, 0.1, 0.08),
      this.materialSystem.clone('safetyYellow')
    )
    guideRailL.position.set(0, 0.2, 1.3)
    stationGroup.add(guideRailL)

    const guideRailR = guideRailL.clone()
    guideRailR.position.z = -1.3
    stationGroup.add(guideRailR)

    const chargingPost = new THREE.Group()
    chargingPost.position.set(0, 0.15, -1.2)

    const postBase = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 0.2, 0.5),
      this.materialSystem.clone('metalSteel')
    )
    postBase.position.y = 0.1
    chargingPost.add(postBase)

    const postBody = new THREE.Mesh(
      new THREE.BoxGeometry(0.7, 1.8, 0.4),
      this.materialSystem.clone('plasticDark')
    )
    postBody.position.y = 1.1
    chargingPost.add(postBody)

    const controlPanel = new THREE.Mesh(
      new THREE.BoxGeometry(0.5, 0.4, 0.05),
      this.materialSystem.clone('plasticGray')
    )
    controlPanel.position.set(0, 1.4, 0.226)
    chargingPost.add(controlPanel)

    const displayScreen = new THREE.Mesh(
      new THREE.BoxGeometry(0.35, 0.2, 0.01),
      this.materialSystem.clone('sensor')
    )
    displayScreen.position.set(0, 1.45, 0.256)
    chargingPost.add(displayScreen)

    const indicatorLights = [
      { x: -0.12, color: 'ledGreen' },
      { x: 0, color: 'ledOrange' },
      { x: 0.12, color: 'ledRed' }
    ]
    indicatorLights.forEach(light => {
      const led = new THREE.Mesh(
        new THREE.CircleGeometry(0.02, 8),
        this.materialSystem.clone(light.color as keyof typeof this.materialSystem['materials'])
      )
      led.position.set(light.x, 1.3, 0.256)
      led.rotation.y = 0
      chargingPost.add(led)
    })

    const contactAssembly = new THREE.Group()
    contactAssembly.position.set(0, 0.5, 0.5)

    const contactPlate = new THREE.Mesh(
      new THREE.BoxGeometry(0.6, 0.25, 0.15),
      this.materialSystem.clone('plasticDark')
    )
    contactAssembly.add(contactPlate)

    const positiveContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.12, 0.1),
      this.materialSystem.clone('metalBrass')
    )
    positiveContact.position.set(0.15, 0, 0.125)
    contactAssembly.add(positiveContact)

    const negativeContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.12, 0.1),
      this.materialSystem.clone('metalBrass')
    )
    negativeContact.position.set(-0.15, 0, 0.125)
    contactAssembly.add(negativeContact)

    const guidePinL = new THREE.Mesh(
      new THREE.ConeGeometry(0.05, 0.15, 12),
      this.materialSystem.clone('metalSteel')
    )
    guidePinL.position.set(0.25, 0, 0.2)
    contactAssembly.add(guidePinL)

    const guidePinR = guidePinL.clone()
    guidePinR.position.x = -0.25
    contactAssembly.add(guidePinR)

    chargingPost.add(contactAssembly)

    const cable = new THREE.Mesh(
      new THREE.CylinderGeometry(0.03, 0.03, 1.5, 8),
      this.materialSystem.clone('plasticDark')
    )
    cable.position.set(0.35, 0.75, -0.3)
    cable.rotation.z = Math.PI / 6
    chargingPost.add(cable)

    stationGroup.add(chargingPost)

    const stationSign = new THREE.Mesh(
      new THREE.BoxGeometry(1.2, 0.4, 0.05),
      this.materialSystem.clone('safetyYellow')
    )
    stationSign.position.set(0, 2.5, -1.2)
    stationGroup.add(stationSign)

    const signText = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 0.2, 0.01),
      this.materialSystem.clone('plasticDark')
    )
    signText.position.set(0, 2.5, -1.17)
    stationGroup.add(signText)

    const zoneMarking = new THREE.Mesh(
      new THREE.PlaneGeometry(4, 3.5),
      new THREE.MeshBasicMaterial({ color: 0xffff00, transparent: true, opacity: 0.15, side: THREE.DoubleSide })
    )
    zoneMarking.rotation.x = -Math.PI / 2
    zoneMarking.position.set(0, 0.001, 0)
    stationGroup.add(zoneMarking)

    this.group.add(stationGroup)
  }

  private createObstacleCones() {
    const coneGroup = new THREE.Group()
    coneGroup.name = 'obstacleCones'

    const conePositions = [
      { x: 3, z: 3 },
      { x: -2, z: 6 },
      { x: 4, z: -4 },
      { x: -3, z: -6 },
      { x: 6, z: 2 }
    ]

    conePositions.forEach((pos, idx) => {
      const cone = new THREE.Group()
      cone.name = `cone_${idx}`
      cone.position.set(pos.x, 0, pos.z)

      const base = new THREE.Mesh(
        new THREE.CylinderGeometry(0.25, 0.3, 0.08, 16),
        this.materialSystem.clone('rubberBlack')
      )
      base.position.y = 0.04
      base.castShadow = true
      cone.add(base)

      const coneBody = new THREE.Mesh(
        new THREE.ConeGeometry(0.2, 0.6, 16),
        this.materialSystem.clone('safetyOrange')
      )
      coneBody.position.y = 0.38
      coneBody.castShadow = true
      cone.add(coneBody)

      const stripe1 = new THREE.Mesh(
        new THREE.CylinderGeometry(0.14, 0.16, 0.06, 16),
        new THREE.MeshBasicMaterial({ color: 0xffffff })
      )
      stripe1.position.y = 0.35
      cone.add(stripe1)

      const stripe2 = new THREE.Mesh(
        new THREE.CylinderGeometry(0.1, 0.12, 0.06, 16),
        new THREE.MeshBasicMaterial({ color: 0xffffff })
      )
      stripe2.position.y = 0.55
      cone.add(stripe2)

      const top = new THREE.Mesh(
        new THREE.SphereGeometry(0.04, 8, 8),
        this.materialSystem.clone('safetyRed')
      )
      top.position.y = 0.7
      cone.add(top)

      coneGroup.add(cone)
    })

    this.group.add(coneGroup)
  }

  private createWorkZones() {
    const zoneGroup = new THREE.Group()
    zoneGroup.name = 'workZones'

    const pickupZone = new THREE.Group()
    pickupZone.name = 'pickupZone'
    pickupZone.position.set(8, 0, 5)

    const pickupFloor = new THREE.Mesh(
      new THREE.PlaneGeometry(3, 3),
      new THREE.MeshBasicMaterial({ color: 0x00ff00, transparent: true, opacity: 0.2, side: THREE.DoubleSide })
    )
    pickupFloor.rotation.x = -Math.PI / 2
    pickupFloor.position.y = 0.001
    pickupZone.add(pickupFloor)

    const pickupBorder = new THREE.Mesh(
      new THREE.RingGeometry(1.45, 1.55, 4),
      new THREE.MeshBasicMaterial({ color: 0x00ff00, side: THREE.DoubleSide })
    )
    pickupBorder.rotation.x = -Math.PI / 2
    pickupBorder.rotation.z = Math.PI / 4
    pickupBorder.position.y = 0.002
    pickupZone.add(pickupBorder)

    const pickupSign = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 0.3, 0.05),
      this.materialSystem.clone('plasticGreen')
    )
    pickupSign.position.set(0, 0.15, 1.6)
    pickupZone.add(pickupSign)

    zoneGroup.add(pickupZone)

    const dropoffZone = new THREE.Group()
    dropoffZone.name = 'dropoffZone'
    dropoffZone.position.set(8, 0, -5)

    const dropoffFloor = new THREE.Mesh(
      new THREE.PlaneGeometry(3, 3),
      new THREE.MeshBasicMaterial({ color: 0xff6600, transparent: true, opacity: 0.2, side: THREE.DoubleSide })
    )
    dropoffFloor.rotation.x = -Math.PI / 2
    dropoffFloor.position.y = 0.001
    dropoffZone.add(dropoffFloor)

    const dropoffBorder = new THREE.Mesh(
      new THREE.RingGeometry(1.45, 1.55, 4),
      new THREE.MeshBasicMaterial({ color: 0xff6600, side: THREE.DoubleSide })
    )
    dropoffBorder.rotation.x = -Math.PI / 2
    dropoffBorder.rotation.z = Math.PI / 4
    dropoffBorder.position.y = 0.002
    dropoffZone.add(dropoffBorder)

    const dropoffSign = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 0.3, 0.05),
      this.materialSystem.clone('safetyOrange')
    )
    dropoffSign.position.set(0, 0.15, 1.6)
    dropoffZone.add(dropoffSign)

    zoneGroup.add(dropoffZone)

    const maintenanceZone = new THREE.Group()
    maintenanceZone.name = 'maintenanceZone'
    maintenanceZone.position.set(-12, 0, 8)

    const maintenanceFloor = new THREE.Mesh(
      new THREE.PlaneGeometry(4, 4),
      new THREE.MeshBasicMaterial({ color: 0xff0000, transparent: true, opacity: 0.15, side: THREE.DoubleSide })
    )
    maintenanceFloor.rotation.x = -Math.PI / 2
    maintenanceFloor.position.y = 0.001
    maintenanceZone.add(maintenanceFloor)

    const stripeLines = new THREE.Group()
    for (let i = 0; i < 8; i++) {
      const stripe = new THREE.Mesh(
        new THREE.PlaneGeometry(4, 0.15),
        new THREE.MeshBasicMaterial({ color: 0xff0000, side: THREE.DoubleSide })
      )
      stripe.rotation.x = -Math.PI / 2
      stripe.rotation.z = Math.PI / 4
      stripe.position.set(-1.5 + i * 0.5, 0.002, 0)
      stripeLines.add(stripe)
    }
    maintenanceZone.add(stripeLines)

    const maintenanceSign = new THREE.Mesh(
      new THREE.BoxGeometry(1, 0.4, 0.08),
      this.materialSystem.clone('safetyRed')
    )
    maintenanceSign.position.set(0, 2, 2.1)
    maintenanceZone.add(maintenanceSign)

    zoneGroup.add(maintenanceZone)

    this.group.add(zoneGroup)
  }

  private createPalletRack() {
    const rackGroup = new THREE.Group()
    rackGroup.name = 'palletRack'

    const rackPositions = [
      { x: -10, z: 5 },
      { x: -10, z: -5 }
    ]

    rackPositions.forEach((pos, idx) => {
      const rack = new THREE.Group()
      rack.name = `palletRack_${idx}`
      rack.position.set(pos.x, 0, pos.z)

      const framePositions = [
        { x: -1.2, z: -0.8 },
        { x: 1.2, z: -0.8 },
        { x: -1.2, z: 0.8 },
        { x: 1.2, z: 0.8 }
      ]

      framePositions.forEach(fp => {
        const frame = new THREE.Mesh(
          new THREE.BoxGeometry(0.12, 4, 0.12),
          this.materialSystem.clone('metalSteel')
        )
        frame.position.set(fp.x, 2, fp.z)
        frame.castShadow = true
        rack.add(frame)
      })

      for (let level = 0; level < 3; level++) {
        const y = 0.8 + level * 1.2

        const beamF = new THREE.Mesh(
          new THREE.BoxGeometry(2.6, 0.15, 0.1),
          this.materialSystem.clone('metalSteelBrushed')
        )
        beamF.position.set(0, y, 0.8)
        beamF.castShadow = true
        rack.add(beamF)

        const beamB = beamF.clone()
        beamB.position.z = -0.8
        rack.add(beamB)

        const deck = new THREE.Mesh(
          new THREE.BoxGeometry(2.4, 0.08, 1.6),
          this.materialSystem.clone('metalSteel')
        )
        deck.position.set(0, y + 0.08, 0)
        deck.castShadow = true
        deck.receiveShadow = true
        rack.add(deck)

        if (level === 1) {
          const pallet = new THREE.Mesh(
            new THREE.BoxGeometry(2, 0.15, 1.4),
            this.materialSystem.clone('plasticWood')
          )
          pallet.position.set(0, y + 0.2, 0)
          pallet.castShadow = true
          rack.add(pallet)

          const boxStack = new THREE.Mesh(
            new THREE.BoxGeometry(1.5, 1, 1),
            this.materialSystem.clone('plasticBlue')
          )
          boxStack.position.set(0, y + 0.775, 0)
          boxStack.castShadow = true
          rack.add(boxStack)
        }
      }

      rackGroup.add(rack)
    })

    this.group.add(rackGroup)
  }

  getObstacles(): THREE.Object3D[] {
    const obstacles: THREE.Object3D[] = []

    const shelfGroup = this.group.getObjectByName('shelvingSystem')
    if (shelfGroup) {
      shelfGroup.traverse((child) => {
        if (child.name.startsWith('shelf_') && child !== shelfGroup) {
          obstacles.push(child)
        }
      })
    }

    const coneGroup = this.group.getObjectByName('obstacleCones')
    if (coneGroup) {
      coneGroup.traverse((child) => {
        if (child.name.startsWith('cone_') && child !== coneGroup) {
          obstacles.push(child)
        }
      })
    }

    const rackGroup = this.group.getObjectByName('heavyDutyRacks')
    if (rackGroup) {
      rackGroup.traverse((child) => {
        if (child.name.startsWith('rack_') && child !== rackGroup) {
          obstacles.push(child)
        }
      })
    }

    const charger = this.group.getObjectByName('chargingStation')
    if (charger) {
      obstacles.push(charger)
    }

    return obstacles
  }
}
