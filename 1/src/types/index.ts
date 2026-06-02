import * as THREE from 'three'

export type RobotAnimationState = 'idle' | 'moving' | 'turning' | 'lifting' | 'charging' | 'avoiding'

export type MaterialType = 'plastic' | 'metal' | 'rubber' | 'glass' | 'emissive'

export interface RobotPart {
  id: string
  name: string
  description: string
  mesh: THREE.Object3D
  category: string
  originalPosition: THREE.Vector3
}

export interface RobotState {
  position: THREE.Vector3
  rotation: number
  batteryLevel: number
  liftHeight: number
  isCharging: boolean
  isMoving: boolean
  isAvoiding: boolean
  hasPayload: boolean
  currentAnimation: RobotAnimationState
}

export interface AnimationState {
  current: RobotAnimationState
  progress: number
  speed: number
}

export interface PartInfo {
  id: string
  name: string
  description: string
  category: string
  specs?: Record<string, string>
}
