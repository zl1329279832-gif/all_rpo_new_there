import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'
import { ModelFactory } from '../models/ModelFactory'
import { AnimationController } from '../animation/AnimationController'
import { PickerSystem } from '../interaction/PickerSystem'
import { LabelSystem } from '../labels/LabelSystem'
import type { LocationData, ZoneType, Vector3 } from '../../types'
import * as TWEEN from '@tweenjs/tween.js'

export class WarehouseScene {
  private container: HTMLElement
  private scene: THREE.Scene
  private camera: THREE.PerspectiveCamera
  private renderer: THREE.WebGLRenderer
  private controls: OrbitControls
  private modelFactory: ModelFactory
  private animationController: AnimationController
  private pickerSystem: PickerSystem
  private labelSystem: LabelSystem
  private locations: Map<string, LocationData> = new Map()
  private locationMarkers: Map<string, THREE.Mesh> = new Map()
  private animationFrameId: number = 0
  private isRunning: boolean = false
  private rackGroups: THREE.Group[] = []
  private stackers: THREE.Group[] = []
  private conveyors: THREE.Group[] = []
  private cargoGroups: Map<string, THREE.Group> = new Map()
  private onLocationClick?: (locationId: string) => void
  private onDeviceClick?: (deviceId: string) => void
  private needsRender: boolean = true
  private isDragging: boolean = false
  private lastHoverTime: number = 0
  private hoverThrottle: number = 50

  constructor(container: HTMLElement) {
    this.container = container
    this.scene = new THREE.Scene()
    this.camera = new THREE.PerspectiveCamera(60, container.clientWidth / container.clientHeight, 0.1, 1000)
    this.renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' })
    this.controls = new OrbitControls(this.camera, this.renderer.domElement)
    this.modelFactory = new ModelFactory()
    this.animationController = new AnimationController()
    this.pickerSystem = new PickerSystem(this.camera)
    this.labelSystem = new LabelSystem(this.camera, this.renderer)

    this.init()
  }

