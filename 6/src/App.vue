<script setup lang="ts">
import { ref, onMounted } from 'vue'
import WarehouseScene from './components/WarehouseScene.vue'
import Toolbar from './components/Toolbar.vue'
import InfoPanel from './components/InfoPanel.vue'
import LocationModal from './components/LocationModal.vue'
import TutorialPanel from './components/TutorialPanel.vue'

const showTutorial = ref(false)

onMounted(() => {
  showTutorial.value = true
})

const handleCloseTutorial = () => {
  showTutorial.value = false
}

const handleShowTutorial = () => {
  showTutorial.value = true
}
</script>

<template>
  <div class="app-container">
    <header class="app-header">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 7L12 3L4 7V17L12 21L20 17V7Z" stroke="#00d4ff" stroke-width="2"/>
            <path d="M12 12L20 7M12 12V21M12 12L4 7" stroke="#00d4ff" stroke-width="2"/>
          </svg>
        </div>
        <div class="title">
          <h1>自动化立体仓库</h1>
          <p>3D 可视化管理系统</p>
        </div>
      </div>
      <div class="header-right">
        <div class="status-indicator">
          <span class="status-dot running"></span>
          <span class="status-text">系统运行中</span>
        </div>
        <div class="time-display" id="current-time"></div>
      </div>
    </header>

    <main class="app-main">
      <Toolbar @show-tutorial="handleShowTutorial" />
      <WarehouseScene />
      <InfoPanel />
    </main>

    <LocationModal />
    
    <TutorialPanel 
      v-if="showTutorial" 
      @close="handleCloseTutorial" 
    />
  </div>
</template>

<style scoped>
.app-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f2e 100%);
  overflow: hidden;
}

.app-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(10, 14, 23, 0.9);
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  backdrop-filter: blur(10px);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo svg {
  width: 36px;
  height: 36px;
}

.title h1 {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
  letter-spacing: 1px;
}

.title p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin: 2px 0 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-dot.running {
  background: #00ff88;
  box-shadow: 0 0 10px #00ff88;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

.status-text {
  font-size: 12px;
  color: #00ff88;
}

.time-display {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-family: 'Courier New', monospace;
}

.app-main {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}
</style>
