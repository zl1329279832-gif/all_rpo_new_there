import * as THREE from 'three'
import { PartInfo, PathPoint, Task, FaultState } from '../types'

export const robotPartsInfo: PartInfo[] = [
  {
    id: 'chassis',
    name: '机器人底盘',
    description: '高强度铝合金底盘，承载所有机械和电子部件，防护等级 IP54。采用一体成型工艺，表面经过阳极氧化处理，具有优异的耐磨性和耐腐蚀性。',
    category: 'structure',
    specs: {
      '材料': '6061 铝合金',
      '尺寸': '1800 x 1400 x 400 mm',
      '重量': '85 kg',
      '承重': '500 kg',
      '防护等级': 'IP54',
      '工艺': '一体成型/阳极氧化'
    }
  },
  {
    id: 'wheel_0',
    name: '驱动轮 1',
    description: '高性能麦克纳姆轮，支持全向移动，精准定位。采用聚氨酯辊轮，耐磨静音，配合高精度伺服电机实现毫米级定位。',
    category: 'motion',
    specs: {
      '类型': '麦克纳姆轮',
      '直径': '360 mm',
      '辊轮数量': '12 个',
      '电机': '直流伺服电机',
      '扭矩': '25 Nm',
      '最大转速': '180 rpm'
    }
  },
  {
    id: 'wheel_1',
    name: '驱动轮 2',
    description: '高性能麦克纳姆轮，支持全向移动，精准定位。采用聚氨酯辊轮，耐磨静音，配合高精度伺服电机实现毫米级定位。',
    category: 'motion',
    specs: {
      '类型': '麦克纳姆轮',
      '直径': '360 mm',
      '辊轮数量': '12 个',
      '电机': '直流伺服电机',
      '扭矩': '25 Nm',
      '最大转速': '180 rpm'
    }
  },
  {
    id: 'wheel_2',
    name: '驱动轮 3',
    description: '高性能麦克纳姆轮，支持全向移动，精准定位。采用聚氨酯辊轮，耐磨静音，配合高精度伺服电机实现毫米级定位。',
    category: 'motion',
    specs: {
      '类型': '麦克纳姆轮',
      '直径': '360 mm',
      '辊轮数量': '12 个',
      '电机': '直流伺服电机',
      '扭矩': '25 Nm',
      '最大转速': '180 rpm'
    }
  },
  {
    id: 'wheel_3',
    name: '驱动轮 4',
    description: '高性能麦克纳姆轮，支持全向移动，精准定位。采用聚氨酯辊轮，耐磨静音，配合高精度伺服电机实现毫米级定位。',
    category: 'motion',
    specs: {
      '类型': '麦克纳姆轮',
      '直径': '360 mm',
      '辊轮数量': '12 个',
      '电机': '直流伺服电机',
      '扭矩': '25 Nm',
      '最大转速': '180 rpm'
    }
  },
  {
    id: 'liftMechanism',
    name: '升降机构',
    description: '剪叉式液压升降系统，最大举升高度 1.2 米，平稳可靠。采用双液压缸同步驱动，配备安全锁止装置，确保升降过程安全稳定。',
    category: 'mechanism',
    specs: {
      '类型': '剪叉式液压',
      '最大高度': '1200 mm',
      '升降速度': '50 mm/s',
      '定位精度': '±2 mm',
      '驱动方式': '双液压缸同步',
      '安全装置': '机械锁止/过载保护'
    }
  },
  {
    id: 'lidar',
    name: '激光雷达',
    description: '360 度激光测距传感器，用于环境感知和避障。采用 TOF 飞行时间技术，可同时检测 360 度范围内的障碍物，构建实时环境地图。',
    category: 'sensor',
    specs: {
      '扫描角度': '360°',
      '探测距离': '0.1-10 m',
      '扫描频率': '10 Hz',
      '分辨率': '0.25°',
      '技术': 'TOF 飞行时间',
      '数据点': '1440 点/圈'
    }
  },
  {
    id: 'frontCamera',
    name: '前置摄像头',
    description: '1080P 高清视觉摄像头，用于视觉导航和二维码识别。配备广角镜头，支持低照度拍摄，可识别地面二维码地标实现精确定位。',
    category: 'sensor',
    specs: {
      '分辨率': '1920x1080',
      '帧率': '30 fps',
      '视场角': '90°',
      '焦距': '3.6 mm',
      '低照度': '0.1 Lux',
      '功能': '二维码识别/视觉导航'
    }
  },
  {
    id: 'rearCamera',
    name: '后置摄像头',
    description: '720P 后视辅助摄像头，用于倒车辅助和后方障碍物检测。配合图像识别算法实现后方行人检测和障碍物预警。',
    category: 'sensor',
    specs: {
      '分辨率': '1280x720',
      '帧率': '25 fps',
      '视场角': '120°',
      '焦距': '2.8 mm',
      '功能': '倒车辅助/障碍物检测'
    }
  },
  {
    id: 'batteryCompartment',
    name: '电池仓',
    description: '48V 20Ah 磷酸铁锂电池组，续航 8 小时，支持快充。配备智能 BMS 电池管理系统，实时监控电池状态，保障安全运行。',
    category: 'power',
    specs: {
      '类型': '磷酸铁锂',
      '电压': '48V',
      '容量': '20Ah',
      '续航': '8 小时',
      '充电时间': '2 小时（快充）',
      '循环寿命': '2000 次'
    }
  },
  {
    id: 'chargingContacts',
    name: '充电触点',
    description: '自动充电对接触点，支持快速充电，导向定位精确。采用铜合金镀金工艺，导电性能优异，耐磨寿命长。',
    category: 'power',
    specs: {
      '类型': '铜合金镀金触点',
      '最大电流': '50A',
      '定位精度': '±5 mm',
      '寿命': '10000 次',
      '接触电阻': '< 5 mΩ',
      '防护': 'IP54'
    }
  },
  {
    id: 'outerShell',
    name: '外壳',
    description: 'ABS 工程塑料外壳，美观耐用，防护等级 IP54。采用流线型设计，表面经过磨砂处理，手感舒适且不易留下指纹。',
    category: 'structure',
    specs: {
      '材料': 'ABS 工程塑料',
      '防护等级': 'IP54',
      '工作温度': '-10°C ~ 45°C',
      '颜色': '灰白色',
      '表面处理': '磨砂',
      '工艺': '注塑成型'
    }
  },
  {
    id: 'payloadTray',
    name: '货架托盘',
    description: '标准货架托盘，防滑设计，最大承重 500kg。采用钢结构焊接成型，表面铺设橡胶防滑垫，防止货物滑动。',
    category: 'payload',
    specs: {
      '尺寸': '2000 x 1500 mm',
      '材质': 'Q235 钢材',
      '最大承重': '500 kg',
      '防滑垫': '橡胶材质',
      '表面处理': '喷塑',
      '适用标准': 'EU 标准托盘'
    }
  },
  {
    id: 'statusLights',
    name: '状态指示灯',
    description: '多色 LED 状态指示灯，直观显示机器人运行状态。支持红、绿、蓝、黄四种颜色，配合闪烁模式传递丰富信息。',
    category: 'indicator',
    specs: {
      '类型': 'RGB LED',
      '颜色': '红/绿/蓝/黄',
      '亮度': '可调',
      '可视距离': '10 m',
      '模式': '常亮/闪烁/呼吸'
    }
  },
  {
    id: 'coolingVents',
    name: '散热孔',
    description: '精密设计的散热通风孔，确保内部电子设备温度稳定。采用格栅式设计，配合内部风扇形成良好的空气对流。',
    category: 'structure',
    specs: {
      '类型': '格栅式通风',
      '开孔率': '40%',
      '防尘网': '配备',
      '风扇': '2 x 120mm',
      '噪音': '< 45 dB'
    }
  }
]

