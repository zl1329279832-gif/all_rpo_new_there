<template>
  <div class="status-bar">
    <div class="status-group">
      <div class="status-item">
        <span class="label">运行状态</span>
        <span :class="['value', 'status', statusClass]">
          <span class="dot"></span>
          {{ statusText }}
        </span>
      </div>
      <div class="status-item">
        <span class="label">任务进度</span>
        <div class="progress-display">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: taskProgress + '%' }"></div>
          </div>
          <span class="value">{{ taskProgress.toFixed(0) }}%</span>
        </div>
      </div>
    </div>
    
    <div class="status-group">
      <div class="status-item">
        <span class="label">电量</span>
        <div class="battery-display">
          <div class="battery-bar">
            <div class="battery-fill" :class="batteryClass" :style="{ width: batteryLevel + '%' }"></div>
          </div>
          <span class="value">{{ batteryLevel.toFixed(0) }}%</span>
        </div>
      </div>
      <div class="status-item">
        <span class="label">举升高度</span>
        <span class="value">{{ (liftHeight * 1200).toFixed(0) }} mm</span>
      </div>
      <div class="status-item">
        <span class="label">行驶速度</span>
        <span class="value">{{ speed.toFixed(2) }} m/s</span>
      </div>
    </div>

    <div class="status-group">
      <div class="status-item">
        <span class="label">位置</span>
        <span class="value mono">X: {{ position.x.toFixed(2) }} Z: {{ position.z.toFixed(2) }}</span>
      </div>
      <div class="status-item">
        <span class="label">朝向</span>
        <span class="value mono">{{ rotation.toFixed(1) }}°</span>
      </div>
    </div>

    <div class="status-group performance" v-if="showPerformance">
      <div class="status-item">
        <span class="label">帧率</span>
        <span :class="['value', 'mono', fpsClass]">{{ performance.fps.toFixed(0) }} FPS</span>
      </div>
      <div class="status-item">
        <span class="label">Draw Calls</span>
        <span class="value mono">{{ performance.drawCalls }}</span>
      </div>
      <div class="status-item">
        <span class="label">三角形</span>
        <span class="value mono">{{ formatNumber(performance.triangles) }}</span>
      </div>
      <div class="status-item">
        <span class="label">内存</span>
        <span class="value mono">{{ formatMemory(performance.memory) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RobotState, PerformanceStats } from '../types'

const props = defineProps<{
  robotState: RobotState
  performance?: PerformanceStats
  showPerformance?: boolean
}>()

const batteryLevel = computed(() => props.robotState.batteryLevel)
const liftHeight = computed(() => props.robotState.liftHeight)
const position = computed(() => props.robotState.position)
const rotation = computed(() => (props.robotState.rotation * 180 / Math.PI + 360) % 360)
const speed = computed(() => props.robotState.speed || 0)
const taskProgress = computed(() => props.robotState.taskProgress || 0)
const performance = computed(() => props.performance || { fps: 60, drawCalls: 0, triangles: 0, memory: 0 })

const batteryClass = computed(() => {
  if (batteryLevel.value > 60) return 'high'
  if (batteryLevel.value > 30) return 'medium'
  return 'low'
})

const statusClass = computed(() => {
  const state = props.robotState.currentAnimation
  if (state === 'charging') return 'charging'
  if (state === 'moving' || state === 'turning' || state === 'pickingUp' || state === 'droppingOff') return 'moving'
  if (state === 'lifting' || state === 'lowering') return 'lifting'
  if (state === 'avoiding') return 'avoiding'
  if (state === 'fault') return 'fault'
  if (state === 'paused') return 'paused'
  return 'idle'
})

const statusText = computed(() => {
  const texts: Record<string, string> = {
    idle: '待机中',
    moving: '行驶中',
    turning: '转向中',
    lifting: '顶升中',
    lowering: '下降中',
    charging: '充电中',
    avoiding: '避障中',
    pickingUp: '取货中',
    droppingOff: '放货中',
    returning: '返航中',
    fault: '故障',
    paused: '已暂停'
  }
  return texts[props.robotState.currentAnimation] || '未知'
})

const fpsClass = computed(() => {
  if (performance.value.fps >= 50) return 'fps-high'
  if (performance.value.fps >= 30) return 'fps-medium'
  return 'fps-low'
})

const formatNumber = (num: number) => {
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

const formatMemory = (bytes: number) => {
  if (bytes >= 1073741824) return (bytes / 1073741824).toFixed(2) + ' GB'
  if (bytes >= 1048576) return (bytes / 1048576).toFixed(1) + ' MB'
  if (bytes >= 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return bytes + ' B'
}
</script>

<style scoped>
.status-bar {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 24px;
  padding: 12px 24px;
  background: var(--bg-panel);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  z-index: 100;
  flex-wrap: wrap;
  justify-content: center;
  max-width: 95%;
}

.status-group {
  display: flex;
  gap: 20px;
  align-items: center;
}

.status-group.performance {
  border-left: 1px solid var(--border-color);
  padding-left: 20px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 80px;
}

.status-item .label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-item .value {
  font-size: 13px;
  font-weight: 600;
}

.status-item .value.mono {
  font-family: 'Consolas', monospace;
}

.status-item .value.status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status.idle .dot {
  background: var(--text-secondary);
  animation: none;
}

.status.moving .dot {
  background: var(--primary-color);
}

.status.charging .dot {
  background: var(--success-color);
}

.status.lifting .dot {
  background: var(--warning-color);
}

.status.avoiding .dot {
  background: var(--danger-color);
}

.status.fault .dot {
  background: var(--danger-color);
  animation: fastPulse 0.5s infinite;
}

.status.paused .dot {
  background: var(--warning-color);
  animation: none;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

@keyframes fastPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.battery-display,
.progress-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.battery-bar,
.progress-bar {
  width: 60px;
  height: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 5px;
  overflow: hidden;
}

.battery-fill,
.progress-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s;
}

.battery-fill.high {
  background: linear-gradient(90deg, var(--success-color), #4ade80);
}

.battery-fill.medium {
  background: linear-gradient(90deg, var(--warning-color), #fbbf24);
}

.battery-fill.low {
  background: linear-gradient(90deg, var(--danger-color), #ef4444);
}

.progress-fill {
  background: linear-gradient(90deg, var(--primary-color), #60a5fa);
}

.fps-high {
  color: var(--success-color);
}

.fps-medium {
  color: var(--warning-color);
}

.fps-low {
  color: var(--danger-color);
}
</style>
