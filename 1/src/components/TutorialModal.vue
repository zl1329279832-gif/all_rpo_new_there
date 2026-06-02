<template>
  <Transition name="fade">
    <div v-if="visible" class="tutorial-overlay" @click.self="$emit('close')">
      <div class="tutorial-modal">
        <div class="modal-header">
          <h2>使用教程</h2>
          <button class="close-btn" @click="$emit('close')">×</button>
        </div>
        
        <div class="modal-body">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: ((currentStep + 1) / steps.length) * 100 + '%' }"></div>
          </div>
          
          <div class="step-indicator">
            {{ currentStep + 1 }} / {{ steps.length }}
          </div>

          <div class="step-content">
            <h3>{{ steps[currentStep].title }}</h3>
            <p>{{ steps[currentStep].content }}</p>
          </div>
        </div>

        <div class="modal-footer">
          <button 
            class="nav-btn prev" 
            :disabled="currentStep === 0"
            @click="currentStep--"
          >
            上一步
          </button>
          <button 
            v-if="currentStep < steps.length - 1"
            class="nav-btn next"
            @click="currentStep++"
          >
            下一步
          </button>
          <button 
            v-else
            class="nav-btn finish"
            @click="$emit('close')"
          >
            完成
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { tutorialSteps } from '../data/mockData'

const props = defineProps<{
  visible: boolean
}>()

defineEmits<{
  close: []
}>()

const currentStep = ref(0)
const steps = tutorialSteps

watch(() => props.visible, (val) => {
  if (val) {
    currentStep.value = 0
  }
})
</script>

<style scoped>
.tutorial-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.tutorial-modal {
  width: 480px;
  background: var(--bg-panel);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: white;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 24px;
}

.progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), #096dd9);
  border-radius: 2px;
  transition: width 0.3s;
}

.step-indicator {
  text-align: right;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.step-content h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
}

.step-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  padding: 16px 24px 24px;
  gap: 12px;
}

.nav-btn {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.nav-btn.prev {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.nav-btn.prev:not(:disabled):hover {
  background: rgba(255, 255, 255, 0.2);
}

.nav-btn.next {
  background: var(--primary-color);
  color: white;
}

.nav-btn.next:hover {
  background: #096dd9;
}

.nav-btn.finish {
  background: var(--success-color);
  color: white;
}

.nav-btn.finish:hover {
  background: #389e0d;
}
</style>
