import * as THREE from 'three'

export class MaterialSystem {
  private materials: Map<string, THREE.Material> = new Map()

  constructor() {
    this.initializeMaterials()
  }

  private initializeMaterials() {
    const plasticWhite = new THREE.MeshStandardMaterial({
      color: 0xf0f0f0,
      roughness: 0.6,
      metalness: 0.1,
      side: THREE.DoubleSide
    })
    this.materials.set('plasticWhite', plasticWhite)

    const plasticDark = new THREE.MeshStandardMaterial({
      color: 0x2a2a2a,
      roughness: 0.7,
      metalness: 0.1
    })
    this.materials.set('plasticDark', plasticDark)

    const plasticBlue = new THREE.MeshStandardMaterial({
      color: 0x1890ff,
      roughness: 0.5,
      metalness: 0.2
    })
    this.materials.set('plasticBlue', plasticBlue)

    const metalSteel = new THREE.MeshStandardMaterial({
      color: 0x888888,
      roughness: 0.3,
      metalness: 0.8
    })
    this.materials.set('metalSteel', metalSteel)

    const metalAluminum = new THREE.MeshStandardMaterial({
      color: 0xcccccc,
      roughness: 0.2,
      metalness: 0.9
    })
    this.materials.set('metalAluminum', metalAluminum)

    const metalBrass = new THREE.MeshStandardMaterial({
      color: 0xcd9b1d,
      roughness: 0.3,
      metalness: 0.85
    })
    this.materials.set('metalBrass', metalBrass)

    const rubberBlack = new THREE.MeshStandardMaterial({
      color: 0x1a1a1a,
      roughness: 0.9,
      metalness: 0.0
    })
    this.materials.set('rubberBlack', rubberBlack)

    const glassClear = new THREE.MeshPhysicalMaterial({
      color: 0xffffff,
      roughness: 0.0,
      metalness: 0.0,
      transmission: 0.9,
      transparent: true,
      opacity: 0.3,
      thickness: 0.5
    })
    this.materials.set('glassClear', glassClear)

    const ledRed = new THREE.MeshStandardMaterial({
      color: 0xff3333,
      emissive: 0xff0000,
      emissiveIntensity: 0.5,
      roughness: 0.3,
      metalness: 0.5
    })
    this.materials.set('ledRed', ledRed)

    const ledGreen = new THREE.MeshStandardMaterial({
      color: 0x33ff33,
      emissive: 0x00ff00,
      emissiveIntensity: 0.5,
      roughness: 0.3,
      metalness: 0.5
    })
    this.materials.set('ledGreen', ledGreen)

    const ledBlue = new THREE.MeshStandardMaterial({
      color: 0x3333ff,
      emissive: 0x0088ff,
      emissiveIntensity: 0.8,
      roughness: 0.3,
      metalness: 0.5
    })
    this.materials.set('ledBlue', ledBlue)

    const ledOrange = new THREE.MeshStandardMaterial({
      color: 0xffaa00,
      emissive: 0xff8800,
      emissiveIntensity: 0.6,
      roughness: 0.3,
      metalness: 0.5
    })
    this.materials.set('ledOrange', ledOrange)

    const lidarMaterial = new THREE.MeshStandardMaterial({
      color: 0x444466,
      emissive: 0x2222ff,
      emissiveIntensity: 0.3,
      roughness: 0.2,
      metalness: 0.6
    })
    this.materials.set('lidar', lidarMaterial)

    const batteryMaterial = new THREE.MeshStandardMaterial({
      color: 0x228822,
      roughness: 0.5,
      metalness: 0.3
    })
    this.materials.set('battery', batteryMaterial)

    const sensorMaterial = new THREE.MeshPhysicalMaterial({
      color: 0x111111,
      roughness: 0.1,
      metalness: 0.1,
      clearcoat: 1.0,
      clearcoatRoughness: 0.1
    })
    this.materials.set('sensor', sensorMaterial)
  }

  get(name: string): THREE.Material {
    return this.materials.get(name) || this.materials.get('plasticWhite')!
  }

  setEmissiveIntensity(name: string, intensity: number) {
    const material = this.materials.get(name)
    if (material && 'emissiveIntensity' in material) {
      (material as THREE.MeshStandardMaterial).emissiveIntensity = intensity
    }
  }

  setOpacity(name: string, opacity: number) {
    const material = this.materials.get(name)
    if (material) {
      material.transparent = opacity < 1
      material.opacity = opacity
    }
  }

  clone(name: string): THREE.Material {
    return this.materials.get(name)?.clone() || this.materials.get('plasticWhite')!.clone()
  }

  dispose() {
    this.materials.forEach(material => material.dispose())
    this.materials.clear()
  }
}
