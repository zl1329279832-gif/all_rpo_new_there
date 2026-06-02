import * as THREE from 'three'
import { RobotAnimationState } from '../types'

export class AnimationStateMachine {
  private currentState: RobotAnimationState = 'idle'
  private stateProgress: number = 0
  private stateSpeed: number = 1
  private animationCallbacks: Map<RobotAnimationState, (progress: number) => void> = new Map()
  private stateEnterCallbacks: Map<RobotAnimationState, () => void> = new Map()
  private stateExitCallbacks: Map<RobotAnimationState, () => void> = new Map()

  setState(state: RobotAnimationState, speed: number = 1) {
    if (this.currentState === state) return

    const exitCallback = this.stateExitCallbacks.get(this.currentState)
    if (exitCallback) exitCallback()

    this.currentState = state
    this.stateProgress = 0
    this.stateSpeed = speed

    const enterCallback = this.stateEnterCallbacks.get(state)
    if (enterCallback) enterCallback()
  }

  getState(): RobotAnimationState {
    return this.currentState
  }

  getProgress(): number {
    return this.stateProgress
  }

  update(deltaTime: number) {
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
}
