import * as THREE from 'three'
import { PartInfo } from '../types'

export const robotPartsInfo: PartInfo[] = [
  {
    id: 'chassis',
    name: '机器人底盘',
    description: '高强度铝合金底盘，承载所有机械和电子部件，防护等级 IP54',
    category: 'structure',
    specs: {
      '材料': '6061 铝合金',
      '尺寸': '1800 x 1400 x 400 mm',
      '重量': '85 kg',
      '承重': '500 kg'
    }
  },
  {
    id: 'wheel_0',
    name: '驱动轮 1',
    description: '高性能麦克纳姆轮，支持全向移动，精准定位',
    category: 'motion',
    specs: {
      '类型': '麦克纳姆轮',
      '直径': '360 mm',
      '电机': '直流伺服电机',
      '扭矩': '25 Nm'
    }
  },
  {
    id: 'liftMechanism',
    name: '升降机构',
    description: '剪叉式液压升降系统，最大举升高度 1.2 米，平稳可靠',
    category: 'mechanism',
    specs: {
      '类型': '剪叉式液压',
      '最大高度': '1200 mm',
      '升降速度': '50 mm/s',
      '定位精度': '±2 mm'
    }
  },
  {
    id: 'lidar',
    name: '激光雷达',
    description: '360 度激光测距传感器，用于环境感知和避障',
    category: 'sensor',
    specs: {
      '扫描角度': '360°',
      '探测距离': '0.1-10 m',
      '扫描频率': '10 Hz',
      '分辨率': '0.25°'
    }
  },
  {
    id: 'frontCamera',
    name: '前置摄像头',
    description: '1080P 高清视觉摄像头，用于视觉导航和二维码识别',
    category: 'sensor',
    specs: {
      '分辨率': '1920x1080',
      '帧率': '30 fps',
      '视场角': '90°',
      '焦距': '3.6 mm'
    }
  },
  {
    id: 'batteryCompartment',
    name: '电池仓',
    description: '48V 20Ah 磷酸铁锂电池组，续航 8 小时，支持快充',
    category: 'power',
    specs: {
      '类型': '磷酸铁锂',
      '电压': '48V',
      '容量': '20Ah',
      '续航': '8 小时',
      '充电时间': '2 小时'
    }
  },
  {
    id: 'chargingContacts',
    name: '充电触点',
    description: '自动充电对接触点，支持快速充电，导向定位精确',
    category: 'power',
    specs: {
      '类型': '铜合金触点',
      '最大电流': '50A',
      '定位精度': '±5 mm',
      '寿命': '10000 次'
    }
  },
  {
    id: 'outerShell',
    name: '外壳',
    description: 'ABS 工程塑料外壳，美观耐用，防护等级 IP54',
    category: 'structure',
    specs: {
      '材料': 'ABS 工程塑料',
      '防护等级': 'IP54',
      '工作温度': '-10°C ~ 45°C',
      '颜色': '灰白色'
    }
  },
  {
    id: 'payloadTray',
    name: '货架托盘',
    description: '标准货架托盘，防滑设计，最大承重 500kg',
    category: 'payload',
    specs: {
      '尺寸': '2000 x 1500 mm',
      '材质': '钢材',
      '最大承重': '500 kg',
      '防滑垫': '橡胶材质'
    }
  }
]

export const animationPresets = [
  { id: 'idle', name: '待机', icon: '⏸', description: '机器人静止待机状态' },
  { id: 'moving', name: '行驶', icon: '🚗', description: '机器人正常行驶动画' },
  { id: 'turning', name: '转向', icon: '🔄', description: '机器人转向调整方向' },
  { id: 'lifting', name: '顶升', icon: '⬆', description: '升降机构举升货架' },
  { id: 'charging', name: '充电', icon: '🔋', description: '机器人自动充电过程' },
  { id: 'avoiding', name: '避障', icon: '⚠', description: '机器人避障动作演示' }
]

export const defaultPath = [
  { position: new THREE.Vector3(0, 0, 0), speed: 2 },
  { position: new THREE.Vector3(4, 0, 0), speed: 2 },
  { position: new THREE.Vector3(4, 0, 4), speed: 1.5 },
  { position: new THREE.Vector3(0, 0, 4), speed: 2 },
  { position: new THREE.Vector3(-4, 0, 4), speed: 2 },
  { position: new THREE.Vector3(-4, 0, 0), speed: 1.5 },
  { position: new THREE.Vector3(0, 0, 0), speed: 2 }
]

export const tutorialSteps = [
  {
    title: '欢迎使用',
    content: '这是一个智能仓储机器人 3D 展示系统。您可以通过鼠标拖拽旋转视角，滚轮缩放，右键平移。'
  },
  {
    title: '部件交互',
    content: '点击机器人任意部件可以查看详细信息。鼠标悬停时部件会高亮显示。'
  },
  {
    title: '动画演示',
    content: '使用左侧控制面板可以播放不同的动画效果，包括行驶、转向、顶升、充电和避障。'
  },
  {
    title: '视图模式',
    content: '支持爆炸视图查看内部结构，透明外壳模式，以及维护状态展示。'
  },
  {
    title: '路径演示',
    content: '开启路径模式后，机器人会在预设路径上自动行驶，展示导航能力。'
  }
]
