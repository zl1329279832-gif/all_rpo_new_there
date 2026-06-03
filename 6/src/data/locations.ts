import type { LocationData, ZoneType } from '../types'

function generateLocations(): LocationData[] {
  const locations: LocationData[] = []
  
  const rackConfigs = [
    { groupX: -12, groupZ: -8, rows: 2, direction: 1, zone: 'storage' as ZoneType, rackPrefix: 'R0' },
    { groupX: -12, groupZ: 8, rows: 2, direction: -1, zone: 'storage' as ZoneType, rackPrefix: 'R0' },
    { groupX: 12, groupZ: -8, rows: 2, direction: -1, zone: 'storage' as ZoneType, rackPrefix: 'R0' },
    { groupX: 12, groupZ: 8, rows: 2, direction: -1, zone: 'storage' as ZoneType, rackPrefix: 'R0' },
  ]

  const bays = 8
  const levels = 6
  const bayWidth = 1.2
  const levelHeight = 0.8
  const rowSpacing = 1.5

  let rackIndex = 1

  rackConfigs.forEach((group) => {
    for (let row = 0; row < group.rows; row++) {
      const rackId = `${group.rackPrefix}${String(rackIndex).padStart(2, '0')}`
      const rowOffset = row * rowSpacing * group.direction
      
      for (let bay = 0; bay < bays; bay++) {
        for (let level = 0; level < levels; level++) {
          const locationId = `${rackId}-${String(bay + 1).padStart(2, '0')}-${String(level + 1).padStart(2, '0')}`
          const occupied = Math.random() > 0.4
          
          const baseX = group.groupX + rowOffset
          const baseZ = group.groupZ + (bay - bays / 2 + 0.5) * bayWidth
          const baseY = level * levelHeight + levelHeight / 2
          
          locations.push({
            id: locationId,
            zone: group.zone,
            row: rackIndex,
            bay: bay + 1,
            level: level + 1,
            maxWeight: 500,
            position: {
              x: baseX,
              y: baseY,
              z: baseZ,
            },
            occupied,
          })
        }
      }
      rackIndex++
    }
  })

  for (let i = 0; i < 10; i++) {
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
      occupied: Math.random() > 0.5,
    })
  }

  for (let i = 0; i < 10; i++) {
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
      occupied: Math.random() > 0.6,
    })
  }

  for (let i = 0; i < 8; i++) {
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
      occupied: Math.random() > 0.3,
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
