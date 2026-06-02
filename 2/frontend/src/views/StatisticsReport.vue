<template>
  <div class="statistics-report-container">
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

      <el-table :data="monthlyData" border stripe>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, Tools, Calendar, TrendCharts, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const statusChartRef = ref(null)
const trendChartRef = ref(null)
const completionChartRef = ref(null)
const departmentChartRef = ref(null)
const faultTypeChartRef = ref(null)

const statusChartType = ref('pie')
const trendPeriod = ref('month')
const completionPeriod = ref('month')

const summaryData = reactive({
  totalDevices: 128,
  totalRepairs: 45,
  totalInspections: 156,
  completionRate: 89.7
})

const monthlyData = ref([
  { month: '2026-01', newDevices: 5, repairOrders: 12, completedRepairs: 10, inspectionTasks: 120, completedInspections: 108, completionRate: 90, qcRecords: 45, qcPassRate: 95.6 },
  { month: '2026-02', newDevices: 3, repairOrders: 8, completedRepairs: 8, inspectionTasks: 110, completedInspections: 100, completionRate: 91, qcRecords: 42, qcPassRate: 92.9 },
  { month: '2026-03', newDevices: 8, repairOrders: 15, completedRepairs: 13, inspectionTasks: 130, completedInspections: 115, completionRate: 88, qcRecords: 50, qcPassRate: 94.0 },
  { month: '2026-04', newDevices: 6, repairOrders: 10, completedRepairs: 9, inspectionTasks: 125, completedInspections: 110, completionRate: 88, qcRecords: 48, qcPassRate: 96.2 },
  { month: '2026-05', newDevices: 4, repairOrders: 14, completedRepairs: 12, inspectionTasks: 140, completedInspections: 125, completionRate: 89, qcRecords: 52, qcPassRate: 93.5 },
  { month: '2026-06', newDevices: 2, repairOrders: 8, completedRepairs: 5, inspectionTasks: 80, completedInspections: 65, completionRate: 81, qcRecords: 28, qcPassRate: 92.9 }
])

const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#67C23A'
  if (percentage >= 80) return '#E6A23C'
  return '#F56C6C'
}

const initStatusChart = () => {
  if (!statusChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(statusChartRef.value)
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
            data: [
              { value: 105, name: '运行中', itemStyle: { color: '#67C23A' } },
              { value: 15, name: '维修中', itemStyle: { color: '#E6A23C' } },
              { value: 8, name: '待校准', itemStyle: { color: '#F56C6C' } }
            ]
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
          data: ['运行中', '维修中', '待校准']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '设备数量',
            type: 'bar',
            barWidth: '50%',
            data: [
              { value: 105, itemStyle: { color: '#67C23A' } },
              { value: 15, itemStyle: { color: '#E6A23C' } },
              { value: 8, itemStyle: { color: '#F56C6C' } }
            ]
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
    const xData = trendPeriod.value === 'week' 
      ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      : ['第1周', '第2周', '第3周', '第4周']
    const data1 = trendPeriod.value === 'week' ? [3, 5, 4, 6, 8, 2, 1] : [12, 15, 10, 8]
    const data2 = trendPeriod.value === 'week' ? [2, 4, 3, 5, 6, 1, 1] : [10, 12, 8, 6]
    
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
        data: xData
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
    const xData = completionPeriod.value === 'month' 
      ? ['第1周', '第2周', '第3周', '第4周']
      : ['1月', '2月', '3月']
    const data = completionPeriod.value === 'month' ? [85, 88, 92, 87] : [90, 91, 88]
    
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
        data: xData
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
          data: data.map((value, index) => ({
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
        data: ['检验科', '放射科', '急诊科', '外科', '内科']
      },
      series: [
        {
          name: '高风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#F56C6C' },
          data: [2, 5, 4, 2, 3]
        },
        {
          name: '中风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#E6A23C' },
          data: [1, 2, 3, 4, 5]
        },
        {
          name: '低风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#67C23A' },
          data: [5, 8, 12, 15, 20]
        }
      ]
    })
  })
}

const initFaultTypeChart = () => {
  if (!faultTypeChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(faultTypeChartRef.value)
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
          data: [
            { value: 18, name: '机械故障', itemStyle: { color: '#409EFF' } },
            { value: 12, name: '电路故障', itemStyle: { color: '#67C23A' } },
            { value: 8, name: '软件故障', itemStyle: { color: '#E6A23C' } },
            { value: 7, name: '其他故障', itemStyle: { color: '#909399' } }
          ],
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
  initStatusChart()
  initTrendChart()
  initCompletionChart()
  initDepartmentChart()
  initFaultTypeChart()
})
</script>

<style scoped>
.statistics-report-container {
  padding: 0;
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
