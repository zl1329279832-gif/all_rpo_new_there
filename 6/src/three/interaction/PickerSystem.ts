import * as THREE from 'three'

export class PickerSystem {
  private camera: THREE.Camera
  private scene: THREE.Scene
  private raycaster: THREE.Raycaster
  private mouse: THREE.Vector2
  private hoveredObject: THREE.Object3D | null = null
  private originalMaterials: Map<THREE.Object3D, THREE.Material | THREE.Material[]> = new Map()
  private onHover?: (object: THREE.Object3D | null) => void
  private onClick?: (object: THREE.Object3D) => void

  constructor(camera: THREE.Camera, scene: THREE.Scene) {
    this.camera = camera
    this.scene = scene
    this.raycaster = new THREE.Raycaster()
    this.mouse = new THREE.Vector2()
  }

  handleClick(event: MouseEvent, domElement: HTMLElement): THREE.Intersection[] {
    this.updateMousePosition(event, domElement)
    this.raycaster.setFromCamera(this.mouse, this.camera)

    const intersects = this.raycaster.intersectObjects(this.scene.children, true)
    
    const locationIntersects = intersects.filter(
      (i) => i.object.userData.type === 'location' || i.object.userData.type === 'device'
    )

    if (locationIntersects.length > 0) {
      this.onClick?.(locationIntersects[0].object)
    }

    return locationIntersects
  }

  handleHover(event: MouseEvent, domElement: HTMLElement): THREE.Object3D | null {
    this.updateMousePosition(event, domElement)
    this.raycaster.setFromCamera(this.mouse, this.camera)

    const intersects = this.raycaster.intersectObjects(this.scene.children, true)
    
    const hoverableIntersects = intersects.filter(
      (i) => i.object.userData.type === 'location' || i.object.userData.type === 'device'
    )

    if (hoverableIntersects.length > 0) {
      const newHovered = hoverableIntersects[0].object
      
      if (this.hoveredObject !== newHovered) {
        this.restoreMaterial()
        this.hoveredObject = newHovered
        this.applyHoverEffect(newHovered)
        this.onHover?.(newHovered)
      }
    } else {
      if (this.hoveredObject) {
        this.restoreMaterial()
        this.hoveredObject = null
        this.onHover?.(null)
      }
    }

    return this.hoveredObject
  }

  private updateMousePosition(event: MouseEvent, domElement: HTMLElement): void {
    const rect = domElement.getBoundingClientRect()
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  }

  private applyHoverEffect(object: THREE.Object3D): void {
    const mesh = object as THREE.Mesh
    if (mesh.material) {
      this.originalMaterials.set(object, mesh.material)
      
      const hoverMaterial = new THREE.MeshBasicMaterial({
        color: 0xffff00,
        transparent: true,
        opacity: 0.5,
      })
      mesh.material = hoverMaterial
    }
  }

  private restoreMaterial(): void {
    if (this.hoveredObject) {
      const originalMaterial = this.originalMaterials.get(this.hoveredObject)
      if (originalMaterial) {
        const mesh = this.hoveredObject as THREE.Mesh
        mesh.material = originalMaterial
      }
      this.originalMaterials.delete(this.hoveredObject)
    }
  }

  setOnHover(callback: (object: THREE.Object3D | null) => void): void {
    this.onHover = callback
  }

  setOnClick(callback: (object: THREE.Object3D) => void): void {
    this.onClick = callback
  }

  getHoveredObject(): THREE.Object3D | null {
    return this.hoveredObject
  }

  dispose(): void {
    this.restoreMaterial()
    this.originalMaterials.clear()
  }
}
