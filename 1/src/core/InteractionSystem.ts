import * as THREE from 'three'
import { RobotPart, FaultState } from '../types'

export type HighlightType = 'hover' | 'selected' | 'fault' | 'maintenance'

export class InteractionSystem {
  private raycaster: THREE.Raycaster
  private mouse: THREE.Vector2
  private camera: THREE.Camera
  private domElement: HTMLElement
  private parts: Map<string, RobotPart> = new Map()
  private hoveredPart: string | null = null
  private selectedPart: string | null = null
  private selectedParts: Set<string> = new Set()
  private onPartClick: ((part: RobotPart) => void) | null = null
  private onPartDoubleClick: ((part: RobotPart) => void) | null = null
  private onPartHover: ((part: RobotPart | null) => void) | null = null
  private onPartSelect: ((part: RobotPart | null) => void) | null = null
  private originalMaterials: Map<string, Map<THREE.Object3D, THREE.Material | THREE.Material[]>> = new Map()
  private highlightColors: Record<HighlightType, number> = {
    hover: 0x00ffff,
    selected: 0x00ff00,
    fault: 0xff0000,
    maintenance: 0xffaa00
  }
  private isMultiSelectMode: boolean = false
  private lastClickTime: number = 0
  private doubleClickThreshold: number = 300

  constructor(camera: THREE.Camera, domElement: HTMLElement) {
    this.raycaster = new THREE.Raycaster()
    this.mouse = new THREE.Vector2()
    this.camera = camera
    this.domElement = domElement

    this.domElement.addEventListener('click', this.handleClick)
    this.domElement.addEventListener('dblclick', this.handleDoubleClick)
    this.domElement.addEventListener('mousemove', this.handleMouseMove)
    this.domElement.addEventListener('keydown', this.handleKeyDown)
    this.domElement.addEventListener('keyup', this.handleKeyUp)
  }

  setParts(parts: Map<string, RobotPart>) {
    this.parts = parts
  }

  setOnPartClick(callback: (part: RobotPart) => void) {
    this.onPartClick = callback
  }

  setOnPartDoubleClick(callback: (part: RobotPart) => void) {
    this.onPartDoubleClick = callback
  }

  setOnPartHover(callback: (part: RobotPart | null) => void) {
    this.onPartHover = callback
  }

  setOnPartSelect(callback: (part: RobotPart | null) => void) {
    this.onPartSelect = callback
  }

  setMultiSelectMode(enabled: boolean) {
    this.isMultiSelectMode = enabled
  }

  private handleClick = (event: MouseEvent) => {
    const now = Date.now()
    if (now - this.lastClickTime < this.doubleClickThreshold) {
      this.lastClickTime = 0
      return
    }
    this.lastClickTime = now

    const part = this.raycast(event)
    if (part) {
      if (this.isMultiSelectMode) {
        this.togglePartSelection(part.id)
      } else {
        this.selectPart(part.id)
      }
      if (this.onPartClick) {
        this.onPartClick(part)
      }
    } else if (!this.isMultiSelectMode) {
      this.clearSelection()
    }
  }

  private handleDoubleClick = (event: MouseEvent) => {
    const part = this.raycast(event)
    if (part && this.onPartDoubleClick) {
      this.onPartDoubleClick(part)
    }
  }

  private handleMouseMove = (event: MouseEvent) => {
    const part = this.raycast(event)
    
    if (part && part.id !== this.hoveredPart) {
      this.unhighlightPart(this.hoveredPart, 'hover')
      this.highlightPart(part.id, 'hover')
      this.hoveredPart = part.id
      if (this.onPartHover) this.onPartHover(part)
    } else if (!part && this.hoveredPart) {
      this.unhighlightPart(this.hoveredPart, 'hover')
      this.hoveredPart = null
      if (this.onPartHover) this.onPartHover(null)
    }

    if (part) {
      this.domElement.style.cursor = 'pointer'
    } else if (this.isMultiSelectMode) {
      this.domElement.style.cursor = 'crosshair'
    } else {
      this.domElement.style.cursor = 'grab'
    }
  }

  private handleKeyDown = (event: KeyboardEvent) => {
    if (event.shiftKey) {
      this.setMultiSelectMode(true)
    }
  }