  private init(): void {
    this.scene.background = new THREE.Color(0x1a1a2e)
    this.scene.fog = new THREE.FogExp2(0x1a1a2e, 0.012)

    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5))
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFShadowMap
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping
    this.renderer.toneMappingExposure = 1.0

    this.container.appendChild(this.renderer.domElement)

    this.camera.position.set(25, 20, 25)
    this.camera.lookAt(0, 0, 0)

    this.controls.enableDamping = true
    this.controls.dampingFactor = 0.08
    this.controls.minDistance = 5
    this.controls.maxDistance = 60
    this.controls.maxPolarAngle = Math.PI / 2.2
    this.controls.minPolarAngle = Math.PI / 10

    this.setupLighting()
    this.setupEventListeners()
  }

  private setupLighting(): void {
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
    this.scene.add(ambientLight)

    const mainLight = new THREE.DirectionalLight(0xffffff, 0.7)
    mainLight.position.set(20, 30, 20)
    mainLight.castShadow = true
    mainLight.shadow.mapSize.width = 1024
    mainLight.shadow.mapSize.height = 1024
    mainLight.shadow.camera.near = 0.5
    mainLight.shadow.camera.far = 100
    mainLight.shadow.camera.left = -30
    mainLight.shadow.camera.right = 30
    mainLight.shadow.camera.top = 30
    mainLight.shadow.camera.bottom = -30
    mainLight.shadow.bias = -0.001
    this.scene.add(mainLight)

    const lights = this.modelFactory.createLights()
    this.scene.add(lights)
  }

  private setupEventListeners(): void {
    window.addEventListener('resize', this.onResize.bind(this))

    this.controls.addEventListener('start', () => {
      this.isDragging = true
    })

    this.controls.addEventListener('end', () => {
      this.isDragging = false
      this.needsRender = true
    })

    this.controls.addEventListener('change', () => {
      this.needsRender = true
    })

    this.renderer.domElement.addEventListener('click', (event) => {
      const intersects = this.pickerSystem.handleClick(event, this.renderer.domElement)
      if (intersects.length > 0) {
        const object = intersects[0].object
        if (object.userData.type === 'location') {
          this.onLocationClick?.(object.userData.locationId)
          this.highlightLocation(object.userData.locationId)
        } else if (object.userData.type === 'device') {
          this.onDeviceClick?.(object.userData.deviceId)
        }
      }
      this.needsRender = true
    })

    this.renderer.domElement.addEventListener('mousemove', (event) => {
      const now = Date.now()
      if (!this.isDragging && now - this.lastHoverTime > this.hoverThrottle) {
        this.lastHoverTime = now
        this.pickerSystem.handleHover(event, this.renderer.domElement)
      }
    })
  }

  private onResize(): void {
    const width = this.container.clientWidth
    const height = this.container.clientHeight

    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()

    this.renderer.setSize(width, height)
    this.labelSystem.updateSize()
  }

  buildWarehouse(locationsData: LocationData[]): void {
    locationsData.forEach((loc) => {
      this.locations.set(loc.id, loc)
    })

    const ground = this.modelFactory.createGround(50, 40)
    this.scene.add(ground)

    const markingLines = this.modelFactory.createMarkingLines(50, 40)
    this.scene.add(markingLines)

    this.buildRacks(locationsData)
    this.buildStackers()
    this.buildConveyors()
    this.buildFences()
    this.buildCabinets()
    this.buildScanners()
    this.buildElevator()
    this.buildLocationMarkers(locationsData)
  }

  private buildRacks(_locationsData: LocationData[]): void {
    const rackConfigs = [
      { x: -12, z: -8, rows: 2, bays: 8, levels: 6, rotation: 0 },
      { x: -12, z: 8, rows: 2, bays: 8, levels: 6, rotation: 0 },
      { x: 12, z: -8, rows: 2, bays: 8, levels: 6, rotation: Math.PI },
      { x: 12, z: 8, rows: 2, bays: 8, levels: 6, rotation: Math.PI },
    ]

    rackConfigs.forEach((config, index) => {
      const rackGroup = new THREE.Group()
      rackGroup.name = `rackGroup_${index}`

      for (let row = 0; row < config.rows; row++) {
        const rack = this.modelFactory.createRack(1, config.levels, config.bays)
        rack.position.x = row * 1.5
        rack.rotation.y = config.rotation
        rackGroup.add(rack)
      }

      rackGroup.position.set(config.x, 0, config.z)
      this.rackGroups.push(rackGroup)
      this.scene.add(rackGroup)
    })
  }

  private buildStackers(): void {
    const stackerPositions = [
      { x: -10, z: 0, id: 'STK-001' },
      { x: 10, z: 0, id: 'STK-002' },
    ]

    stackerPositions.forEach((pos) => {
      const stacker = this.modelFactory.createStacker(pos.id)
      stacker.position.set(pos.x, 0, pos.z)
      stacker.userData = { type: 'device', deviceId: pos.id, deviceType: 'stacker' }
      this.stackerPositions.push({ id: pos.id, x: pos.x, minZ: -10, maxZ: 10 })
      this.stackerMap.set(pos.id, stacker)
      this.stackerCarriageMap.set(pos.id, stacker.getObjectByName('carriage') as THREE.Group)
      this.stackerForkMap.set(pos.id, {
        left: stacker.getObjectByName('forkLeft') as THREE.Mesh,
        right: stacker.getObjectByName('forkRight') as THREE.Mesh,
      })
      this.stackerCurrentPosition.set(pos.id, { x: pos.x, z: pos.z, level: 1 })
      this.stackerCurrentTask.set(pos.id, null)
      this.stackerState.set(pos.id, 'idle')
      this.stackerTaskQueue.set(pos.id, [])
      this.stackers.push(stacker)
      this.scene.add(stacker)
    })
  }

  private buildConveyors(): void {
    const conveyorConfigs = [
      { x: 0, z: -15, length: 10, rotation: 0 },
      { x: 0, z: 15, length: 10, rotation: 0 },
      { x: -18, z: 0, length: 20, rotation: Math.PI / 2 },
      { x: 18, z: 0, length: 20, rotation: Math.PI / 2 },
    ]

    conveyorConfigs.forEach((config, index) => {
      const conveyor = this.modelFactory.createConveyor(config.length)
      conveyor.position.set(config.x, 0, config.z)
      conveyor.rotation.y = config.rotation
      conveyor.userData = { type: 'device', deviceId: `CONV-${String(index + 1).padStart(3, '0')}`, deviceType: 'conveyor' }
      this.conveyors.push(conveyor)
      this.scene.add(conveyor)
    })
  }

  private buildFences(): void {
    const fenceConfigs = [
      { x: 0, z: -18, length: 30, rotation: 0, hasGate: true },
      { x: 0, z: 18, length: 30, rotation: 0, hasGate: false },
      { x: -20, z: 0, length: 34, rotation: Math.PI / 2, hasGate: false },
      { x: 20, z: 0, length: 34, rotation: Math.PI / 2, hasGate: false },
    ]

    fenceConfigs.forEach((config) => {
      const fence = this.modelFactory.createFence(config.length, config.hasGate)
      fence.position.set(config.x, 0, config.z)
      fence.rotation.y = config.rotation
      this.scene.add(fence)
    })
  }

  private buildCabinets(): void {
    const cabinetPositions = [
      { x: -19, z: -10 },
      { x: -19, z: -5 },
      { x: 19, z: 10 },
      { x: 19, z: 5 },
    ]

    cabinetPositions.forEach((pos) => {
      const cabinet = this.modelFactory.createCabinet()
      cabinet.position.set(pos.x, 0, pos.z)
      this.scene.add(cabinet)
    })
  }

  private buildScanners(): void {
    const scannerPositions = [
      { x: -5, z: -15 },
      { x: 5, z: 15 },
    ]

    scannerPositions.forEach((pos, index) => {
      const scanner = this.modelFactory.createScanner()
      scanner.position.set(pos.x, 0, pos.z)
      scanner.rotation.y = pos.z < 0 ? Math.PI / 2 : -Math.PI / 2
      scanner.userData = { type: 'device', deviceId: `SCN-${String(index + 1).padStart(3, '0')}`, deviceType: 'scanner' }
      this.scene.add(scanner)
    })
  }

  private buildElevator(): void {
    const elevator = this.modelFactory.createElevator()
    elevator.position.set(0, 0, -15)
    elevator.userData = { type: 'device', deviceId: 'ELEV-001', deviceType: 'elevator' }
    this.scene.add(elevator)
  }

  private buildLocationMarkers(locationsData: LocationData[]): void {
    locationsData.forEach((location) => {
      if (!location.id.startsWith('R')) return

      const marker = this.modelFactory.createLocationMarker(
        location.id,
        0.9,
        0.9,
        location.occupied
      )
      const deckY = (location.level - 1) * 0.8 + 0.44
      marker.position.set(
        location.position.x,
        deckY + 0.02,
        location.position.z
      )
      this.locationMarkers.set(location.id, marker)
      this.pickerSystem.addInteractiveObject(marker)
      this.scene.add(marker)
    })
  }


  private stackerPositions: Array<{ id: string; x: number; minZ: number; maxZ: number }> = []
  private stackerMap: Map<string, THREE.Group> = new Map()
  private stackerCarriageMap: Map<string, THREE.Group> = new Map()
  private stackerForkMap: Map<string, { left: THREE.Mesh; right: THREE.Mesh }> = new Map()
  private stackerCurrentPosition: Map<string, { x: number; z: number; level: number }> = new Map()
  private stackerCurrentTask: Map<string, any | null> = new Map()
  private stackerState: Map<string, string> = new Map()
  private stackerTaskQueue: Map<string, any[]> = new Map()

  placeCargo(locationId: string, animate: boolean = false): Promise<void> {
    return new Promise((resolve) => {
      const location = this.locations.get(locationId)
      if (!location) {
        resolve()
        return
      }

      const cargoGroup = new THREE.Group()
      cargoGroup.name = `cargo_${locationId}`

      const pallet = this.modelFactory.createPallet('nine')
      cargoGroup.add(pallet)

      const boxColors = [0xD4A574, 0x4A90D9, 0xE25C5C, 0x6BCB77]
      const colorIndex = Math.floor(Math.random() * boxColors.length)
      const box = this.modelFactory.createBox('medium', true, boxColors[colorIndex])
      box.position.y = 0.11
      cargoGroup.add(box)

      const deckY = (location.level - 1) * 0.8 + 0.44 + 0.02
      cargoGroup.position.set(
        location.position.x,
        deckY,
        location.position.z
      )

      this.cargoGroups.set(locationId, cargoGroup)
      this.scene.add(cargoGroup)

      if (animate) {
        cargoGroup.scale.set(0, 0, 0)
        new TWEEN.Tween(cargoGroup.scale)
          .to({ x: 1, y: 1, z: 1 }, 500)
          .easing(TWEEN.Easing.Back.Out)
          .start()
          .onComplete(() => resolve())
      } else {
        resolve()
      }
    })
  }

  removeCargo(locationId: string, animate: boolean = false): Promise<void> {
    return new Promise((resolve) => {
      const cargo = this.cargoGroups.get(locationId)
      if (!cargo) {
        resolve()
        return
      }

      if (animate) {
        new TWEEN.Tween(cargo.scale)
          .to({ x: 0, y: 0, z: 0 }, 300)
          .easing(TWEEN.Easing.Back.In)
          .start()
          .onComplete(() => {
            this.scene.remove(cargo)
            this.cargoGroups.delete(locationId)
            resolve()
          })
      } else {
        this.scene.remove(cargo)
        this.cargoGroups.delete(locationId)
        resolve()
      }
    })
  }

  highlightLocation(locationId: string): void {
    this.locationMarkers.forEach((marker, id) => {
      if (id === locationId) {
        marker.material = this.modelFactory['materials'].locationSelectedMaterial
      } else {
        const location = this.locations.get(id)
        marker.material = location?.occupied
          ? this.modelFactory['materials'].locationOccupiedMaterial
          : this.modelFactory['materials'].locationEmptyMaterial
      }
    })
  }

  clearHighlight(): void {
    this.locationMarkers.forEach((marker, id) => {
      const location = this.locations.get(id)
      marker.material = location?.occupied
        ? this.modelFactory['materials'].locationOccupiedMaterial
        : this.modelFactory['materials'].locationEmptyMaterial
    })
  }

  moveToZone(zone: ZoneType): void {
    const zonePositions: Record<ZoneType, { pos: Vector3; target: Vector3 }> = {
      inbound: { pos: { x: 0, y: 15, z: -25 }, target: { x: 0, y: 2, z: -10 } },
      storage: { pos: { x: 25, y: 20, z: 25 }, target: { x: 0, y: 3, z: 0 } },
      outbound: { pos: { x: 0, y: 15, z: 25 }, target: { x: 0, y: 2, z: 10 } },
      picking: { pos: { x: -25, y: 15, z: 0 }, target: { x: -15, y: 2, z: 0 } },
    }

    const zonePos = zonePositions[zone]
    this.animateCamera(
      new THREE.Vector3(zonePos.pos.x, zonePos.pos.y, zonePos.pos.z),
      new THREE.Vector3(zonePos.target.x, zonePos.target.y, zonePos.target.z)
    )
  }

  animateCamera(newPosition: THREE.Vector3, newTarget: THREE.Vector3): void {
    const startPosition = this.camera.position.clone()
    const startTarget = this.controls.target.clone()

    new TWEEN.Tween({ t: 0 })
      .to({ t: 1 }, 800)
      .easing(TWEEN.Easing.Cubic.InOut)
      .onUpdate((obj) => {
        this.camera.position.lerpVectors(startPosition, newPosition, obj.t)
        this.controls.target.lerpVectors(startTarget, newTarget, obj.t)
        this.controls.update()
      })
      .start()
  }

  resetCamera(): void {
    this.animateCamera(
      new THREE.Vector3(25, 20, 25),
      new THREE.Vector3(0, 0, 0)
    )
  }

  playStackerAnimation(stackerId: string, fromLocation?: string, toLocation?: string): Promise<void> {
    return this.animationController.playStackerAnimation(
      stackerId,
      {
        stackerMap: this.stackerMap,
        stackerCarriageMap: this.stackerCarriageMap,
        stackerForkMap: this.stackerForkMap,
        stackerCurrentPosition: this.stackerCurrentPosition,
        locations: this.locations,
        cargoGroups: this.cargoGroups,
        placeCargo: this.placeCargo.bind(this),
        removeCargo: this.removeCargo.bind(this),
        scene: this.scene,
      },
      fromLocation,
      toLocation
    )
  }

  getStackerIds(): string[] {
    return this.stackerPositions.map(s => s.id)
  }

  setOnLocationClick(callback: (locationId: string) => void): void {
    this.onLocationClick = callback
  }

  setOnDeviceClick(callback: (deviceId: string) => void): void {
    this.onDeviceClick = callback
  }

  start(): void {
    if (this.isRunning) return
    this.isRunning = true
    this.animate()
  }

  private animate(): void {
    if (!this.isRunning) return

    this.animationFrameId = requestAnimationFrame(this.animate.bind(this))

    const hasActiveTweens = TWEEN.getAll().length > 0
    const hasAnimation = this.animationController.isAnimationPlaying()
    let shouldUpdateControls = this.controls.enableDamping

    if (hasActiveTweens) {
      TWEEN.update()
      this.needsRender = true
      shouldUpdateControls = true
    }

    if (hasAnimation) {
      this.animationController.update()
      this.updateConveyorAnimation()
      this.needsRender = true
      shouldUpdateControls = true
    }

    if (shouldUpdateControls) {
      const prevTarget = this.controls.target.clone()
      const prevPos = this.camera.position.clone()
      this.controls.update()
      if (!prevTarget.equals(this.controls.target) || !prevPos.equals(this.camera.position)) {
        this.needsRender = true
      }
    }

    if (this.needsRender) {
      this.labelSystem.update()
      this.renderer.render(this.scene, this.camera)
      this.needsRender = false
    }
  }

  private updateConveyorAnimation(): void {
    this.conveyors.forEach((conveyor) => {
      conveyor.traverse((child) => {
        if (child.name.startsWith('roller_')) {
          child.rotation.x += 0.02
        }
      })
    })
  }

  stop(): void {
    this.isRunning = false
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId)
    }
  }

  dispose(): void {
    this.stop()
    window.removeEventListener('resize', this.onResize.bind(this))
    this.container.removeChild(this.renderer.domElement)
    this.renderer.dispose()
    this.labelSystem.dispose()
  }
}
