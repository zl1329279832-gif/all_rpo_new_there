import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LocationData, CargoData } from '../types'
import { locationData, getStatistics } from '../data/locations'
import { generateInventoryData, getCargoByLocation } from '../data/inventory'

export const useInventoryStore = defineStore('inventory', () => {
  const locations = ref<LocationData[]>([...locationData])
  const cargoList = ref<CargoData[]>(generateInventoryData(locationData.filter(l => l.occupied).map(l => l.id)))

  const selectedLocationId = ref<string | null>(null)

  const statistics = computed(() => {
    return getStatistics(locations.value)
  })

  const selectedLocation = computed(() => {
    if (!selectedLocationId.value) return null
    return locations.value.find(l => l.id === selectedLocationId.value) || null
  })

  const selectedCargo = computed(() => {
    if (!selectedLocationId.value) return null
    return getCargoByLocation(selectedLocationId.value, cargoList.value) || null
  })

  const cargoStats = computed(() => {
    const total = cargoList.value.length
    const normal = cargoList.value.filter(c => c.status === 'normal').length
    const reserved = cargoList.value.filter(c => c.status === 'reserved').length
    const damaged = cargoList.value.filter(c => c.status === 'damaged').length
    return { total, normal, reserved, damaged }
  })

  function selectLocation(id: string | null) {
    selectedLocationId.value = id
  }

  function getLocationById(id: string) {
    return locations.value.find(l => l.id === id)
  }

  function getCargoByLocationId(locationId: string) {
    return cargoList.value.find(c => c.locationId === locationId)
  }

  return {
    locations,
    cargoList,
    selectedLocationId,
    selectedLocation,
    selectedCargo,
    statistics,
    cargoStats,
    selectLocation,
    getLocationById,
    getCargoByLocationId,
  }
})
