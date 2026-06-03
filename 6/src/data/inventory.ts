import type { CargoData } from '../types'

const cargoNames = [
  '电子元件 A', '电路板 B', '传感器 C', '电机组件 D', '控制器 E',
  '显示屏 F', '键盘 G', '鼠标 H', '电源 I', '连接器 J',
  '芯片 K', '电阻 L', '电容 M', '电感 N', '变压器 O',
  '电池 P', '充电器 Q', '数据线 R', '适配器 S', '开关 T',
]

function generateCargoData(locationIds: string[]): CargoData[] {
  const cargoList: CargoData[] = []
  const occupiedLocations = locationIds.filter(() => Math.random() > 0.4)

  occupiedLocations.forEach((locationId, index) => {
    const sku = `SKU-${String(index + 1).padStart(5, '0')}`
    const batchNo = `B${new Date().getFullYear()}${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`
    
    cargoList.push({
      id: `CG-${String(index + 1).padStart(6, '0')}`,
      sku,
      name: cargoNames[index % cargoNames.length],
      quantity: Math.floor(Math.random() * 100) + 1,
      weight: Math.round((Math.random() * 50 + 5) * 10) / 10,
      batchNo,
      inboundDate: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      expiryDate: Math.random() > 0.5 
        ? new Date(Date.now() + Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
        : undefined,
      locationId,
      status: Math.random() > 0.9 ? 'damaged' : Math.random() > 0.7 ? 'reserved' : 'normal',
    })
  })

  return cargoList
}

export function generateInventoryData(locationIds: string[]): CargoData[] {
  return generateCargoData(locationIds)
}

export function getCargoById(id: string, cargoList: CargoData[]): CargoData | undefined {
  return cargoList.find((c) => c.id === id)
}

export function getCargoByLocation(locationId: string, cargoList: CargoData[]): CargoData | undefined {
  return cargoList.find((c) => c.locationId === locationId)
}

export function getCargoBySku(sku: string, cargoList: CargoData[]): CargoData[] {
  return cargoList.filter((c) => c.sku === sku)
}

export function getCargoStatistics(cargoList: CargoData[]) {
  const totalCargo = cargoList.length
  const totalQuantity = cargoList.reduce((sum, c) => sum + c.quantity, 0)
  const totalWeight = cargoList.reduce((sum, c) => sum + c.weight, 0)
  const normalCount = cargoList.filter((c) => c.status === 'normal').length
  const reservedCount = cargoList.filter((c) => c.status === 'reserved').length
  const damagedCount = cargoList.filter((c) => c.status === 'damaged').length

  return {
    totalCargo,
    totalQuantity,
    totalWeight: Math.round(totalWeight * 10) / 10,
    normalCount,
    reservedCount,
    damagedCount,
  }
}
