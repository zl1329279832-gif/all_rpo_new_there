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
          <span>维护状态</span>
        </label>
      </div>
    </div>

    <div class="panel-section">
      <h3>路径控制</h3>
      <div class="path-controls">
        <button :class="['path-btn', { active: pathActive }]" @click="$emit('togglePath')">
          {{ pathActive ? '停止路径' : '开始路径' }}
        </button>
        <button class="path-btn" @click="$emit('resetPath')">重置位置</button>
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
import { animationPresets } from '../data/mockData'

defineProps<{
  currentAnimation: string
  explodedView: boolean
  transparentShell: boolean
  maintenanceMode: boolean
  pathActive: boolean
}>()

defineEmits<{
  playAnimation: [animId: string]
  toggleExploded: []
  toggleTransparent: []
  toggleMaintenance: []
  togglePath: []
  resetPath: []
  showTutorial: []
}>()

const animations = animationPresets
</script>

<style scoped>
.control-panel {
  position: absolute;
  left: 20px;
  top: 20px;
  width: 280px;
  background: var(--bg-panel);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  overflow: hidden;
  z-index: 100;
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
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.animation-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.anim-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
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
  font-size: 20px;
}

.anim-btn .name {
  font-size: 11px;
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
  font-size: 14px;
}

.toggle-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: var(--primary-color);
}

.path-controls {
  display: flex;
  gap: 8px;
}

.path-btn {
  flex: 1;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 13px;
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

.tutorial-btn {
  width: 100%;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--warning-color), #d48806);
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.tutorial-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.4);
}
</style>
