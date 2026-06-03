import type { DeviceData, TaskData } from '../types'

export const deviceData: DeviceData[] = [
  {
    id: 'STK-001',
    type: 'stacker',
    name: '堆垛机 1号',
    status: 'idle',
    position: { x: -10, y: 0, z: 0 },
  },
  {
    id: 'STK-002',
    type: 'stacker',
    name: '堆垛机 2号',
    status: 'idle',
    position: { x: 10, y: 0, z: 0 },
  },
  {
    id: 'CONV-001',
    type: 'conveyor',
    name: '入库输送线',
    status: 'running',
    position: { x: 0, y: 0.75, z: -15 },
  },
  {
    id: 'CONV-002',
    type: 'conveyor',
    name: '出库输送线',
    status: 'running',
    position: { x: 0, y: 0.75, z: 15 },
  },
  {
    id: 'CONV-003',
    type: 'conveyor',
    name: '左侧输送线',
    status: 'idle',
    position: { x: -18, y: 0.75, z: 0 },
  },
  {
    id: 'CONV-004',
    type: 'conveyor',
    name: '右侧输送线',
    status: 'running',
    position: { x: 18, y: 0.75, z: 0 },
  },
  {
    id: 'ELEV-001',
    type: 'elevator',
    name: '提升机',
    status: 'idle',
    position: { x: 0, y: 0, z: -15 },
  },
  {
    id: 'SCN-001',
    type: 'scanner',
    name: '扫码器 1号',
    status: 'running',
    position: { x: -5, y: 0, z: -15 },
  },
  {
    id: 'SCN-002',
    type: 'scanner',
    name: '扫码器 2号',
    status: 'running',
    position: { x: 5, y: 0, z: 15 },
  },
]

export const activeTasks: TaskData[] = [
  {
    id: 'TASK-001',
    type: 'inbound',
    sourceLocation: 'INB-03',
    targetLocation: 'R01-04-03',
    cargoId: 'CG-000001',
    progress: 0,
    startTime: new Date().toISOString(),
  },
]

export function getDeviceById(id: string, devices: DeviceData[]): DeviceData | undefined {
  return devices.find((d) => d.id === id)
}

export function getDevicesByType(type: DeviceData['type'], devices: DeviceData[]): DeviceData[] {
  return devices.filter((d) => d.type === type)
}

export function getDevicesByStatus(status: DeviceData['status'], devices: DeviceData[]): DeviceData[] {
  return devices.filter((d) => d.status === status)
}

export function updateDeviceStatus(
  id: string,
  status: DeviceData['status'],
  devices: DeviceData[]
): DeviceData[] {
  return devices.map((d) => (d.id === id ? { ...d, status } : d))
}

export function getDeviceStatistics(devices: DeviceData[]) {
  const running = devices.filter((d) => d.status === 'running').length
  const idle = devices.filter((d) => d.status === 'idle').length
  const error = devices.filter((d) => d.status === 'error').length
  const maintenance = devices.filter((d) => d.status === 'maintenance').length

  return {
    total: devices.length,
    running,
    idle,
    error,
    maintenance,
  }
}
