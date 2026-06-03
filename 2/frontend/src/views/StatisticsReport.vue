<template>
  <div class="statistics-report-container" v-loading="loading" element-loading-text="数据加载中...">
    <div v-if="error" class="error-container">
      <el-empty :description="error">
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </el-empty>
    </div>
    <template v-else>
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon device-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.totalDevices }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon repair-icon">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.totalRepairs }}</div>
              <div class="stat-label">维修工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon inspection-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.totalInspections }}</div>
              <div class="stat-label">巡检任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.completionRate }}%</div>
              <div class="stat-label">巡检完成率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>设备状态统计</span>
              <el-radio-group v-model="statusChartType" size="small" @change="updateStatusChart">
                <el-radio-button value="pie">饼图</el-radio-button>
                <el-radio-button value="bar">柱状图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="statusChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>维修工单趋势</span>
              <el-radio-group v-model="trendPeriod" size="small" @change="updateTrendChart">
                <el-radio-button value="week">本周</el-radio-button>
                <el-radio-button value="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>巡检完成率</span>
              <el-radio-group v-model="completionPeriod" size="small" @change="updateCompletionChart">
                <el-radio-button value="month">本月</el-radio-button>
                <el-radio-button value="quarter">本季度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="completionChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>各科室设备分布</template>
          <div ref="departmentChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>故障类型统计</template>
          <div ref="faultTypeChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>月度数据汇总</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>

      <el-table v-if="hasData(monthlyData)" :data="monthlyData" border stripe>
        <el-table-column prop="month" label="月份" min-width="100" />
        <el-table-column prop="newDevices" label="新增设备" min-width="100" />
        <el-table-column prop="repairOrders" label="维修工单" min-width="100" />
        <el-table-column prop="completedRepairs" label="已完成维修" min-width="120" />
        <el-table-column prop="inspectionTasks" label="巡检任务" min-width="100" />
        <el-table-column prop="completedInspections" label="已完成巡检" min-width="120" />
        <el-table-column label="巡检完成率" min-width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.completionRate" :stroke-width="12" :color="getProgressColor(row.completionRate)" />
          </template>
        </el-table-column>
        <el-table-column prop="qcRecords" label="质控记录" min-width="100" />
        <el-table-column prop="qcPassRate" label="质控合格率" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.qcPassRate >= 95 ? 'success' : row.qcPassRate >= 85 ? 'warning' : 'danger'">
              {{ row.qcPassRate }}%
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无月度数据" />
    </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElEmpty } from 'element-plus'
import { Monitor, Tools, Calendar, TrendCharts, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getOverview, getMonthlySummary as getMonthly, getDashboard } from '@/api/statistics'

const statusChartRef = ref(null)
const trendChartRef = ref(null)
const completionChartRef = ref(null)
const departmentChartRef = ref(null)
const faultTypeChartRef = ref(null)

const statusChartType = ref('pie')
const trendPeriod = ref('month')
const completionPeriod = ref('month')

const loading = ref(false)
const error = ref(null)

const overviewData = ref(null)
const dashboardData = ref(null)
const monthlyData = ref([])

const summaryData = reactive({
  totalDevices: 0,
  totalRepairs: 0,
  totalInspections: 0,
  completionRate: 0
})

const hasData = (data) => {
  if (!data) return false
  if (Array.isArray(data)) return data.length > 0
  if (typeof data === 'object') return Object.keys(data).length > 0
  return !!data
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

const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#67C23A'
  if (percentage >= 80) return '#E6A23C'
  return '#F56C6C'
}

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const [overviewRes, dashboardRes, monthlyRes] = await Promise.all([
      getOverview(),
      getDashboard(),
      getMonthly()
    ])

    overviewData.value = overviewRes.data || overviewRes
    dashboardData.value = dashboardRes.data || dashboardRes
    monthlyData.value = monthlyRes.data || monthlyRes || []

    if (overviewData.value) {
      summaryData.totalDevices = overviewData.value.totalDevices || 0
      summaryData.totalInspections = overviewData.value.totalInspection || 0
      summaryData.completionRate = overviewData.value.passRate || 0
    }

    if (dashboardData.value && dashboardData.value.repairOrderStatusDistribution) {
      const repairDist = dashboardData.value.repairOrderStatusDistribution
      summaryData.totalRepairs = Object.values(repairDist).reduce((sum, val) => sum + (val || 0), 0)
    }

    nextTick(() => {
      initStatusChart()
      initTrendChart()
      initCompletionChart()
      initDepartmentChart()
      initFaultTypeChart()
    })
  } catch (err) {
    error.value = '数据加载失败，请稍后重试'
    ElMessage.error(error.value)
    console.error('Fetch statistics data error:', err)
  } finally {
    loading.value = false
  }
}

