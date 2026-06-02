import * as THREE from 'three'

export class MaterialSystem {
  private materials: Map<string, THREE.Material> = new Map()
  private geometryCache: Map<string, THREE.BufferGeometry> = new Map()

  constructor() {
    this.initializeMaterials()
  }

  private initializeMaterials() {
    const plasticWhite = new THREE.MeshPhysicalMaterial({
      color: 0xf5f5f5,
      roughness: 0.55,
      metalness: 0.05,
      clearcoat: 0.2,
      clearcoatRoughness: 0.4,
      side: THREE.DoubleSide,
      envMapIntensity: 0.8
    })
    this.materials.set('plasticWhite', plasticWhite)

    const plasticDark = new THREE.MeshPhysicalMaterial({
      color: 0x1a1a1f,
      roughness: 0.65,
      metalness: 0.05,
      clearcoat: 0.15,
      clearcoatRoughness: 0.5,
      envMapIntensity: 0.7
    })
    this.materials.set('plasticDark', plasticDark)

    const plasticBlue = new THREE.MeshPhysicalMaterial({
      color: 0x1890ff,
      roughness: 0.45,
      metalness: 0.15,
      clearcoat: 0.3,
      clearcoatRoughness: 0.3,
      envMapIntensity: 0.9
    })
    this.materials.set('plasticBlue', plasticBlue)

    const plasticGray = new THREE.MeshPhysicalMaterial({
      color: 0x666666,
      roughness: 0.5,
      metalness: 0.1,
      clearcoat: 0.2,
      clearcoatRoughness: 0.4
    })
    this.materials.set('plasticGray', plasticGray)

    const metalSteel = new THREE.MeshStandardMaterial({
      color: 0x555555,
      roughness: 0.35,
      metalness: 0.85,
      envMapIntensity: 1.2
    })
    this.materials.set('metalSteel', metalSteel)

    const metalSteelBrushed = new THREE.MeshStandardMaterial({
      color: 0x666666,
      roughness: 0.45,
      metalness: 0.9,
      envMapIntensity: 1.0
    })
    this.materials.set('metalSteelBrushed', metalSteelBrushed)

    const metalAluminum = new THREE.MeshStandardMaterial({
      color: 0xd4d4d4,
      roughness: 0.25,
      metalness: 0.95,
      envMapIntensity: 1.3
    })
    this.materials.set('metalAluminum', metalAluminum)

    const metalBrass = new THREE.MeshStandardMaterial({
      color: 0xcd9b1d,
      roughness: 0.28,
      metalness: 0.9,
      envMapIntensity: 1.1
    })
    this.materials.set('metalBrass', metalBrass)

    const metalCopper = new THREE.MeshStandardMaterial({
      color: 0xb87333,
      roughness: 0.32,
      metalness: 0.92,
      envMapIntensity: 1.0
    })
    this.materials.set('metalCopper', metalCopper)

    const metalStainless = new THREE.MeshStandardMaterial({
      color: 0xc4c4c4,
      roughness: 0.18,
      metalness: 0.98,
      envMapIntensity: 1.4
    })
    this.materials.set('metalStainless', metalStainless)

    const rubberBlack = new THREE.MeshPhysicalMaterial({
      color: 0x0d0d0d,
      roughness: 0.95,
      metalness: 0.0,
      clearcoat: 0.05,
      clearcoatRoughness: 0.9
    })
    this.materials.set('rubberBlack', rubberBlack)

    const rubberGray = new THREE.MeshPhysicalMaterial({
      color: 0x2a2a2a,
      roughness: 0.9,
      metalness: 0.0
    })
    this.materials.set('rubberGray', rubberGray)

    const glassLens = new THREE.MeshPhysicalMaterial({
      color: 0xffffff,
      roughness: 0.02,
      metalness: 0.0,
      transmission: 0.95,
      transparent: true,
      opacity: 0.15,
      thickness: 0.3,
      ior: 1.5,
      envMapIntensity: 1.5
    })
    this.materials.set('glassLens', glassLens)

    const glassCover = new THREE.MeshPhysicalMaterial({
      color: 0x88ccff,
      roughness: 0.05,
      metalness: 0.0,
      transmission: 0.85,
      transparent: true,
      opacity: 0.25,
      thickness: 0.5,
      ior: 1.45
    })
    this.materials.set('glassCover', glassCover)

    const ledRed = new THREE.MeshStandardMaterial({
      color: 0xff2222,
      emissive: 0xff0000,
      emissiveIntensity: 0.8,
      roughness: 0.2,
      metalness: 0.4
    })
    this.materials.set('ledRed', ledRed)

    const ledGreen = new THREE.MeshStandardMaterial({
      color: 0x22ff22,
      emissive: 0x00ff00,
      emissiveIntensity: 0.8,
      roughness: 0.2,
      metalness: 0.4
    })
    this.materials.set('ledGreen', ledGreen)

    const ledBlue = new THREE.MeshStandardMaterial({
      color: 0x2266ff,
      emissive: 0x0044ff,
      emissiveIntensity: 1.0,
      roughness: 0.2,
      metalness: 0.4
    })
    this.materials.set('ledBlue', ledBlue)

    const ledOrange = new THREE.MeshStandardMaterial({
      color: 0xffaa00,
      emissive: 0xff6600,
      emissiveIntensity: 0.9,
      roughness: 0.2,
      metalness: 0.4
    })
    this.materials.set('ledOrange', ledOrange)

    const ledWhite = new THREE.MeshStandardMaterial({
      color: 0xffffff,
      emissive: 0xffffee,
      emissiveIntensity: 0.7,
      roughness: 0.2,
      metalness: 0.3
    })
    this.materials.set('ledWhite', ledWhite)

    const lidarMaterial = new THREE.MeshPhysicalMaterial({
      color: 0x222244,
      emissive: 0x0000ff,
      emissiveIntensity: 0.4,
      roughness: 0.15,
      metalness: 0.7,
      clearcoat: 0.5,
      clearcoatRoughness: 0.2
    })
    this.materials.set('lidar', lidarMaterial)

    const batteryGreen = new THREE.MeshPhysicalMaterial({
      color: 0x1a661a,
      roughness: 0.6,
      metalness: 0.2,
      clearcoat: 0.2,
      clearcoatRoughness: 0.5
    })
    this.materials.set('battery', batteryGreen)

    const sensorBlack = new THREE.MeshPhysicalMaterial({
      color: 0x0a0a0a,
      roughness: 0.08,
      metalness: 0.15,
      clearcoat: 1.0,
      clearcoatRoughness: 0.08
    })
    this.materials.set('sensor', sensorBlack)

    const floorMaterial = new THREE.MeshStandardMaterial({
      color: 0x1a1a24,
      roughness: 0.95,
      metalness: 0.05
    })
    this.materials.set('floor', floorMaterial)

    const safetyYellow = new THREE.MeshStandardMaterial({
      color: 0xffcc00,
      roughness: 0.6,
      metalness: 0.1
    })
    this.materials.set('safetyYellow', safetyYellow)

    const safetyRed = new THREE.MeshStandardMaterial({
      color: 0xcc2222,
      roughness: 0.6,
      metalness: 0.1
    })
    this.materials.set('safetyRed', safetyRed)

    const barcodeMat = new THREE.MeshStandardMaterial({
      color: 0xffffff,
      roughness: 0.8,
      metalness: 0.0
    })
    this.materials.set('barcode', barcodeMat)

    const warningMat = new THREE.MeshStandardMaterial({
      color: 0xff8800,
      emissive: 0xff4400,
      emissiveIntensity: 0.3,
      roughness: 0.5,
      metalness: 0.2
    })
    this.materials.set('warning', warningMat)

    const scanLineMat = new THREE.MeshBasicMaterial({
      color: 0x00ff00,
      transparent: true,
      opacity: 0.6,
      side: THREE.DoubleSide
    })
    this.materials.set('scanLine', scanLineMat)

    const plasticGreen = new THREE.MeshPhysicalMaterial({
      color: 0x22aa22,
      roughness: 0.55,
      metalness: 0.05,
      clearcoat: 0.2,
      clearcoatRoughness: 0.4
    })
    this.materials.set('plasticGreen', plasticGreen)

    const safetyOrange = new THREE.MeshStandardMaterial({
      color: 0xff6600,
      roughness: 0.6,
      metalness: 0.1
    })
    this.materials.set('safetyOrange', safetyOrange)

    const plasticWood = new THREE.MeshPhysicalMaterial({
      color: 0x8b4513,
      roughness: 0.85,
      metalness: 0.0,
      clearcoat: 0.1,
      clearcoatRoughness: 0.6
    })
    this.materials.set('plasticWood', plasticWood)
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

  getCachedGeometry(key: string, factory: () => THREE.BufferGeometry): THREE.BufferGeometry {
    if (!this.geometryCache.has(key)) {
      this.geometryCache.set(key, factory())
    }
    return this.geometryCache.get(key)!
  }

  createChamferBox(width: number, height: number, depth: number, radius: number): THREE.BufferGeometry {
    const key = `chamferBox_${width}_${height}_${depth}_${radius}`
    return this.getCachedGeometry(key, () => {
      const geometry = new THREE.BoxGeometry(width, height, depth)
      return geometry
    })
  }

  dispose() {
    this.materials.forEach(material => material.dispose())
    this.materials.clear()
    this.geometryCache.forEach(geometry => geometry.dispose())
    this.geometryCache.clear()
  }
}