export const animationPresets = [
  { id: 'idle', name: '待机', icon: '⏸', description: '机器人静止待机状态' },
  { id: 'moving', name: '行驶', icon: '🚗', description: '机器人正常行驶动画' },
  { id: 'turning', name: '转向', icon: '🔄', description: '机器人转向调整方向' },
  { id: 'lifting', name: '顶升', icon: '⬆', description: '升降机构举升货架' },
  { id: 'lowering', name: '下降', icon: '⬇', description: '升降机构下降复位' },
  { id: 'charging', name: '充电', icon: '🔋', description: '机器人自动充电过程' },
  { id: 'avoiding', name: '避障', icon: '⚠', description: '机器人避障动作演示' },
  { id: 'pickingUp', name: '取货', icon: '📦', description: '机器人取货完整流程' },
  { id: 'droppingOff', name: '放货', icon: '📤', description: '机器人放货完整流程' },
  { id: 'returning', name: '返航', icon: '🏠', description: '机器人低电量返航充电' }
]

export const defaultPath: PathPoint[] = [
  { position: new THREE.Vector3(0, 0, 0), speed: 2, rotation: 0 },
  { position: new THREE.Vector3(4, 0, 0), speed: 2, rotation: 0 },
  { position: new THREE.Vector3(4, 0, 4), speed: 1.5, rotation: Math.PI / 2 },
  { position: new THREE.Vector3(0, 0, 4), speed: 2, rotation: Math.PI },
  { position: new THREE.Vector3(-4, 0, 4), speed: 2, rotation: Math.PI },
  { position: new THREE.Vector3(-4, 0, 0), speed: 1.5, rotation: -Math.PI / 2 },
  { position: new THREE.Vector3(0, 0, 0), speed: 2, rotation: 0 }
]

