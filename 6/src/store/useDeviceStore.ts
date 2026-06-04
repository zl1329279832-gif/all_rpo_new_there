import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { DeviceData, TaskData } from '../types'
import { deviceData, activeTasks } from '../data/devices'

export const useDeviceStore = defineStore('device', () => {
  const devices = ref<DeviceData[]>([...deviceData])
  const tasks = ref<TaskData[]>([...activeTasks])
  const selectedDeviceId = ref<string | null>(null)
  const animationSpeed = ref(1)
  const isAnimationPlaying = ref(false)

  const selectedDevice = computed(() => {
    if (!selectedDeviceId.value) return null
    return devices.value.find(d => d.id === selectedDeviceId.value) || null
  })

  const stackers = computed(() => devices.value.filter(d => d.type === 'stacker'))
  const conveyors = computed(() => devices.value.filter(d => d.type === 'conveyor'))
  const elevators = computed(() => devices.value.filter(d => d.type === 'elevator'))
  const scanners = computed(() => devices.value.filter(d => d.type === 'scanner'))

  const deviceStats = computed(() => {
    const total = devices.value.length
    const running = devices.value.filter(d => d.status === 'running').length
    const idle = devices.value.filter(d => d.status === 'idle').length
    const error = devices.value.filter(d => d.status === 'error').length
    const maintenance = devices.value.filter(d => d.status === 'maintenance').length
    return { total, running, idle, error, maintenance }
  })

  function selectDevice(id: string | null) {
    selectedDeviceId.value = id
  }

  function clearSelection() {
    selectedDeviceId.value = null
  }

  function updateDeviceStatus(id: string, status: DeviceData['status']) {
    const device = devices.value.find(d => d.id === id)
    if (device) {
      device.status = status
    }
  }

  function setAnimationSpeed(speed: number) {
    animationSpeed.value = Math.max(0.25, Math.min(4, speed))
  }

  function toggleAnimationPlaying() {
    isAnimationPlaying.value = !isAnimationPlaying.value
  }

  function addTask(task: TaskData) {
    tasks.value.push(task)
  }

  function updateTaskProgress(taskId: string, progress: number) {
    const task = tasks.value.find(t => t.id === taskId)
    if (task) {
      task.progress = progress
    }
  }

  return {
    devices,
    tasks,
    selectedDeviceId,
    selectedDevice,
    animationSpeed,
    isAnimationPlaying,
    stackers,
    conveyors,
    elevators,
    scanners,
    deviceStats,
    selectDevice,
    clearSelection,
    updateDeviceStatus,
    setAnimationSpeed,
    toggleAnimationPlaying,
    addTask,
    updateTaskProgress,
  }
})
