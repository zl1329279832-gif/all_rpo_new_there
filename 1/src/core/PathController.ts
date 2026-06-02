import * as THREE from 'three'

export interface PathPoint {
  position: THREE.Vector3
  speed?: number
}

export class PathController {
  private path: PathPoint[] = []
  private currentIndex: number = 0
  private progress: number = 0
  private isMoving: boolean = false
  private robotGroup: THREE.Group
  private speed: number = 2

  constructor(robotGroup: THREE.Group) {
    this.robotGroup = robotGroup
  }

  setPath(points: PathPoint[]) {
    this.path = points
    this.currentIndex = 0
    this.progress = 0
  }

  start() {
    this.isMoving = true
  }

  stop() {
    this.isMoving = false
  }

  update(deltaTime: number): boolean {
    if (!this.isMoving || this.path.length < 2) return false

    const currentPoint = this.path[this.currentIndex]
    const nextIndex = (this.currentIndex + 1) % this.path.length
    const nextPoint = this.path[nextIndex]

    const speed = currentPoint.speed || this.speed
    this.progress += deltaTime * speed * 0.1

    if (this.progress >= 1) {
      this.progress = 0
      this.currentIndex = nextIndex
    }

    const t = this.progress
    const newPos = new THREE.Vector3().lerpVectors(
      currentPoint.position,
      nextPoint.position,
      t
    )
    
    this.robotGroup.position.copy(newPos)

    const direction = new THREE.Vector3().subVectors(
      nextPoint.position,
      currentPoint.position
    ).normalize()
    
    const angle = Math.atan2(direction.x, direction.z)
    this.robotGroup.rotation.y = angle

    return true
  }

  isActive(): boolean {
    return this.isMoving
  }

  reset() {
    this.currentIndex = 0
    this.progress = 0
    this.isMoving = false
    if (this.path.length > 0) {
      this.robotGroup.position.copy(this.path[0].position)
    }
  }
}
