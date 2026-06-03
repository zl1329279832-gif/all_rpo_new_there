export type ZoneType = 'inbound' | 'storage' | 'outbound' | 'picking'
export type DeviceType = 'stacker' | 'conveyor' | 'elevator' | 'scanner'
export type DeviceStatus = 'running' | 'idle' | 'error' | 'maintenance'
export type TaskType = 'inbound' | 'outbound' | 'transfer'
export type CargoStatus = 'normal' | 'reserved' | 'damaged'
export type PalletType = 'chuan' | 'nine' | 'double'
export type BoxSize = 'small' | 'medium' | 'large'
export type LabelType = 'location' | 'device' | 'path' | 'cargo'
export type AnimationState = 'idle' | 'moving' | 'lifting' | 'forking' | 'complete'

export interface Vector3 {
  x: number
  y: number
  z: number
}

export interface CargoData {
  id: string
  sku: string
  name: string
  quantity: number
  weight: number
  batchNo: string
  inboundDate: string
  expiryDate?: string
  locationId: string
  status: CargoStatus
}

export interface LocationData {
  id: string
  zone: ZoneType
  row: number
  bay: number
  level: number
  maxWeight: number
  currentCargo?: CargoData
  position: Vector3
  occupied: boolean
}

export interface DeviceData {
  id: string
  type: DeviceType
  name: string
  status: DeviceStatus
  position: Vector3
  currentTask?: TaskData
  errorCode?: string
}

export interface TaskData {
  id: string
  type: TaskType
  sourceLocation?: string
  targetLocation?: string
  cargoId: string
  progress: number
  startTime: string
}

export interface WarehouseStatistics {
  totalLocations: number
  occupiedLocations: number
  utilizationRate: number
  totalCargo: number
  inboundToday: number
  outboundToday: number
}

export interface CameraPosition {
  position: Vector3
  target: Vector3
}

export interface ZoneCameraPreset {
  name: string
  position: Vector3
  target: Vector3
}

export interface AnimationConfig {
  speed: number
  loop: boolean
  autoPlay: boolean
}
