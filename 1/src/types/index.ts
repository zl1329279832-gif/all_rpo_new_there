import * as THREE from 'three'

export type RobotAnimationState = 
  | 'idle' 
  | 'moving' 
  | 'turning' 
  | 'lifting' 
  | 'lowering'
  | 'charging' 
  | 'avoiding'
  | 'pickingUp'
  | 'droppingOff'
  | 'returning'
  | 'fault'
  | 'paused'

export type MaterialType = 'plastic' | 'metal' | 'rubber' | 'glass' | 'emissive'

export interface RobotPart {
  id: string
  name: string
  description: string
  mesh: THREE.Object3D
  category: string
  originalPosition: THREE.Vector3
  originalRotation?: THREE.Euler
}

export interface RobotState {
  position: THREE.Vector3
  rotation: number
  targetRotation: number
  batteryLevel: number
  liftHeight: number
  targetLiftHeight: number
  isCharging: boolean
  isMoving: boolean
  isAvoiding: boolean
  hasPayload: boolean
  currentAnimation: RobotAnimationState
  previousAnimation: RobotAnimationState
  speed: number
  targetPosition?: THREE.Vector3
  pathIndex: number
  currentTask?: string
  faultCode?: string
}

export interface AnimationState {
  current: RobotAnimationState
  previous: RobotAnimationState
  progress: number
  speed: number
  isPaused: boolean
}

export interface Task {
  id: string
  type: 'pickup' | 'delivery' | 'charge' | 'return'
  pickupLocation: THREE.Vector3
  dropoffLocation: THREE.Vector3
  status: 'pending' | 'inProgress' | 'completed' | 'failed'
  priority: 'low' | 'medium' | 'high'
}

export interface PathPoint {
  position: THREE.Vector3
  action?: 'lift' | 'lower' | 'wait'
  duration?: number
}

export interface PartInfo {
  id: string
  name: string
  description: string
  category: string
  specs?: Record<string, string>
  maintenance?: string
}

export interface SensorData {
  lidarDistance: number[]
  cameraImage?: string
  batteryVoltage: number
  motorTemp: number
  wheelSpeed: number[]
}

export interface FaultState {
  id: string
  type: string
  severity: 'warning' | 'critical'
  message: string
  affectedParts: string[]
  timestamp: number
  isActive?: boolean
}

export interface PerformanceStats {
  fps: number
  frameTime?: number
  drawCalls: number
  triangles: number
  memory: number | {
    geometries: number
    textures: number
  }
}
