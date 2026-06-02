<template>
  <div class="control-panel">
    <div class="panel-header">
      <h2>控制面板</h2>
    </div>

    <div class="panel-section">
      <h3>动画演示</h3>
      <div class="animation-buttons">
        <button
          v-for="anim in animations"
          :key="anim.id"
          :class="['anim-btn', { active: currentAnimation === anim.id }]"
          @click="$emit('playAnimation', anim.id)"
          :title="anim.description"
        >
          <span class="icon">{{ anim.icon }}</span>
          <span class="name">{{ anim.name }}</span>
        </button>
      </div>
    </div>

    <div class="panel-section">
      <h3>视图模式</h3>
      <div class="view-toggles">
        <label class="toggle-item">
          <input type="checkbox" :checked="explodedView" @change="$emit('toggleExploded')" />
          <span>爆炸视图</span>
        </label>
        <label class="toggle-item">
          <input type="checkbox" :checked="transparentShell" @change="$emit('toggleTransparent')" />
          <span>透明外壳</span>
        </label>
        <label class="toggle-item">
          <input type="checkbox" :checked="maintenanceMode" @change="$emit('toggleMaintenance')" />
          <span>维护模式</span>
        </label>
        <label class="toggle-item">
          <input type="checkbox" :checked="sensorViz" @change="$emit('toggleSensorViz')" />
          <span>传感器可视化</span>
        </label>
        <label class="toggle-item">
          <input type="checkbox" :checked="showTrajectory" @change="$emit('toggleTrajectory')" />
          <span>运动轨迹</span>
        </label>
      </div>
    </div>

    <div class="panel-section">
      <h3>播放控制</h3>
      <div class="playback-controls">
        <button class="play-btn" @click="$emit('pauseAnimation')" title="暂停">
          ⏸️
        </button>
        <button class="play-btn" @click="$emit('resumeAnimation')" title="继续">
          ▶️
        </button>
        <div class="speed-control">
          <span class="speed-label">速度</span>
          <input 
            type="range" 
            :value="animationSpeed * 100" 
            @input="handleSpeedChange"
            min="10" 
            max="300" 
            step="10"
          />
          <span class="speed-value">{{ animationSpeed.toFixed(1) }}x</span>
        </div>
      </div>
    </div>

    <div class="panel-section">
      <h3>路径控制</h3>
      <div class="path-controls">
        <button :class="['path-btn', { active: pathActive }]" @click="$emit('togglePath')">
          {{ pathActive ? '停止路径' : '开始路径' }}
        </button>
        <button class="path-btn" @click="$emit('resetPath')">重置位置</button>
        <button class="path-btn secondary" @click="$emit('clearTrajectory')">清除轨迹</button>
      </div>
    </div>

    <div class="panel-section">
      <h3>业务演示</h3>
      <div class="demo-controls">
        <button class="demo-btn" @click="$emit('runFullDemo')">
          🚀 完整业务流程
        </button>
        <button class="demo-btn secondary" @click="$emit('followPickupPath')">
          📦 取货演示
        </button>
      </div>
    </div>

    <div class="panel-section">
      <h3>画质设置</h3>
      <div class="quality-controls">
        <button 
          v-for="level in qualityLevels"
          :key="level.value"
          :class="['quality-btn', { active: qualityLevel === level.value }]"
          @click="$emit('setQuality', level.value)"
        >
          {{ level.label }}
        </button>
      </div>
    </div>

    <div class="panel-section">
      <h3>故障模拟</h3>
      <div class="fault-controls">
        <button 
          v-for="fault in faults"
          :key="fault.code"
          class="fault-btn"
          :class="{ [fault.severity]: true }"
          @click="$emit('triggerFault', fault.code)"
        >
          {{ fault.label }}
        </button>
        <button class="fault-btn clear" @click="$emit('clearFault')">
          清除故障
        </button>
      </div>
    </div>

    <div class="panel-section">
      <h3>教程</h3>
      <button class="tutorial-btn" @click="$emit('showTutorial')">
        📖 查看使用教程
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { animationPresets, faultTypes } from '../data/mockData'

