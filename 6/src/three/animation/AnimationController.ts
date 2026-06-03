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

export class AnimationController {
  private isPlaying: boolean = false
  private speed: number = 1

  constructor() {}

  setSpeed(speed: number): void {
    this.speed = Math.max(0.25, Math.min(4, speed))
  }

  getSpeed(): number {
    return this.speed
  }

  async playStackerAnimation(
    stackerId: string,
    context: StackerAnimationContext,
    fromLocationId?: string,
    toLocationId?: string
  ): Promise<void> {
    if (this.isPlaying) {
      return
    }

    this.isPlaying = true

    try {
      const stacker = context.stackerMap.get(stackerId)
      const carriage = context.stackerCarriageMap.get(stackerId)
      const forks = context.stackerForkMap.get(stackerId)
      const currentPos = context.stackerCurrentPosition.get(stackerId)

      if (!stacker || !carriage || !forks || !currentPos) {
        throw new Error('Stacker not found')
      }

      if (fromLocationId) {
        const fromLocation = context.locations.get(fromLocationId)
        if (fromLocation) {
          await this.moveStackerTo(stacker, carriage, currentPos, fromLocation)
          await this.extendForks(forks)
          await this.liftCargo(carriage, 0.3)
          await this.retractForks(forks)
        }
      }

      if (toLocationId) {
        const toLocation = context.locations.get(toLocationId)
        if (toLocation) {
          await this.moveStackerTo(stacker, carriage, currentPos, toLocation)
          await this.extendForks(forks)
          await this.lowerCargo(carriage, 0.3)
          await this.retractForks(forks)
        }
      }

      await this.returnToHome(stacker, carriage, currentPos)
    } finally {
      this.isPlaying = false
    }
  }

  private moveStackerTo(
    stacker: THREE.Group,
    carriage: THREE.Group,
    currentPos: { x: number; z: number; level: number },
    targetLocation: LocationData
  ): Promise<void> {
    return new Promise((resolve) => {
      const targetZ = targetLocation.position.z
      const targetY = targetLocation.position.y + 0.5
      const duration = 1500 / this.speed

      const startZ = stacker.position.z
      const startY = carriage.position.y

      new TWEEN.Tween({ z: startZ, y: startY })
        .to({ z: targetZ, y: targetY }, duration)
        .easing(TWEEN.Easing.Quadratic.InOut)
        .onUpdate((obj) => {
          stacker.position.z = obj.z
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => {
          currentPos.z = targetZ
          currentPos.level = targetLocation.level
          resolve()
        })
    })
  }

  private extendForks(forks: { left: THREE.Mesh; right: THREE.Mesh }): Promise<void> {
    return new Promise((resolve) => {
      const duration = 800 / this.speed

      new TWEEN.Tween({ z: 0.9 })
        .to({ z: 1.5 }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          forks.left.position.z = obj.z
          forks.right.position.z = obj.z
        })
        .start()
        .onComplete(() => resolve())
    })
  }

  private retractForks(forks: { left: THREE.Mesh; right: THREE.Mesh }): Promise<void> {
    return new Promise((resolve) => {
      const duration = 800 / this.speed

      new TWEEN.Tween({ z: 1.5 })
        .to({ z: 0.9 }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          forks.left.position.z = obj.z
          forks.right.position.z = obj.z
        })
        .start()
        .onComplete(() => resolve())
    })
  }

  private liftCargo(carriage: THREE.Group, distance: number): Promise<void> {
    return new Promise((resolve) => {
      const duration = 500 / this.speed
      const startY = carriage.position.y

      new TWEEN.Tween({ y: startY })
        .to({ y: startY + distance }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => resolve())
    })
  }

  private lowerCargo(carriage: THREE.Group, distance: number): Promise<void> {
    return new Promise((resolve) => {
      const duration = 500 / this.speed
      const startY = carriage.position.y

      new TWEEN.Tween({ y: startY })
        .to({ y: startY - distance }, duration)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate((obj) => {
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => resolve())
    })
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

      new TWEEN.Tween({ z: startZ, y: startY })
        .to({ z: 0, y: 1.5 }, duration)
        .easing(TWEEN.Easing.Quadratic.InOut)
        .onUpdate((obj) => {
          stacker.position.z = obj.z
          carriage.position.y = obj.y
        })
        .start()
        .onComplete(() => {
          currentPos.z = 0
          currentPos.level = 1
          resolve()
        })
    })
  }

  playConveyorAnimation(conveyor: THREE.Group, speed: number = 1): void {
    conveyor.traverse((child) => {
      if (child.name.startsWith('roller_')) {
        child.rotation.x += 0.02 * speed
      }
    })
  }

  update(): void {
    TWEEN.update()
  }

  isAnimationPlaying(): boolean {
    return this.isPlaying
  }

  stopAll(): void {
    TWEEN.removeAll()
    this.isPlaying = false
  }
}