  private handleKeyUp = (event: KeyboardEvent) => {
    if (!event.shiftKey) {
      this.setMultiSelectMode(false)
    }
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

  private highlightPart(partId: string, type: HighlightType) {
    const part = this.parts.get(partId)
    if (!part) return

    if (!this.originalMaterials.has(partId)) {
      this.originalMaterials.set(partId, new Map())
    }
    const partMaterials = this.originalMaterials.get(partId)!

    part.mesh.traverse(child => {
      if (child instanceof THREE.Mesh) {
        if (!partMaterials.has(child)) {
          partMaterials.set(child, child.material)
        }
        
        const highlightMaterial = new THREE.MeshStandardMaterial({
          color: this.highlightColors[type],
          emissive: this.highlightColors[type],
          emissiveIntensity: 0.4,
          transparent: true,
          opacity: 0.85
        })
        
        child.material = highlightMaterial
      }
    })
  }

  private unhighlightPart(partId: string | null, type: HighlightType) {
    if (!partId) return
    const part = this.parts.get(partId)
    if (!part) return

    const isSelected = this.selectedParts.has(partId)
    const shouldRestore = !isSelected || (isSelected && type !== 'selected')

    if (!shouldRestore) return

    const partMaterials = this.originalMaterials.get(partId)
    if (!partMaterials) return

    part.mesh.traverse(child => {
      if (child instanceof THREE.Mesh && partMaterials.has(child)) {
        child.material = partMaterials.get(child)!
      }
    })

    if (!isSelected) {
      this.originalMaterials.delete(partId)
    }
  }

  private selectPart(partId: string) {
    if (this.selectedPart) {
      this.unhighlightPart(this.selectedPart, 'selected')
      this.selectedParts.delete(this.selectedPart)
    }

    this.selectedPart = partId
    this.selectedParts.clear()
    this.selectedParts.add(partId)
    this.highlightPart(partId, 'selected')

    if (this.onPartSelect) {
      this.onPartSelect(this.parts.get(partId) || null)
    }
  }

  private togglePartSelection(partId: string) {
    if (this.selectedParts.has(partId)) {
      this.selectedParts.delete(partId)
      this.unhighlightPart(partId, 'selected')
    } else {
      this.selectedParts.add(partId)
      this.highlightPart(partId, 'selected')
    }

    if (this.onPartSelect) {
      this.onPartSelect(this.parts.get(partId) || null)
    }
  }

  private clearSelection() {
    this.selectedParts.forEach(partId => {
      this.unhighlightPart(partId, 'selected')
    })
    this.selectedParts.clear()
    this.selectedPart = null

    if (this.onPartSelect) {
      this.onPartSelect(null)
    }
  }

  highlightFaultyParts(faults: FaultState[]) {
    faults.forEach(fault => {
      fault.affectedParts.forEach(partId => {
        this.highlightPart(partId, 'fault')
      })
    })
  }

  clearFaultHighlight() {
    this.parts.forEach((_, partId) => {
      if (!this.selectedParts.has(partId) && partId !== this.hoveredPart) {
        this.unhighlightPart(partId, 'fault')
      }
    })
  }

  getSelectedParts(): RobotPart[] {
    const parts: RobotPart[] = []
    this.selectedParts.forEach(partId => {
      const part = this.parts.get(partId)
      if (part) parts.push(part)
    })
    return parts
  }

  getSelectedPart(): RobotPart | null {
    return this.selectedPart ? this.parts.get(this.selectedPart) || null : null
  }

  dispose() {
    this.domElement.removeEventListener('click', this.handleClick)
    this.domElement.removeEventListener('dblclick', this.handleDoubleClick)
    this.domElement.removeEventListener('mousemove', this.handleMouseMove)
    this.domElement.removeEventListener('keydown', this.handleKeyDown)
    this.domElement.removeEventListener('keyup', this.handleKeyUp)
    
    this.originalMaterials.forEach((partMats) => {
      partMats.forEach((mat) => {
        if (Array.isArray(mat)) {
          mat.forEach(m => m.dispose())
        } else {
          mat.dispose()
        }
      })
    })
    this.originalMaterials.clear()
  }
}
