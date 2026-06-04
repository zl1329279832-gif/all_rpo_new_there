import * as THREE from 'three'
import * as TWEEN from '@tweenjs/tween.js'
import type { LocationData } from '../../types'

interface StackerAnimationContext {
  stackerMap: Map<string, THREE.Group>
  stackerCarriageMap: Map<string, THREE.Group>
  stackerForkMap: Map<string, { left: THREE.Mesh; right: THREE.Mesh }>
  stackerCurrentPosition: Map<string, { x: number; z: number; level: number }>
  locations: Map<string, LocationData>
  cargoGroups: Map<string, THREE.Group>
  placeCargo: (locationId: string, animate?: boolean) => Promise<void>
  removeCargo: (locationId: string, animate?: boolean) => Promise<void>
  scene: THREE.Scene
}

export type WarehouseTask = {
  type: 'inbound' | 'outbound' | 'transfer'
  fromLocationId?: string
  toLocationId?: string
  stackerId: string
}

const LEVEL_HEIGHT = 0.8
const HOME_Z = 0
const HOME_LEVEL = 1
const FORK_EXTENDED = 1.5
const FORK_RETRACTED = 0.9
const CARGO_LIFT_DISTANCE = 0.3

export class AnimationController {
  private isPlaying: boolean = false
  private speed: number = 1
  private paused: boolean = false
  private activeTweens: TWEEN.Tween<any>[] = []
  private currentTask: WarehouseTask | null = null
  private contextRef: StackerAnimationContext | null = null

  taskQueue: WarehouseTask[] = []
  onPathUpdate?: (path: string[]) => void

  constructor() {}

  setSpeed(speed: number): void {
    this.speed = Math.max(0.25, Math.min(4, speed))
  }

  getSpeed(): number {
    return this.speed
  }

  addTask(task: WarehouseTask): void {
    this.taskQueue.push(task)
    if (!this.isPlaying) {
      this.processNextTask()
    }
  }

  processNextTask(): void {
    if (this.taskQueue.length === 0) {
      this.currentTask = null
      this.isPlaying = false
      return
    }

    const task = this.taskQueue.shift()!
    this.currentTask = task

    switch (task.type) {
      case 'inbound':
        this.playInboundAnimation(task.stackerId, this.contextRef!, task.toLocationId!)
        break
      case 'outbound':
        this.playOutboundAnimation(task.stackerId, this.contextRef!, task.fromLocationId!)
        break
      case 'transfer':
        this.playTransferAnimation(
          task.stackerId,
          this.contextRef!,
          task.fromLocationId!,
          task.toLocationId!
        )
        break
    }
  }

  async playInboundAnimation(
    stackerId: string,
    context: StackerAnimationContext,
    toLocationId: string
  ): Promise<void> {
    if (this.isPlaying && this.currentTask?.type !== 'inbound') return
    this.isPlaying = true
    this.contextRef = context

    this.emitPath(['入库口', `库位 ${toLocationId}`])

    try {
      const { stacker, carriage, forks, currentPos } = this.resolveStacker(stackerId, context)
      const toLocation = context.locations.get(toLocationId)
      if (!toLocation) throw new Error(`Location not found: ${toLocationId}`)

      await this.moveStackerToPosition(stacker, currentPos, context.stackerMap.get(stackerId)!.position.x, toLocation.position.z)
      this.emitPath(['堆垛机移动至列', `库位 ${toLocationId}`])

      await this.moveCarriageToLevel(carriage, currentPos, toLocation.level)
      this.emitPath(['载货台升降到位', `库位 ${toLocationId}`])

      await context.removeCargo(toLocationId, false)

      await this.extendForks(forks)
      await this.lowerCargo(carriage, CARGO_LIFT_DISTANCE)
      await this.retractForks(forks)
      this.emitPath(['货物放置完成', `库位 ${toLocationId}`])

      await context.placeCargo(toLocationId, false)

      await this.returnToHome(stacker, carriage, currentPos)
    } finally {
      this.processNextTask()
    }
  }

  async playOutboundAnimation(
    stackerId: string,
    context: StackerAnimationContext,
    fromLocationId: string
  ): Promise<void> {
    if (this.isPlaying && this.currentTask?.type !== 'outbound') return
    this.isPlaying = true
    this.contextRef = context

    this.emitPath([`库位 ${fromLocationId}`, '出库口'])

    try {
      const { stacker, carriage, forks, currentPos } = this.resolveStacker(stackerId, context)
      const fromLocation = context.locations.get(fromLocationId)
      if (!fromLocation) throw new Error(`Location not found: ${fromLocationId}`)

      await this.moveStackerToPosition(stacker, currentPos, context.stackerMap.get(stackerId)!.position.x, fromLocation.position.z)
      this.emitPath(['堆垛机移动至列', `库位 ${fromLocationId}`])

      await this.moveCarriageToLevel(carriage, currentPos, fromLocation.level)
      this.emitPath(['载货台升降到位', `库位 ${fromLocationId}`])

      await this.extendForks(forks)
      await this.liftCargo(carriage, CARGO_LIFT_DISTANCE)
      await this.retractForks(forks)
      this.emitPath(['货物取起', `库位 ${fromLocationId}`])

      await context.removeCargo(fromLocationId, false)

      const outZ = 15
      await this.moveStackerToPosition(stacker, currentPos, stacker.position.x, outZ)
      await this.moveCarriageToLevel(carriage, currentPos, HOME_LEVEL)
      this.emitPath(['堆垛机移动至出库口', '出库口'])

      await this.extendForks(forks)
      await this.lowerCargo(carriage, CARGO_LIFT_DISTANCE)
      await this.retractForks(forks)
      this.emitPath(['货物放到出库口', '出库口'])

      await this.returnToHome(stacker, carriage, currentPos)
    } finally {
      this.processNextTask()
    }
  }