export const warehousePickupPath: PathPoint[] = [
  { position: new THREE.Vector3(0, 0, 0), speed: 2, rotation: 0 },
  { position: new THREE.Vector3(0, 0, 5), speed: 2, rotation: 0 },
  { position: new THREE.Vector3(0, 0, 8), speed: 1, rotation: 0 },
  { position: new THREE.Vector3(0, 0, 8), speed: 0, rotation: 0 }
]

export const warehouseDeliveryPath: PathPoint[] = [
  { position: new THREE.Vector3(0, 0, 8), speed: 2, rotation: Math.PI },
  { position: new THREE.Vector3(8, 0, 8), speed: 2, rotation: Math.PI / 2 },
  { position: new THREE.Vector3(8, 0, 0), speed: 1.5, rotation: 0 },
  { position: new THREE.Vector3(8, 0, 0), speed: 0, rotation: 0 }
]

export const returnToChargerPath: PathPoint[] = [
  { position: new THREE.Vector3(8, 0, 0), speed: 2, rotation: Math.PI },
  { position: new THREE.Vector3(0, 0, 0), speed: 1.5, rotation: 0 },
  { position: new THREE.Vector3(-6, 0, 0), speed: 1, rotation: 0 },
  { position: new THREE.Vector3(-6, 0, 0), speed: 0, rotation: 0 }
]

export const demoTasks: Task[] = [
  {
    id: 'task_001',
    name: '货架 A1 取货',
    type: 'pickup',
    priority: 'high',
    from: { position: new THREE.Vector3(0, 0, 8), name: '货架区 A1' },
    to: { position: new THREE.Vector3(8, 0, 0), name: '放货区 B2' },
    status: 'pending',
    estimatedTime: 120
  },
  {
    id: 'task_002',
    name: '补货任务',
    type: 'delivery',
    priority: 'medium',
    from: { position: new THREE.Vector3(5, 0, 5), name: '入库区' },
    to: { position: new THREE.Vector3(-3, 0, 8), name: '货架区 C3' },
    status: 'pending',
    estimatedTime: 90
  },
  {
    id: 'task_003',
    name: '盘点巡检',
    type: 'patrol',
    priority: 'low',
    from: { position: new THREE.Vector3(0, 0, 0), name: '充电站' },
    to: { position: new THREE.Vector3(0, 0, 0), name: '充电站' },
    status: 'pending',
    estimatedTime: 300
  }
]

