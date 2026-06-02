import * as THREE from 'three'
import { RobotPart } from '../types'

export class InteractionSystem {
  private raycaster: THREE.Raycaster
  private mouse: THREE.Vector2
  private camera: THREE.Camera
  private domElement: HTMLElement
  private parts: Map<string, RobotPart> = new Map()
  private hoveredPart: string | null = null
  private onPartClick: ((part: RobotPart) => void) | null = null
  private onPartHover: ((part: RobotPart | null) => void) | null = null
  private originalMaterials: Map<THREE.Object3D, THREE.Material | THREE.Material[]> = new Map()

  constructor(camera: THREE.Camera, domElement: HTMLElement) {
    this.raycaster = new THREE.Raycaster()
    this.mouse = new THREE.Vector2()
    this.camera = camera
    this.domElement = domElement

    this.domElement.addEventListener('click', this.handleClick)
    this.domElement.addEventListener('mousemove', this.handleMouseMove)
  }

  setParts(parts: Map<string, RobotPart>) {
    this.parts = parts
  }

  setOnPartClick(callback: (part: RobotPart) => void) {
    this.onPartClick = callback
  }

  setOnPartHover(callback: (part: RobotPart | null) => void) {
    this.onPartHover = callback
  }

  private handleClick = (event: MouseEvent) => {
    const part = this.raycast(event)
    if (part && this.onPartClick) {
      this.onPartClick(part)
    }
  }

  private handleMouseMove = (event: MouseEvent) => {
    const part = this.raycast(event)
    
    if (part && part.id !== this.hoveredPart) {
      this.restoreMaterial(this.hoveredPart)
      this.highlightPart(part.id)
      this.hoveredPart = part.id
      if (this.onPartHover) this.onPartHover(part)
    } else if (!part && this.hoveredPart) {
      this.restoreMaterial(this.hoveredPart)
      this.hoveredPart = null
      if (this.onPartHover) this.onPartHover(null)
    }

    this.domElement.style.cursor = part ? 'pointer' : 'grab'
  }

  private raycast(event: MouseEvent): RobotPart | null {
    const rect = this.domElement.getBoundingClientRect()
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

    this.raycaster.setFromCamera(this.mouse, this.camera)

    const meshes: THREE.Object3D[] = []
    this.parts.forEach(part => {
      part.mesh.traverse(child => {
        if (child instanceof THREE.Mesh) {
          meshes.push(child)
        }
      })
    })

    const intersects = this.raycaster.intersectObjects(meshes)

    if (intersects.length > 0) {
      let obj: THREE.Object3D | null = intersects[0].object
      while (obj) {
        if (obj.userData.partId) {
          return this.parts.get(obj.userData.partId) || null
        }
        obj = obj.parent
      }
    }

    return null
  }

  private highlightPart(partId: string) {
    const part = this.parts.get(partId)
    if (!part) return

    part.mesh.traverse(child => {
      if (child instanceof THREE.Mesh) {
        if (!this.originalMaterials.has(child)) {
          this.originalMaterials.set(child, child.material)
        }
        
        const highlightMaterial = new THREE.MeshStandardMaterial({
          color: 0x00ffff,
          emissive: 0x00ffff,
          emissiveIntensity: 0.3,
          transparent: true,
          opacity: 0.8
        })
        
        child.material = highlightMaterial
      }
    })
  }

  private restoreMaterial(partId: string | null) {
    if (!partId) return
    const part = this.parts.get(partId)
    if (!part) return

    part.mesh.traverse(child => {
      if (child instanceof THREE.Mesh && this.originalMaterials.has(child)) {
        child.material = this.originalMaterials.get(child)!
        this.originalMaterials.delete(child)
      }
    })
  }

  dispose() {
    this.domElement.removeEventListener('click', this.handleClick)
    this.domElement.removeEventListener('mousemove', this.handleMouseMove)
    this.originalMaterials.forEach((mat) => {
      if (Array.isArray(mat)) {
        mat.forEach(m => m.dispose())
      } else {
        mat.dispose()
      }
    })
  }
}