defineProps<{
  currentAnimation: string
  explodedView: boolean
  transparentShell: boolean
  maintenanceMode: boolean
  pathActive: boolean
  sensorViz: boolean
  showTrajectory: boolean
  animationSpeed: number
  qualityLevel: 'low' | 'medium' | 'high'
}>()

const emit = defineEmits<{
  playAnimation: [animId: string]
  toggleExploded: []
  toggleTransparent: []
  toggleMaintenance: []
  togglePath: []
  resetPath: []
  toggleSensorViz: []
  toggleTrajectory: []
  clearTrajectory: []
  setSpeed: [speed: number]
  setQuality: [quality: 'low' | 'medium' | 'high']
  triggerFault: [faultType: string]
  clearFault: []
  pauseAnimation: []
  resumeAnimation: []
  showTutorial: []
  runFullDemo: []
  followPickupPath: []
}>()

const animations = animationPresets

const qualityLevels = [
  { value: 'low' as const, label: '低' },
  { value: 'medium' as const, label: '中' },
  { value: 'high' as const, label: '高' }
]

const faults = faultTypes.map(f => ({
  code: f.type,
  label: f.message,
  severity: f.severity
}))

const handleSpeedChange = (event: Event) => {
  const value = Number((event.target as HTMLInputElement).value)
  emit('setSpeed', value / 100)
}
</script>

<style scoped>
.control-panel {
  position: absolute;
  left: 20px;
  top: 20px;
  width: 300px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  background: var(--bg-panel);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  z-index: 100;
}

.control-panel::-webkit-scrollbar {
  width: 6px;
}

.control-panel::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.control-panel::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.panel-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, var(--primary-color), #096dd9);
  border-bottom: 1px solid var(--border-color);
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.panel-section {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.panel-section:last-child {
  border-bottom: none;
}

.panel-section h3 {
  margin: 0 0 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.animation-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.anim-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 6px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s;
}

.anim-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--primary-color);
}

.anim-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.anim-btn .icon {
  font-size: 18px;
}

.anim-btn .name {
  font-size: 10px;
}

.view-toggles {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toggle-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 13px;
}

.toggle-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: var(--primary-color);
}

.playback-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.playback-controls > div:first-child {
  display: flex;
  gap: 8px;
}

.play-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.play-btn:hover {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.speed-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.speed-label {
  font-size: 12px;
  color: var(--text-secondary);
  min-width: 40px;
}

.speed-control input[type="range"] {
  flex: 1;
  height: 4px;
  accent-color: var(--primary-color);
}

.speed-value {
  font-size: 12px;
  font-weight: 600;
  min-width: 45px;
  text-align: right;
}

.path-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.path-btn {
  flex: 1;
  min-width: 80px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.path-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--primary-color);
}

.path-btn.active {
  background: var(--success-color);
  border-color: var(--success-color);
}

.path-btn.secondary {
  background: rgba(255, 255, 255, 0.03);
}

.demo-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.demo-btn {
  width: 100%;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--primary-color), #096dd9);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.demo-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.demo-btn.secondary {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.demo-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.12);
}

.quality-controls {
  display: flex;
  gap: 8px;
}

.quality-btn {
  flex: 1;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.quality-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.quality-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.fault-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.fault-btn {
  flex: 1;
  min-width: 80px;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: white;
  cursor: pointer;
  font-size: 11px;
  transition: all 0.2s;
}

.fault-btn.warning {
  background: var(--warning-color);
}

.fault-btn.error {
  background: var(--error-color);
}

.fault-btn.critical {
  background: #dc2626;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.fault-btn.clear {
  background: var(--success-color);
  width: 100%;
}

.fault-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.tutorial-btn {
  width: 100%;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--warning-color), #d48806);
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.tutorial-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.4);
}
</style>
