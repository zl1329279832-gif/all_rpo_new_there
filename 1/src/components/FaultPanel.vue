<template>
  <div class="fault-panel" :class="severityClass">
    <div class="fault-header">
      <div class="fault-icon">
        <span v-if="fault.severity === 'critical'">🚨</span>
        <span v-else>⚠️</span>
      </div>
      <div class="fault-title">
        <h3>{{ fault.severity === 'critical' ? '严重故障' : '警告' }}</h3>
        <p class="fault-time">{{ formatTime(fault.timestamp) }}</p>
      </div>
      <button class="close-btn" @click="$emit('clear')">×</button>
    </div>
    
    <div class="fault-content">
      <div class="fault-message">
        <span class="label">故障信息</span>
        <span class="value">{{ fault.message }}</span>
      </div>
      
      <div class="fault-type">
        <span class="label">故障类型</span>
        <span class="value">{{ typeText }}</span>
      </div>
      
      <div class="fault-parts" v-if="fault.affectedParts && fault.affectedParts.length > 0">
        <span class="label">受影响部件</span>
        <div class="parts-list">
          <span v-for="part in fault.affectedParts" :key="part" class="part-tag">
            {{ getPartName(part) }}
          </span>
        </div>
      </div>
    </div>
    
    <div class="fault-actions">
      <button class="action-btn primary" @click="$emit('clear')">
        清除故障
      </button>
      <button class="action-btn secondary" @click="$emit('maintenance')">
        进入维修模式
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FaultState } from '../types'

const props = defineProps<{
  fault: FaultState
}>()

defineEmits<{
  clear: []
  maintenance: []
}>()

const severityClass = computed(() => ({
  critical: props.fault.severity === 'critical',
  warning: props.fault.severity === 'warning'
}))

const typeText = computed(() => {
  const types: Record<string, string> = {
    motor: '电机故障',
    sensor: '传感器故障',
    power: '电源故障',
    navigation: '导航故障',
    mechanical: '机械故障'
  }
  return types[props.fault.type] || '未知故障'
})

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getPartName = (partId: string) => {
  const names: Record<string, string> = {
    chassis: '底盘',
    wheel_0: '驱动轮1',
    wheel_1: '驱动轮2',
    wheel_2: '驱动轮3',
    wheel_3: '驱动轮4',
    liftMechanism: '升降机构',
    lidar: '激光雷达',
    frontCamera: '前置摄像头',
    rearCamera: '后置摄像头',
    batteryCompartment: '电池仓',
    chargingContacts: '充电触点',
    outerShell: '外壳',
    payloadTray: '货架托盘'
  }
  return names[partId] || partId
}
</script>

<style scoped>
.fault-panel {
  position: absolute;
  top: 80px;
  right: 20px;
  width: 320px;
  background: var(--bg-panel);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  z-index: 200;
  animation: slideIn 0.3s ease;
}

.fault-panel.critical {
  border-color: var(--danger-color);
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

.fault-panel.warning {
  border-color: var(--warning-color);
  box-shadow: 0 0 20px rgba(251, 191, 36, 0.3);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.fault-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05));
  border-bottom: 1px solid var(--border-color);
}

.fault-panel.warning .fault-header {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.1), rgba(251, 191, 36, 0.05));
}

.fault-icon {
  font-size: 28px;
}

.fault-title {
  flex: 1;
}

.fault-title h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--danger-color);
}

.fault-panel.warning .fault-title h3 {
  color: var(--warning-color);
}

.fault-time {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  border-radius: 6px;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: var(--text-primary);
}

.fault-content {
  padding: 16px;
}

.fault-message,
.fault-type,
.fault-parts {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.fault-parts {
  margin-bottom: 0;
}

.fault-message .label,
.fault-type .label,
.fault-parts .label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.fault-message .value,
.fault-type .value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.parts-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.part-tag {
  padding: 4px 10px;
  background: rgba(239, 68, 68, 0.15);
  color: var(--danger-color);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.fault-panel.warning .part-tag {
  background: rgba(251, 191, 36, 0.15);
  color: var(--warning-color);
}

.fault-actions {
  display: flex;
  gap: 10px;
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.action-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.primary {
  background: var(--danger-color);
  color: white;
}

.action-btn.primary:hover {
  background: #dc2626;
}

.fault-panel.warning .action-btn.primary {
  background: var(--warning-color);
  color: #1f2937;
}

.fault-panel.warning .action-btn.primary:hover {
  background: #f59e0b;
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.action-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}
</style>
