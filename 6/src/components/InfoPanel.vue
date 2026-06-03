<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { useInventoryStore } from '../store/useInventoryStore'
import { useDeviceStore } from '../store/useDeviceStore'
import { useSceneStore } from '../store/useSceneStore'

const inventoryStore = useInventoryStore()
const deviceStore = useDeviceStore()
const sceneStore = useSceneStore()

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const statusColors: Record<string, string> = {
  running: '#00B42A',
  idle: '#86909C',
  error: '#F53F3F',
  maintenance: '#FF7D00',
}

const statusTexts: Record<string, string> = {
  running: '运行中',
  idle: '空闲',
  error: '故障',
  maintenance: '维护中',
}

type CargoStatusKey = 'total' | 'normal' | 'reserved' | 'damaged'

const cargoStatusColors: Record<CargoStatusKey, string> = {
  total: '#86909C',
  normal: '#00B42A',
  reserved: '#165DFF',
  damaged: '#F53F3F',
}

const cargoStatusTexts: Record<CargoStatusKey, string> = {
  total: '总计',
  normal: '正常',
  reserved: '预留',
  damaged: '损坏',
}

const cargoStatusKeys: CargoStatusKey[] = ['total', 'normal', 'reserved', 'damaged']

const currentTime = ref(new Date().toLocaleString('zh-CN'))

onMounted(() => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    updateChart()
  }

  setInterval(() => {
    currentTime.value = new Date().toLocaleString('zh-CN')
  }, 1000)
})

watch(() => [inventoryStore.statistics, inventoryStore.cargoStats], () => {
  updateChart()
}, { deep: true })

function updateChart() {
  if (!chartInstance) return

  const option = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: '#C9CDD4',
        fontSize: 11,
      },
    },
    series: [
      {
        name: '库存状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#1a1a2e',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#fff',
          },
        },
        labelLine: {
          show: false,
        },
        data: [
          { value: inventoryStore.statistics.occupiedLocations, name: '已占用', itemStyle: { color: '#FF7D00' } },
          { value: inventoryStore.statistics.totalLocations - inventoryStore.statistics.occupiedLocations, name: '空闲', itemStyle: { color: '#00B42A' } },
        ],
      },
    ],
  }

  chartInstance.setOption(option)
}

const utilizationBarData = computed(() => {
  const zones = [
    { name: '入库区', total: 0, occupied: 0 },
    { name: '存储区', total: 0, occupied: 0 },
    { name: '出库区', total: 0, occupied: 0 },
    { name: '拣选区', total: 0, occupied: 0 },
  ]

  inventoryStore.locations.forEach(loc => {
    const zoneIndex = ['inbound', 'storage', 'outbound', 'picking'].indexOf(loc.zone)
    if (zoneIndex >= 0) {
      zones[zoneIndex].total++
      if (loc.occupied) zones[zoneIndex].occupied++
    }
  })

  return zones
})
</script>

