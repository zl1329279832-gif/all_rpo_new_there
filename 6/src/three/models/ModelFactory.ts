import * as THREE from 'three'
import { MaterialSystem } from '../materials/MaterialSystem'
import type { PalletType, BoxSize } from '../../types'

export class ModelFactory {
  private materials: MaterialSystem
  private readonly RACK_CONFIG = {
    bayWidth: 1.2,
    bayDepth: 1.0,
    levelHeight: 0.8,
    columnSize: 0.08,
    beamSize: 0.06,
    deckThickness: 0.02,
  }

  constructor() {
    this.materials = MaterialSystem.getInstance()
  }

  createRack(_rows: number, levels: number, bays: number): THREE.Group {
    const rack = new THREE.Group()
    rack.name = 'rack'

    const { bayWidth, bayDepth, levelHeight, columnSize, beamSize, deckThickness } = this.RACK_CONFIG

    const totalWidth = bays * bayWidth
    const totalHeight = levels * levelHeight

    for (let bay = 0; bay <= bays; bay++) {
      const column = new THREE.Mesh(
        new THREE.BoxGeometry(columnSize, totalHeight, columnSize),
        this.materials.rackMaterial
      )
      column.position.set(bay * bayWidth - totalWidth / 2 + columnSize / 2, totalHeight / 2, 0)
      column.castShadow = true
      column.receiveShadow = true
      rack.add(column)
    }

    for (let level = 1; level <= levels; level++) {
      for (let bay = 0; bay < bays; bay++) {
        const beamFront = new THREE.Mesh(
          new THREE.BoxGeometry(bayWidth, beamSize, beamSize),
          this.materials.rackBeamMaterial
        )
        beamFront.position.set(
          bay * bayWidth - totalWidth / 2 + bayWidth / 2,
          level * levelHeight - levelHeight / 2,
          bayDepth / 2 - beamSize / 2
        )
        beamFront.castShadow = true
        rack.add(beamFront)

        const beamBack = beamFront.clone()
        beamBack.position.z = -bayDepth / 2 + beamSize / 2
        rack.add(beamBack)

        const deck = new THREE.Mesh(
          new THREE.BoxGeometry(bayWidth - 0.1, deckThickness, bayDepth - 0.1),
          this.materials.metalDarkMaterial
        )
        deck.position.set(
          bay * bayWidth - totalWidth / 2 + bayWidth / 2,
          level * levelHeight - levelHeight / 2 + beamSize / 2 + deckThickness / 2,
          0
        )
        deck.receiveShadow = true
        rack.add(deck)
      }
    }

    const uprightGeometry = new THREE.BoxGeometry(0.03, levelHeight * 0.9, bayDepth * 0.95)
    for (let level = 0; level < levels; level++) {
      for (let bay = 0; bay <= bays; bay++) {
        const upright = new THREE.Mesh(uprightGeometry, this.materials.rackMaterial)
        upright.position.set(
          bay * bayWidth - totalWidth / 2,
          level * levelHeight + levelHeight / 2,
          0
        )
        rack.add(upright)
      }
    }

    return rack
  }

