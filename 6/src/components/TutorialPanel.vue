<script setup lang="ts">
import { ref } from 'vue'
import { useSceneStore } from '../store/useSceneStore'

const sceneStore = useSceneStore()

const activeTab = ref<'operation' | 'model'>('operation')

const operationSteps = [
  {
    icon: '🖱️',
    title: '鼠标左键拖拽',
    description: '按住鼠标左键并拖拽，可以旋转场景视角，从不同方向观察仓库'
  },
  {
    icon: '🔍',
    title: '鼠标滚轮缩放',
    description: '滚动鼠标滚轮，可以放大或缩小场景，查看细节或全景'
  },
  {
    icon: '✋',
    title: '鼠标右键平移',
    description: '按住鼠标右键并拖拽，可以平移整个场景'
  },
  {
    icon: '👆',
    title: '点击库位',
    description: '点击库位标记（绿色/橙色方块），可以查看该库位的详细信息和库存情况'
  },
  {
    icon: '🔄',
    title: '播放动画',
    description: '在左侧工具栏点击动画按钮，可以观看堆垛机的取放货作业演示'
  },
  {
    icon: '🗺️',
    title: '区域切换',
    description: '点击区域按钮，相机会自动移动到对应区域，便于快速查看'
  }
]

const modelComponents = [
  {
    name: '高位货架',
    icon: '🏗️',
    description: '采用蓝色钢结构，6层8列设计，每层承重500kg，总高4.8米，共8组货架，384个库位'
  },
  {
    name: '堆垛机',
    icon: '🤖',
    description: '巷道式堆垛机，具备水平行走、垂直升降、货叉伸缩三种运动，实现自动存取货物'
  },
  {
    name: '输送线',
    icon: '🔄',
    description: '滚筒式输送线，负责货物的水平运输，连接入库区、存储区和出库区'
  },
  {
    name: '提升机',
    icon: '⬆️',
    description: '负责不同楼层之间的货物垂直运输，提升高度8米'
  },
  {
    name: '托盘与货箱',
    icon: '📦',
    description: '九脚塑料托盘配合瓦楞纸箱，托盘尺寸1000×1000×140mm，货箱带有封箱胶带和标签'
  },
  {
    name: '扫码设备',
    icon: '📷',
    description: '固定扫描枪，出入库时自动扫描货物条码，记录物流信息'
  },
  {
    name: '安全围栏',
    icon: '🚧',
    description: '金属网格围栏，划分作业区域，确保人员安全，设有进出安全门'
  },
  {
    name: '控制柜',
    icon: '🖥️',
    description: 'PLC控制系统，负责设备的逻辑控制和状态监控，配备工业显示屏和操作按钮'
  }
]

function handleClose() {
  sceneStore.toggleTutorial()
}

function handleOverlayClick(event: MouseEvent) {
  if (event.target === event.currentTarget) {
    handleClose()
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="sceneStore.showTutorial" class="tutorial-overlay" @click="handleOverlayClick">
      <div class="tutorial-container">
        <div class="tutorial-header">
          <h3 class="tutorial-title">使用教程</h3>
          <button class="close-btn" @click="handleClose">✕</button>
        </div>

        <div class="tabs">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'operation' }"
            @click="activeTab = 'operation'"
          >
            操作指南
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'model' }"
            @click="activeTab = 'model'"
          >
            模型说明
          </button>
        </div>

        <div class="tutorial-content">
          <div v-if="activeTab === 'operation'" class="operation-guide">
            <div
              v-for="(step, index) in operationSteps"
              :key="index"
              class="step-item"
            >
              <div class="step-icon">{{ step.icon }}</div>
              <div class="step-content">
                <div class="step-number">步骤 {{ index + 1 }}</div>
                <div class="step-title">{{ step.title }}</div>
                <div class="step-description">{{ step.description }}</div>
              </div>
            </div>
          </div>

          <div v-if="activeTab === 'model'" class="model-guide">
            <div
              v-for="(component, index) in modelComponents"
              :key="index"
              class="component-item"
            >
              <div class="component-icon">{{ component.icon }}</div>
              <div class="component-content">
                <div class="component-name">{{ component.name }}</div>
                <div class="component-description">{{ component.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="tutorial-footer">
          <button class="btn primary" @click="handleClose">我知道了</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="scss">
.tutorial-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.tutorial-container {
  background: linear-gradient(135deg, #1e1e2e 0%, #2a2a3e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  width: 580px;
  max-width: 90vw;
  max-height: 85vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.tutorial-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .tutorial-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #fff;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    color: #fff;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.tabs {
  display: flex;
  padding: 12px 24px 0;
  gap: 8px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: #86909C;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: #C9CDD4;
  }

  &.active {
    color: #165DFF;
    border-bottom-color: #165DFF;
  }
}

.tutorial-content {
  padding: 24px;
  max-height: 55vh;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
}

.operation-guide {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border-left: 3px solid #165DFF;

  .step-icon {
    font-size: 32px;
    flex-shrink: 0;
  }

  .step-content {
    flex: 1;
  }

  .step-number {
    font-size: 11px;
    color: #165DFF;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 4px;
  }

  .step-title {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 6px;
  }

  .step-description {
    font-size: 13px;
    color: #86909C;
    line-height: 1.6;
  }
}

.model-guide {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.component-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  .component-icon {
    font-size: 28px;
  }

  .component-name {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
  }

  .component-description {
    font-size: 12px;
    color: #86909C;
    line-height: 1.6;
  }
}

.tutorial-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &.primary {
    background: linear-gradient(135deg, #165DFF, #4080FF);
    color: #fff;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.4);
    }
  }
}
</style>