const initStatusChart = () => {
  if (!statusChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(statusChartRef.value)
    const dist = dashboardData.value?.deviceStatusDistribution
    const colors = { '运行中': '#67C23A', '维修中': '#E6A23C', '待校准': '#F56C6C' }

    if (!hasData(dist)) {
      chart.setOption(getEmptyOption())
      return
    }

    const pieData = Object.entries(dist).map(([name, value]) => ({
      value,
      name,
      itemStyle: { color: colors[name] || '#409EFF' }
    }))
    const barData = Object.entries(dist).map(([name, value]) => ({
      value,
      itemStyle: { color: colors[name] || '#409EFF' }
    }))
    const xData = Object.keys(dist)

    if (statusChartType.value === 'pie') {
      chart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          bottom: '0%',
          left: 'center'
        },
        series: [
          {
            name: '设备状态',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '40%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: pieData
          }
        ]
      })
    } else {
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xData
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '设备数量',
            type: 'bar',
            barWidth: '50%',
            data: barData
          }
        ]
      })
    }
  })
}

const updateStatusChart = () => {
  initStatusChart()
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(trendChartRef.value)
    const monthlyTrend = overviewData.value?.monthlyTrend

    if (!hasData(monthlyTrend) || !hasData(monthlyTrend.repairCount)) {
      chart.setOption(getEmptyOption())
      return
    }

    const repairCount = monthlyTrend.repairCount || {}
    const completedCount = monthlyTrend.completedRepairCount || {}
    const months = Object.keys(repairCount).sort()
    const data1 = months.map(m => repairCount[m] || 0)
    const data2 = months.map(m => completedCount[m] || 0)

    chart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['新建工单', '完成工单'],
        bottom: '0%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: months
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '新建工单',
          type: 'line',
          smooth: true,
          data: data1,
          itemStyle: { color: '#409EFF' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ])
          }
        },
        {
          name: '完成工单',
          type: 'line',
          smooth: true,
          data: data2,
          itemStyle: { color: '#67C23A' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
            ])
          }
        }
      ]
    })
  })
}

const updateTrendChart = () => {
  initTrendChart()
}

const initCompletionChart = () => {
  if (!completionChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(completionChartRef.value)
    const monthlyTrend = overviewData.value?.monthlyTrend

    if (!hasData(monthlyTrend) || !hasData(monthlyTrend.completionRate)) {
      chart.setOption(getEmptyOption())
      return
    }

    const completionRate = monthlyTrend.completionRate || {}
    const months = Object.keys(completionRate).sort()
    const data = months.map(m => completionRate[m] || 0)

    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: '{b}: {c}%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: months
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '完成率',
          type: 'bar',
          barWidth: '50%',
          data: data.map((value) => ({
            value,
            itemStyle: {
              color: value >= 90 ? '#67C23A' : value >= 80 ? '#E6A23C' : '#F56C6C'
            }
          })),
          label: {
            show: true,
            position: 'top',
            formatter: '{c}%'
          }
        }
      ]
    })
  })
}

const updateCompletionChart = () => {
  initCompletionChart()
}

const initDepartmentChart = () => {
  if (!departmentChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(departmentChartRef.value)
    const dist = dashboardData.value?.deptDeviceDistribution

    if (!hasData(dist)) {
      chart.setOption(getEmptyOption())
      return
    }

    const yData = dist.map(item => item.deptName)
    const highRiskData = dist.map(item => item.highRisk || 0)
    const mediumRiskData = dist.map(item => item.mediumRisk || 0)
    const lowRiskData = dist.map(item => item.lowRisk || 0)

    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['高风险', '中风险', '低风险'],
        bottom: '0%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: yData
      },
      series: [
        {
          name: '高风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#F56C6C' },
          data: highRiskData
        },
        {
          name: '中风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#E6A23C' },
          data: mediumRiskData
        },
        {
          name: '低风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#67C23A' },
          data: lowRiskData
        }
      ]
    })
  })
}

const initFaultTypeChart = () => {
  if (!faultTypeChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(faultTypeChartRef.value)
    const dist = dashboardData.value?.faultTypeDistribution

    if (!hasData(dist)) {
      chart.setOption(getEmptyOption())
      return
    }

    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#909399', '#F56C6C']
    const pieData = dist.map((item, index) => ({
      value: item.value,
      name: item.name,
      itemStyle: { color: colors[index % colors.length] }
    }))

    chart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: '0%',
        left: 'center'
      },
      series: [
        {
          name: '故障类型',
          type: 'pie',
          radius: ['30%', '60%'],
          center: ['50%', '45%'],
          data: pieData,
          label: {
            formatter: '{b}: {d}%'
          }
        }
      ]
    })
  })
}

const handleExport = () => {
  ElMessage.success('报表导出成功')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.statistics-report-container {
  padding: 0;
}

.error-container {
  padding: 60px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stats-row {
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
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.device-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.repair-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.inspection-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.rate-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 380px;
}

.chart {
  width: 100%;
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
