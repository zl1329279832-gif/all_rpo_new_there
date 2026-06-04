import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LocationData, LocationStatus, CargoData } from '../types'
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
    return { total, normal, reserved, damaged, inbound: inboundCount.value, outbound: outboundCount.value, fault: faultCount.value, maintenance: maintenanceCount.value }
  })

  const inboundCount = computed(() => locations.value.filter(l => l.status === 'inbound').length)
  const outboundCount = computed(() => locations.value.filter(l => l.status === 'outbound').length)
  const faultCount = computed(() => locations.value.filter(l => l.status === 'fault').length)
  const maintenanceCount = computed(() => locations.value.filter(l => l.status === 'maintenance').length)

  function selectLocation(id: string | null) {
    selectedLocationId.value = id
  }

  function clearSelection() {
    selectedLocationId.value = null
  }

  function getLocationById(id: string) {
    return locations.value.find(l => l.id === id)
  }

  function updateLocationStatus(id: string, status: LocationStatus) {
    const location = locations.value.find(l => l.id === id)
    if (location) {
      location.status = status
      if (status === 'inbound' || status === 'outbound' || status === 'fault' || status === 'maintenance') {
        location.occupied = true
      } else if (status === 'empty') {
        location.occupied = false
      }
    }
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
    inboundCount,
    outboundCount,
    faultCount,
    maintenanceCount,
    selectLocation,
    clearSelection,
    getLocationById,
    updateLocationStatus,
    getCargoByLocationId,
  }
})