  async playTransferAnimation(
    stackerId: string,
    context: StackerAnimationContext,
    fromLocationId: string,
    toLocationId: string
  ): Promise<void> {
    if (this.isPlaying && this.currentTask?.type !== 'transfer') return
    this.isPlaying = true
    this.contextRef = context

    this.emitPath([`库位 ${fromLocationId}`, `库位 ${toLocationId}`])

    try {
      const { stacker, carriage, forks, currentPos } = this.resolveStacker(stackerId, context)
      const fromLocation = context.locations.get(fromLocationId)
      const toLocation = context.locations.get(toLocationId)
      if (!fromLocation) throw new Error(`Location not found: ${fromLocationId}`)
      if (!toLocation) throw new Error(`Location not found: ${toLocationId}`)

      await this.moveStackerToPosition(stacker, currentPos, context.stackerMap.get(stackerId)!.position.x, fromLocation.position.z)
      this.emitPath(['堆垛机移动至取货列', `库位 ${fromLocationId}`])

      await this.moveCarriageToLevel(carriage, currentPos, fromLocation.level)
      this.emitPath(['载货台升降到位', `库位 ${fromLocationId}`])

      await context.removeCargo(fromLocationId, false)

      await this.extendForks(forks)
      await this.liftCargo(carriage, CARGO_LIFT_DISTANCE)
      await this.retractForks(forks)
      this.emitPath(['取货完成', `库位 ${fromLocationId}`])

      await this.moveStackerToPosition(stacker, currentPos, stacker.position.x, toLocation.position.z)
      this.emitPath(['堆垛机移动至放货列', `库位 ${toLocationId}`])

      await this.moveCarriageToLevel(carriage, currentPos, toLocation.level)
      this.emitPath(['载货台升降到位', `库位 ${toLocationId}`])

      await this.extendForks(forks)
      await this.lowerCargo(carriage, CARGO_LIFT_DISTANCE)
      await this.retractForks(forks)
      this.emitPath(['放货完成', `库位 ${toLocationId}`])

      await context.placeCargo(toLocationId, false)

      await this.returnToHome(stacker, carriage, currentPos)
    } finally {
      this.processNextTask()
    }
  }

  async playStackerAnimation(
    stackerId: string,
    context: StackerAnimationContext,
    fromLocationId?: string,
    toLocationId?: string
  ): Promise<void> {
    if (this.isPlaying) return
    this.isPlaying = true
    this.contextRef = context

    try {
      const { stacker, carriage, forks, currentPos } = this.resolveStacker(stackerId, context)

      if (fromLocationId) {
        const fromLocation = context.locations.get(fromLocationId)
        if (fromLocation) {
          this.emitPath(['出发', `库位 ${fromLocationId}`])

          await this.moveStackerToPosition(stacker, currentPos, stacker.position.x, fromLocation.position.z)
          await this.moveCarriageToLevel(carriage, currentPos, fromLocation.level)

          await context.removeCargo(fromLocationId, false)

          await this.extendForks(forks)
          await this.liftCargo(carriage, CARGO_LIFT_DISTANCE)
          await this.retractForks(forks)

          this.emitPath(['取货完成', `库位 ${fromLocationId}`])
        }
      }

      if (toLocationId) {
        const toLocation = context.locations.get(toLocationId)
        if (toLocation) {
          this.emitPath(['移动中', `库位 ${toLocationId}`])

          await this.moveStackerToPosition(stacker, currentPos, stacker.position.x, toLocation.position.z)
          await this.moveCarriageToLevel(carriage, currentPos, toLocation.level)

          await this.extendForks(forks)
          await this.lowerCargo(carriage, CARGO_LIFT_DISTANCE)
          await this.retractForks(forks)

          await context.placeCargo(toLocationId, false)

          this.emitPath(['放货完成', `库位 ${toLocationId}`])
        }
      }

      await this.returnToHome(stacker, carriage, currentPos)
    } finally {
      this.isPlaying = false
    }
  }

