import * as THREE from 'three'
import { MaterialSystem } from '../materials/MaterialSystem'
import type { PalletType, BoxSize } from '../../types'

export class ModelFactory {
  private materials: MaterialSystem
  private readonly RACK_CONFIG = {
    bayWidth: 1.2,
    bayDepth: 1.2,
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

    const columnGeo = new THREE.BoxGeometry(columnSize, totalHeight, columnSize)
    const beamGeo = new THREE.BoxGeometry(bayWidth, beamSize, beamSize)
    const deckGeo = new THREE.BoxGeometry(bayWidth - 0.1, deckThickness, bayDepth - 0.1)
    const holeGeo = new THREE.BoxGeometry(0.025, 0.03, columnSize + 0.006)
    const bracketGeo = new THREE.BoxGeometry(0.03, 0.03, 0.03)
    const endCapGeo = new THREE.BoxGeometry(0.015, beamSize + 0.01, beamSize + 0.01)

    for (let bay = 0; bay <= bays; bay++) {
      const x = bay * bayWidth - totalWidth / 2 + columnSize / 2

      const column = new THREE.Mesh(columnGeo, this.materials.rackMaterial)
      column.position.set(x, totalHeight / 2, 0)
      column.castShadow = false
      rack.add(column)

      for (let level = 1; level <= levels; level++) {
        const holeY = level * levelHeight - levelHeight / 2

        const holeFront = new THREE.Mesh(holeGeo, this.materials.metalDarkMaterial)
        holeFront.position.set(x, holeY, bayDepth / 2 + columnSize / 2 - 0.003)
        holeFront.castShadow = false
        rack.add(holeFront)

        const holeBack = new THREE.Mesh(holeGeo, this.materials.metalDarkMaterial)
        holeBack.position.set(x, holeY, -bayDepth / 2 - columnSize / 2 + 0.003)
        holeBack.castShadow = false
        rack.add(holeBack)
      }
    }

    for (let level = 1; level <= levels; level++) {
      const beamY = level * levelHeight - levelHeight / 2

      for (let bay = 0; bay < bays; bay++) {
        const beamX = bay * bayWidth - totalWidth / 2 + bayWidth / 2

        const zPositions = [bayDepth / 2 - beamSize / 2, -bayDepth / 2 + beamSize / 2]
        zPositions.forEach((z) => {
          const beam = new THREE.Mesh(beamGeo, this.materials.rackBeamMaterial)
          beam.position.set(beamX, beamY, z)
          beam.castShadow = false
          rack.add(beam)

          const endCapL = new THREE.Mesh(endCapGeo, this.materials.metalMaterial)
          endCapL.position.set(beamX - bayWidth / 2, beamY, z)
          endCapL.castShadow = false
          rack.add(endCapL)

          const endCapR = new THREE.Mesh(endCapGeo, this.materials.metalMaterial)
          endCapR.position.set(beamX + bayWidth / 2, beamY, z)
          endCapR.castShadow = false
          rack.add(endCapR)

          const leftColX = beamX - bayWidth / 2 + columnSize / 2
          const rightColX = beamX + bayWidth / 2 - columnSize / 2
          const signZ = z > 0 ? -1 : 1

          const bracketLV = new THREE.Mesh(bracketGeo, this.materials.metalMaterial)
          bracketLV.position.set(leftColX, beamY + beamSize / 2 + 0.015, z + signZ * 0.01)
          bracketLV.castShadow = false
          rack.add(bracketLV)

          const bracketLH = new THREE.Mesh(bracketGeo, this.materials.metalMaterial)
          bracketLH.position.set(leftColX, beamY + beamSize / 2 - 0.015, z + signZ * 0.03)
          bracketLH.castShadow = false
          rack.add(bracketLH)

          const bracketRV = new THREE.Mesh(bracketGeo, this.materials.metalMaterial)
          bracketRV.position.set(rightColX, beamY + beamSize / 2 + 0.015, z + signZ * 0.01)
          bracketRV.castShadow = false
          rack.add(bracketRV)

          const bracketRH = new THREE.Mesh(bracketGeo, this.materials.metalMaterial)
          bracketRH.position.set(rightColX, beamY + beamSize / 2 - 0.015, z + signZ * 0.03)
          bracketRH.castShadow = false
          rack.add(bracketRH)
        })

        const deck = new THREE.Mesh(deckGeo, this.materials.metalDarkMaterial)
        deck.position.set(beamX, beamY + beamSize / 2 + deckThickness / 2, 0)
        deck.castShadow = false
        rack.add(deck)
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
    chassis.castShadow = false
    stacker.add(chassis)

    const bumperGeo = new THREE.BoxGeometry(1.6, 0.1, 0.05)
    const bumperFront = new THREE.Mesh(bumperGeo, this.materials.rubberMaterial)
    bumperFront.position.set(0, 0.15, 0.425)
    bumperFront.castShadow = false
    stacker.add(bumperFront)

    const bumperBack = new THREE.Mesh(bumperGeo, this.materials.rubberMaterial)
    bumperBack.position.set(0, 0.15, -0.425)
    bumperBack.castShadow = false
    stacker.add(bumperBack)

    const wheelGeo = new THREE.CylinderGeometry(0.15, 0.15, 0.1, 16)
    const wheelPositions = [
      [-0.5, 0.15, 0.35],
      [0.5, 0.15, 0.35],
      [-0.5, 0.15, -0.35],
      [0.5, 0.15, -0.35],
    ]
    wheelPositions.forEach((pos) => {
      const wheel = new THREE.Mesh(wheelGeo, this.materials.conveyorRollerMaterial)
      wheel.rotation.z = Math.PI / 2
      wheel.position.set(pos[0], pos[1], pos[2])
      wheel.castShadow = false
      stacker.add(wheel)
    })

    const mastHeight = 8
    const mastGeo = new THREE.BoxGeometry(0.15, mastHeight, 0.15)
    const mast = new THREE.Mesh(mastGeo, this.materials.metalMaterial)
    mast.position.set(-0.5, mastHeight / 2 + 0.3, 0)
    mast.castShadow = false
    stacker.add(mast)

    const mast2 = new THREE.Mesh(mastGeo, this.materials.metalMaterial)
    mast2.position.set(0.5, mastHeight / 2 + 0.3, 0)
    mast2.castShadow = false
    stacker.add(mast2)

    const cabin = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 1.2, 0.7),
      this.materials.cabinetMaterial
    )
    cabin.position.set(0, 0.9, -0.3)
    cabin.castShadow = false
    stacker.add(cabin)

    const cabinWindowFront = new THREE.Mesh(
      new THREE.PlaneGeometry(0.6, 0.5),
      this.materials.glassMaterial
    )
    cabinWindowFront.position.set(0, 1.1, 0.051)
    cabinWindowFront.castShadow = false
    stacker.add(cabinWindowFront)

    const cabinWindowSide = new THREE.Mesh(
      new THREE.PlaneGeometry(0.5, 0.5),
      this.materials.glassMaterial
    )
    cabinWindowSide.position.set(0.401, 1.1, -0.3)
    cabinWindowSide.rotation.y = Math.PI / 2
    cabinWindowSide.castShadow = false
    stacker.add(cabinWindowSide)

    const cabinWindowSide2 = new THREE.Mesh(
      new THREE.PlaneGeometry(0.5, 0.5),
      this.materials.glassMaterial
    )
    cabinWindowSide2.position.set(-0.401, 1.1, -0.3)
    cabinWindowSide2.rotation.y = Math.PI / 2
    cabinWindowSide2.castShadow = false
    stacker.add(cabinWindowSide2)

    const carriage = new THREE.Group()
    carriage.name = 'carriage'
    carriage.position.y = 1.5

    const carriageFrame = new THREE.Mesh(
      new THREE.BoxGeometry(1.4, 0.15, 1.2),
      this.materials.metalMaterial
    )
    carriageFrame.castShadow = false
    carriage.add(carriageFrame)

    const forkBase = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 0.08, 0.3),
      this.materials.metalDarkMaterial
    )
    forkBase.position.z = 0.5
    forkBase.castShadow = false
    carriage.add(forkBase)

    const forkGuideMeshGeo = new THREE.BoxGeometry(0.08, 0.06, 0.3)
    const forkArmMeshGeo = new THREE.BoxGeometry(0.08, 0.04, 0.6)

    const forkGuideLeft = new THREE.Group()
    forkGuideLeft.position.set(-0.25, 0, 0.5)
    const forkGuideMeshL = new THREE.Mesh(forkGuideMeshGeo, this.materials.metalDarkMaterial)
    forkGuideMeshL.position.z = 0.15
    forkGuideMeshL.castShadow = false
    forkGuideLeft.add(forkGuideMeshL)

    const forkLeft = new THREE.Group()
    forkLeft.name = 'forkLeft'
    forkLeft.position.z = 0.3
    const forkMeshL = new THREE.Mesh(forkArmMeshGeo, this.materials.metalDarkMaterial)
    forkMeshL.position.z = 0.3
    forkMeshL.castShadow = false
    forkLeft.add(forkMeshL)
    forkGuideLeft.add(forkLeft)
    carriage.add(forkGuideLeft)

    const forkGuideRight = new THREE.Group()
    forkGuideRight.position.set(0.25, 0, 0.5)
    const forkGuideMeshR = new THREE.Mesh(forkGuideMeshGeo, this.materials.metalDarkMaterial)
    forkGuideMeshR.position.z = 0.15
    forkGuideMeshR.castShadow = false
    forkGuideRight.add(forkGuideMeshR)

    const forkRight = new THREE.Group()
    forkRight.name = 'forkRight'
    forkRight.position.z = 0.3
    const forkMeshR = new THREE.Mesh(forkArmMeshGeo, this.materials.metalDarkMaterial)
    forkMeshR.position.z = 0.3
    forkMeshR.castShadow = false
    forkRight.add(forkMeshR)
    forkGuideRight.add(forkRight)
    carriage.add(forkGuideRight)

    const controlBox = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.5, 0.3),
      this.materials.cabinetMaterial
    )
    controlBox.position.set(0, 0.25, -0.3)
    controlBox.castShadow = false
    carriage.add(controlBox)

    stacker.add(carriage)

    const statusLight = new THREE.Mesh(
      new THREE.SphereGeometry(0.08, 16, 16),
      this.materials.ledGreen
    )
    statusLight.position.set(0, mastHeight + 0.3, 0)
    statusLight.name = 'statusLight'
    statusLight.castShadow = false
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
    frame.castShadow = false
    conveyor.add(frame)

    const beltGeo = new THREE.BoxGeometry(length - 0.05, 0.008, 0.5)
    const beltMat = new THREE.MeshStandardMaterial({
      color: 0x1a1a1a,
      metalness: 0.1,
      roughness: 0.95,
    })
    const belt = new THREE.Mesh(beltGeo, beltMat)
    belt.position.y = 0.81
    belt.castShadow = false
    conveyor.add(belt)

    const legGeo = new THREE.BoxGeometry(0.08, 0.7, 0.08)
    const legCount = Math.ceil(length / 1.5) + 1
    for (let i = 0; i < legCount; i++) {
      const legX = -length / 2 + (i * length) / (legCount - 1)

      const leg1 = new THREE.Mesh(legGeo, this.materials.metalMaterial)
      leg1.position.set(legX, 0.35, 0.2)
      leg1.castShadow = false
      conveyor.add(leg1)

      const leg2 = new THREE.Mesh(legGeo, this.materials.metalMaterial)
      leg2.position.set(legX, 0.35, -0.2)
      leg2.castShadow = false
      conveyor.add(leg2)
    }

    if (hasRollers) {
      const rollerGeo = new THREE.CylinderGeometry(0.06, 0.06, 0.55, 16)
      const bearingGeo = new THREE.CylinderGeometry(0.025, 0.025, 0.04, 8)
      const rollerCount = Math.ceil(length / 0.15)
      for (let i = 0; i < rollerCount; i++) {
        const rollerX = -length / 2 + (i + 0.5) * (length / rollerCount)

        const roller = new THREE.Mesh(rollerGeo, this.materials.conveyorRollerMaterial)
        roller.rotation.x = Math.PI / 2
        roller.position.set(rollerX, 0.82, 0)
        roller.name = `roller_${i}`
        roller.castShadow = false
        conveyor.add(roller)

        const bearing1 = new THREE.Mesh(bearingGeo, this.materials.metalMaterial)
        bearing1.rotation.x = Math.PI / 2
        bearing1.position.set(rollerX, 0.82, 0.275)
        bearing1.castShadow = false
        conveyor.add(bearing1)

        const bearing2 = new THREE.Mesh(bearingGeo, this.materials.metalMaterial)
        bearing2.rotation.x = Math.PI / 2
        bearing2.position.set(rollerX, 0.82, -0.275)
        bearing2.castShadow = false
        conveyor.add(bearing2)
      }
    }

    const sideRailGeo = new THREE.BoxGeometry(length, 0.08, 0.03)
    const sideRail1 = new THREE.Mesh(sideRailGeo, this.materials.metalMaterial)
    sideRail1.position.set(0, 0.9, 0.28)
    sideRail1.castShadow = false
    conveyor.add(sideRail1)

    const sideRail2 = new THREE.Mesh(sideRailGeo, this.materials.metalMaterial)
    sideRail2.position.set(0, 0.9, -0.28)
    sideRail2.castShadow = false
    conveyor.add(sideRail2)

    const motorBoxGeo = new THREE.BoxGeometry(0.2, 0.15, 0.3)
    const motorBox = new THREE.Mesh(motorBoxGeo, this.materials.metalDarkMaterial)
    motorBox.position.set(length / 2 - 0.15, 0.8, 0)
    motorBox.castShadow = false
    conveyor.add(motorBox)

    const motorLed = new THREE.Mesh(
      new THREE.SphereGeometry(0.015, 8, 8),
      this.materials.ledGreen
    )
    motorLed.position.set(length / 2 - 0.05, 0.88, 0.151)
    motorLed.castShadow = false
    conveyor.add(motorLed)

    return conveyor
  }

  createElevator(): THREE.Group {
    const elevator = new THREE.Group()
    elevator.name = 'elevator'

    const shaftWidth = 1.8
    const shaftDepth = 1.5
    const shaftHeight = 8

    const columnGeo = new THREE.BoxGeometry(0.15, shaftHeight, 0.15)
    const columnPositions = [
      [-shaftWidth / 2, shaftHeight / 2, -shaftDepth / 2],
      [shaftWidth / 2, shaftHeight / 2, -shaftDepth / 2],
      [-shaftWidth / 2, shaftHeight / 2, shaftDepth / 2],
      [shaftWidth / 2, shaftHeight / 2, shaftDepth / 2],
    ]
    columnPositions.forEach((pos) => {
      const column = new THREE.Mesh(columnGeo, this.materials.metalMaterial)
      column.position.set(pos[0], pos[1], pos[2])
      column.castShadow = false
      elevator.add(column)
    })

    const guideRailGeo = new THREE.BoxGeometry(0.03, shaftHeight, 0.03)
    const guideRailPositions = [
      [-shaftWidth / 2 + 0.12, shaftHeight / 2, -shaftDepth / 2 + 0.12],
      [shaftWidth / 2 - 0.12, shaftHeight / 2, -shaftDepth / 2 + 0.12],
      [-shaftWidth / 2 + 0.12, shaftHeight / 2, shaftDepth / 2 - 0.12],
      [shaftWidth / 2 - 0.12, shaftHeight / 2, shaftDepth / 2 - 0.12],
    ]
    guideRailPositions.forEach((pos) => {
      const guideRail = new THREE.Mesh(guideRailGeo, this.materials.metalDarkMaterial)
      guideRail.position.set(pos[0], pos[1], pos[2])
      guideRail.castShadow = false
      elevator.add(guideRail)
    })

    const carriage = new THREE.Group()
    carriage.name = 'carriage'
    carriage.position.y = 0.8

    const platform = new THREE.Mesh(
      new THREE.BoxGeometry(shaftWidth - 0.4, 0.1, shaftDepth - 0.4),
      this.materials.metalDarkMaterial
    )
    platform.castShadow = false
    carriage.add(platform)

    const fenceGeo = new THREE.BoxGeometry(0.03, 1.0, shaftDepth - 0.4)
    const fenceLeft = new THREE.Mesh(fenceGeo, this.materials.fenceMaterial)
    fenceLeft.position.set(-shaftWidth / 2 + 0.25, 0.55, 0)
    fenceLeft.castShadow = false
    carriage.add(fenceLeft)

    const fenceRight = new THREE.Mesh(fenceGeo, this.materials.fenceMaterial)
    fenceRight.position.set(shaftWidth / 2 - 0.25, 0.55, 0)
    fenceRight.castShadow = false
    carriage.add(fenceRight)

    const fenceBack = new THREE.Mesh(
      new THREE.BoxGeometry(shaftWidth - 0.4, 1.0, 0.03),
      this.materials.fenceMaterial
    )
    fenceBack.position.set(0, 0.55, -shaftDepth / 2 + 0.25)
    fenceBack.castShadow = false
    carriage.add(fenceBack)

    elevator.add(carriage)

    const chainGeo = new THREE.BoxGeometry(0.02, shaftHeight, 0.02)
    const chainPositions = [
      [-shaftWidth / 2 + 0.15, shaftHeight / 2, 0],
      [shaftWidth / 2 - 0.15, shaftHeight / 2, 0],
    ]
    chainPositions.forEach((pos) => {
      const chain = new THREE.Mesh(chainGeo, this.materials.chainMaterial)
      chain.position.set(pos[0], pos[1], pos[2])
      chain.castShadow = false
      elevator.add(chain)
    })

    const counterWeight = new THREE.Mesh(
      new THREE.BoxGeometry(0.3, 1.0, 0.2),
      this.materials.metalDarkMaterial
    )
    counterWeight.position.set(0, shaftHeight / 2, -shaftDepth / 2 + 0.15)
    counterWeight.castShadow = false
    elevator.add(counterWeight)

    const motorBox = new THREE.Mesh(
      new THREE.BoxGeometry(0.5, 0.4, 0.3),
      this.materials.cabinetMaterial
    )
    motorBox.position.set(0, shaftHeight + 0.2, 0)
    motorBox.castShadow = false
    elevator.add(motorBox)

    const maintPanelGeo = new THREE.BoxGeometry(0.3, 0.25, 0.02)
    const maintPanel = new THREE.Mesh(maintPanelGeo, this.materials.plasticGrayMaterial)
    maintPanel.position.set(shaftWidth / 2 + 0.01, shaftHeight * 0.75, 0)
    maintPanel.rotation.y = Math.PI / 2
    maintPanel.castShadow = false
    elevator.add(maintPanel)

    const screwGeo = new THREE.CylinderGeometry(0.008, 0.008, 0.005, 6)
    const screwOffsets = [
      [0.1, 0.08],
      [0.1, -0.08],
      [-0.1, 0.08],
      [-0.1, -0.08],
    ]
    screwOffsets.forEach(([dy, dz]) => {
      const screw = new THREE.Mesh(screwGeo, this.materials.metalMaterial)
      screw.rotation.z = Math.PI / 2
      screw.position.set(shaftWidth / 2 + 0.015, shaftHeight * 0.75 + dy, dz)
      screw.castShadow = false
      elevator.add(screw)
    })

    const statusLight = new THREE.Mesh(
      new THREE.SphereGeometry(0.1, 16, 16),
      this.materials.ledGreen
    )
    statusLight.position.set(shaftWidth / 2 - 0.3, shaftHeight + 0.4, 0)
    statusLight.name = 'statusLight'
    statusLight.castShadow = false
    elevator.add(statusLight)

    return elevator
  }

  createPallet(type: PalletType = 'chuan'): THREE.Group {
    const pallet = new THREE.Group()
    pallet.name = 'pallet'

    const palletWidth = 0.9
    const palletDepth = 0.9
    const palletHeight = 0.10

    const topDeck = new THREE.Mesh(
      new THREE.BoxGeometry(palletWidth, 0.02, palletDepth),
      this.materials.woodMaterial
    )
    topDeck.position.y = palletHeight - 0.01
    topDeck.castShadow = false
    pallet.add(topDeck)

    const slotGeo = new THREE.BoxGeometry(palletWidth * 0.7, 0.003, 0.008)
    const slotPositions = [-0.35, 0, 0.35]
    slotPositions.forEach((z) => {
      const slot = new THREE.Mesh(slotGeo, this.materials.metalDarkMaterial)
      slot.position.set(0, palletHeight - 0.001, z)
      slot.castShadow = false
      pallet.add(slot)
    })

    const bottomDeck = new THREE.Mesh(
      new THREE.BoxGeometry(palletWidth, 0.02, palletDepth * 0.9),
      this.materials.woodMaterial
    )
    bottomDeck.position.y = 0.01
    bottomDeck.castShadow = false
    pallet.add(bottomDeck)

    const antiSlipGeo = new THREE.BoxGeometry(palletWidth * 0.15, 0.003, 0.02)
    const antiSlipPositions = [-0.3, -0.1, 0.1, 0.3]
    antiSlipPositions.forEach((x) => {
      const antiSlip = new THREE.Mesh(antiSlipGeo, this.materials.metalDarkMaterial)
      antiSlip.position.set(x, 0.001, 0)
      antiSlip.castShadow = false
      pallet.add(antiSlip)
    })

    const entryMarkerGeo = new THREE.BoxGeometry(0.15, 0.005, palletDepth * 0.8)
    const entryMarkerMat = this.materials.plasticYellowMaterial

    const entryFront = new THREE.Mesh(entryMarkerGeo, entryMarkerMat)
    entryFront.position.set(palletWidth / 2 - 0.08, palletHeight / 2 + 0.01, 0)
    entryFront.castShadow = false
    pallet.add(entryFront)

    const entryBack = new THREE.Mesh(entryMarkerGeo, entryMarkerMat)
    entryBack.position.set(-palletWidth / 2 + 0.08, palletHeight / 2 + 0.01, 0)
    entryBack.castShadow = false
    pallet.add(entryBack)

    if (type === 'chuan') {
      const stringerGeo = new THREE.BoxGeometry(0.1, palletHeight - 0.04, palletDepth)
      const stringerPositions = [-0.35, 0, 0.35]
      stringerPositions.forEach((x) => {
        const stringer = new THREE.Mesh(stringerGeo, this.materials.woodMaterial)
        stringer.position.set(x, palletHeight / 2, 0)
        stringer.castShadow = false
        pallet.add(stringer)
      })
    } else if (type === 'nine') {
      const blockGeo = new THREE.BoxGeometry(0.12, palletHeight - 0.04, 0.12)
      const blockPositions = [
        [-0.35, -0.35], [0, -0.35], [0.35, -0.35],
        [-0.35, 0], [0, 0], [0.35, 0],
        [-0.35, 0.35], [0, 0.35], [0.35, 0.35],
      ]
      blockPositions.forEach(([x, z]) => {
        const block = new THREE.Mesh(blockGeo, this.materials.woodMaterial)
        block.position.set(x, palletHeight / 2, z)
        block.castShadow = false
        pallet.add(block)
      })
    } else {
      const blockGeo = new THREE.BoxGeometry(0.12, palletHeight - 0.04, palletDepth * 0.8)
      const blockPositions = [-0.35, 0, 0.35]
      blockPositions.forEach((x) => {
        const block = new THREE.Mesh(blockGeo, this.materials.woodMaterial)
        block.position.set(x, palletHeight / 2, 0)
        block.castShadow = false
        pallet.add(block)
      })
    }

    return pallet
  }

  createBox(size: BoxSize = 'medium', hasLabel: boolean = true, color?: number): THREE.Group {
    const box = new THREE.Group()
    box.name = 'box'

    const dimensions = {
      small: { w: 0.3, h: 0.2, d: 0.3 },
      medium: { w: 0.5, h: 0.3, d: 0.5 },
      large: { w: 0.6, h: 0.45, d: 0.6 },
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
    body.castShadow = false
    box.add(body)

    if (hasLabel) {
      const labelBgGeo = new THREE.PlaneGeometry(w * 0.3, h * 0.25)
      const labelBgMat = new THREE.MeshBasicMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.85,
        side: THREE.DoubleSide,
      })
      const labelBg = new THREE.Mesh(labelBgGeo, labelBgMat)
      labelBg.position.set(0, h / 2, d / 2 + 0.001)
      labelBg.castShadow = false
      box.add(labelBg)

      const barMat = new THREE.MeshBasicMaterial({
        color: 0x000000,
        side: THREE.DoubleSide,
      })
      const barWidths = [0.008, 0.012, 0.006, 0.01, 0.008, 0.014, 0.006, 0.01]
      let barX = -w * 0.1
      barWidths.forEach((bw) => {
        const bar = new THREE.Mesh(
          new THREE.PlaneGeometry(bw, h * 0.2),
          barMat
        )
        bar.position.set(barX, h / 2, d / 2 + 0.002)
        bar.castShadow = false
        box.add(bar)
        barX += bw + 0.006
      })
    }

    const tapeGeoX = new THREE.BoxGeometry(w * 0.9, 0.003, 0.025)
    const tapeX = new THREE.Mesh(tapeGeoX, this.materials.plasticBoxMaterial)
    tapeX.position.set(0, h + 0.002, 0)
    tapeX.castShadow = false
    box.add(tapeX)

    const tapeGeoZ = new THREE.BoxGeometry(0.025, 0.003, d * 0.9)
    const tapeZ = new THREE.Mesh(tapeGeoZ, this.materials.plasticBoxMaterial)
    tapeZ.position.set(0, h + 0.002, 0)
    tapeZ.castShadow = false
    box.add(tapeZ)

    const cornerGeo = new THREE.BoxGeometry(0.025, 0.025, 0.025)
    const cornerPositions = [
      [-w / 2, h, -d / 2],
      [w / 2, h, -d / 2],
      [-w / 2, h, d / 2],
      [w / 2, h, d / 2],
      [-w / 2, 0, -d / 2],
      [w / 2, 0, -d / 2],
      [-w / 2, 0, d / 2],
      [w / 2, 0, d / 2],
    ]
    cornerPositions.forEach(([cx, cy, cz]) => {
      const corner = new THREE.Mesh(cornerGeo, this.materials.plasticBoxMaterial)
      corner.position.set(cx, cy, cz)
      corner.castShadow = false
      box.add(corner)
    })

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
    base.castShadow = false
    scanner.add(base)

    const pole = new THREE.Mesh(
      new THREE.CylinderGeometry(0.03, 0.03, 1.2, 16),
      this.materials.metalMaterial
    )
    pole.position.y = 0.65
    pole.castShadow = false
    scanner.add(pole)

    const head = new THREE.Group()
    head.position.y = 1.25

    const headBody = new THREE.Mesh(
      new THREE.BoxGeometry(0.2, 0.15, 0.3),
      this.materials.plasticMaterial
    )
    headBody.castShadow = false
    head.add(headBody)

    const scanWindow = new THREE.Mesh(
      new THREE.PlaneGeometry(0.15, 0.1),
      this.materials.glassMaterial
    )
    scanWindow.position.z = 0.151
    scanWindow.rotation.x = -0.2
    scanWindow.castShadow = false
    head.add(scanWindow)

    const scanLight = new THREE.Mesh(
      new THREE.PlaneGeometry(0.12, 0.02),
      this.materials.ledRed
    )
    scanLight.position.set(0, 0, 0.152)
    scanLight.rotation.x = -0.2
    scanLight.name = 'scanLight'
    scanLight.castShadow = false
    head.add(scanLight)

    scanner.add(head)

    const display = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.1, 0.02),
      this.materials.getEmissiveMaterial(0x00ff00)
    )
    display.position.set(0, 0.8, 0.1)
    display.castShadow = false
    scanner.add(display)

    const buzzer = new THREE.Mesh(
      new THREE.CylinderGeometry(0.02, 0.02, 0.03, 16),
      this.materials.metalDarkMaterial
    )
    buzzer.position.set(0, 1.18, 0.1)
    buzzer.castShadow = false
    scanner.add(buzzer)

    const coneGeo = new THREE.ConeGeometry(0.2, 0.8, 16, 1, true)
    const coneMat = new THREE.MeshBasicMaterial({
      color: 0xff0000,
      transparent: true,
      opacity: 0.08,
      side: THREE.DoubleSide,
      depthWrite: false,
    })
    const scanRange = new THREE.Mesh(coneGeo, coneMat)
    scanRange.position.set(0, 0.85, 0.3)
    scanRange.rotation.x = Math.PI / 2 + 0.2
    scanRange.castShadow = false
    scanner.add(scanRange)

    return scanner
  }

  createFence(length: number, hasGate: boolean = false): THREE.Group {
    const fence = new THREE.Group()
    fence.name = 'fence'

    const height = 1.2
    const postSpacing = 1.0
    const postCount = Math.ceil(length / postSpacing) + 1

    const postGeo = new THREE.BoxGeometry(0.05, height, 0.05)
    for (let i = 0; i < postCount; i++) {
      const post = new THREE.Mesh(postGeo, this.materials.metalMaterial)
      post.position.set(-length / 2 + i * postSpacing, height / 2, 0)
      post.castShadow = false
      fence.add(post)
    }

    const railGeo = new THREE.BoxGeometry(length, 0.03, 0.02)
    const railTop = new THREE.Mesh(railGeo, this.materials.metalMaterial)
    railTop.position.set(0, height - 0.05, 0)
    railTop.castShadow = false
    fence.add(railTop)

    const railMiddle = new THREE.Mesh(railGeo, this.materials.metalMaterial)
    railMiddle.position.set(0, height / 2, 0)
    railMiddle.castShadow = false
    fence.add(railMiddle)

    const railBottom = new THREE.Mesh(railGeo, this.materials.metalMaterial)
    railBottom.position.set(0, 0.05, 0)
    railBottom.castShadow = false
    fence.add(railBottom)

    const meshWidth = length - 0.05
    const meshHeight = height - 0.1
    const meshGeo = new THREE.PlaneGeometry(meshWidth, meshHeight)
    const meshMat = new THREE.MeshBasicMaterial({
      color: 0x888888,
      transparent: true,
      opacity: 0.4,
      side: THREE.DoubleSide,
    })
    const mesh = new THREE.Mesh(meshGeo, meshMat)
    mesh.position.set(0, height / 2, 0)
    mesh.castShadow = false
    fence.add(mesh)

    if (hasGate) {
      const gateWidth = 1.0
      const gateX = -length / 2 + length / 2

      const gatePostGeo = new THREE.BoxGeometry(0.05, height, 0.05)
      const gatePost1 = new THREE.Mesh(gatePostGeo, this.materials.warningMaterial)
      gatePost1.position.set(gateX - gateWidth / 2, height / 2, 0)
      gatePost1.castShadow = false
      fence.add(gatePost1)

      const gatePost2 = new THREE.Mesh(gatePostGeo, this.materials.warningMaterial)
      gatePost2.position.set(gateX + gateWidth / 2, height / 2, 0)
      gatePost2.castShadow = false
      fence.add(gatePost2)

      const lockBody = new THREE.Mesh(
        new THREE.BoxGeometry(0.04, 0.06, 0.025),
        this.materials.metalMaterial
      )
      lockBody.position.set(gateX, height / 2, 0.04)
      lockBody.castShadow = false
      fence.add(lockBody)

      const lockLatch = new THREE.Mesh(
        new THREE.BoxGeometry(0.015, 0.03, 0.015),
        this.materials.metalDarkMaterial
      )
      lockLatch.position.set(gateX + 0.025, height / 2 + 0.01, 0.04)
      lockLatch.castShadow = false
      fence.add(lockLatch)

      const triangleShape = new THREE.Shape()
      triangleShape.moveTo(0, 0.06)
      triangleShape.lineTo(-0.05, -0.03)
      triangleShape.lineTo(0.05, -0.03)
      triangleShape.lineTo(0, 0.06)
      const triangleGeo = new THREE.ShapeGeometry(triangleShape)
      const warningSign = new THREE.Mesh(triangleGeo, this.materials.warningMaterial)
      warningSign.position.set(gateX, height - 0.15, 0.026)
      warningSign.castShadow = false
      fence.add(warningSign)

      const hingeGeo = new THREE.CylinderGeometry(0.015, 0.015, 0.04, 8)
      const hinge1 = new THREE.Mesh(hingeGeo, this.materials.metalMaterial)
      hinge1.position.set(gateX - gateWidth / 2, height * 0.7, 0.025)
      hinge1.castShadow = false
      fence.add(hinge1)

      const hinge2 = new THREE.Mesh(hingeGeo, this.materials.metalMaterial)
      hinge2.position.set(gateX - gateWidth / 2, height * 0.3, 0.025)
      hinge2.castShadow = false
      fence.add(hinge2)
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
    body.castShadow = false
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
    door.castShadow = false
    cabinet.add(door)

    const handle = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.02, 0.03),
      this.materials.metalMaterial
    )
    handle.position.set(width * 0.35, height / 2, depth / 2 + 0.04)
    handle.castShadow = false
    cabinet.add(handle)

    const displayGeo = new THREE.BoxGeometry(0.15, 0.08, 0.01)
    const displayMat = this.materials.getEmissiveMaterial(0x00ff00)

    const display1 = new THREE.Mesh(displayGeo, displayMat)
    display1.position.set(-width * 0.2, height * 0.75, depth / 2 + 0.02)
    display1.castShadow = false
    cabinet.add(display1)

    const buttonGeo = new THREE.CylinderGeometry(0.02, 0.02, 0.01, 16)
    const buttonColors = [0xff0000, 0x00ff00, 0xffff00, 0x0088ff]
    buttonColors.forEach((color, i) => {
      const button = new THREE.Mesh(
        buttonGeo,
        this.materials.getEmissiveMaterial(color)
      )
      button.rotation.x = Math.PI / 2
      button.position.set(
        -width * 0.25 + i * 0.06,
        height * 0.65,
        depth / 2 + 0.025
      )
      button.castShadow = false
      cabinet.add(button)
    })

    const ventGeo = new THREE.BoxGeometry(0.3, 0.2, 0.01)
    const ventMat = new THREE.MeshStandardMaterial({
      color: 0x1a1a1a,
      metalness: 0.8,
      roughness: 0.3,
    })
    const vent = new THREE.Mesh(ventGeo, ventMat)
    vent.position.set(0, height * 0.25, depth / 2 + 0.02)
    vent.castShadow = false
    cabinet.add(vent)

    const maintPanelGeo = new THREE.BoxGeometry(0.25, 0.3, 0.02)
    const maintPanel = new THREE.Mesh(maintPanelGeo, this.materials.plasticGrayMaterial)
    maintPanel.position.set(width / 2 + 0.01, height * 0.7, 0)
    maintPanel.rotation.y = Math.PI / 2
    maintPanel.castShadow = false
    cabinet.add(maintPanel)

    const screwGeo = new THREE.CylinderGeometry(0.006, 0.006, 0.004, 6)
    const panelScrewOffsets = [
      [0.1, 0.1],
      [0.1, -0.1],
      [-0.1, 0.1],
      [-0.1, -0.1],
    ]
    panelScrewOffsets.forEach(([sy, sz]) => {
      const screw = new THREE.Mesh(screwGeo, this.materials.metalMaterial)
      screw.rotation.z = Math.PI / 2
      screw.position.set(width / 2 + 0.015, height * 0.7 + sy, sz)
      screw.castShadow = false
      cabinet.add(screw)
    })

    const grilleGeo = new THREE.BoxGeometry(0.02, 0.18, 0.01)
    const grilleCount = 8
    const grilleStartX = -0.1
    const grilleStep = 0.2 / (grilleCount - 1)
    for (let i = 0; i < grilleCount; i++) {
      const grille = new THREE.Mesh(grilleGeo, this.materials.metalDarkMaterial)
      grille.position.set(
        width / 2 + 0.01,
        height * 0.35,
        grilleStartX + i * grilleStep
      )
      grille.rotation.y = Math.PI / 2
      grille.castShadow = false
      cabinet.add(grille)
    }

    const terminalGeo = new THREE.BoxGeometry(0.05, 0.04, 0.04)
    const terminalCount = 6
    for (let i = 0; i < terminalCount; i++) {
      const terminal = new THREE.Mesh(terminalGeo, this.materials.metalDarkMaterial)
      terminal.position.set(
        -width * 0.25 + i * 0.1,
        0.04,
        -depth / 2 + 0.1
      )
      terminal.castShadow = false
      cabinet.add(terminal)
    }

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
      fixture.castShadow = false
      lights.add(fixture)

      const diffuser = new THREE.Mesh(diffuserGeometry, diffuserMaterial)
      diffuser.position.set(x, y - 0.05, z)
      diffuser.castShadow = false
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
