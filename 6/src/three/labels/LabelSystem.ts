import * as THREE from 'three'

export class LabelSystem {
  private camera: THREE.Camera
  private renderer: THREE.WebGLRenderer
  private labels: Map<string, HTMLElement> = new Map()
  private labelPositions: Map<string, THREE.Vector3> = new Map()
  private container: HTMLElement

  constructor(camera: THREE.Camera, renderer: THREE.WebGLRenderer) {
    this.camera = camera
    this.renderer = renderer
    
    this.container = document.createElement('div')
    this.container.style.position = 'absolute'
    this.container.style.top = '0'
    this.container.style.left = '0'
    this.container.style.width = '100%'
    this.container.style.height = '100%'
    this.container.style.pointerEvents = 'none'
    this.container.style.overflow = 'hidden'
    this.renderer.domElement.parentElement?.appendChild(this.container)
  }

  addLocationLabel(id: string, text: string, position: THREE.Vector3): void {
    const label = document.createElement('div')
    label.className = 'location-label'
    label.textContent = text
    label.style.cssText = `
      position: absolute;
      padding: 4px 8px;
      background: rgba(22, 93, 255, 0.9);
      color: white;
      font-size: 12px;
      font-family: 'Segoe UI', sans-serif;
      border-radius: 4px;
      white-space: nowrap;
      pointer-events: auto;
      transform: translate(-50%, -50%);
      z-index: 10;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    `
    label.dataset.id = id
    label.dataset.type = 'location'
    
    this.labels.set(`location_${id}`, label)
    this.labelPositions.set(`location_${id}`, position.clone())
    this.container.appendChild(label)
  }

  addDeviceLabel(id: string, name: string, status: string, position: THREE.Vector3): void {
    const statusColors: Record<string, string> = {
      running: '#00B42A',
      idle: '#86909C',
      error: '#F53F3F',
      maintenance: '#FF7D00',
    }

    const label = document.createElement('div')
    label.className = 'device-label'
    label.innerHTML = `
      <div style="display: flex; align-items: center; gap: 6px;">
        <span style="width: 8px; height: 8px; border-radius: 50%; background: ${statusColors[status] || '#86909C'};"></span>
        <span>${name}</span>
      </div>
    `
    label.style.cssText = `
      position: absolute;
      padding: 6px 10px;
      background: rgba(0, 0, 0, 0.8);
      color: white;
      font-size: 12px;
      font-family: 'Segoe UI', sans-serif;
      border-radius: 4px;
      border-left: 3px solid ${statusColors[status] || '#86909C'};
      white-space: nowrap;
      pointer-events: auto;
      transform: translate(-50%, -50%);
      z-index: 10;
    `
    label.dataset.id = id
    label.dataset.type = 'device'
    
    this.labels.set(`device_${id}`, label)
    this.labelPositions.set(`device_${id}`, position.clone())
    this.container.appendChild(label)
  }

  updateDeviceLabel(id: string, status: string): void {
    const label = this.labels.get(`device_${id}`)
    if (!label) return

    const statusColors: Record<string, string> = {
      running: '#00B42A',
      idle: '#86909C',
      error: '#F53F3F',
      maintenance: '#FF7D00',
    }

    const dot = label.querySelector('span')
    if (dot) {
      (dot as HTMLElement).style.background = statusColors[status] || '#86909C'
    }
    label.style.borderLeftColor = statusColors[status] || '#86909C'
  }

  update(): void {
    this.labels.forEach((label, key) => {
      const position = this.labelPositions.get(key)
      if (!position) return

      const vector = position.clone()
      vector.project(this.camera)

      const x = (vector.x * 0.5 + 0.5) * this.renderer.domElement.clientWidth
      const y = (-vector.y * 0.5 + 0.5) * this.renderer.domElement.clientHeight

      if (vector.z > 1) {
        label.style.display = 'none'
      } else {
        label.style.display = 'block'
        label.style.left = `${x}px`
        label.style.top = `${y}px`
      }
    })
  }

  updateSize(): void {
    
  }

  setLocationLabelVisibility(visible: boolean): void {
    this.labels.forEach((label, key) => {
      if (key.startsWith('location_')) {
        label.style.display = visible ? 'block' : 'none'
      }
    })
  }

  setDeviceLabelVisibility(visible: boolean): void {
    this.labels.forEach((label, key) => {
      if (key.startsWith('device_')) {
        label.style.display = visible ? 'block' : 'none'
      }
    })
  }

  removeLabel(key: string): void {
    const label = this.labels.get(key)
    if (label) {
      this.container.removeChild(label)
      this.labels.delete(key)
      this.labelPositions.delete(key)
    }
  }

  clear(): void {
    this.labels.forEach((label) => {
      this.container.removeChild(label)
    })
    this.labels.clear()
    this.labelPositions.clear()
  }

  dispose(): void {
    this.clear()
    this.container.remove()
  }
}
