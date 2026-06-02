<template>
  <div class="status-bar">
    <div class="status-item">
      <span class="label">状态</span>
      <span :class="['value', 'status', statusClass]">
        <span class="dot"></span>
        {{ statusText }}
      </span>
    </div>
    <div class="status-item">
      <span class="label">电量</span>
      <div class="battery-display">
        <div class="battery-bar">
          <div class="battery-fill" :style="{ width: batteryLevel + '%' }"></div>
        </div>
        <span class="value">{{ batteryLevel.toFixed(0) }}%</span>
      </div>
    </div>
    <div class="status-item">
      <span class="label">举升高度</span>
      <span class="value">{{ (liftHeight * 1200).toFixed(0) }} mm</span>
    </div>
    <div class="status-item">
      <span class="label">位置</span>
      <span class="value">X: {{ position.x.toFixed(2) }} Z: {{ position.z.toFixed(2) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RobotState } from '../types'

const props = defineProps<{
  robotState: RobotState
}>()

const batteryLevel = computed(() => props.robotState.batteryLevel)
const liftHeight = computed(() => props.robotState.liftHeight)
const position = computed(() => props.robotState.position)

const statusClass = computed(() => {
  const state = props.robotState.currentAnimation
  if (state === 'charging') return 'charging'
  if (state === 'moving' || state === 'turning') return 'moving'
  if (state === 'lifting') return 'lifting'
  if (state === 'avoiding') return 'avoiding'
  return 'idle'
})

const statusText = computed(() => {
  const texts: Record<string, string> = {
    idle: '待机中',
    moving: '行驶中',
    turning: '转向中',
    lifting: '顶升中',
    charging: '充电中',
    avoiding: '避障中'
  }
  return texts[props.robotState.currentAnimation] || '未知'
})
</script>

<style scoped>
.status-bar {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 40px;
  padding: 16px 32px;
  background: var(--bg-panel);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  z-index: 100;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 100px;
}

.status-item .label {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-item .value {
  font-size: 14px;
  font-weight: 600;
  font-family: 'Consolas', monospace;
}

.status-item .value.status {
  display: flex;
  align-items: center;
  gap: 8px;
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

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.battery-display {
  display: flex;
  align-items: center;
  gap: 10px;
}

.battery-bar {
  width: 80px;
  height: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

.battery-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--success-color), var(--warning-color));
  border-radius: 6px;
  transition: width 0.3s;
}
</style>
