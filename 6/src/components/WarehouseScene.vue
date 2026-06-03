<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { WarehouseScene } from '../three/core/WarehouseScene'
import { useInventoryStore } from '../store/useInventoryStore'
import { useDeviceStore } from '../store/useDeviceStore'
import { useSceneStore } from '../store/useSceneStore'

const containerRef = ref<HTMLElement | null>(null)
let warehouseScene: WarehouseScene | null = null

const inventoryStore = useInventoryStore()
const deviceStore = useDeviceStore()
const sceneStore = useSceneStore()

onMounted(() => {
  if (containerRef.value) {
    warehouseScene = new WarehouseScene(containerRef.value)
    
    const locationsWithCargo = inventoryStore.locations.map(loc => {
      const cargo = inventoryStore.getCargoByLocationId(loc.id)
      return { ...loc, currentCargo: cargo || undefined }
    })
    
    warehouseScene.buildWarehouse(locationsWithCargo)
    
    locationsWithCargo.forEach((loc) => {
      if (loc.occupied) {
        warehouseScene!.placeCargo(loc.id, false)
      }
    })
    
    warehouseScene.setOnLocationClick((locationId) => {
      inventoryStore.selectLocation(locationId)
    })
    
    warehouseScene.setOnDeviceClick((deviceId) => {
      deviceStore.selectDevice(deviceId)
    })
    
    warehouseScene.start()
  }
})

onUnmounted(() => {
  if (warehouseScene) {
    warehouseScene.dispose()
  }
})

watch(() => sceneStore.currentZone, (zone) => {
  if (warehouseScene) {
    warehouseScene.moveToZone(zone)
  }
})

function resetCamera() {
  warehouseScene?.resetCamera()
}

function playStackerAnimation() {
  if (warehouseScene) {
    const stackerIds = warehouseScene.getStackerIds()
    if (stackerIds.length > 0) {
      const occupiedLocations = inventoryStore.locations.filter(l => l.occupied && l.id.startsWith('R'))
      const emptyLocations = inventoryStore.locations.filter(l => !l.occupied && l.id.startsWith('R'))
      
      if (occupiedLocations.length > 0 && emptyLocations.length > 0) {
        const fromLoc = occupiedLocations[Math.floor(Math.random() * occupiedLocations.length)]
        const toLoc = emptyLocations[Math.floor(Math.random() * emptyLocations.length)]
        
        warehouseScene.playStackerAnimation(
          stackerIds[0],
          fromLoc.id,
          toLoc.id
        )
      }
    }
  }
}

function playInboundAnimation() {
  if (warehouseScene) {
    const stackerIds = warehouseScene.getStackerIds()
    const emptyLocations = inventoryStore.locations.filter(l => !l.occupied && l.id.startsWith('R'))
    
    if (stackerIds.length > 0 && emptyLocations.length > 0) {
      const toLoc = emptyLocations[Math.floor(Math.random() * emptyLocations.length)]
      warehouseScene.playStackerAnimation(
        stackerIds[0],
        undefined,
        toLoc.id
      )
    }
  }
}

defineExpose({
  resetCamera,
  playStackerAnimation,
  playInboundAnimation,
})
</script>

<template>
  <div ref="containerRef" class="scene-container"></div>
</template>

<style scoped lang="scss">
.scene-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}
</style>
