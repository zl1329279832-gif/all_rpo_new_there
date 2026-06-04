import type { LocationData, LocationStatus, ZoneType } from '../types'

const RACK_CONFIG = {
  bayWidth: 1.2,
  bayDepth: 1.2,
  levelHeight: 0.8,
  bays: 8,
  levels: 6,
  rowSpacing: 1.5,
}

const RACK_GROUPS = [
  { groupX: -12, groupZ: -8, rows: 2, rotation: 0, rackPrefix: 'R' },
  { groupX: -12, groupZ: 8, rows: 2, rotation: 0, rackPrefix: 'R' },
  { groupX: 12, groupZ: -8, rows: 2, rotation: Math.PI, rackPrefix: 'R' },
  { groupX: 12, groupZ: 8, rows: 2, rotation: Math.PI, rackPrefix: 'R' },
]

function generateLocations(): LocationData[] {
  const locations: LocationData[] = []
  let rackIndex = 1

  function randomStatus(occupied: boolean): { status: LocationStatus; occupied: boolean } {
    const rand = Math.random()
    if (rand < 0.01) return { status: 'fault', occupied: true }
    if (rand < 0.02) return { status: 'maintenance', occupied: true }
    if (rand < 0.05) return { status: 'outbound', occupied: true }
    if (rand < 0.10) return { status: 'inbound', occupied: true }
    return { status: occupied ? 'occupied' : 'empty', occupied }
  }

  RACK_GROUPS.forEach((group) => {
    for (let row = 0; row < group.rows; row++) {
      const rackId = `R${String(rackIndex).padStart(2, '0')}`
      const rackLocalX = row * RACK_CONFIG.rowSpacing

      for (let bay = 0; bay < RACK_CONFIG.bays; bay++) {
        for (let level = 0; level < RACK_CONFIG.levels; level++) {
          const locationId = `${rackId}-${String(bay + 1).padStart(2, '0')}-${String(level + 1).padStart(2, '0')}`
          const occupied = Math.random() > 0.4
          const { status, occupied: finalOccupied } = randomStatus(occupied)

          const bayLocalX = bay * RACK_CONFIG.bayWidth - (RACK_CONFIG.bays * RACK_CONFIG.bayWidth) / 2 + RACK_CONFIG.bayWidth / 2
          const localX = rackLocalX + bayLocalX
          const localZ = 0
          const localY = level * RACK_CONFIG.levelHeight

          let worldX: number, worldZ: number
          if (group.rotation === 0) {
            worldX = group.groupX + localX
            worldZ = group.groupZ + localZ
          } else {
            worldX = group.groupX - localX
            worldZ = group.groupZ - localZ
          }

          locations.push({
            id: locationId,
            zone: 'storage',
            row: rackIndex,
            bay: bay + 1,
            level: level + 1,
            maxWeight: 500,
            position: {
              x: worldX,
              y: localY,
              z: worldZ,
            },
            occupied: finalOccupied,
            status,
          })
        }
      }
      rackIndex++
    }
  })

  for (let i = 0; i < 10; i++) {
    const occupied = Math.random() > 0.5
    const { status, occupied: finalOccupied } = randomStatus(occupied)
    locations.push({
      id: `INB-${String(i + 1).padStart(2, '0')}`,
      zone: 'inbound',
      row: 0,
      bay: i + 1,
      level: 1,
      maxWeight: 1000,
      position: {
        x: -5 + i,
        y: 0.05,
        z: -15,
      },
      occupied: finalOccupied,
      status,
    })
  }

  for (let i = 0; i < 10; i++) {
    const occupied = Math.random() > 0.6
    const { status, occupied: finalOccupied } = randomStatus(occupied)
    locations.push({
      id: `OUT-${String(i + 1).padStart(2, '0')}`,
      zone: 'outbound',
      row: 0,
      bay: i + 1,
      level: 1,
      maxWeight: 1000,
      position: {
        x: -5 + i,
        y: 0.05,
        z: 15,
      },
      occupied: finalOccupied,
      status,
    })
  }

  for (let i = 0; i < 8; i++) {
    const occupied = Math.random() > 0.3
    const { status, occupied: finalOccupied } = randomStatus(occupied)
    locations.push({
      id: `PCK-${String(i + 1).padStart(2, '0')}`,
      zone: 'picking',
      row: 0,
      bay: i + 1,
      level: 1,
      maxWeight: 500,
      position: {
        x: -18,
        y: 0.05,
        z: -8 + i * 2,
      },
      occupied: finalOccupied,
      status,
    })
  }

  return locations
}

export const locationData = generateLocations()

export function getLocationById(id: string, locations: LocationData[]): LocationData | undefined {
  return locations.find((loc) => loc.id === id)
}

export function getLocationsByZone(zone: ZoneType, locations: LocationData[]): LocationData[] {
  return locations.filter((loc) => loc.zone === zone)
}

export function getOccupiedLocations(locations: LocationData[]): LocationData[] {
  return locations.filter((loc) => loc.occupied)
}

export function getAvailableLocations(locations: LocationData[]): LocationData[] {
  return locations.filter((loc) => !loc.occupied)
}

export function getStatistics(locations: LocationData[]) {
  const total = locations.length
  const occupied = locations.filter((loc) => loc.occupied).length
  return {
    totalLocations: total,
    occupiedLocations: occupied,
    utilizationRate: total > 0 ? Math.round((occupied / total) * 100) : 0,
  }
}
