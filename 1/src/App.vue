<template>
  <div class="app-container">
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <ControlPanel
      :current-animation="currentAnimation"
      :exploded-view="explodedView"
      :transparent-shell="transparentShell"
      :maintenance-mode="maintenanceMode"
      :path-active="pathActive"
      @play-animation="handlePlayAnimation"
      @toggle-exploded="handleToggleExploded"
      @toggle-transparent="handleToggleTransparent"
      @toggle-maintenance="handleToggleMaintenance"
      @toggle-path="handleTogglePath"
      @reset-path="handleResetPath"
      @show-tutorial="showTutorial = true"
    />

    <StatusBar :robot-state="robotState" />

    <PartInfoPanel 
      :part="selectedPart" 
      @close="selectedPart = null" 
    />

    <TutorialModal 
      :visible="showTutorial" 
      @close="showTutorial = false" 
    />

    <div class="title-bar">
      <h1>智能仓储机器人 3D 展示</h1>
      <p>Intelligent Warehouse Robot 3D Viewer</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import * as THREE from 'three'
import type { RobotPart, RobotState } from './types'
import { SceneManager } from './core/SceneManager'
import { MaterialSystem } from './core/MaterialSystem'
import { RobotController } from './robot/RobotController'
import { InteractionSystem } from './core/InteractionSystem'
import { PathController } from './core/PathController'
import { EnvironmentBuilder } from './environment/EnvironmentBuilder'
import { defaultPath } from './data/mockData'
import ControlPanel from './components/ControlPanel.vue'
import StatusBar from './components/StatusBar.vue'
import PartInfoPanel from './components/PartInfoPanel.vue'
import TutorialModal from './components/TutorialModal.vue'

const canvasContainer = ref<HTMLElement | null>(null)

let sceneManager: SceneManager | null = null
let materialSystem: MaterialSystem | null = null
let robotController: RobotController | null = null
let interactionSystem: InteractionSystem | null = null
let pathController: PathController | null = null

const currentAnimation = ref('idle')
const explodedView = ref(false)
const transparentShell = ref(false)
const maintenanceMode = ref(false)
const pathActive = ref(false)
const selectedPart = ref<RobotPart | null>(null)
const showTutorial = ref(false)

const robotState = reactive<RobotState>({
  position: new THREE.Vector3(0, 0, 0),
  rotation: 0,
  batteryLevel: 85,
  liftHeight: 0,
  isCharging: false,
  isMoving: false,
  isAvoiding: false,
  hasPayload: false,
  currentAnimation: 'idle'
})

let lastTime = 0
let animationId: number | null = null

const animate = (time: number) => {
  animationId = requestAnimationFrame(animate)
  
  const deltaTime = (time - lastTime) / 1000
  lastTime = time

  if (robotController) {
    robotController.update(deltaTime)
    const state = robotController.getState()
    Object.assign(robotState, state)
    robotState.position.copy(robotController.robotGroup.position)
  }

  if (pathController && pathActive.value) {
    pathController.update(deltaTime)
  }
}

const handlePlayAnimation = (animId: string) => {
  currentAnimation.value = animId
  if (robotController) {
    robotController.playAnimation(animId)
  }
  if (animId === 'moving' && !pathActive.value) {
    pathActive.value = true
    pathController?.start()
  }
}

const handleToggleExploded = () => {
  explodedView.value = !explodedView.value
  robotController?.toggleExplodedView()
}

const handleToggleTransparent = () => {
  transparentShell.value = !transparentShell.value
  robotController?.toggleTransparentShell()
}

const handleToggleMaintenance = () => {
  maintenanceMode.value = !maintenanceMode.value
  robotController?.toggleMaintenanceMode()
}

const handleTogglePath = () => {
  pathActive.value = !pathActive.value
  if (pathActive.value) {
    pathController?.start()
    if (currentAnimation.value !== 'moving') {
      handlePlayAnimation('moving')
    }
  } else {
    pathController?.stop()
    handlePlayAnimation('idle')
  }
}

const handleResetPath = () => {
  pathController?.reset()
  if (robotController) {
    robotController.robotGroup.position.set(0, 0, 0)
    robotController.robotGroup.rotation.set(0, 0, 0)
  }
}

const handlePartClick = (part: RobotPart) => {
  selectedPart.value = part
}

const handlePartHover = (part: RobotPart | null) => {
}

onMounted(() => {
  if (!canvasContainer.value) return

  sceneManager = new SceneManager(canvasContainer.value)
  materialSystem = new MaterialSystem()
  
  robotController = new RobotController(materialSystem)
  sceneManager.scene.add(robotController.robotGroup)

  const environmentBuilder = new EnvironmentBuilder(materialSystem)
  const environment = environmentBuilder.build()
  sceneManager.scene.add(environment)

  interactionSystem = new InteractionSystem(
    sceneManager.camera,
    sceneManager.renderer.domElement
  )
  interactionSystem.setParts(robotController.parts)
  interactionSystem.setOnPartClick(handlePartClick)
  interactionSystem.setOnPartHover(handlePartHover)

  pathController = new PathController(robotController.robotGroup)
  pathController.setPath(defaultPath)

  sceneManager.setAnimationCallback(() => {})
  sceneManager.start()

  lastTime = performance.now()
  animationId = requestAnimationFrame(animate)

  setTimeout(() => {
    showTutorial.value = true
  }, 500)
})

onUnmounted(() => {
  if (animationId !== null) {
    cancelAnimationFrame(animationId)
  }
  
  interactionSystem?.dispose()
  robotController?.dispose()
  materialSystem?.dispose()
  sceneManager?.dispose()
})
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: var(--bg-dark);
}

.canvas-container {
  width: 100%;
  height: 100%;
}

.title-bar {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  color: var(--text-primary);
  z-index: 50;
  pointer-events: none;
}

.title-bar h1 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}

.title-bar p {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 1px;
}
</style>
