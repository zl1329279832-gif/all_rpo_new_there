import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ZoneType, LabelType } from '../types'

export const useSceneStore = defineStore('scene', () => {
  const currentZone = ref<ZoneType>('storage')
  const showLocationLabels = ref(true)
  const showDeviceLabels = ref(true)
  const showPathLines = ref(false)
  const isFullscreen = ref(false)
  const showTutorial = ref(false)
  const showModelInfo = ref(false)

  const zoneNames: Record<ZoneType, string> = {
    inbound: '入库区',
    storage: '存储区',
    outbound: '出库区',
    picking: '拣选区',
  }

  function setCurrentZone(zone: ZoneType) {
    currentZone.value = zone
  }

  function toggleLabel(type: LabelType) {
    switch (type) {
      case 'location':
        showLocationLabels.value = !showLocationLabels.value
        break
      case 'device':
        showDeviceLabels.value = !showDeviceLabels.value
        break
      case 'path':
        showPathLines.value = !showPathLines.value
        break
    }
  }

  function toggleFullscreen() {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen()
      isFullscreen.value = true
    } else {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }

  function toggleTutorial() {
    showTutorial.value = !showTutorial.value
  }

  function toggleModelInfo() {
    showModelInfo.value = !showModelInfo.value
  }

  return {
    currentZone,
    showLocationLabels,
    showDeviceLabels,
    showPathLines,
    isFullscreen,
    showTutorial,
    showModelInfo,
    zoneNames,
    setCurrentZone,
    toggleLabel,
    toggleFullscreen,
    toggleTutorial,
    toggleModelInfo,
  }
})
