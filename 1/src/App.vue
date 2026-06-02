<template>
  <div class="app-container">
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <ControlPanel
      :current-animation="currentAnimation"
      :exploded-view="explodedView"
      :transparent-shell="transparentShell"
      :maintenance-mode="maintenanceMode"
      :path-active="pathActive"
      :sensor-viz="sensorVizEnabled"
      :show-trajectory="showTrajectory"
      :animation-speed="animationSpeed"
      :quality-level="qualityLevel"
      @play-animation="handlePlayAnimation"
      @toggle-exploded="handleToggleExploded"
      @toggle-transparent="handleToggleTransparent"
      @toggle-maintenance="handleToggleMaintenance"
      @toggle-path="handleTogglePath"
      @reset-path="handleResetPath"
      @toggle-sensor-viz="handleToggleSensorViz"
      @toggle-trajectory="handleToggleTrajectory"
      @clear-trajectory="handleClearTrajectory"
      @set-speed="handleSetSpeed"
      @set-quality="handleSetQuality"
      @trigger-fault="handleTriggerFault"
      @clear-fault="handleClearFault"
      @pause-animation="handlePauseAnimation"
      @resume-animation="handleResumeAnimation"
      @show-tutorial="showTutorial = true"
      @run-full-demo="handleRunFullDemo"
      @follow-pickup-path="handleFollowPickupPath"
    />

    <StatusBar 
      :robot-state="robotState" 
      :performance="performanceStats"
      :show-performance="true"
    />

    <PartInfoPanel 
      :part="selectedPart" 
      @close="selectedPart = null" 
    />

    <TutorialModal 
      :visible="showTutorial" 
      @close="showTutorial = false" 
    />

    <FaultPanel
      v-if="currentFault"
      :fault="currentFault"
      @clear="handleClearFault"
      @maintenance="handleEnterMaintenanceFromFault"
    />

    <div class="title-bar">
      <h1>智能仓储机器人 3D 展示</h1>
      <p>Intelligent Warehouse Robot 3D Viewer</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, onDeactivated, onActivated } from 'vue'
import * as THREE from 'three'
import type { RobotPart, RobotState, FaultState, PathPoint, PerformanceStats } from './types'
import { SceneManager } from './core/SceneManager'
import { MaterialSystem } from './core/MaterialSystem'
import { RobotController } from './robot/RobotController'
import { InteractionSystem } from './core/InteractionSystem'
import { PathController } from './core/PathController'
import { EnvironmentBuilder } from './environment/EnvironmentBuilder'
import { defaultPath, warehousePickupPath, warehouseDeliveryPath, returnToChargerPath, faultTypes, demoTasks } from './data/mockData'
import ControlPanel from './components/ControlPanel.vue'
import StatusBar from './components/StatusBar.vue'
import PartInfoPanel from './components/PartInfoPanel.vue'
import TutorialModal from './components/TutorialModal.vue'
import FaultPanel from './components/FaultPanel.vue'

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
const sensorVizEnabled = ref(false)
const showTrajectory = ref(false)
const animationSpeed = ref(1)
const qualityLevel = ref<'low' | 'medium' | 'high'>('high')
const selectedPart = ref<RobotPart | null>(null)
const showTutorial = ref(false)
const currentFault = ref<FaultState | null>(null)
const isPaused = ref(false)
const isDemoRunning = ref(false)

const robotState = reactive<RobotState>({
  position: new THREE.Vector3(-8, 0, 0),
  rotation: 0,
  targetRotation: 0,
  batteryLevel: 85,
  liftHeight: 0,
  targetLiftHeight: 0,
  isCharging: false,
  isMoving: false,
  isAvoiding: false,
  hasPayload: false,
  currentAnimation: 'idle',
  previousAnimation: 'idle',
  speed: 1,
  pathIndex: 0
})

const performanceStats = reactive<PerformanceStats>({
  fps: 0,
  frameTime: 0,
  drawCalls: 0,
  triangles: 0,
  memory: {
    geometries: 0,
    textures: 0
  }
})

let lastTime = 0
let animationId: number | null = null