<template>
  <div class="info-panel">
    <div class="panel-header">
      <h2 class="panel-title">仓库监控中心</h2>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <div class="stat-value">{{ inventoryStore.statistics.totalLocations }}</div>
          <div class="stat-label">总库位数</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ inventoryStore.statistics.occupiedLocations }}</div>
          <div class="stat-label">已使用</div>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ inventoryStore.statistics.utilizationRate }}%</div>
          <div class="stat-label">利用率</div>
        </div>
      </div>
      <div class="stat-card info">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ inventoryStore.cargoStats.total }}</div>
          <div class="stat-label">货物总数</div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <h3 class="section-title">库存分布</h3>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <div class="utilization-section">
      <h3 class="section-title">区域利用率</h3>
      <div class="utilization-list">
        <div v-for="zone in utilizationBarData" :key="zone.name" class="utilization-item">
          <div class="zone-name">{{ zone.name }}</div>
          <div class="utilization-bar">
            <div
              class="utilization-fill"
              :style="{ width: `${zone.total > 0 ? (zone.occupied / zone.total) * 100 : 0}%` }"
            ></div>
          </div>
          <div class="utilization-value">
            {{ zone.total > 0 ? Math.round((zone.occupied / zone.total) * 100) : 0 }}%
          </div>
        </div>
      </div>
    </div>

    <div class="devices-section">
      <h3 class="section-title">设备状态</h3>
      <div class="device-list">
        <div v-for="device in deviceStore.devices.slice(0, 6)" :key="device.id" class="device-item">
          <div class="device-status" :style="{ background: statusColors[device.status] }"></div>
          <div class="device-info">
            <div class="device-name">{{ device.name }}</div>
            <div class="device-type">{{ device.type.toUpperCase() }}</div>
          </div>
          <div class="device-status-text" :style="{ color: statusColors[device.status] }">
            {{ statusTexts[device.status] }}
          </div>
        </div>
      </div>
    </div>

    <div class="cargo-status-section">
      <h3 class="section-title">货物状态</h3>
      <div class="cargo-status-list">
        <div v-for="status in cargoStatusKeys" :key="status" class="cargo-status-item">
          <div class="cargo-status-dot" :style="{ background: cargoStatusColors[status] }"></div>
          <span class="cargo-status-label">{{ cargoStatusTexts[status] }}</span>
          <span class="cargo-status-count">
            {{ inventoryStore.cargoStats[status] }}
          </span>
        </div>
      </div>
    </div>

    <div class="zone-info">
      <h3 class="section-title">当前区域</h3>
      <div class="current-zone">
        <span class="zone-icon">{{ sceneStore.currentZone === 'inbound' ? '📥' : sceneStore.currentZone === 'outbound' ? '📤' : sceneStore.currentZone === 'picking' ? '🛒' : '📦' }}</span>
        <span class="zone-name">{{ sceneStore.zoneNames[sceneStore.currentZone] }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.info-panel {
  position: absolute;
  right: 16px;
  top: 16px;
  bottom: 16px;
  width: 320px;
  background: rgba(20, 20, 30, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 16px;
  overflow-y: auto;
  z-index: 100;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 2px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
}

.panel-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .panel-title {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  .current-time {
    font-size: 12px;
    color: #86909C;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);

  &.primary {
    border-left: 3px solid #165DFF;
  }
  &.success {
    border-left: 3px solid #00B42A;
  }
  &.warning {
    border-left: 3px solid #FF7D00;
  }
  &.info {
    border-left: 3px solid #86909C;
  }

  .stat-icon {
    font-size: 24px;
  }

  .stat-content {
    .stat-value {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
      line-height: 1;
    }
    .stat-label {
      font-size: 11px;
      color: #86909C;
      margin-top: 4px;
    }
  }
}

.chart-section,
.utilization-section,
.devices-section,
.cargo-status-section,
.zone-info {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: #C9CDD4;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.chart-container {
  height: 180px;
}

.utilization-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.utilization-item {
  display: flex;
  align-items: center;
  gap: 10px;

  .zone-name {
    width: 60px;
    font-size: 12px;
    color: #C9CDD4;
  }

  .utilization-bar {
    flex: 1;
    height: 8px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    overflow: hidden;

    .utilization-fill {
      height: 100%;
      background: linear-gradient(90deg, #165DFF, #4080FF);
      border-radius: 4px;
      transition: width 0.3s ease;
    }
  }

  .utilization-value {
    width: 40px;
    text-align: right;
    font-size: 12px;
    font-weight: 600;
    color: #165DFF;
  }
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;

  .device-status {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  .device-info {
    flex: 1;

    .device-name {
      font-size: 13px;
      color: #fff;
      font-weight: 500;
    }

    .device-type {
      font-size: 10px;
      color: #86909C;
      margin-top: 2px;
    }
  }

  .device-status-text {
    font-size: 11px;
    font-weight: 500;
  }
}

.cargo-status-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cargo-status-item {
  display: flex;
  align-items: center;
  gap: 10px;

  .cargo-status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  .cargo-status-label {
    flex: 1;
    font-size: 12px;
    color: #C9CDD4;
  }

  .cargo-status-count {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
  }
}

.zone-info {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: 0;
}

.current-zone {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.2), rgba(64, 128, 255, 0.1));
  border-radius: 12px;
  border: 1px solid rgba(22, 93, 255, 0.3);

  .zone-icon {
    font-size: 28px;
  }

  .zone-name {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }
}
</style>
