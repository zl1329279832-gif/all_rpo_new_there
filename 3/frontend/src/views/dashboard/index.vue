<template>
  <div class="dashboard-container">
    <div class="page-header flex-between">
      <h2 class="page-title">预警看板</h2>
      <div class="header-actions">
        <el-tag :type="autoRefresh ? 'success' : 'info'" class="mr-10">
          {{ autoRefresh ? '自动刷新中' : '自动刷新已暂停' }}
        </el-tag>
        <el-switch
          v-model="autoRefresh"
          active-text="30秒"
          inactive-text="关闭"
          class="mr-10"
        />
        <el-button type="primary" @click="loadAllData">
          <el-icon><Refresh /></el-icon>手动刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="4.8" v-for="stat in statCards" :key="stat.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :class="stat.iconClass">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                <el-badge :value="stat.value" :hidden="stat.value === 0" :type="stat.badgeType">
                  {{ stat.value || 0 }}
                </el-badge>
              </div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>预警类型分布</span>
          </template>
          <div ref="chartTypeRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>预警级别分布</span>
          </template>
          <div ref="chartLevelRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="flex-between">
              <span>预警趋势（近7天）</span>
              <el-radio-group v-model="trendType" size="small" @change="initTrendChart">
                <el-radio-button value="count">数量</el-radio-button>
                <el-radio-button value="level">级别</el-radio-button>
              </el-radio-group>
            </span>
          </template>
          <div ref="chartTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <template #header>
        <span class="flex-between">
          <span>预警列表</span>
          <div class="filter-actions">
            <el-select v-model="filterLevel" placeholder="全部级别" clearable style="width: 120px; margin-right: 10px" @change="loadAlertList">
              <el-option label="紧急" :value="1" />
              <el-option label="高" :value="2" />
              <el-option label="中" :value="3" />
              <el-option label="低" :value="4" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 120px" @change="loadAlertList">
              <el-option label="待处理" :value="0" />
              <el-option label="已处理" :value="1" />
            </el-select>
          </div>
        </span>
      </template>
      <el-table :data="alertList" v-loading="loading" stripe @sort-change="handleSortChange">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="alertType" label="预警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeTag(row.alertType)">{{ getAlertTypeName(row.alertType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="优先级" width="100" sortable="custom">
          <template #default="{ row }">
            <el-tag :type="getAlertLevelTag(row.alertLevel)" effect="dark">{{ getAlertLevelName(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="message" label="预警内容" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              link
              @click="openHandleDialog(row)"
            >
              处理
            </el-button>
            <el-button type="info" size="small" link @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAlertList"
          @current-change="loadAlertList"
        />
      </div>
    </el-card>

    <el-dialog v-model="handleDialogVisible" title="处理预警" width="500px">
      <el-form :model="handleForm" :rules="handleRules" ref="handleFormRef" label-width="100px">
        <el-form-item label="预警类型">
          <el-tag :type="getAlertTypeTag(currentAlert?.alertType)">{{ getAlertTypeName(currentAlert?.alertType) }}</el-tag>
        </el-form-item>
        <el-form-item label="预警内容">
          <span>{{ currentAlert?.message }}</span>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-select v-model="handleForm.handleResult" placeholder="请选择处理结果">
            <el-option label="已确认并处理" value="已确认并处理" />
            <el-option label="已通知相关人员" value="已通知相关人员" />
            <el-option label="误报，已忽略" value="误报，已忽略" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="预警详情" width="600px">
      <el-descriptions :column="2" border v-if="currentAlert">
        <el-descriptions-item label="预警ID">{{ currentAlert.id }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">
          <el-tag :type="getAlertTypeTag(currentAlert.alertType)">{{ getAlertTypeName(currentAlert.alertType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警级别">
          <el-tag :type="getAlertLevelTag(currentAlert.alertLevel)" effect="dark">{{ getAlertLevelName(currentAlert.alertLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentAlert.status === 0 ? 'warning' : 'success'">
            {{ currentAlert.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentAlert.productName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentAlert.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="预警内容" :span="2">{{ currentAlert.message }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentAlert.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlert.handleResult" label="处理结果" :span="2">{{ currentAlert.handleResult }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlert.handleRemark" label="处理备注" :span="2">{{ currentAlert.handleRemark }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlert.handleTime" label="处理时间" :span="2">{{ currentAlert.handleTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, getCurrentInstance, markRaw } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlertDashboardApi, getAlertListApi, checkAlertApi, handleAlertApi } from '@/api'
import { Refresh, Warning, Clock, Bottom, Top, Bell } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const autoRefresh = ref(true)
const trendType = ref('count')
const filterLevel = ref(null)
const filterStatus = ref(null)
const dashboardData = ref({})
const alertList = ref([])
const handleDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentAlert = ref(null)
const handleFormRef = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const handleForm = reactive({
  handleResult: '',
  handleRemark: ''
})

const handleRules = {
  handleResult: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  handleRemark: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

const chartTypeRef = ref(null)
const chartLevelRef = ref(null)
const chartTrendRef = ref(null)

let chartType = null
let chartLevel = null
let chartTrend = null
let refreshTimer = null

const statCards = computed(() => [
  {
    key: 'pending',
    label: '待处理预警',
    value: dashboardData.value.pendingCount || 0,
    icon: markRaw(Bell),
    iconClass: 'warning',
    badgeType: 'warning'
  },
  {
    key: 'expiring',
    label: '临期预警',
    value: dashboardData.value.expiringCount || 0,
    icon: markRaw(Clock),
    iconClass: 'orange',
    badgeType: 'warning'
  },
  {
    key: 'expired',
    label: '过期预警',
    value: dashboardData.value.expiredCount || 0,
    icon: markRaw(Warning),
    iconClass: 'danger',
    badgeType: 'danger'
  },
  {
    key: 'insufficient',
    label: '库存不足',
    value: dashboardData.value.insufficientCount || 0,
    icon: markRaw(Bottom),
    iconClass: 'info',
    badgeType: 'info'
  },
  {
    key: 'overstock',
    label: '库存超储',
    value: dashboardData.value.overstockCount || 0,
    icon: markRaw(Top),
    iconClass: 'success',
    badgeType: 'success'
  }
])

const getAlertTypeName = (type) => {
  const map = { 1: '临期预警', 2: '过期预警', 3: '库存下限', 4: '库存上限', 5: '质量预警', 6: '设备预警' }
  return map[type] || '未知'
}

const getAlertTypeTag = (type) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'info', 4: 'success', 5: 'warning', 6: 'primary' }
  return map[type] || 'info'
}

const getAlertLevelName = (level) => {
  const map = { 1: '紧急', 2: '高', 3: '中', 4: '低' }
  return map[level] || '未知'
}

const getAlertLevelTag = (level) => {
  const map = { 1: 'danger', 2: 'warning', 3: 'warning', 4: 'info' }
  return map[level] || 'info'
}

const generateMockDashboard = () => {
  return {
    pendingCount: Math.floor(Math.random() * 20) + 5,
    expiringCount: Math.floor(Math.random() * 15) + 3,
    expiredCount: Math.floor(Math.random() * 8) + 1,
    insufficientCount: Math.floor(Math.random() * 25) + 5,
    overstockCount: Math.floor(Math.random() * 10) + 2
  }
}

const generateMockAlerts = () => {
  const types = [1, 2, 3, 4, 5, 6]
  const levels = [1, 2, 3, 4]
  const statuses = [0, 1]
  const products = ['商品A', '商品B', '商品C', '商品D', '商品E', '商品F']
  const warehouses = ['中心仓库', '华东分仓', '华南分仓', '华北分仓']
  const messages = {
    1: '商品将于7天内过期，请及时处理',
    2: '商品已过期，请立即下架',
    3: '库存低于安全库存，请及时补货',
    4: '库存超过上限，请控制入库',
    5: '商品质量检测异常',
    6: '仓储设备运行异常'
  }

  const list = []
  for (let i = 1; i <= 20; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const level = levels[Math.floor(Math.random() * levels.length)]
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    list.push({
      id: i,
      alertType: type,
      alertLevel: level,
      productName: products[Math.floor(Math.random() * products.length)],
      warehouseName: warehouses[Math.floor(Math.random() * warehouses.length)],
      message: messages[type],
      status: status,
      createTime: dayjs().subtract(Math.floor(Math.random() * 7), 'day').format('YYYY-MM-DD HH:mm:ss'),
      handleResult: status === 1 ? '已确认并处理' : null,
      handleRemark: status === 1 ? '已通知仓库管理员处理' : null,
      handleTime: status === 1 ? dayjs().subtract(Math.floor(Math.random() * 3), 'day').format('YYYY-MM-DD HH:mm:ss') : null
    })
  }
  return list.sort((a, b) => a.alertLevel - b.alertLevel)
}

const loadDashboardData = async () => {
  try {
    const res = await getAlertDashboardApi()
    dashboardData.value = res.data || generateMockDashboard()
  } catch (e) {
    console.log('loadDashboardData error:', e)
    dashboardData.value = generateMockDashboard()
  }
}

const loadAlertList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      alertLevel: filterLevel.value,
      status: filterStatus.value
    }
    const res = await getAlertListApi(params)
    alertList.value = res.data?.list || generateMockAlerts()
    pagination.total = res.data?.total || 50
  } catch (e) {
    console.log('loadAlertList error:', e)
    alertList.value = generateMockAlerts()
    pagination.total = 50
  } finally {
    loading.value = false
  }
}

const loadAllData = () => {
  loadDashboardData()
  loadAlertList()
  initCharts()
  ElMessage.success('数据已刷新')
}

const handleSortChange = ({ prop, order }) => {
  if (prop === 'alertLevel' && order) {
    alertList.value.sort((a, b) => {
      return order === 'ascending' ? a.alertLevel - b.alertLevel : b.alertLevel - a.alertLevel
    })
  }
}

const openHandleDialog = (row) => {
  currentAlert.value = row
  handleForm.handleResult = ''
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

const viewDetail = (row) => {
  currentAlert.value = row
  detailDialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleFormRef.value) return
  await handleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await ElMessageBox.confirm('确认标记该预警为已处理？', '确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await handleAlertApi(currentAlert.value.id, handleForm)
        ElMessage.success('处理成功')
        handleDialogVisible.value = false
        loadAllData()
      } catch (e) {
        if (e !== 'cancel') {
          console.log('submitHandle error:', e)
          ElMessage.error('处理失败，请重试')
        }
      }
    }
  })
}

const initTypeChart = () => {
  if (!chartTypeRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartType = echarts.init(chartTypeRef.value)
  chartType.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      name: '预警类型',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: [
        { value: 35, name: '临期预警', itemStyle: { color: '#e6a23c' } },
        { value: 20, name: '过期预警', itemStyle: { color: '#f56c6c' } },
        { value: 30, name: '库存下限', itemStyle: { color: '#909399' } },
        { value: 15, name: '库存上限', itemStyle: { color: '#67c23a' } }
      ]
    }]
  })
}

const initLevelChart = () => {
  if (!chartLevelRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartLevel = echarts.init(chartLevelRef.value)
  chartLevel.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      name: '预警级别',
      type: 'pie',
      radius: '60%',
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 5 },
      label: { show: true, formatter: '{b}: {d}%' },
      data: [
        { value: 15, name: '紧急', itemStyle: { color: '#f56c6c' } },
        { value: 35, name: '高', itemStyle: { color: '#e6a23c' } },
        { value: 30, name: '中', itemStyle: { color: '#f0c619' } },
        { value: 20, name: '低', itemStyle: { color: '#409eff' } }
      ]
    }]
  })
}

const initTrendChart = () => {
  if (!chartTrendRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartTrend = echarts.init(chartTrendRef.value)
  
  const days = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('MM-DD'))
  }

  const option = trendType.value === 'count' ? {
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增预警', '处理预警'], bottom: '5%' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增预警',
        type: 'line',
        smooth: true,
        data: [8, 12, 6, 15, 10, 5, 8],
        itemStyle: { color: '#f56c6c' },
        areaStyle: { color: 'rgba(245, 108, 108, 0.1)' }
      },
      {
        name: '处理预警',
        type: 'line',
        smooth: true,
        data: [6, 10, 8, 12, 9, 7, 6],
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: 'rgba(103, 194, 58, 0.1)' }
      }
    ]
  } : {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['紧急', '高', '中', '低'], bottom: '5%' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value' },
    series: [
      { name: '紧急', type: 'bar', stack: 'total', data: [2, 3, 1, 4, 2, 1, 2], itemStyle: { color: '#f56c6c' } },
      { name: '高', type: 'bar', stack: 'total', data: [3, 5, 2, 6, 4, 2, 3], itemStyle: { color: '#e6a23c' } },
      { name: '中', type: 'bar', stack: 'total', data: [2, 3, 2, 3, 2, 1, 2], itemStyle: { color: '#f0c619' } },
      { name: '低', type: 'bar', stack: 'total', data: [1, 1, 1, 2, 2, 1, 1], itemStyle: { color: '#409eff' } }
    ]
  }

  chartTrend.setOption(option)
}