const animate = (time: number) => {
  animationId = requestAnimationFrame(animate)
  
  const deltaTime = Math.min((time - lastTime) / 1000, 0.1)
  lastTime = time

  if (robotController && !isPaused.value) {
    robotController.update(deltaTime)
    const state = robotController.getState()
    Object.assign(robotState, state)
    currentAnimation.value = state.currentAnimation
    
    if (state.currentAnimation === 'fault') {
      currentFault.value = robotController.getAnimationStateMachine().getCurrentFault()
    }
  }

  if (pathController && pathActive.value && !isPaused.value) {
    pathController.update(deltaTime)
  }

  if (sceneManager) {
    const stats = sceneManager.getPerformanceStats()
    Object.assign(performanceStats, stats)
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
  robotController?.clearTrajectory()
  if (robotController) {
    robotController.robotGroup.position.set(-8, 0, 0)
    robotController.robotGroup.rotation.set(0, 0, 0)
    robotState.position.set(-8, 0, 0)
    robotState.rotation = 0
  }
  handlePlayAnimation('idle')
}

const handleToggleSensorViz = () => {
  sensorVizEnabled.value = !sensorVizEnabled.value
  robotController?.toggleSensorVisualizations()
}

const handleToggleTrajectory = () => {
  showTrajectory.value = !showTrajectory.value
  robotController?.toggleTrajectory(showTrajectory.value)
}

const handleClearTrajectory = () => {
  robotController?.clearTrajectory()
}

const handleSetSpeed = (speed: number) => {
  animationSpeed.value = speed
  robotController?.setSpeed(speed)
}

const handleSetQuality = (quality: 'low' | 'medium' | 'high') => {
  qualityLevel.value = quality
  sceneManager?.setQuality(quality)
}

const handleTriggerFault = (faultType: string) => {
  const fault = faultTypes.find(f => f.type === faultType) || faultTypes[0]
  robotController?.triggerFault(fault.id, fault.message, fault.severity, fault.affectedParts)
  currentFault.value = { ...fault, timestamp: Date.now() }
}

const handleClearFault = () => {
  currentFault.value = null
  robotController?.clearFault()
  handlePlayAnimation('idle')
}

const handleEnterMaintenanceFromFault = () => {
  maintenanceMode.value = true
  robotController?.toggleMaintenanceMode(true)
}

const handlePauseAnimation = () => {
  isPaused.value = true
  robotController?.pauseAnimation()
}

const handleResumeAnimation = () => {
  isPaused.value = false
  robotController?.resumeAnimation()
}

const handleRunFullDemo = () => {
  if (isDemoRunning.value) return
  isDemoRunning.value = true
  handleResetPath()
  
  setTimeout(() => {
    handleFollowPickupPath()
  }, 500)
}

const handleFollowPickupPath = () => {
  pathActive.value = true
  const fullPath = [...warehousePickupPath, ...warehouseDeliveryPath.slice(1), ...returnToChargerPath.slice(1)]
  pathController?.setPath(fullPath)
  pathController?.start()
  handlePlayAnimation('moving')
}

const handlePartClick = (part: RobotPart) => {
  selectedPart.value = part
}

const handlePartSelect = (part: RobotPart | null) => {
  if (part) {
    selectedPart.value = part
  }
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

  const obstacles = environmentBuilder.getObstacles()
  robotController.registerObstacles(obstacles)

  interactionSystem = new InteractionSystem(
    sceneManager.camera,
    sceneManager.renderer.domElement
  )
  interactionSystem.setParts(robotController.parts)
  interactionSystem.setOnPartClick(handlePartClick)
  interactionSystem.setOnPartSelect(handlePartSelect)
  interactionSystem.setOnPartHover(handlePartHover)

  pathController = new PathController(robotController.robotGroup)
  pathController.setPath(defaultPath)

  sceneManager.setAnimationCallback(() => {})
  sceneManager.start()
  sceneManager.optimizeScene()

  lastTime = performance.now()
  animationId = requestAnimationFrame(animate)

  setTimeout(() => {
    showTutorial.value = true
  }, 800)
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

onDeactivated(() => {
  sceneManager?.stop()
})

onActivated(() => {
  sceneManager?.start()
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
