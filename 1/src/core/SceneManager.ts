import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

export class SceneManager {
  public scene: THREE.Scene
  public camera: THREE.PerspectiveCamera
  public renderer: THREE.WebGLRenderer
  public controls: OrbitControls
  private container: HTMLElement
  private animationId: number | null = null
  private onAnimate: (() => void) | null = null

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

    this.renderer = new THREE.WebGLRenderer({ antialias: true })
    this.renderer.setSize(container.clientWidth, container.clientHeight)
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping
    this.renderer.toneMappingExposure = 1.2

    container.appendChild(this.renderer.domElement)

    this.controls = new OrbitControls(this.camera, this.renderer.domElement)
    this.controls.enableDamping = true
    this.controls.dampingFactor = 0.05
    this.controls.minDistance = 3
    this.controls.maxDistance = 25
    this.controls.maxPolarAngle = Math.PI / 2.1

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

  public setAnimationCallback(callback: () => void) {
    this.onAnimate = callback
  }

  public start() {
    const animate = () => {
      this.animationId = requestAnimationFrame(animate)
      this.controls.update()
      
      if (this.onAnimate) {
        this.onAnimate()
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

  private handleResize = () => {
    this.camera.aspect = this.container.clientWidth / this.container.clientHeight
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
  }

  public dispose() {
    this.stop()
    window.removeEventListener('resize', this.handleResize)
    this.controls.dispose()
    this.renderer.dispose()
    this.container.removeChild(this.renderer.domElement)
  }
}
