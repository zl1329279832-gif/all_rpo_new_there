<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon device-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDevices }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon repair-icon">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.repairCount }}</div>
              <div class="stat-label">待维修</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon running-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.runningCount }}</div>
              <div class="stat-label">运行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon warning-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.calibrationCount }}</div>
              <div class="stat-label">待校准</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts">
      <el-col :span="12">
        <el-card class="chart-card" v-loading="loading">
          <template #header>设备状态分布</template>
          <div ref="statusChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" v-loading="loading">
          <template #header>维修趋势</template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Monitor, Tools, CircleCheck, Warning } from '@element-plus/icons-vue'
import { getOverview, getDashboard } from '@/api/statistics'

const statusChartRef = ref(null)
const trendChartRef = ref(null)
const loading = ref(false)

const stats = ref({
  totalDevices: 0,
  runningCount: 0,
  repairCount: 0,
  calibrationCount: 0
})

const colorMap = {
  '运行中': '#67C23A',
  '维修中': '#E6A23C',
  '待校准': '#F56C6C',
  '已停用': '#909399'
}

const getEmptyOption = () => ({
  graphic: {
    type: 'text',
    left: 'center',
    top: 'middle',
    style: {
      text: '暂无数据',
      fontSize: 16,
      fill: '#999'
    }
  }
})

const initStatusChart = (data) => {
  const chart = echarts.init(statusChartRef.value)
  const hasData = data && data.length > 0

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: hasData ? [
      {
        name: '设备状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data.map(item => ({
          value: item.value,
          name: item.name,
          itemStyle: { color: colorMap[item.name] || '#409EFF' }
        }))
      }
    ] : [],
    ...(!hasData ? getEmptyOption() : {})
  }

  chart.setOption(option)

  const resizeObserver = new ResizeObserver(() => {
    chart.resize()
  })
  resizeObserver.observe(statusChartRef.value)
}

const initTrendChart = (data) => {
  const chart = echarts.init(trendChartRef.value)
  const hasData = data && data.length > 0

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['维修工单', '校准任务']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: hasData ? {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.month)
    } : {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: hasData ? [
      {
        name: '维修工单',
        type: 'line',
        data: data.map(item => item.repairCount),
        smooth: true,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '校准任务',
        type: 'line',
        data: data.map(item => item.calibrationCount),
        smooth: true,
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      }
    ] : [],
    ...(!hasData ? getEmptyOption() : {})
  }

  chart.setOption(option)

  const resizeObserver = new ResizeObserver(() => {
    chart.resize()
  })
  resizeObserver.observe(trendChartRef.value)
}

const fetchData = async () => {
  loading.value = true
  try {
    const [overviewRes, dashboardRes] = await Promise.all([
      getOverview(),
      getDashboard()
    ])

    const overviewData = overviewRes.data || overviewRes
    const dashboardData = dashboardRes.data || dashboardRes

    stats.value = {
      totalDevices: overviewData.totalDevices ?? 0,
      runningCount: overviewData.runningCount ?? 0,
      repairCount: overviewData.repairCount ?? 0,
      calibrationCount: overviewData.calibrationCount ?? 0
    }

    await nextTick()

    initStatusChart(dashboardData.deviceStatusDistribution || [])
    initTrendChart((dashboardData.monthlyTrend || []).slice(-6))
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error(error.message || '获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #fff;
}

.device-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.repair-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.running-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.warning-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.chart-card {
  height: 400px;
}

.chart {
  width: 100%;
  height: 300px;
}
</style>
