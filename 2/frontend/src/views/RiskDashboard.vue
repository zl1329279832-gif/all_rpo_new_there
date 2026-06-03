<template>
  <div class="risk-dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon high-risk-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highRiskCount }}</div>
              <div class="stat-label">高风险设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon medium-risk-icon">
              <el-icon><InfoFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.mediumRiskCount }}</div>
              <div class="stat-label">中风险设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon low-risk-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowRiskCount }}</div>
              <div class="stat-label">低风险设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>风险等级分布</template>
          <div ref="riskChartRef" class="chart"></div>
          <div v-if="!hasRiskData" class="empty-chart-tip">
            <el-empty description="暂无数据" :image-size="60" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>设备状态统计</template>
          <div ref="statusChartRef" class="chart"></div>
          <div v-if="!hasStatusData" class="empty-chart-tip">
            <el-empty description="暂无数据" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="high-risk-title">
            <el-icon class="warning-icon"><Warning /></el-icon>
            高风险设备列表
          </span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </div>
      </template>

      <el-table :data="highRiskDevices" v-loading="tableLoading" border stripe>
        <template #empty>
          <el-empty description="暂无高风险设备" />
        </template>
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="deviceCode" label="设备编号" min-width="120" />
        <el-table-column prop="deviceModel" label="型号" min-width="100" />
        <el-table-column prop="deptName" label="所属科室" min-width="100" />
        <el-table-column prop="riskLevel" label="风险等级" min-width="100">
          <template #default="{ row }">
            <el-tag type="danger" effect="dark">{{ getRiskText(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskReason" label="风险原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="lastInspectionDate" label="上次巡检日期" min-width="130" />
        <el-table-column prop="nextInspectionDate" label="下次巡检日期" min-width="130" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="warning" size="small" @click="handleScheduleInspection(row)">安排巡检</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchHighRiskDevices"
        @current-change="fetchHighRiskDevices"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="inspectionDialogVisible" title="安排巡检" width="500px" :close-on-click-modal="false">
      <el-form :model="inspectionForm" :rules="inspectionFormRules" ref="inspectionFormRef" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="inspectionForm.deviceName" disabled />
        </el-form-item>
        <el-form-item label="巡检类型" prop="inspectionType">
          <el-select v-model="inspectionForm.inspectionType" placeholder="请选择巡检类型" style="width: 100%">
            <el-option label="紧急巡检" value="URGENT" />
            <el-option label="常规巡检" value="NORMAL" />
            <el-option label="专项巡检" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期" prop="plannedDate">
          <el-date-picker
            v-model="inspectionForm.plannedDate"
            type="date"
            placeholder="选择计划日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="巡检人员" prop="inspector">
          <el-select v-model="inspectionForm.inspector" placeholder="请选择巡检人员" style="width: 100%">
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
            <el-option label="王五" value="王五" />
            <el-option label="赵六" value="赵六" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="inspectionForm.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitInspection" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="设备风险详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ currentDevice.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentDevice.deviceModel }}</el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ currentDevice.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">{{ currentDevice.deptName }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag type="danger" effect="dark">{{ getRiskText(currentDevice.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="采购日期">{{ currentDevice.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="使用年限">{{ currentDevice.serviceYears }}年</el-descriptions-item>
        <el-descriptions-item label="风险原因" :span="2">{{ currentDevice.riskReason }}</el-descriptions-item>
        <el-descriptions-item label="风险评估" :span="2">{{ currentDevice.riskAssessment }}</el-descriptions-item>
        <el-descriptions-item label="处理建议" :span="2">{{ currentDevice.suggestion }}</el-descriptions-item>
        <el-descriptions-item label="上次巡检日期">{{ currentDevice.lastInspectionDate }}</el-descriptions-item>
        <el-descriptions-item label="下次巡检日期">{{ currentDevice.nextInspectionDate }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, InfoFilled, CircleCheck, Monitor, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getOverview, getDashboard } from '@/api/statistics'
import { getDevicePage } from '@/api/device'

const STORAGE_KEY = 'risk_dashboard_state'

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

const loading = ref(false)
const tableLoading = ref(false)
const submitting = ref(false)
const riskChartRef = ref(null)
const statusChartRef = ref(null)
const inspectionDialogVisible = ref(false)
const detailVisible = ref(false)
const inspectionFormRef = ref(null)
let riskChart = null
let statusChart = null

const loadStateFromStorage = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      return JSON.parse(saved)
    }
  } catch (e) {
    console.error('Failed to load state from localStorage:', e)
  }
  return null
}

const savedState = loadStateFromStorage()

const pagination = reactive({
  pageNum: savedState?.pagination?.pageNum ?? 1,
  pageSize: savedState?.pagination?.pageSize ?? 10,
  total: 0
})

const saveStateToStorage = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      pagination: { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    }))
  } catch (e) {
    console.error('Failed to save state to localStorage:', e)
  }
}

watch(
  () => [pagination.pageNum, pagination.pageSize],
  () => {
    saveStateToStorage()
  }
)

const highRiskDevices = ref([])

const stats = reactive({
  highRiskCount: 0,
  mediumRiskCount: 0,
  lowRiskCount: 0,
  totalCount: 0
})

const riskLevelDistribution = ref(null)
const deptDeviceDistribution = ref(null)

const hasRiskData = computed(() => {
  if (!riskLevelDistribution.value) return false
  const { highRisk, mediumRisk, lowRisk } = riskLevelDistribution.value
  return (highRisk || 0) + (mediumRisk || 0) + (lowRisk || 0) > 0
})

const hasStatusData = computed(() => {
  return deptDeviceDistribution.value && deptDeviceDistribution.value.length > 0
})

const currentDevice = reactive({
  deviceName: '',
  deviceCode: '',
  deviceModel: '',
  manufacturer: '',
  deptName: '',
  riskLevel: null,
  purchaseDate: '',
  serviceYears: '',
  riskReason: '',
  riskAssessment: '',
  suggestion: '',
  lastInspectionDate: '',
  nextInspectionDate: ''
})

const inspectionForm = reactive({
  deviceId: null,
  deviceName: '',
  inspectionType: '',
  plannedDate: '',
  inspector: '',
  remark: ''
})

const inspectionFormRules = {
  inspectionType: [{ required: true, message: '请选择巡检类型', trigger: 'change' }],
  plannedDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }],
  inspector: [{ required: true, message: '请选择巡检人员', trigger: 'change' }]
}