const initCharts = () => {
  initTypeChart()
  initLevelChart()
  initTrendChart()
}

const handleResize = () => {
  chartType?.resize()
  chartLevel?.resize()
  chartTrend?.resize()
}

const startAutoRefresh = () => {
  if (refreshTimer) clearInterval(refreshTimer)
  refreshTimer = setInterval(() => {
    if (autoRefresh.value) {
      loadAllData()
    }
  }, 30000)
}

onMounted(() => {
  loadAllData()
  window.addEventListener('resize', handleResize)
  startAutoRefresh()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) clearInterval(refreshTimer)
  chartType?.dispose()
  chartLevel?.dispose()
  chartTrend?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  min-height: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mr-10 {
  margin-right: 10px;
}

.filter-actions {
  display: flex;
  align-items: center;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    flex-shrink: 0;
    
    &.warning {
      background: linear-gradient(135deg, #f6d365, #fda085);
    }
    &.orange {
      background: linear-gradient(135deg, #ff9a56, #ff6b6b);
    }
    &.danger {
      background: linear-gradient(135deg, #ff6b6b, #ee5a52);
    }
    &.info {
      background: linear-gradient(135deg, #89f7fe, #66a6ff);
    }
    &.success {
      background: linear-gradient(135deg, #84fab0, #8fd3f4);
    }
  }
  
  .stat-info {
    .stat-value {
      font-size: 26px;
      font-weight: 600;
      color: #303133;
      line-height: 1.2;
    }
    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.chart-container {
  height: 280px;
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
</style>
