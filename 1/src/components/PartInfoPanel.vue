<template>
  <Transition name="slide">
    <div v-if="part" class="part-info-panel">
      <div class="panel-header">
        <h3>{{ part.name }}</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      <div class="panel-body">
        <p class="description">{{ part.description }}</p>
        
        <div class="category-tag">
          <span :class="['tag', part.category]">{{ getCategoryName(part.category) }}</span>
        </div>

        <div v-if="partInfo?.specs" class="specs-section">
          <h4>技术参数</h4>
          <div class="specs-list">
            <div v-for="(value, key) in partInfo.specs" :key="key" class="spec-item">
              <span class="spec-key">{{ key }}</span>
              <span class="spec-value">{{ value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RobotPart } from '../types'
import { robotPartsInfo } from '../data/mockData'

const props = defineProps<{
  part: RobotPart | null
}>()

defineEmits<{
  close: []
}>()

const partInfo = computed(() => {
  if (!props.part) return null
  return robotPartsInfo.find(p => p.id === props.part?.id || props.part?.id.startsWith(p.id))
})

const getCategoryName = (category: string) => {
  const names: Record<string, string> = {
    structure: '结构件',
    motion: '运动系统',
    mechanism: '机械机构',
    sensor: '传感器',
    indicator: '指示灯',
    power: '电源系统',
    payload: '负载系统'
  }
  return names[category] || category
}
</script>

<style scoped>
.part-info-panel {
  position: absolute;
  right: 20px;
  top: 20px;
  width: 320px;
  background: var(--bg-panel);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  overflow: hidden;
  z-index: 100;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateX(100px);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, var(--primary-color), #096dd9);
  border-bottom: 1px solid var(--border-color);
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  color: white;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.panel-body {
  padding: 20px;
}

.description {
  margin: 0 0 16px 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.category-tag {
  margin-bottom: 20px;
}

.tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.tag.structure {
  background: rgba(24, 144, 255, 0.2);
  color: #1890ff;
}

.tag.motion {
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
}

.tag.mechanism {
  background: rgba(250, 173, 20, 0.2);
  color: #faad14;
}

.tag.sensor {
  background: rgba(114, 46, 209, 0.2);
  color: #722ed1;
}

.tag.indicator {
  background: rgba(255, 77, 79, 0.2);
  color: #ff4d4f;
}

.tag.power {
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
}

.tag.payload {
  background: rgba(19, 194, 194, 0.2);
  color: #13c2c2;
}

.specs-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.specs-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spec-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
}

.spec-key {
  font-size: 13px;
  color: var(--text-secondary);
}

.spec-value {
  font-size: 13px;
  font-weight: 600;
  font-family: 'Consolas', monospace;
}
</style>
