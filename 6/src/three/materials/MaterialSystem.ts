import * as THREE from 'three'

export class MaterialSystem {
  private static instance: MaterialSystem
  
  rackMaterial!: THREE.MeshStandardMaterial
  rackBeamMaterial!: THREE.MeshStandardMaterial
  metalMaterial!: THREE.MeshStandardMaterial
  metalDarkMaterial!: THREE.MeshStandardMaterial
  plasticMaterial!: THREE.MeshStandardMaterial
  plasticYellowMaterial!: THREE.MeshStandardMaterial
  plasticGrayMaterial!: THREE.MeshStandardMaterial
  woodMaterial!: THREE.MeshStandardMaterial
  floorMaterial!: THREE.MeshStandardMaterial
  glassMaterial!: THREE.MeshStandardMaterial
  boxMaterial!: THREE.MeshStandardMaterial
  boxMaterialBlue!: THREE.MeshStandardMaterial
  boxMaterialRed!: THREE.MeshStandardMaterial
  boxMaterialGreen!: THREE.MeshStandardMaterial
  conveyorMaterial!: THREE.MeshStandardMaterial
  conveyorRollerMaterial!: THREE.MeshStandardMaterial
  fenceMaterial!: THREE.MeshStandardMaterial
  cabinetMaterial!: THREE.MeshStandardMaterial
  ledRed!: THREE.MeshStandardMaterial
  ledGreen!: THREE.MeshStandardMaterial
  ledYellow!: THREE.MeshStandardMaterial
  ledBlue!: THREE.MeshStandardMaterial
  warningMaterial!: THREE.MeshStandardMaterial
  scanLineMaterial!: THREE.LineBasicMaterial
  pathLineMaterial!: THREE.LineBasicMaterial
  locationEmptyMaterial!: THREE.MeshBasicMaterial
  locationOccupiedMaterial!: THREE.MeshBasicMaterial
  locationSelectedMaterial!: THREE.MeshBasicMaterial

  private constructor() {
    this.initMaterials()
  }

  static getInstance(): MaterialSystem {
    if (!MaterialSystem.instance) {
      MaterialSystem.instance = new MaterialSystem()
    }
    return MaterialSystem.instance
  }

  private initMaterials(): void {
    this.rackMaterial = new THREE.MeshStandardMaterial({
      color: 0x165DFF,
      metalness: 0.6,
      roughness: 0.4,
    })

    this.rackBeamMaterial = new THREE.MeshStandardMaterial({
      color: 0x0E42D2,
      metalness: 0.5,
      roughness: 0.5,
    })

    this.metalMaterial = new THREE.MeshStandardMaterial({
      color: 0x86909C,
      metalness: 0.8,
      roughness: 0.3,
    })

    this.metalDarkMaterial = new THREE.MeshStandardMaterial({
      color: 0x4E5969,
      metalness: 0.7,
      roughness: 0.4,
    })

    this.plasticMaterial = new THREE.MeshStandardMaterial({
      color: 0x333333,
      metalness: 0.1,
      roughness: 0.8,
    })

    this.plasticYellowMaterial = new THREE.MeshStandardMaterial({
      color: 0xFFB100,
      metalness: 0.2,
      roughness: 0.6,
    })

    this.plasticGrayMaterial = new THREE.MeshStandardMaterial({
      color: 0xC9CDD4,
      metalness: 0.1,
      roughness: 0.7,
    })

    this.woodMaterial = new THREE.MeshStandardMaterial({
      color: 0x8B5A2B,
      metalness: 0.0,
      roughness: 0.9,
    })

    this.floorMaterial = new THREE.MeshStandardMaterial({
      color: 0x2A2A2A,
      metalness: 0.1,
      roughness: 0.9,
    })

    this.glassMaterial = new THREE.MeshPhysicalMaterial({
      color: 0xffffff,
      metalness: 0.0,
      roughness: 0.1,
      transparent: true,
      opacity: 0.3,
    })

    this.boxMaterial = new THREE.MeshStandardMaterial({
      color: 0xD4A574,
      metalness: 0.0,
      roughness: 0.9,
    })

    this.boxMaterialBlue = new THREE.MeshStandardMaterial({
      color: 0x4A90D9,
      metalness: 0.0,
      roughness: 0.8,
    })

    this.boxMaterialRed = new THREE.MeshStandardMaterial({
      color: 0xE25C5C,
      metalness: 0.0,
      roughness: 0.8,
    })

    this.boxMaterialGreen = new THREE.MeshStandardMaterial({
      color: 0x6BCB77,
      metalness: 0.0,
      roughness: 0.8,
    })

    this.conveyorMaterial = new THREE.MeshStandardMaterial({
      color: 0x5A5A5A,
      metalness: 0.6,
      roughness: 0.4,
    })

    this.conveyorRollerMaterial = new THREE.MeshStandardMaterial({
      color: 0x3A3A3A,
      metalness: 0.7,
      roughness: 0.3,
    })

    this.fenceMaterial = new THREE.MeshStandardMaterial({
      color: 0x7B7B7B,
      metalness: 0.4,
      roughness: 0.6,
    })

    this.cabinetMaterial = new THREE.MeshStandardMaterial({
      color: 0x2C3E50,
      metalness: 0.3,
      roughness: 0.7,
    })

    this.ledRed = new THREE.MeshStandardMaterial({
      color: 0xF53F3F,
      emissive: 0xF53F3F,
      emissiveIntensity: 0.5,
    })

    this.ledGreen = new THREE.MeshStandardMaterial({
      color: 0x00B42A,
      emissive: 0x00B42A,
      emissiveIntensity: 0.5,
    })

    this.ledYellow = new THREE.MeshStandardMaterial({
      color: 0xFF7D00,
      emissive: 0xFF7D00,
      emissiveIntensity: 0.5,
    })

    this.ledBlue = new THREE.MeshStandardMaterial({
      color: 0x165DFF,
      emissive: 0x165DFF,
      emissiveIntensity: 0.5,
    })

    this.warningMaterial = new THREE.MeshStandardMaterial({
      color: 0xF7BA21,
      emissive: 0xF7BA21,
      emissiveIntensity: 0.3,
    })

    this.scanLineMaterial = new THREE.LineBasicMaterial({
      color: 0x00FF00,
      linewidth: 2,
    })

    this.pathLineMaterial = new THREE.LineBasicMaterial({
      color: 0x165DFF,
      linewidth: 3,
    })

    this.locationEmptyMaterial = new THREE.MeshBasicMaterial({
      color: 0x00B42A,
      transparent: true,
      opacity: 0.3,
    })

    this.locationOccupiedMaterial = new THREE.MeshBasicMaterial({
      color: 0xFF7D00,
      transparent: true,
      opacity: 0.3,
    })

    this.locationSelectedMaterial = new THREE.MeshBasicMaterial({
      color: 0x165DFF,
      transparent: true,
      opacity: 0.6,
    })
  }

  getBoxMaterialByColor(color: number): THREE.MeshStandardMaterial {
    return new THREE.MeshStandardMaterial({
      color,
      metalness: 0.0,
      roughness: 0.85,
    })
  }

  getEmissiveMaterial(color: number): THREE.MeshStandardMaterial {
    return new THREE.MeshStandardMaterial({
      color,
      emissive: color,
      emissiveIntensity: 0.5,
    })
  }

  dispose(): void {
    Object.values(this).forEach((material) => {
      if (material instanceof THREE.Material) {
        material.dispose()
      }
    })
  }
}