const getRiskText = (level) => {
  const map = {
    3: '高风险',
    2: '中风险',
    1: '低风险'
  }
  return map[level] || '未知'
}

const initRiskChart = () => {
  if (!riskChartRef.value) return
  nextTick(() => {
    if (!riskChart) {
      riskChart = echarts.init(riskChartRef.value)
    }
    const data = riskLevelDistribution.value || { highRisk: 0, mediumRisk: 0, lowRisk: 0 }
    const hasChartData = data && (data.highRisk > 0 || data.mediumRisk > 0 || data.lowRisk > 0)
    
    if (!hasChartData) {
      riskChart.setOption(getEmptyOption())
      return
    }
    
    const chartData = [
      { value: data.highRisk || 0, name: '高风险', itemStyle: { color: '#F56C6C' } },
      { value: data.mediumRisk || 0, name: '中风险', itemStyle: { color: '#E6A23C' } },
      { value: data.lowRisk || 0, name: '低风险', itemStyle: { color: '#67C23A' } }
    ]
    riskChart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: '5%',
        left: 'center'
      },
      series: [
        {
          name: '风险等级',
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
          data: chartData
        }
      ]
    })
  })
}

const initStatusChart = () => {
  if (!statusChartRef.value) return
  nextTick(() => {
    if (!statusChart) {
      statusChart = echarts.init(statusChartRef.value)
    }
    const deptData = deptDeviceDistribution.value || []
    const hasChartData = deptData && deptData.length > 0
    
    if (!hasChartData) {
      statusChart.setOption(getEmptyOption())
      return
    }
    
    const deptNames = deptData.map(item => item.deptName || item.name || '未知科室')
    const highRiskData = deptData.map(item => item.highRisk || item.highRiskCount || 0)
    const mediumRiskData = deptData.map(item => item.mediumRisk || item.mediumRiskCount || 0)
    const lowRiskData = deptData.map(item => item.lowRisk || item.lowRiskCount || 0)
    
    statusChart.setOption({
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
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: deptNames
      },
      yAxis: {
        type: 'value'
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

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const [overviewRes, dashboardRes] = await Promise.all([
      getOverview(),
      getDashboard()
    ])
    
    const overviewData = overviewRes.data || overviewRes
    const dashboardData = dashboardRes.data || dashboardRes
    
    stats.highRiskCount = overviewData.highRiskCount || 0
    stats.totalCount = overviewData.totalDevices || overviewData.totalCount || 0
    
    riskLevelDistribution.value = dashboardData.riskLevelDistribution || null
    deptDeviceDistribution.value = dashboardData.deptDeviceDistribution || null
    
    if (riskLevelDistribution.value) {
      stats.mediumRiskCount = riskLevelDistribution.value.mediumRisk || riskLevelDistribution.value.mediumRiskCount || 0
      stats.lowRiskCount = riskLevelDistribution.value.lowRisk || riskLevelDistribution.value.lowRiskCount || 0
    }
    
    initRiskChart()
    initStatusChart()
  } catch (error) {
    console.error('获取仪表板数据失败:', error)
    ElMessage.error('获取仪表板数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const fetchHighRiskDevices = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      riskLevel: 3
    }
    const res = await getDevicePage(params)
    const data = res.data || res
    highRiskDevices.value = data.records || data.list || data.rows || []
    pagination.total = data.total || 0
    saveStateToStorage()
  } catch (error) {
    console.error('获取高风险设备列表失败:', error)
    ElMessage.error('获取高风险设备列表失败，请稍后重试')
    highRiskDevices.value = []
    pagination.total = 0
  } finally {
    tableLoading.value = false
  }
}

const fetchData = () => {
  fetchDashboardData()
  fetchHighRiskDevices()
}

const handleExport = () => {
  ElMessage.success('报告导出成功')
}

const handleView = (row) => {
  Object.keys(currentDevice).forEach(key => {
    currentDevice[key] = row[key] || ''
  })
  detailVisible.value = true
}

const handleScheduleInspection = (row) => {
  inspectionForm.deviceId = row.id
  inspectionForm.deviceName = row.deviceName
  inspectionForm.inspectionType = 'URGENT'
  inspectionForm.plannedDate = ''
  inspectionForm.inspector = ''
  inspectionForm.remark = ''
  inspectionDialogVisible.value = true
}

const handleSubmitInspection = async () => {
  if (!inspectionFormRef.value) return
  await inspectionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        ElMessage.success('巡检安排成功')
        inspectionDialogVisible.value = false
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleResize = () => {
  riskChart && riskChart.resize()
  statusChart && statusChart.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

watch(hasRiskData, () => {
  initRiskChart()
})

watch(hasStatusData, () => {
  initStatusChart()
})
</script>

<style scoped>
.risk-dashboard-container {
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

.high-risk-icon {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
}

.medium-risk-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.low-risk-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.total-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  height: 350px;
  position: relative;
}

.chart {
  width: 100%;
  height: 280px;
}

.empty-chart-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.high-risk-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #F56C6C;
}

.warning-icon {
  font-size: 18px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
