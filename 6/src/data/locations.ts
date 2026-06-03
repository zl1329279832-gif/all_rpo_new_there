import type { LocationData, ZoneType } from '../types'

function generateLocations(): LocationData[] {
  const locations: LocationData[] = []
  
  const rackConfigs = [
    { x: -12.5, z: -8.5, direction: 1, zone: 'storage' as ZoneType, rackId: 'R01' },
    { x: -11, z: -8.5, direction: 1, zone: 'storage' as ZoneType, rackId: 'R02' },
    { x: -12.5, z: 7.5, direction: -1, zone: 'storage' as ZoneType, rackId: 'R03' },
    { x: -11, z: 7.5, direction: -1, zone: 'storage' as ZoneType, rackId: 'R04' },
    { x: 11, z: -8.5, direction: 1, zone: 'storage' as ZoneType, rackId: 'R05' },
    { x: 12.5, z: -8.5, direction: 1, zone: 'storage' as ZoneType, rackId: 'R06' },
    { x: 11, z: 7.5, direction: -1, zone: 'storage' as ZoneType, rackId: 'R07' },
    { x: 12.5, z: 7.5, direction: -1, zone: 'storage' as ZoneType, rackId: 'R08' },
  ]

  const bays = 8
  const levels = 6

  rackConfigs.forEach((rack) => {
    for (let bay = 0; bay < bays; bay++) {
      for (let level = 0; level < levels; level++) {
        const locationId = `${rack.rackId}-${String(bay + 1).padStart(2, '0')}-${String(level + 1).padStart(2, '0')}`
        const occupied = Math.random() > 0.4
        
        locations.push({
          id: locationId,
          zone: rack.zone,
          row: parseInt(rack.rackId.slice(1)),
          bay: bay + 1,
          level: level + 1,
          maxWeight: 500,
          position: {
            x: rack.x,
            y: level * 0.8 + 0.05,
            z: rack.z + bay * 1.2 - 4.2,
          },
          occupied,
        })
      }
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