  moveStackerToPosition(
    stacker: THREE.Group,
    currentPos: { x: number; z: number; level: number },
    x: number,
    z: number
  ): Promise<void> {
    return new Promise((resolve) => {
      const startZ = stacker.position.z
      const startX = stacker.position.x
      const distZ = Math.abs(z - startZ)
      const distX = Math.abs(x - startX)
      const duration = (Math.max(distZ, distX) * 800 + 600) / this.speed

      const tween = new TWEEN.Tween({ x: startX, z: startZ })
        .to({ x, z }, duration)
        .easing(TWEEN.Easing.Quadratic.InOut)
        .onUpdate((obj) => {
          stacker.position.z = obj.z
          stacker.position.x = obj.x
        })
        .start()
        .onComplete(() => {
          currentPos.z = z
          currentPos.x = x
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  moveCarriageToLevel(
    carriage: THREE.Group,
    currentPos: { x: number; z: number; level: number },
    level: number
  ): Promise<void> {
    return new Promise((resolve) => {
      const targetY = (level - 1) * LEVEL_HEIGHT + 0.5
      const startY = carriage.position.y
      const distance = Math.abs(targetY - startY)
      const duration = (distance * 600 + 400) / this.speed

      const tween = new TWEEN.Tween({ y: startY })
        .to({ y: targetY }, duration)
        .easing(TWEEN.Easing.Quadratic.InOut)
        .onUpdate((obj) => {
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => {
          currentPos.level = level
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  extendForks(forks: { left: THREE.Mesh; right: THREE.Mesh }): Promise<void> {
    return new Promise((resolve) => {
      const duration = 800 / this.speed

      const tween = new TWEEN.Tween({ z: FORK_RETRACTED })
        .to({ z: FORK_EXTENDED }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          forks.left.position.z = obj.z
          forks.right.position.z = obj.z
        })
        .start()
        .onComplete(() => {
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  retractForks(forks: { left: THREE.Mesh; right: THREE.Mesh }): Promise<void> {
    return new Promise((resolve) => {
      const duration = 800 / this.speed

      const tween = new TWEEN.Tween({ z: FORK_EXTENDED })
        .to({ z: FORK_RETRACTED }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          forks.left.position.z = obj.z
          forks.right.position.z = obj.z
        })
        .start()
        .onComplete(() => {
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  liftCargo(carriage: THREE.Group, distance: number = CARGO_LIFT_DISTANCE): Promise<void> {
    return new Promise((resolve) => {
      const duration = 500 / this.speed
      const startY = carriage.position.y

      const tween = new TWEEN.Tween({ y: startY })
        .to({ y: startY + distance }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => {
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  lowerCargo(carriage: THREE.Group, distance: number = CARGO_LIFT_DISTANCE): Promise<void> {
    return new Promise((resolve) => {
      const duration = 500 / this.speed
      const startY = carriage.position.y

      const tween = new TWEEN.Tween({ y: startY })
        .to({ y: startY - distance }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => {
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  pause(): void {
    if (this.paused) return
    this.paused = true
    for (const tween of this.activeTweens) {
      tween.pause()
    }
  }

  resume(): void {
    if (!this.paused) return
    this.paused = false
    for (const tween of this.activeTweens) {
      tween.resume()
    }
  }

  isAnimationPlaying(): boolean {
    return this.isPlaying
  }

  update(): void {
    TWEEN.update()
  }

  stopAll(): void {
    TWEEN.removeAll()
    this.activeTweens = []
    this.isPlaying = false
    this.paused = false
    this.currentTask = null
  }

  private resolveStacker(stackerId: string, context: StackerAnimationContext) {
    const stacker = context.stackerMap.get(stackerId)
    const carriage = context.stackerCarriageMap.get(stackerId)
    const forks = context.stackerForkMap.get(stackerId)
    const currentPos = context.stackerCurrentPosition.get(stackerId)

    if (!stacker || !carriage || !forks || !currentPos) {
      throw new Error(`Stacker not found: ${stackerId}`)
    }

    return { stacker, carriage, forks, currentPos }
  }

  private returnToHome(
    stacker: THREE.Group,
    carriage: THREE.Group,
    currentPos: { x: number; z: number; level: number }
  ): Promise<void> {
    return new Promise((resolve) => {
      const duration = 1000 / this.speed
      const startZ = stacker.position.z
      const startY = carriage.position.y
      const homeY = (HOME_LEVEL - 1) * LEVEL_HEIGHT + 0.5

      const tween = new TWEEN.Tween({ z: startZ, y: startY })
        .to({ z: HOME_Z, y: homeY }, duration)
        .easing(TWEEN.Easing.Quadratic.InOut)
        .onUpdate((obj) => {
          stacker.position.z = obj.z
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => {
          currentPos.z = HOME_Z
          currentPos.level = HOME_LEVEL
          this.removeActiveTween(tween)
          resolve()
        })

      this.activeTweens.push(tween)
    })
  }

  private emitPath(path: string[]): void {
    this.onPathUpdate?.(path)
  }

  private removeActiveTween(tween: TWEEN.Tween<any>): void {
    const idx = this.activeTweens.indexOf(tween)
    if (idx !== -1) {
      this.activeTweens.splice(idx, 1)
    }
  }

  playConveyorAnimation(conveyor: THREE.Group, speed: number = 1): void {
    conveyor.traverse((child) => {
      if (child.name.startsWith('roller_')) {
        child.rotation.x += 0.02 * speed
      }
    })
  }
}
