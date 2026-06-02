import * as THREE from 'three'
import { RobotAnimationState, Task, PathPoint, FaultState } from '../types'

export class AnimationStateMachine {
  private currentState: RobotAnimationState = 'idle'
  private previousState: RobotAnimationState = 'idle'
  private stateProgress: number = 0
  private stateSpeed: number = 1
  private isPaused: boolean = false
  private animationCallbacks: Map<RobotAnimationState, (progress: number) => void> = new Map()
  private stateEnterCallbacks: Map<RobotAnimationState, () => void> = new Map()
  private stateExitCallbacks: Map<RobotAnimationState, () => void> = new Map()
  private taskQueue: Task[] = []
  private currentTask: Task | null = null
  private currentPath: PathPoint[] = []
  private pathIndex: number = 0
  private faultHistory: FaultState[] = []
  private stateHistory: { state: RobotAnimationState; timestamp: number }[] = []
  private maxHistoryLength: number = 100

  setState(state: RobotAnimationState, speed: number = 1) {
    if (this.currentState === state) return

    const exitCallback = this.stateExitCallbacks.get(this.currentState)
    if (exitCallback) exitCallback()

    this.previousState = this.currentState
    this.currentState = state
    this.stateProgress = 0
    this.stateSpeed = speed

    this.stateHistory.push({ state, timestamp: Date.now() })
    if (this.stateHistory.length > this.maxHistoryLength) {
      this.stateHistory.shift()
    }

    const enterCallback = this.stateEnterCallbacks.get(state)
    if (enterCallback) enterCallback()
  }

  getState(): RobotAnimationState {
    return this.currentState
  }

  getPreviousState(): RobotAnimationState {
    return this.previousState
  }

  getProgress(): number {
    return this.stateProgress
  }

  setPaused(paused: boolean) {
    this.isPaused = paused
    if (paused) {
      this.previousState = this.currentState
      this.currentState = 'paused'
    } else {
      this.currentState = this.previousState
    }
  }

  getIsPaused(): boolean {
    return this.isPaused
  }

  update(deltaTime: number) {
    if (this.isPaused) return

    this.stateProgress += deltaTime * this.stateSpeed
    
    const callback = this.animationCallbacks.get(this.currentState)
    if (callback) {
      callback(this.stateProgress)
    }
  }

  onStateAnimate(state: RobotAnimationState, callback: (progress: number) => void) {
    this.animationCallbacks.set(state, callback)
  }

  onStateEnter(state: RobotAnimationState, callback: () => void) {
    this.stateEnterCallbacks.set(state, callback)
  }

  onStateExit(state: RobotAnimationState, callback: () => void) {
    this.stateExitCallbacks.set(state, callback)
  }

  addTask(task: Task) {
    this.taskQueue.push(task)
    if (!this.currentTask) {
      this.processNextTask()
    }
  }

  private processNextTask() {
    if (this.taskQueue.length === 0) {
      this.currentTask = null
      this.setState('idle')
      return
    }

    this.currentTask = this.taskQueue.shift()!
    this.currentTask.status = 'inProgress'
    
    switch (this.currentTask.type) {
      case 'pickup':
        this.setState('moving')
        break
      case 'delivery':
        this.setState('moving')
        break
      case 'charge':
        this.setState('returning')
        break
      case 'return':
        this.setState('returning')
        break
    }
  }

  getCurrentTask(): Task | null {
    return this.currentTask
  }

  getTaskQueue(): Task[] {
    return [...this.taskQueue]
  }

  setPath(path: PathPoint[]) {
    this.currentPath = path
    this.pathIndex = 0
  }

  getPath(): PathPoint[] {
    return [...this.currentPath]
  }

  getPathIndex(): number {
    return this.pathIndex
  }

  advancePath(): boolean {
    if (this.pathIndex < this.currentPath.length - 1) {
      this.pathIndex++
      return true
    }
    return false
  }

  resetPath() {
    this.pathIndex = 0
  }

  triggerFault(fault: Omit<FaultState, 'timestamp'>) {
    const fullFault: FaultState = {
      ...fault,
      timestamp: Date.now()
    }
    this.faultHistory.push(fullFault)
    this.setState('fault')
  }

  clearFault() {
    this.faultHistory = this.faultHistory.filter(f => f.severity !== 'critical')
    if (this.currentTask) {
      this.setState('moving')
    } else {
      this.setState('idle')
    }
  }

  getFaultHistory(): FaultState[] {
    return [...this.faultHistory]
  }

  getCurrentFault(): FaultState | null {
    if (this.currentState !== 'fault') return null
    return this.faultHistory[this.faultHistory.length - 1] || null
  }

  getStateHistory(): { state: RobotAnimationState; timestamp: number }[] {
    return [...this.stateHistory]
  }

  resetProgress() {
    this.stateProgress = 0
  }

  setSpeed(speed: number) {
    this.stateSpeed = Math.max(0.1, Math.min(5, speed))
  }

  getSpeed(): number {
    return this.stateSpeed
  }
}
