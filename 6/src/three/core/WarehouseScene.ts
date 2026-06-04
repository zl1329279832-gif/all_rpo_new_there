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
  private locationMarkerMap: Map<string, { mesh: THREE.InstancedMesh; index: number }> = new Map()
  private emptyInstances: THREE.InstancedMesh | null = null
  private occupiedInstances: THREE.InstancedMesh | null = null
  private locationMarkerGeo: THREE.BoxGeometry | null = null
  private animationFrameId: number = 0
  private isRunning: boolean = false
  private isPaused: boolean = false
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
  private loadedZones: Set<ZoneType> = new Set()
  private pathLines: THREE.Line[] = []
  private pathGroup: THREE.Group = new THREE.Group()
  private zoneGroups: Map<ZoneType, THREE.Group[]> = new Map()
  private locationGrid: Map<string, { x: number; z: number; level: number }> = new Map()

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

    const lightsGroup = new THREE.Group()
    lightsGroup.name = 'lights'

    const fixtureGeometry = new THREE.BoxGeometry(1.5, 0.1, 0.3)
    const fixtureMaterial = new THREE.MeshStandardMaterial({
      color: 0x333333,
      metalness: 0.5,
      roughness: 0.5,
    })
    const diffuserGeometry = new THREE.BoxGeometry(1.4, 0.05, 0.25)
    const diffuserMaterial = new THREE.MeshStandardMaterial({
      color: 0xffffff,
      emissive: 0xffffee,
      emissiveIntensity: 0.5,
    })

    const lightPositions = [
      [-8, 6, -8],
      [-8, 6, 8],
      [8, 6, -8],
      [8, 6, 8],
    ]

    lightPositions.forEach(([x, y, z]) => {
      const fixture = new THREE.Mesh(fixtureGeometry, fixtureMaterial)
      fixture.position.set(x, y, z)
      fixture.castShadow = false
      lightsGroup.add(fixture)

      const diffuser = new THREE.Mesh(diffuserGeometry, diffuserMaterial)
      diffuser.position.set(x, y - 0.05, z)
      diffuser.castShadow = false
      lightsGroup.add(diffuser)

      const pointLight = new THREE.PointLight(0xffffee, 0.8, 20, 2)
      pointLight.position.set(x, y - 0.5, z)
      pointLight.castShadow = false
      lightsGroup.add(pointLight)
    })

    this.scene.add(lightsGroup)
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
      let handled = false
      if (intersects.length > 0) {
        const object = intersects[0].object
        if (object.userData.type === 'device') {
          this.onDeviceClick?.(object.userData.deviceId)
          handled = true
        }
      }
      if (!handled) {
        const locationId = this.findLocationByClick(event)
        if (locationId) {
          this.onLocationClick?.(locationId)
          this.highlightLocation(locationId)
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

  private findLocationByClick(event: MouseEvent): string | null {
    const rect = this.renderer.domElement.getBoundingClientRect()
    const mouse = new THREE.Vector2(
      ((event.clientX - rect.left) / rect.width) * 2 - 1,
      -((event.clientY - rect.top) / rect.height) * 2 + 1
    )
    const raycaster = new THREE.Raycaster()
    raycaster.setFromCamera(mouse, this.camera)

    const groundPlane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0)
    const intersection = new THREE.Vector3()
    raycaster.ray.intersectPlane(groundPlane, intersection)
    if (!intersection) return null

    let closestId: string | null = null
    let closestDist = 0.6

    this.locationGrid.forEach((grid, id) => {
      const dx = intersection.x - grid.x
      const dz = intersection.z - grid.z
      const dist = Math.sqrt(dx * dx + dz * dz)
      if (dist < closestDist) {
        closestDist = dist
        closestId = id
      }
    })

    return closestId
  }

  buildWarehouse(locationsData: LocationData[]): void {
    locationsData.forEach((loc) => {
      this.locations.set(loc.id, loc)
    })

    const ground = this.modelFactory.createGround(50, 40)
    this.scene.add(ground)

    const markingLines = this.modelFactory.createMarkingLines(50, 40)
    this.scene.add(markingLines)

    this.scene.add(this.pathGroup)

    this.loadZone('storage')

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
      { x: -12, z: -8, rows: 2, bays: 8, levels: 6, rotation: 0, zone: 'storage' as ZoneType },
      { x: -12, z: 8, rows: 2, bays: 8, levels: 6, rotation: 0, zone: 'storage' as ZoneType },
      { x: 12, z: -8, rows: 2, bays: 8, levels: 6, rotation: Math.PI, zone: 'storage' as ZoneType },
      { x: 12, z: 8, rows: 2, bays: 8, levels: 6, rotation: Math.PI, zone: 'storage' as ZoneType },
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
      rackGroup.userData = { zone: config.zone }
      this.rackGroups.push(rackGroup)

      if (!this.zoneGroups.has(config.zone)) {
        this.zoneGroups.set(config.zone, [])
      }
      this.zoneGroups.get(config.zone)!.push(rackGroup)
    })
  }

  loadZone(zone: ZoneType): void {
    if (this.loadedZones.has(zone)) return
    this.loadedZones.add(zone)

    if (zone === 'storage') {
      this.buildRacks([])
    }

    const groups = this.zoneGroups.get(zone) || []
    groups.forEach((group) => {
      if (!group.parent) {
        this.scene.add(group)
      }
    })
    this.needsRender = true
  }

  unloadZone(zone: ZoneType): void {
    if (!this.loadedZones.has(zone)) return
    this.loadedZones.delete(zone)

    const groups = this.zoneGroups.get(zone) || []
    groups.forEach((group) => {
      if (group.parent) {
        this.scene.remove(group)
      }
    })
    this.needsRender = true
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
    const storageLocations = locationsData.filter(
      (loc) => loc.id.startsWith('R') && loc.zone === 'storage'
    )
    if (storageLocations.length === 0) return

    const emptyLocs = storageLocations.filter((loc) => !loc.occupied)
    const occupiedLocs = storageLocations.filter((loc) => loc.occupied)

    this.locationMarkerGeo = new THREE.BoxGeometry(0.9, 0.02, 0.9)
    const materials = this.modelFactory['materials']

    if (emptyLocs.length > 0) {
      this.emptyInstances = new THREE.InstancedMesh(
        this.locationMarkerGeo,
        materials.locationEmptyMaterial,
        emptyLocs.length
      )
      this.emptyInstances.name = 'emptyLocationMarkers'
      const matrix = new THREE.Matrix4()
      emptyLocs.forEach((loc, i) => {
        const deckY = (loc.level - 1) * 0.8 + 0.44 + 0.02
        matrix.setPosition(loc.position.x, deckY, loc.position.z)
        this.emptyInstances!.setMatrixAt(i, matrix)
        this.locationMarkerMap.set(loc.id, { mesh: this.emptyInstances!, index: i })
        this.locationGrid.set(loc.id, { x: loc.position.x, z: loc.position.z, level: loc.level })
      })
      this.emptyInstances.instanceMatrix.needsUpdate = true
      this.scene.add(this.emptyInstances)
    }

    if (occupiedLocs.length > 0) {
      this.occupiedInstances = new THREE.InstancedMesh(
        this.locationMarkerGeo,
        materials.locationOccupiedMaterial,
        occupiedLocs.length
      )
      this.occupiedInstances.name = 'occupiedLocationMarkers'
      const matrix = new THREE.Matrix4()
      occupiedLocs.forEach((loc, i) => {
        const deckY = (loc.level - 1) * 0.8 + 0.44 + 0.02
        matrix.setPosition(loc.position.x, deckY, loc.position.z)
        this.occupiedInstances!.setMatrixAt(i, matrix)
        this.locationMarkerMap.set(loc.id, { mesh: this.occupiedInstances!, index: i })
        this.locationGrid.set(loc.id, { x: loc.position.x, z: loc.position.z, level: loc.level })
      })
      this.occupiedInstances.instanceMatrix.needsUpdate = true
      this.scene.add(this.occupiedInstances)
    }
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
    this.locationMarkerMap.forEach((entry, id) => {
      const color = id === locationId
        ? new THREE.Color(0x165DFF)
        : this.locations.get(id)?.occupied
          ? new THREE.Color(0xFF7D00)
          : new THREE.Color(0x00B42A)
      entry.mesh.setColorAt(entry.index, color)
    })
    if (this.emptyInstances) this.emptyInstances.instanceColor!.needsUpdate = true
    if (this.occupiedInstances) this.occupiedInstances.instanceColor!.needsUpdate = true
  }

  clearHighlight(): void {
    this.locationMarkerMap.forEach((entry, id) => {
      const color = this.locations.get(id)?.occupied
        ? new THREE.Color(0xFF7D00)
        : new THREE.Color(0x00B42A)
      entry.mesh.setColorAt(entry.index, color)
    })
    if (this.emptyInstances) this.emptyInstances.instanceColor!.needsUpdate = true
    if (this.occupiedInstances) this.occupiedInstances.instanceColor!.needsUpdate = true
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

  setLabelsVisible(visible: boolean): void {
    this.labelSystem.setLocationLabelVisibility(visible)
    this.labelSystem.setDeviceLabelVisibility(visible)
  }

  pauseAnimation(): void {
    this.isPaused = true
  }

  resumeAnimation(): void {
    this.isPaused = false
    this.needsRender = true
  }

  showPath(locationIds: string[]): void {
    this.clearPath()
    if (locationIds.length < 2) return

    const materials = this.modelFactory['materials']
    const points: THREE.Vector3[] = []
    locationIds.forEach((id) => {
      const grid = this.locationGrid.get(id)
      if (grid) {
        const deckY = (grid.level - 1) * 0.8 + 0.44 + 0.1
        points.push(new THREE.Vector3(grid.x, deckY, grid.z))
      }
    })

    if (points.length < 2) return

    const geometry = new THREE.BufferGeometry().setFromPoints(points)
    const line = new THREE.Line(geometry, materials.pathLineMaterial)
    this.pathLines.push(line)
    this.pathGroup.add(line)
    this.needsRender = true
  }

  clearPath(): void {
    this.pathLines.forEach((line) => {
      line.geometry.dispose()
      this.pathGroup.remove(line)
    })
    this.pathLines = []
    this.needsRender = true
  }

  updateDeviceStatus(deviceId: string, status: string): void {
    const materials = this.modelFactory['materials']
    const statusMaterialMap: Record<string, THREE.MeshStandardMaterial> = {
      running: materials.ledGreen,
      idle: materials.ledYellow,
      error: materials.ledRed,
      maintenance: materials.ledBlue,
    }
    const targetMaterial = statusMaterialMap[status]
    if (!targetMaterial) return

    const stacker = this.stackerMap.get(deviceId)
    if (stacker) {
      const statusLight = stacker.getObjectByName('statusLight') as THREE.Mesh | undefined
      if (statusLight) {
        statusLight.material = targetMaterial
      }
    }

    this.scene.traverse((child) => {
      if (
        child instanceof THREE.Mesh &&
        child.userData.type === 'device' &&
        child.userData.deviceId === deviceId
      ) {
        const group = child.parent
        if (group) {
          const light = group.getObjectByName('statusLight') as THREE.Mesh | undefined
          if (light) {
            light.material = targetMaterial
          }
        }
      }
    })
  }

  start(): void {
    if (this.isRunning) return
    this.isRunning = true
    this.animate()
  }

  private animate(): void {
    if (!this.isRunning) return

    this.animationFrameId = requestAnimationFrame(this.animate.bind(this))

    if (this.isPaused) return

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

    this.clearPath()
    this.scene.remove(this.pathGroup)

    this.locationMarkerMap.clear()
    this.locationGrid.clear()
    this.locationMarkers.clear()

    if (this.emptyInstances) {
      this.scene.remove(this.emptyInstances)
      this.emptyInstances.dispose()
      this.emptyInstances = null
    }
    if (this.occupiedInstances) {
      this.scene.remove(this.occupiedInstances)
      this.occupiedInstances.dispose()
      this.occupiedInstances = null
    }
    if (this.locationMarkerGeo) {
      this.locationMarkerGeo.dispose()
      this.locationMarkerGeo = null
    }

    this.scene.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        child.geometry?.dispose()
        if (Array.isArray(child.material)) {
          child.material.forEach((m) => m.dispose())
        } else if (child.material) {
          child.material.dispose()
        }
      }
    })

    this.cargoGroups.forEach((group) => {
      this.scene.remove(group)
    })
    this.cargoGroups.clear()

    this.rackGroups.forEach((group) => {
      this.scene.remove(group)
    })
    this.rackGroups = []

    this.stackers.forEach((group) => {
      this.scene.remove(group)
    })
    this.stackers = []

    this.conveyors.forEach((group) => {
      this.scene.remove(group)
    })
    this.conveyors = []

    this.stackerPositions = []
    this.stackerMap.clear()
    this.stackerCarriageMap.clear()
    this.stackerForkMap.clear()
    this.stackerCurrentPosition.clear()
    this.stackerCurrentTask.clear()
    this.stackerState.clear()
    this.stackerTaskQueue.clear()

    this.loadedZones.clear()
    this.zoneGroups.clear()
    this.locations.clear()

    this.pickerSystem.dispose()
    this.labelSystem.dispose()
    this.modelFactory['materials'].dispose()

    this.container.removeChild(this.renderer.domElement)
    this.renderer.dispose()
  }
}