export const faultTypes: FaultState[] = [
  {
    id: 'fault_motor',
    type: 'motor',
    severity: 'critical',
    message: '左前轮电机过载',
    affectedParts: ['wheel_0'],
    timestamp: Date.now(),
    isActive: true
  },
  {
    id: 'fault_lidar',
    type: 'sensor',
    severity: 'warning',
    message: '激光雷达数据异常',
    affectedParts: ['lidar'],
    timestamp: Date.now(),
    isActive: true
  },
  {
    id: 'fault_battery',
    type: 'power',
    severity: 'warning',
    message: '电池温度偏高',
    affectedParts: ['batteryCompartment'],
    timestamp: Date.now(),
    isActive: true
  },
  {
    id: 'fault_camera',
    type: 'sensor',
    severity: 'warning',
    message: '前置摄像头遮挡',
    affectedParts: ['frontCamera'],
    timestamp: Date.now(),
    isActive: true
  }
]

export const tutorialSteps = [
  {
    title: '欢迎使用',
    content: '这是一个智能仓储机器人 3D 展示系统。您可以通过鼠标拖拽旋转视角，滚轮缩放，右键平移。'
  },
  {
    title: '部件交互',
    content: '点击机器人任意部件可以查看详细信息。鼠标悬停时部件会高亮显示。按住 Shift 键可多选部件。'
  },
  {
    title: '动画演示',
    content: '使用左侧控制面板可以播放不同的动画效果，包括行驶、转向、顶升、充电、避障以及完整的取放货流程。'
  },
  {
    title: '视图模式',
    content: '支持爆炸视图查看内部结构，透明外壳模式，以及维护状态展示。可以单独选中部件进行维修模式高亮。'
  },
  {
    title: '路径演示',
    content: '开启完整业务流程模式后，机器人会在仓储场景中执行完整的取货、搬运、放货任务，展示真实物流作业流程。'
  },
  {
    title: '传感器可视化',
    content: '开启传感器可视化功能，可以直观看到激光雷达扫描范围、摄像头视场角、红外传感器探测区域。'
  },
  {
    title: '轨迹回放',
    content: '开启轨迹显示功能，可以查看机器人的行驶路径记录，支持历史轨迹回放分析。'
  },
  {
    title: '性能监控',
    content: '在状态栏可以查看实时性能数据，包括帧率、Draw Calls、三角形数量和内存占用，支持画质切换优化性能。'
  }
]

export const modelDetails = {
  geometryCount: 86,
  materialCount: 18,
  totalTriangles: 156800,
  totalVertices: 94200,
  textures: 6,
  animations: 12
}

export const performanceTips = [
  {
    title: '高画质模式',
    description: '启用全部特效：PCFSoftShadow 软阴影、高分辨率纹理、后处理效果。适合高端显卡。',
    impact: '高'
  },
  {
    title: '平衡模式',
    description: '启用基本阴影和纹理，关闭高级后处理。大多数设备的推荐设置。',
    impact: '中'
  },
  {
    title: '性能模式',
    description: '使用基本阴影和低分辨率纹理，获得最佳帧率表现。适合集成显卡。',
    impact: '低'
  }
]

export const qualityPresets = [
  { id: 'low', name: '性能优先', shadowQuality: 0, antialias: false },
  { id: 'medium', name: '平衡', shadowQuality: 1, antialias: true },
  { id: 'high', name: '高画质', shadowQuality: 2, antialias: true },
  { id: 'ultra', name: '极致', shadowQuality: 3, antialias: true }
]