  createStacker(id: string): THREE.Group {
    const stacker = new THREE.Group()
    stacker.name = `stacker_${id}`

    const chassis = new THREE.Mesh(
      new THREE.BoxGeometry(1.5, 0.3, 0.8),
      this.materials.metalDarkMaterial
    )
    chassis.position.y = 0.15
    chassis.castShadow = true
    stacker.add(chassis)

    const wheelGeometry = new THREE.CylinderGeometry(0.15, 0.15, 0.1, 16)
    const wheelPositions = [
      [-0.5, 0.15, 0.35],
      [0.5, 0.15, 0.35],
      [-0.5, 0.15, -0.35],
      [0.5, 0.15, -0.35],
    ]
    wheelPositions.forEach((pos) => {
      const wheel = new THREE.Mesh(wheelGeometry, this.materials.conveyorRollerMaterial)
      wheel.rotation.z = Math.PI / 2
      wheel.position.set(pos[0], pos[1], pos[2])
      stacker.add(wheel)
    })

    const mastHeight = 8
    const mast = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, mastHeight, 0.15),
      this.materials.metalMaterial
    )
    mast.position.set(-0.5, mastHeight / 2 + 0.3, 0)
    mast.castShadow = true
    stacker.add(mast)

    const mast2 = mast.clone()
    mast2.position.x = 0.5
    stacker.add(mast2)

    const carriage = new THREE.Group()
    carriage.name = 'carriage'
    carriage.position.y = 1.5

    const carriageFrame = new THREE.Mesh(
      new THREE.BoxGeometry(1.4, 0.15, 1.2),
      this.materials.metalMaterial
    )
    carriageFrame.castShadow = true
    carriage.add(carriageFrame)

    const forkBase = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 0.08, 0.3),
      this.materials.metalDarkMaterial
    )
    forkBase.position.z = 0.5
    forkBase.castShadow = true
    carriage.add(forkBase)

    const forkLeft = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.06, 0.8),
      this.materials.metalDarkMaterial
    )
    forkLeft.position.set(-0.25, 0, 0.9)
    forkLeft.castShadow = true
    forkLeft.name = 'forkLeft'
    carriage.add(forkLeft)

    const forkRight = forkLeft.clone()
    forkRight.position.x = 0.25
    forkRight.name = 'forkRight'
    carriage.add(forkRight)

    const controlBox = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.5, 0.3),
      this.materials.cabinetMaterial
    )
    controlBox.position.set(0, 0.25, -0.3)
    carriage.add(controlBox)

    stacker.add(carriage)

    const statusLight = new THREE.Mesh(
      new THREE.SphereGeometry(0.08, 16, 16),
      this.materials.ledGreen
    )
    statusLight.position.set(0, mastHeight + 0.3, 0)
    statusLight.name = 'statusLight'
    stacker.add(statusLight)

    return stacker
  }

  createConveyor(length: number, hasRollers: boolean = true): THREE.Group {
    const conveyor = new THREE.Group()
    conveyor.name = 'conveyor'

    const frame = new THREE.Mesh(
      new THREE.BoxGeometry(length, 0.1, 0.6),
      this.materials.conveyorMaterial
    )
    frame.position.y = 0.75
    frame.receiveShadow = true
    conveyor.add(frame)

    const legGeometry = new THREE.BoxGeometry(0.08, 0.7, 0.08)
    const legCount = Math.ceil(length / 1.5) + 1
    for (let i = 0; i < legCount; i++) {
      const leg = new THREE.Mesh(legGeometry, this.materials.metalMaterial)
      leg.position.set(
        -length / 2 + (i * length) / (legCount - 1),
        0.35,
        0.2
      )
      leg.castShadow = true
      conveyor.add(leg)

      const leg2 = leg.clone()
      leg2.position.z = -0.2
      conveyor.add(leg2)
    }

    if (hasRollers) {
      const rollerGeometry = new THREE.CylinderGeometry(0.06, 0.06, 0.55, 16)
      const rollerCount = Math.ceil(length / 0.15)
      for (let i = 0; i < rollerCount; i++) {
        const roller = new THREE.Mesh(
          rollerGeometry,
          this.materials.conveyorRollerMaterial
        )
        roller.rotation.x = Math.PI / 2
        roller.position.set(
          -length / 2 + (i + 0.5) * (length / rollerCount),
          0.82,
          0
        )
        roller.name = `roller_${i}`
        conveyor.add(roller)
      }
    }

    const sideRail = new THREE.Mesh(
      new THREE.BoxGeometry(length, 0.08, 0.03),
      this.materials.metalMaterial
    )
    sideRail.position.set(0, 0.9, 0.28)
    conveyor.add(sideRail)

    const sideRail2 = sideRail.clone()
    sideRail2.position.z = -0.28
    conveyor.add(sideRail2)

    return conveyor
  }

  createElevator(): THREE.Group {
    const elevator = new THREE.Group()
    elevator.name = 'elevator'

    const shaftWidth = 1.8
    const shaftDepth = 1.5
    const shaftHeight = 8

    const columnGeometry = new THREE.BoxGeometry(0.15, shaftHeight, 0.15)
    const columnPositions = [
      [-shaftWidth / 2, shaftHeight / 2, -shaftDepth / 2],
      [shaftWidth / 2, shaftHeight / 2, -shaftDepth / 2],
      [-shaftWidth / 2, shaftHeight / 2, shaftDepth / 2],
      [shaftWidth / 2, shaftHeight / 2, shaftDepth / 2],
    ]
    columnPositions.forEach((pos) => {
      const column = new THREE.Mesh(columnGeometry, this.materials.metalMaterial)
      column.position.set(pos[0], pos[1], pos[2])
      column.castShadow = true
      elevator.add(column)
    })

    const carriage = new THREE.Group()
    carriage.name = 'carriage'
    carriage.position.y = 0.8

    const platform = new THREE.Mesh(
      new THREE.BoxGeometry(shaftWidth - 0.4, 0.1, shaftDepth - 0.4),
      this.materials.metalDarkMaterial
    )
    platform.castShadow = true
    platform.receiveShadow = true
    carriage.add(platform)

    const fenceGeometry = new THREE.BoxGeometry(0.03, 1.0, shaftDepth - 0.4)
    const fenceLeft = new THREE.Mesh(fenceGeometry, this.materials.fenceMaterial)
    fenceLeft.position.set(-shaftWidth / 2 + 0.25, 0.55, 0)
    carriage.add(fenceLeft)

    const fenceRight = fenceLeft.clone()
    fenceRight.position.x = shaftWidth / 2 - 0.25
    carriage.add(fenceRight)

    const fenceBack = new THREE.Mesh(
      new THREE.BoxGeometry(shaftWidth - 0.4, 1.0, 0.03),
      this.materials.fenceMaterial
    )
    fenceBack.position.set(0, 0.55, -shaftDepth / 2 + 0.25)
    carriage.add(fenceBack)

    const motorBox = new THREE.Mesh(
      new THREE.BoxGeometry(0.5, 0.4, 0.3),
      this.materials.cabinetMaterial
    )
    motorBox.position.set(0, shaftHeight + 0.2, 0)
    elevator.add(motorBox)

    elevator.add(carriage)

    const statusLight = new THREE.Mesh(
      new THREE.SphereGeometry(0.1, 16, 16),
      this.materials.ledGreen
    )
    statusLight.position.set(shaftWidth / 2 - 0.3, shaftHeight + 0.4, 0)
    statusLight.name = 'statusLight'
    elevator.add(statusLight)

    return elevator
  }

  createPallet(type: PalletType = 'chuan'): THREE.Group {
    const pallet = new THREE.Group()
    pallet.name = 'pallet'

    const palletWidth = 1.0
    const palletDepth = 1.0
    const palletHeight = 0.14

    const topDeck = new THREE.Mesh(
      new THREE.BoxGeometry(palletWidth, 0.02, palletDepth),
      this.materials.woodMaterial
    )
    topDeck.position.y = palletHeight - 0.01
    topDeck.receiveShadow = true
    pallet.add(topDeck)

    const bottomDeck = new THREE.Mesh(
      new THREE.BoxGeometry(palletWidth, 0.02, palletDepth * 0.9),
      this.materials.woodMaterial
    )
    bottomDeck.position.y = 0.01
    pallet.add(bottomDeck)

    if (type === 'chuan') {
      const stringerGeometry = new THREE.BoxGeometry(0.1, palletHeight - 0.04, palletDepth)
      const stringerPositions = [-0.35, 0, 0.35]
      stringerPositions.forEach((x) => {
        const stringer = new THREE.Mesh(stringerGeometry, this.materials.woodMaterial)
        stringer.position.set(x, palletHeight / 2, 0)
        stringer.castShadow = true
        pallet.add(stringer)
      })
    } else if (type === 'nine') {
      const blockGeometry = new THREE.BoxGeometry(0.15, palletHeight - 0.04, 0.15)
      const blockPositions = [
        [-0.35, -0.35], [0, -0.35], [0.35, -0.35],
        [-0.35, 0], [0, 0], [0.35, 0],
        [-0.35, 0.35], [0, 0.35], [0.35, 0.35],
      ]
      blockPositions.forEach(([x, z]) => {
        const block = new THREE.Mesh(blockGeometry, this.materials.woodMaterial)
        block.position.set(x, palletHeight / 2, z)
        block.castShadow = true
        pallet.add(block)
      })
    } else {
      const blockGeometry = new THREE.BoxGeometry(0.12, palletHeight - 0.04, palletDepth * 0.8)
      const blockPositions = [-0.35, 0, 0.35]
      blockPositions.forEach((x) => {
        const block = new THREE.Mesh(blockGeometry, this.materials.woodMaterial)
        block.position.set(x, palletHeight / 2, 0)
        block.castShadow = true
        pallet.add(block)
      })
    }

    return pallet
  }

  createBox(size: BoxSize = 'medium', hasLabel: boolean = true, color?: number): THREE.Group {
    const box = new THREE.Group()
    box.name = 'box'

    const dimensions = {
      small: { w: 0.3, h: 0.25, d: 0.3 },
      medium: { w: 0.5, h: 0.4, d: 0.5 },
      large: { w: 0.7, h: 0.55, d: 0.7 },
    }

    const { w, h, d } = dimensions[size]
    const material = color
      ? this.materials.getBoxMaterialByColor(color)
      : this.materials.boxMaterial

    const body = new THREE.Mesh(
      new THREE.BoxGeometry(w, h, d),
      material
    )
    body.position.y = h / 2
    body.castShadow = true
    body.receiveShadow = true
    box.add(body)

    const tapeGeometry = new THREE.BoxGeometry(0.03, h + 0.002, d + 0.002)
    const tapeMaterial = new THREE.MeshStandardMaterial({
      color: 0xcccccc,
      transparent: true,
      opacity: 0.8,
    })

    const tape1 = new THREE.Mesh(tapeGeometry, tapeMaterial)
    tape1.position.set(-w / 4, h / 2, 0)
    box.add(tape1)

    const tape2 = tape1.clone()
    tape2.position.x = w / 4
    box.add(tape2)

    if (hasLabel) {
      const labelGeometry = new THREE.PlaneGeometry(w * 0.3, h * 0.25)
      const labelMaterial = new THREE.MeshBasicMaterial({
        color: 0xffffff,
        side: THREE.DoubleSide,
      })
      const label = new THREE.Mesh(labelGeometry, labelMaterial)
      label.position.set(0, h * 0.3, d / 2 + 0.002)
      box.add(label)
    }

    return box
  }

  createScanner(): THREE.Group {
    const scanner = new THREE.Group()
    scanner.name = 'scanner'

    const base = new THREE.Mesh(
      new THREE.CylinderGeometry(0.15, 0.18, 0.05, 32),
      this.materials.plasticMaterial
    )
    base.position.y = 0.025
    scanner.add(base)

    const pole = new THREE.Mesh(
      new THREE.CylinderGeometry(0.03, 0.03, 1.2, 16),
      this.materials.metalMaterial
    )
    pole.position.y = 0.65
    scanner.add(pole)

    const head = new THREE.Group()
    head.position.y = 1.25

    const headBody = new THREE.Mesh(
      new THREE.BoxGeometry(0.2, 0.15, 0.3),
      this.materials.plasticMaterial
    )
    head.add(headBody)

    const scanWindow = new THREE.Mesh(
      new THREE.PlaneGeometry(0.15, 0.1),
      this.materials.glassMaterial
    )
    scanWindow.position.z = 0.151
    scanWindow.rotation.x = -0.2
    head.add(scanWindow)

    const scanLight = new THREE.Mesh(
      new THREE.PlaneGeometry(0.12, 0.02),
      this.materials.getEmissiveMaterial(0xff0000)
    )
    scanLight.position.set(0, 0, 0.152)
    scanLight.rotation.x = -0.2
    scanLight.name = 'scanLight'
    head.add(scanLight)

    scanner.add(head)

    const display = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.1, 0.02),
      this.materials.getEmissiveMaterial(0x00ff00)
    )
    display.position.set(0, 0.8, 0.1)
    scanner.add(display)

    return scanner
  }

  createFence(length: number, hasGate: boolean = false): THREE.Group {
    const fence = new THREE.Group()
    fence.name = 'fence'

    const height = 1.2
    const postSpacing = 1.0
    const postCount = Math.ceil(length / postSpacing) + 1

    const postGeometry = new THREE.BoxGeometry(0.05, height, 0.05)
    for (let i = 0; i < postCount; i++) {
      const post = new THREE.Mesh(postGeometry, this.materials.metalMaterial)
      post.position.set(-length / 2 + i * postSpacing, height / 2, 0)
      post.castShadow = true
      fence.add(post)
    }

    const railGeometry = new THREE.BoxGeometry(length, 0.03, 0.02)
    const railTop = new THREE.Mesh(railGeometry, this.materials.metalMaterial)
    railTop.position.set(0, height - 0.05, 0)
    fence.add(railTop)

    const railMiddle = new THREE.Mesh(railGeometry, this.materials.metalMaterial)
    railMiddle.position.set(0, height / 2, 0)
    fence.add(railMiddle)

    const railBottom = new THREE.Mesh(railGeometry, this.materials.metalMaterial)
    railBottom.position.set(0, 0.05, 0)
    fence.add(railBottom)

    const meshWidth = length - 0.05
    const meshHeight = height - 0.1
    const meshGeometry = new THREE.PlaneGeometry(meshWidth, meshHeight)
    const meshMaterial = new THREE.MeshBasicMaterial({
      color: 0x888888,
      transparent: true,
      opacity: 0.4,
      side: THREE.DoubleSide,
    })
    const mesh = new THREE.Mesh(meshGeometry, meshMaterial)
    mesh.position.set(0, height / 2, 0)
    fence.add(mesh)

    if (hasGate) {
      const gateWidth = 1.0
      const gateX = -length / 2 + length / 2
      
      const gatePostGeometry = new THREE.BoxGeometry(0.05, height, 0.05)
      const gatePost1 = new THREE.Mesh(gatePostGeometry, this.materials.warningMaterial)
      gatePost1.position.set(gateX - gateWidth / 2, height / 2, 0)
      fence.add(gatePost1)

      const gatePost2 = new THREE.Mesh(gatePostGeometry, this.materials.warningMaterial)
      gatePost2.position.set(gateX + gateWidth / 2, height / 2, 0)
      fence.add(gatePost2)
    }

    return fence
  }

  createCabinet(): THREE.Group {
    const cabinet = new THREE.Group()
    cabinet.name = 'cabinet'

    const width = 0.8
    const height = 1.6
    const depth = 0.4

    const body = new THREE.Mesh(
      new THREE.BoxGeometry(width, height, depth),
      this.materials.cabinetMaterial
    )
    body.position.y = height / 2
    body.castShadow = true
    body.receiveShadow = true
    cabinet.add(body)

    const door = new THREE.Mesh(
      new THREE.BoxGeometry(width * 0.95, height * 0.95, 0.02),
      new THREE.MeshStandardMaterial({
        color: 0x34495e,
        metalness: 0.3,
        roughness: 0.7,
      })
    )
    door.position.set(0, height / 2, depth / 2 + 0.01)
    cabinet.add(door)

    const handle = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.02, 0.03),
      this.materials.metalMaterial
    )
    handle.position.set(width * 0.35, height / 2, depth / 2 + 0.04)
    cabinet.add(handle)

    const displayGeometry = new THREE.BoxGeometry(0.15, 0.08, 0.01)
    const displayMaterial = this.materials.getEmissiveMaterial(0x00ff00)
    
    const display1 = new THREE.Mesh(displayGeometry, displayMaterial)
    display1.position.set(-width * 0.2, height * 0.75, depth / 2 + 0.02)
    cabinet.add(display1)

    const buttonGeometry = new THREE.CylinderGeometry(0.02, 0.02, 0.01, 16)
    const buttonColors = [0xff0000, 0x00ff00, 0xffff00, 0x0088ff]
    buttonColors.forEach((color, i) => {
      const button = new THREE.Mesh(
        buttonGeometry,
        this.materials.getEmissiveMaterial(color)
      )
      button.rotation.x = Math.PI / 2
      button.position.set(
        -width * 0.25 + i * 0.06,
        height * 0.65,
        depth / 2 + 0.025
      )
      cabinet.add(button)
    })

    const ventGeometry = new THREE.BoxGeometry(0.3, 0.2, 0.01)
    const ventMaterial = new THREE.MeshStandardMaterial({
      color: 0x1a1a1a,
      metalness: 0.8,
      roughness: 0.3,
    })
    const vent = new THREE.Mesh(ventGeometry, ventMaterial)
    vent.position.set(0, height * 0.25, depth / 2 + 0.02)
    cabinet.add(vent)

    return cabinet
  }

  createGround(width: number, depth: number): THREE.Group {
    const ground = new THREE.Group()
    ground.name = 'ground'

    const floor = new THREE.Mesh(
      new THREE.PlaneGeometry(width, depth),
      this.materials.floorMaterial
    )
    floor.rotation.x = -Math.PI / 2
    floor.receiveShadow = true
    ground.add(floor)

    return ground
  }

  createMarkingLines(warehouseWidth: number, warehouseDepth: number): THREE.Group {
    const lines = new THREE.Group()
    lines.name = 'markingLines'

    const lineMaterial = new THREE.LineBasicMaterial({ color: 0xffff00, linewidth: 2 })

    const linePositions = [
      [-warehouseWidth / 2 + 1, -warehouseDepth / 2 + 1],
      [-warehouseWidth / 2 + 1, warehouseDepth / 2 - 1],
      [warehouseWidth / 2 - 1, warehouseDepth / 2 - 1],
      [warehouseWidth / 2 - 1, -warehouseDepth / 2 + 1],
      [-warehouseWidth / 2 + 1, -warehouseDepth / 2 + 1],
    ]

    const points = linePositions.map(
      ([x, z]) => new THREE.Vector3(x, 0.01, z)
    )
    const geometry = new THREE.BufferGeometry().setFromPoints(points)
    const line = new THREE.Line(geometry, lineMaterial)
    lines.add(line)

    const zoneMaterial = new THREE.LineDashedMaterial({
      color: 0x00ff00,
      dashSize: 0.3,
      gapSize: 0.2,
    })

    for (let i = 0; i < 4; i++) {
      const zoneX = -warehouseWidth / 2 + 3 + i * 10
      const zonePoints = [
        new THREE.Vector3(zoneX, 0.01, -warehouseDepth / 2 + 2),
        new THREE.Vector3(zoneX, 0.01, warehouseDepth / 2 - 2),
      ]
      const zoneGeometry = new THREE.BufferGeometry().setFromPoints(zonePoints)
      const zoneLine = new THREE.Line(zoneGeometry, zoneMaterial)
      zoneLine.computeLineDistances()
      lines.add(zoneLine)
    }

    const arrowShape = new THREE.Shape()
    arrowShape.moveTo(0, 0.1)
    arrowShape.lineTo(0.3, 0.1)
    arrowShape.lineTo(0.3, 0.2)
    arrowShape.lineTo(0.5, 0)
    arrowShape.lineTo(0.3, -0.2)
    arrowShape.lineTo(0.3, -0.1)
    arrowShape.lineTo(0, -0.1)
    arrowShape.lineTo(0, 0.1)

    const arrowGeometry = new THREE.ShapeGeometry(arrowShape)
    const arrowMaterial = new THREE.MeshBasicMaterial({ color: 0xffffff, side: THREE.DoubleSide })

    const arrowPositions = [
      { x: 0, z: -warehouseDepth / 2 + 3, rot: 0 },
      { x: 0, z: warehouseDepth / 2 - 3, rot: Math.PI },
    ]

    arrowPositions.forEach(({ x, z, rot }) => {
      const arrow = new THREE.Mesh(arrowGeometry, arrowMaterial)
      arrow.rotation.x = -Math.PI / 2
      arrow.rotation.z = rot
      arrow.position.set(x, 0.02, z)
      lines.add(arrow)
    })

    return lines
  }

  createLights(): THREE.Group {
    const lights = new THREE.Group()
    lights.name = 'lights'

    const fixtureGeometry = new THREE.BoxGeometry(1.5, 0.1, 0.3)
    const fixtureMaterial = new THREE.MeshStandardMaterial({
      color: 0x333333,
      metalness: 0.5,
      roughness: 0.5,
    })

    const diffuserGeometry = new THREE.BoxGeometry(1.4, 0.05, 0.25)
    const diffuserMaterial = new THREE.MeshStandardMaterial({
      color: 0xffffff,
      emissive: 0xffffee,
      emissiveIntensity: 0.5,
    })

    const lightPositions = [
      [-8, 6, -8], [-8, 6, 0], [-8, 6, 8],
      [0, 6, -8], [0, 6, 0], [0, 6, 8],
      [8, 6, -8], [8, 6, 0], [8, 6, 8],
    ]

    lightPositions.forEach(([x, y, z]) => {
      const fixture = new THREE.Mesh(fixtureGeometry, fixtureMaterial)
      fixture.position.set(x, y, z)
      lights.add(fixture)

      const diffuser = new THREE.Mesh(diffuserGeometry, diffuserMaterial)
      diffuser.position.set(x, y - 0.05, z)
      lights.add(diffuser)

      const pointLight = new THREE.PointLight(0xffffee, 0.8, 20, 2)
      pointLight.position.set(x, y - 0.5, z)
      pointLight.castShadow = true
      pointLight.shadow.mapSize.width = 512
      pointLight.shadow.mapSize.height = 512
      lights.add(pointLight)
    })

    return lights
  }

  createLocationMarker(
    id: string,
    width: number,
    depth: number,
    occupied: boolean
  ): THREE.Mesh {
    const geometry = new THREE.BoxGeometry(width, 0.02, depth)
    const material = occupied
      ? this.materials.locationOccupiedMaterial
      : this.materials.locationEmptyMaterial

    const marker = new THREE.Mesh(geometry, material)
    marker.name = `location_${id}`
    marker.userData = { locationId: id, type: 'location' }

    return marker
  }
}
