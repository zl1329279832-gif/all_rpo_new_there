import * as THREE from 'three'

export class EnvironmentBuilder {
  private group: THREE.Group
  private materialSystem: any

  constructor(materialSystem: any) {
    this.group = new THREE.Group()
    this.group.name = 'environment'
    this.materialSystem = materialSystem
  }

  build(): THREE.Group {
    this.createChargingStation()
    this.createObstacles()
    this.createShelves()
    return this.group
  }

  private createChargingStation() {
    const stationGroup = new THREE.Group()
    stationGroup.name = 'chargingStation'
    stationGroup.position.set(-6, 0, 0)

    const base = new THREE.Mesh(
      new THREE.BoxGeometry(2.5, 0.1, 2),
      this.materialSystem.get('metalSteel')
    )
    base.receiveShadow = true
    stationGroup.add(base)

    const backPanel = new THREE.Mesh(
      new THREE.BoxGeometry(2, 1.5, 0.1),
      this.materialSystem.get('plasticDark')
    )
    backPanel.position.set(0, 0.75, -0.95)
    stationGroup.add(backPanel)

    const chargerPlate = new THREE.Mesh(
      new THREE.BoxGeometry(0.6, 0.3, 0.2),
      this.materialSystem.get('plasticDark')
    )
    chargerPlate.position.set(0, 0.35, 0)
    stationGroup.add(chargerPlate)

    const positiveContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.1, 0.05),
      this.materialSystem.get('metalBrass')
    )
    positiveContact.position.set(0.15, 0.35, 0.125)
    stationGroup.add(positiveContact)

    const negativeContact = new THREE.Mesh(
      new THREE.BoxGeometry(0.15, 0.1, 0.05),
      this.materialSystem.get('metalBrass')
    )
    negativeContact.position.set(-0.15, 0.35, 0.125)
    stationGroup.add(negativeContact)

    const statusLight = new THREE.Mesh(
      new THREE.SphereGeometry(0.08, 16, 16),
      this.materialSystem.get('ledGreen')
    )
    statusLight.position.set(0.8, 0.5, -0.8)
    stationGroup.add(statusLight)

    const stationLabel = new THREE.Mesh(
      new THREE.BoxGeometry(0.6, 0.2, 0.02),
      this.materialSystem.get('plasticWhite')
    )
    stationLabel.position.set(0, 1.2, -0.9)
    stationGroup.add(stationLabel)

    this.group.add(stationGroup)
  }

  private createObstacles() {
    const obstaclePositions = [
      { x: 4, z: 4, type: 'box' },
      { x: 5, z: -3, type: 'cylinder' },
      { x: -3, z: 5, type: 'box' }
    ]

    obstaclePositions.forEach((pos, index) => {
      const obstacleGroup = new THREE.Group()
      obstacleGroup.name = `obstacle_${index}`
      obstacleGroup.position.set(pos.x, 0, pos.z)

      let obstacle: THREE.Mesh

      if (pos.type === 'cylinder') {
        obstacle = new THREE.Mesh(
          new THREE.CylinderGeometry(0.5, 0.5, 1.2, 16),
          this.materialSystem.get('plasticDark')
        )
        obstacle.position.y = 0.6
      } else {
        obstacle = new THREE.Mesh(
          new THREE.BoxGeometry(1, 1, 1),
          this.materialSystem.get('plasticDark')
        )
        obstacle.position.y = 0.5
      }

      obstacle.castShadow = true
      obstacle.receiveShadow = true
      obstacleGroup.add(obstacle)

      const warningStrip = new THREE.Mesh(
        new THREE.CylinderGeometry(0.52, 0.52, 0.1, 16),
        this.materialSystem.get('ledOrange')
      )
      warningStrip.position.y = 1.25
      obstacleGroup.add(warningStrip)

      this.group.add(obstacleGroup)
    })
  }

  private createShelves() {
    const shelfPositions = [
      { x: 8, z: -3 },
      { x: 8, z: 3 }
    ]

    shelfPositions.forEach((pos, index) => {
      const shelfGroup = new THREE.Group()
      shelfGroup.name = `shelf_${index}`
      shelfGroup.position.set(pos.x, 0, pos.z)

      const verticalSupport1 = new THREE.Mesh(
        new THREE.BoxGeometry(0.1, 2.5, 0.1),
        this.materialSystem.get('metalSteel')
      )
      verticalSupport1.position.set(-0.9, 1.25, -0.45)
      shelfGroup.add(verticalSupport1)

      const verticalSupport2 = verticalSupport1.clone()
      verticalSupport2.position.set(0.9, 1.25, -0.45)
      shelfGroup.add(verticalSupport2)

      const verticalSupport3 = verticalSupport1.clone()
      verticalSupport3.position.set(-0.9, 1.25, 0.45)
      shelfGroup.add(verticalSupport3)

      const verticalSupport4 = verticalSupport1.clone()
      verticalSupport4.position.set(0.9, 1.25, 0.45)
      shelfGroup.add(verticalSupport4)

      for (let i = 0; i < 4; i++) {
        const shelf = new THREE.Mesh(
          new THREE.BoxGeometry(2, 0.05, 1),
          this.materialSystem.get('metalAluminum')
        )
        shelf.position.y = 0.3 + i * 0.7
        shelf.castShadow = true
        shelf.receiveShadow = true
        shelfGroup.add(shelf)
      }

      const boxesOnShelf = new THREE.Mesh(
        new THREE.BoxGeometry(0.5, 0.4, 0.5),
        this.materialSystem.get('plasticBlue')
      )
      boxesOnShelf.position.set(0, 0.55, 0)
      boxesOnShelf.castShadow = true
      shelfGroup.add(boxesOnShelf)

      const box2 = boxesOnShelf.clone()
      box2.position.set(0.5, 0.55, 0.2)
      box2.material = this.materialSystem.get('plasticWhite')
      shelfGroup.add(box2)

      this.group.add(shelfGroup)
    })
  }
}
