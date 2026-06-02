import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

export interface PerformanceStats {
  fps: number
  frameTime: number
  drawCalls: number
  triangles: number
  memory: {
    geometries: number
    textures: number
  }
}

export class SceneManager {
  public scene: THREE.Scene
  public camera: THREE.PerspectiveCamera
  public renderer: THREE.WebGLRenderer
  public controls: OrbitControls
  private container: HTMLElement
  private animationId: number | null = null
  private onAnimate: ((deltaTime: number) => void) | null = null
  private performanceStats: PerformanceStats
  private frameCount: number = 0
  private lastFpsUpdate: number = 0
  private clock: THREE.Clock
  private isHighQuality: boolean = true
  private shadowQuality: 'low' | 'medium' | 'high' = 'high'

  constructor(container: HTMLElement) {
    this.container = container
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x0a0a0f)
    this.scene.fog = new THREE.Fog(0x0a0a0f, 20, 60)

    this.camera = new THREE.PerspectiveCamera(
      60,
      container.clientWidth / container.clientHeight,
      0.1,
      1000
    )
    this.camera.position.set(8, 6, 8)

    this.renderer = new THREE.WebGLRenderer({ 
      antialias: true,
      powerPreference: 'high-performance',
      alpha: false,
      stencil: false,
      depth: true
    })
    this.renderer.setSize(container.clientWidth, container.clientHeight)
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping
    this.renderer.toneMappingExposure = 1.2
    this.renderer.outputColorSpace = THREE.SRGBColorSpace

    container.appendChild(this.renderer.domElement)

    this.controls = new OrbitControls(this.camera, this.renderer.domElement)
    this.controls.enableDamping = true
    this.controls.dampingFactor = 0.05
    this.controls.minDistance = 3
    this.controls.maxDistance = 35
    this.controls.maxPolarAngle = Math.PI / 2.1

    this.performanceStats = {
      fps: 0,
      frameTime: 0,
      drawCalls: 0,
      triangles: 0,
      memory: {
        geometries: 0,
        textures: 0
      }
    }

    this.clock = new THREE.Clock()

    this.setupLighting()
    this.setupGround()
    this.setupGrid()

