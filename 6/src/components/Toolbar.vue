<script setup lang="ts">
import { useSceneStore } from '../store/useSceneStore'
import { useDeviceStore } from '../store/useDeviceStore'
import type { ZoneType } from '../types'

const sceneStore = useSceneStore()
const deviceStore = useDeviceStore()

const emit = defineEmits<{
  resetCamera: []
  playAnimation: []
  playInbound: []
}>()

const zones: { value: ZoneType; label: string; icon: string }[] = [
  { value: 'inbound', label: '入库区', icon: '📥' },
  { value: 'storage', label: '存储区', icon: '📦' },
  { value: 'outbound', label: '出库区', icon: '📤' },
  { value: 'picking', label: '拣选区', icon: '🛒' },
]

function handleZoneChange(zone: ZoneType) {
  sceneStore.setCurrentZone(zone)
}

function handleSpeedChange(delta: number) {
  const newSpeed = deviceStore.animationSpeed + delta
  deviceStore.setAnimationSpeed(newSpeed)
}
</script>

<template>
  <div class="toolbar">
    <div class="toolbar-section">
      <h3 class="section-title">视角控制</h3>
      <div class="button-group">
        <button class="toolbar-btn" @click="emit('resetCamera')" title="重置视角">
          <span class="icon">🎯</span>
          <span class="text">重置</span>
        </button>
      </div>
    </div>

    <div class="toolbar-section">
      <h3 class="section-title">区域切换</h3>
      <div class="zone-buttons">
        <button
          v-for="zone in zones"
          :key="zone.value"
          class="zone-btn"
          :class="{ active: sceneStore.currentZone === zone.value }"
          @click="handleZoneChange(zone.value)"
        >
          <span class="icon">{{ zone.icon }}</span>
          <span class="text">{{ zone.label }}</span>
        </button>
      </div>
    </div>

    <div class="toolbar-section">
      <h3 class="section-title">动画控制</h3>
      <div class="button-group">
        <button class="toolbar-btn" @click="emit('playAnimation')" title="播放移库动画">
          <span class="icon">🔄</span>
          <span class="text">移库</span>
        </button>
        <button class="toolbar-btn" @click="emit('playInbound')" title="播放入库动画">
          <span class="icon">📥</span>
          <span class="text">入库</span>
        </button>
      </div>
      <div class="speed-control">
        <span class="speed-label">速度</span>
        <button class="speed-btn" @click="handleSpeedChange(-0.25)">-</button>
        <span class="speed-value">{{ deviceStore.animationSpeed.toFixed(2) }}x</span>
        <button class="speed-btn" @click="handleSpeedChange(0.25)">+</button>
      </div>
    </div>

    <div class="toolbar-section">
      <h3 class="section-title">显示设置</h3>
      <div class="toggle-group">
        <label class="toggle-item">
          <input type="checkbox" v-model="sceneStore.showLocationLabels" />
          <span>库位标签</span>
        </label>
        <label class="toggle-item">
          <input type="checkbox" v-model="sceneStore.showDeviceLabels" />
          <span>设备标签</span>
        </label>
        <label class="toggle-item">
          <input type="checkbox" v-model="sceneStore.showPathLines" />
          <span>路径显示</span>
        </label>
      </div>
    </div>

    <div class="toolbar-section">
      <h3 class="section-title">帮助</h3>
      <div class="button-group">
        <button class="toolbar-btn" @click="sceneStore.toggleTutorial()">
          <span class="icon">📖</span>
          <span class="text">教程</span>
        </button>
        <button class="toolbar-btn" @click="sceneStore.toggleModelInfo()">
          <span class="icon">ℹ️</span>
          <span class="text">说明</span>
        </button>
      </div>
    </div>

    <div class="toolbar-section">
      <div class="button-group">
        <button class="toolbar-btn fullscreen" @click="sceneStore.toggleFullscreen()">
          <span class="icon">⛶</span>
          <span class="text">{{ sceneStore.isFullscreen ? '退出' : '全屏' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.toolbar {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 100;
}

.toolbar-section {
  background: rgba(20, 20, 30, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px;
  min-width: 160px;

  .section-title {
    color: #86909C;
    font-size: 11px;
    font-weight: 500;
    margin: 0 0 8px 0;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(22, 93, 255, 0.3);
    border-color: rgba(22, 93, 255, 0.5);
  }

  &:active {
    transform: scale(0.98);
  }

  .icon {
    font-size: 16px;
  }

  .text {
    white-space: nowrap;
  }

  &.fullscreen {
    width: 100%;
    justify-content: center;
  }
}

.zone-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.zone-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #fff;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  &.active {
    background: rgba(22, 93, 255, 0.4);
    border-color: #165DFF;
  }

  .icon {
    font-size: 18px;
  }
}

.speed-control {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;

  .speed-label {
    color: #86909C;
    font-size: 12px;
  }

  .speed-btn {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: rgba(22, 93, 255, 0.5);
    }
  }

  .speed-value {
    color: #165DFF;
    font-weight: 600;
    font-size: 13px;
    min-width: 45px;
    text-align: center;
  }
}

.toggle-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toggle-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #C9CDD4;
  font-size: 12px;
  cursor: pointer;

  input[type='checkbox'] {
    width: 16px;
    height: 16px;
    accent-color: #165DFF;
  }
}
</style>