    window.addEventListener('resize', this.handleResize)
  }

  private setupLighting() {
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.4)
    this.scene.add(ambientLight)

    const mainLight = new THREE.DirectionalLight(0xffffff, 1.0)
    mainLight.position.set(10, 15, 10)
    mainLight.castShadow = true
    mainLight.shadow.mapSize.width = 2048
    mainLight.shadow.mapSize.height = 2048
    mainLight.shadow.camera.near = 0.5
    mainLight.shadow.camera.far = 50
    mainLight.shadow.camera.left = -15
    mainLight.shadow.camera.right = 15
    mainLight.shadow.camera.top = 15
    mainLight.shadow.camera.bottom = -15
    this.scene.add(mainLight)

    const fillLight = new THREE.DirectionalLight(0x4488ff, 0.3)
    fillLight.position.set(-8, 5, -8)
    this.scene.add(fillLight)

    const rimLight = new THREE.DirectionalLight(0xff8844, 0.2)
    rimLight.position.set(0, 8, -10)
    this.scene.add(rimLight)

    const pointLight1 = new THREE.PointLight(0x0088ff, 0.5, 15)
    pointLight1.position.set(-5, 3, 5)
    this.scene.add(pointLight1)

    const pointLight2 = new THREE.PointLight(0xff8800, 0.3, 10)
    pointLight2.position.set(5, 2, -5)
    this.scene.add(pointLight2)
  }

  private setupGround() {
    const groundGeometry = new THREE.PlaneGeometry(50, 50)
    const groundMaterial = new THREE.MeshStandardMaterial({
      color: 0x1a1a24,
      roughness: 0.9,
      metalness: 0.1
    })
    const ground = new THREE.Mesh(groundGeometry, groundMaterial)
    ground.rotation.x = -Math.PI / 2
    ground.receiveShadow = true
    ground.position.y = -0.01
    this.scene.add(ground)
  }

  private setupGrid() {
    const gridHelper = new THREE.GridHelper(40, 40, 0x333344, 0x222233)
    gridHelper.position.y = 0
    this.scene.add(gridHelper)
  }

  public setAnimationCallback(callback: (deltaTime: number) => void) {
    this.onAnimate = callback
  }

  public start() {
    const animate = () => {
      this.animationId = requestAnimationFrame(animate)
      
      const deltaTime = Math.min(this.clock.getDelta(), 0.1)
      this.updatePerformanceStats(deltaTime)
      
      this.controls.update()
      
      if (this.onAnimate) {
        this.onAnimate(deltaTime)
      }
      
      this.renderer.render(this.scene, this.camera)
    }
    animate()
  }

  public stop() {
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId)
      this.animationId = null
    }
  }

  private updatePerformanceStats(deltaTime: number) {
    this.frameCount++
    const now = performance.now()

    if (now - this.lastFpsUpdate >= 1000) {
      this.performanceStats.fps = Math.round(this.frameCount * 1000 / (now - this.lastFpsUpdate))
      this.frameCount = 0
      this.lastFpsUpdate = now

      const info = this.renderer.info
      this.performanceStats.drawCalls = info.render.calls
      this.performanceStats.triangles = info.render.triangles
      this.performanceStats.memory.geometries = info.memory.geometries
      this.performanceStats.memory.textures = info.memory.textures
    }

    this.performanceStats.frameTime = deltaTime * 1000
  }

  public getPerformanceStats(): PerformanceStats {
    return { ...this.performanceStats }
  }

  public setQuality(quality: 'low' | 'medium' | 'high') {
    this.isHighQuality = quality === 'high'
    this.shadowQuality = quality

    switch (quality) {
      case 'low':
        this.renderer.shadowMap.type = THREE.BasicShadowMap
        this.renderer.setPixelRatio(1)
        break
      case 'medium':
        this.renderer.shadowMap.type = THREE.PCFShadowMap
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5))
        break
      case 'high':
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
        break
    }

    this.updateShadowQuality()
  }

  private updateShadowQuality() {
    const shadowSizes: Record<string, number> = {
      low: 512,
      medium: 1024,
      high: 2048
    }
    const size = shadowSizes[this.shadowQuality]

    this.scene.traverse((obj) => {
      if (obj instanceof THREE.DirectionalLight || obj instanceof THREE.SpotLight) {
        if (obj.castShadow && obj.shadow) {
          obj.shadow.mapSize.set(size, size)
        }
      }
    })
  }

  public setShadowsEnabled(enabled: boolean) {
    this.renderer.shadowMap.enabled = enabled
    this.scene.traverse((obj) => {
      if (obj instanceof THREE.Mesh) {
        obj.castShadow = enabled
        obj.receiveShadow = enabled
      }
    })
  }

  public optimizeScene() {
    const geometries: Map<string, THREE.BufferGeometry> = new Map()
    const materials: Map<string, THREE.Material> = new Map()

    this.scene.traverse((obj) => {
      if (obj instanceof THREE.Mesh) {
        if (obj.geometry) {
          const geoKey = obj.geometry.uuid
          if (!geometries.has(geoKey)) {
            geometries.set(geoKey, obj.geometry)
          }
        }

        if (obj.material) {
          const matKey = Array.isArray(obj.material) 
            ? obj.material.map(m => m.uuid).join(',')
            : obj.material.uuid
          if (!materials.has(matKey)) {
            materials.set(matKey, Array.isArray(obj.material) ? obj.material[0] : obj.material)
          }
        }
      }
    })

    console.log(`Scene optimized: ${geometries.size} unique geometries, ${materials.size} unique materials`)
  }

  private handleResize = () => {
    this.camera.aspect = this.container.clientWidth / this.container.clientHeight
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
  }

  public resetCamera() {
    this.camera.position.set(8, 6, 8)
    this.controls.target.set(0, 1, 0)
    this.controls.update()
  }

  public setCameraTarget(target: THREE.Vector3) {
    this.controls.target.copy(target)
    this.controls.update()
  }

  public dispose() {
    this.stop()
    window.removeEventListener('resize', this.handleResize)
    this.controls.dispose()

    this.scene.traverse((obj) => {
      if (obj instanceof THREE.Mesh) {
        obj.geometry.dispose()
        if (Array.isArray(obj.material)) {
          obj.material.forEach(m => m.dispose())
        } else {
          obj.material.dispose()
        }
      }
    })

    this.renderer.dispose()
    this.container.removeChild(this.renderer.domElement)
  }
}
