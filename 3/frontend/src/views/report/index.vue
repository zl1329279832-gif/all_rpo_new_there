<template>
  <div class="report-container">
    <div class="page-header flex-between">
      <h2 class="page-title">统计报表</h2>
      <div class="header-actions">
        <el-radio-group v-model="timeRangeType" size="default" @change="handleTimeRangeChange">
          <el-radio-button value="today">今日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="custom">自定义</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-if="timeRangeType === 'custom'"
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px; margin-left: 10px"
          @change="loadReportData"
        />
        <el-button type="primary" @click="refresh" class="ml-10">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
        <el-dropdown @command="handleExport" class="ml-10">
          <el-button type="success">
            <el-icon><Download /></el-icon>导出
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="excel">导出 Excel</el-dropdown-item>
              <el-dropdown-item command="pdf">导出 PDF</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="stat in overviewStats" :key="stat.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :class="stat.iconClass">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value || 0 }}</div>
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
                <el-icon v-if="stat.trend > 0"><Top /></el-icon>
                <el-icon v-else><Bottom /></el-icon>
                {{ Math.abs(stat.trend) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="mb-20">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="入库统计" name="receipt">
          <div class="chart-header flex-between">
            <span class="chart-title">入库统计分析</span>
            <el-radio-group v-model="receiptGroupBy" size="small" @change="initReceiptChart">
              <el-radio-button value="product">按商品</el-radio-button>
              <el-radio-button value="supplier">按供应商</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="chartReceiptRef" class="chart-container-large"></div>
        </el-tab-pane>

        <el-tab-pane label="出库统计" name="shipment">
          <div class="chart-header flex-between">
            <span class="chart-title">出库统计分析</span>
            <el-radio-group v-model="shipmentGroupBy" size="small" @change="initShipmentChart">
              <el-radio-button value="product">按商品</el-radio-button>
              <el-radio-button value="customer">按客户</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="chartShipmentRef" class="chart-container-large"></div>
        </el-tab-pane>

        <el-tab-pane label="库存分析" name="inventory">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover" class="sub-card">
                <template #header>
                  <span>库存周转率趋势</span>
                </template>
                <div ref="chartTurnoverRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover" class="sub-card">
                <template #header>
                  <span>库位利用率</span>
                </template>
                <div ref="chartUtilizationRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="盘点差异" name="stocktake">
          <div class="stocktake-tab-container">
            <div class="chart-header">
              <span class="chart-title">盘点差异统计</span>
            </div>
            <div class="stocktake-table-wrapper">
              <el-table :data="stocktakeData" v-loading="loading" border stripe height="100%">
                <el-table-column prop="warehouseName" label="仓库" width="150" />
                <el-table-column prop="productName" label="商品名称" width="150" />
                <el-table-column prop="batchNo" label="批次号" width="150" />
                <el-table-column prop="systemQty" label="系统数量" width="120" align="right" />
                <el-table-column prop="actualQty" label="实际数量" width="120" align="right" />
                <el-table-column label="差异数量" width="120" align="right">
                  <template #default="{ row }">
                    <span :class="row.diffQty > 0 ? 'profit' : 'loss'">
                      {{ row.diffQty > 0 ? '+' : '' }}{{ row.diffQty }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="差异金额" width="120" align="right">
                  <template #default="{ row }">
                    <span :class="row.diffAmount > 0 ? 'profit' : 'loss'">
                      ¥{{ row.diffAmount > 0 ? '+' : '' }}{{ row.diffAmount.toFixed(2) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="差异类型" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.diffQty > 0 ? 'success' : 'danger'">
                      {{ row.diffQty > 0 ? '盘盈' : '盘亏' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="stocktakeTime" label="盘点时间" width="180" />
              </el-table>
            </div>
            <div class="stocktake-summary">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="summary-card profit">
                  <div class="summary-label">盘盈金额</div>
                  <div class="summary-value">¥{{ totalProfit.toFixed(2) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-card loss">
                  <div class="summary-label">盘亏金额</div>
                  <div class="summary-value">¥{{ totalLoss.toFixed(2) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-card net">
                  <div class="summary-label">净差异</div>
                  <div class="summary-value">
                    <span :class="netDiff > 0 ? 'profit' : 'loss'">
                      ¥{{ netDiff > 0 ? '+' : '' }}{{ netDiff.toFixed(2) }}
                    </span>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, getCurrentInstance, markRaw } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportOverviewApi, exportReportApi } from '@/api'
import { Refresh, Download, ArrowDown, Collection, Goods, Box, Warning, Top, Bottom, TrendCharts } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const timeRangeType = ref('week')
const activeTab = ref('receipt')
const receiptGroupBy = ref('product')
const shipmentGroupBy = ref('product')
const dateRange = ref([])

const overviewData = reactive({
  totalReceipt: 0,
  totalShipment: 0,
  totalInventory: 0,
  totalAlert: 0,
  receiptTrend: 0,
  shipmentTrend: 0,
  inventoryTrend: 0,
  alertTrend: 0
})

const stocktakeData = ref([])

const chartReceiptRef = ref(null)
const chartShipmentRef = ref(null)
const chartTurnoverRef = ref(null)
const chartUtilizationRef = ref(null)

let chartReceipt = null
let chartShipment = null
let chartTurnover = null
let chartUtilization = null

const overviewStats = computed(() => [
  {
    key: 'receipt',
    label: '入库单总数',
    value: overviewData.totalReceipt,
    trend: overviewData.receiptTrend,
    icon: markRaw(Collection),
    iconClass: 'primary'
  },
  {
    key: 'shipment',
    label: '出库单总数',
    value: overviewData.totalShipment,
    trend: overviewData.shipmentTrend,
    icon: markRaw(Goods),
    iconClass: 'success'
  },
  {
    key: 'inventory',
    label: '当前库存总量',
    value: overviewData.totalInventory,
    trend: overviewData.inventoryTrend,
    icon: markRaw(Box),
    iconClass: 'warning'
  },
  {
    key: 'alert',
    label: '待处理预警',
    value: overviewData.totalAlert,
    trend: overviewData.alertTrend,
    icon: markRaw(Warning),
    iconClass: 'danger'
  }
])

const totalProfit = computed(() => {
  return stocktakeData.value.filter(d => d.diffAmount > 0).reduce((sum, d) => sum + d.diffAmount, 0)
})

const totalLoss = computed(() => {
  return Math.abs(stocktakeData.value.filter(d => d.diffAmount < 0).reduce((sum, d) => sum + d.diffAmount, 0))
})

const netDiff = computed(() => {
  return totalProfit.value - totalLoss.value
})

const handleTimeRangeChange = () => {
  const now = dayjs()
  if (timeRangeType.value === 'today') {
    dateRange.value = [now.format('YYYY-MM-DD'), now.format('YYYY-MM-DD')]
  } else if (timeRangeType.value === 'week') {
    dateRange.value = [now.startOf('week').format('YYYY-MM-DD'), now.endOf('week').format('YYYY-MM-DD')]
  } else if (timeRangeType.value === 'month') {
    dateRange.value = [now.startOf('month').format('YYYY-MM-DD'), now.endOf('month').format('YYYY-MM-DD')]
  }
  if (timeRangeType.value !== 'custom') {
    loadReportData()
  }
}

const handleTabChange = (tab) => {
  setTimeout(() => {
    if (tab === 'receipt') initReceiptChart()
    else if (tab === 'shipment') initShipmentChart()
    else if (tab === 'inventory') {
      initTurnoverChart()
      initUtilizationChart()
    }
  }, 100)
}

const generateMockStocktake = () => {
  const warehouses = ['中心仓库', '华东分仓', '华南分仓', '华北分仓']
  const products = ['商品A', '商品B', '商品C', '商品D', '商品E', '商品F', '商品G', '商品H']
  
  const list = []
  for (let i = 0; i < 15; i++) {
    const systemQty = Math.floor(Math.random() * 500) + 50
    const diff = Math.floor(Math.random() * 40) - 20
    const actualQty = systemQty + diff
    const price = (Math.random() * 200 + 10).toFixed(2)
    
    list.push({
      id: i + 1,
      warehouseName: warehouses[Math.floor(Math.random() * warehouses.length)],
      productName: products[Math.floor(Math.random() * products.length)],
      batchNo: `B${dayjs().format('YYYYMMDD')}${String(i + 1).padStart(4, '0')}`,
      systemQty,
      actualQty,
      diffQty: diff,
      diffAmount: diff * price,
      unitPrice: price,
      stocktakeTime: dayjs().subtract(Math.floor(Math.random() * 7), 'day').format('YYYY-MM-DD HH:mm:ss')
    })
  }
  return list
}

const loadReportData = async () => {
  loading.value = true
  try {
    const params = {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
    const res = await getReportOverviewApi(params)
    const data = res.data || {}
    Object.assign(overviewData, {
      totalReceipt: data.totalReceipt || Math.floor(Math.random() * 100) + 50,
      totalShipment: data.totalShipment || Math.floor(Math.random() * 80) + 40,
      totalInventory: data.totalInventory || Math.floor(Math.random() * 5000) + 10000,
      totalAlert: data.totalAlert || Math.floor(Math.random() * 20) + 5,
      receiptTrend: data.receiptTrend || Math.floor(Math.random() * 30) - 10,
      shipmentTrend: data.shipmentTrend || Math.floor(Math.random() * 25) - 5,
      inventoryTrend: data.inventoryTrend || Math.floor(Math.random() * 15) - 5,
      alertTrend: data.alertTrend || Math.floor(Math.random() * 20) - 15
    })
    stocktakeData.value = generateMockStocktake()
  } catch (e) {
    console.log('loadReportData error:', e)
    Object.assign(overviewData, {
      totalReceipt: Math.floor(Math.random() * 100) + 50,
      totalShipment: Math.floor(Math.random() * 80) + 40,
      totalInventory: Math.floor(Math.random() * 5000) + 10000,
      totalAlert: Math.floor(Math.random() * 20) + 5,
      receiptTrend: Math.floor(Math.random() * 30) - 10,
      shipmentTrend: Math.floor(Math.random() * 25) - 5,
      inventoryTrend: Math.floor(Math.random() * 15) - 5,
      alertTrend: Math.floor(Math.random() * 20) - 15
    })
    stocktakeData.value = generateMockStocktake()
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadReportData()
  handleTabChange(activeTab.value)
  ElMessage.success('数据已刷新')
}

const handleExport = async (type) => {
  try {
    await ElMessageBox.confirm(`确认导出当前报表为${type.toUpperCase()}格式？`, '导出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    try {
      await exportReportApi({ type, startDate: dateRange.value[0], endDate: dateRange.value[1], tab: activeTab.value })
      ElMessage.success(`${type.toUpperCase()}导出成功`)
    } catch (e) {
      console.log('export error:', e)
      ElMessage.info('导出功能开发中，请稍后重试')
    }
  } catch (e) {
    if (e !== 'cancel') console.log(e)
  }
}

const initReceiptChart = () => {
  if (!chartReceiptRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartReceipt = echarts.init(chartReceiptRef.value)
  
  const isProduct = receiptGroupBy.value === 'product'
  const categories = isProduct 
    ? ['商品A', '商品B', '商品C', '商品D', '商品E', '商品F', '商品G', '商品H']
    : ['供应商甲', '供应商乙', '供应商丙', '供应商丁', '供应商戊', '供应商己']
  
  const qtyData = categories.map(() => Math.floor(Math.random() * 500) + 100)
  const amountData = categories.map(() => Math.floor(Math.random() * 50000) + 10000)
  
  chartReceipt.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['入库数量', '入库金额'], bottom: '5%' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { rotate: isProduct ? 30 : 0 }
    },
    yAxis: [
      { type: 'value', name: '数量', position: 'left' },
      { type: 'value', name: '金额(元)', position: 'right' }
    ],
    series: [
      {
        name: '入库数量',
        type: 'bar',
        data: qtyData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '入库金额',
        type: 'bar',
        yAxisIndex: 1,
        data: amountData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#84fab0' },
            { offset: 1, color: '#11998e' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  })
}

const initShipmentChart = () => {
  if (!chartShipmentRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartShipment = echarts.init(chartShipmentRef.value)
  
  const isProduct = shipmentGroupBy.value === 'product'
  const categories = isProduct 
    ? ['商品A', '商品B', '商品C', '商品D', '商品E', '商品F', '商品G', '商品H']
    : ['客户A', '客户B', '客户C', '客户D', '客户E', '客户F', '客户G']
  
  const qtyData = categories.map(() => Math.floor(Math.random() * 400) + 80)
  const amountData = categories.map(() => Math.floor(Math.random() * 40000) + 8000)
  
  chartShipment.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['出库数量', '出库金额'], bottom: '5%' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { rotate: isProduct ? 30 : 0 }
    },
    yAxis: [
      { type: 'value', name: '数量', position: 'left' },
      { type: 'value', name: '金额(元)', position: 'right' }
    ],
    series: [
      {
        name: '出库数量',
        type: 'bar',
        data: qtyData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ff9a9e' },
            { offset: 1, color: '#f5576c' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '出库金额',
        type: 'bar',
        yAxisIndex: 1,
        data: amountData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffecd2' },
            { offset: 1, color: '#fcb69f' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  })
}

const initTurnoverChart = () => {
  if (!chartTurnoverRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartTurnover = echarts.init(chartTurnoverRef.value)
  
  const months = []
  for (let i = 11; i >= 0; i--) {
    months.push(dayjs().subtract(i, 'month').format('YYYY-MM'))
  }
  
  chartTurnover.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['库存周转率'], bottom: '5%' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months
    },
    yAxis: { type: 'value', name: '周转次数' },
    series: [
      {
        name: '库存周转率',
        type: 'line',
        smooth: true,
        data: months.map(() => (Math.random() * 3 + 0.5).toFixed(2)),
        itemStyle: { color: '#8e7cc3' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(142, 124, 195, 0.3)' },
            { offset: 1, color: 'rgba(142, 124, 195, 0.05)' }
          ])
        },
        markPoint: {
          data: [
            { type: 'max', name: '最大值' },
            { type: 'min', name: '最小值' }
          ]
        }
      }
    ]
  })
}

const initUtilizationChart = () => {
  if (!chartUtilizationRef.value || !proxy) return
  const echarts = proxy.$echarts
  
  chartUtilization = echarts.init(chartUtilizationRef.value)
  
  chartUtilization.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}% ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [{
      name: '库位利用率',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}%' },
      data: [
        { value: 35, name: '空闲', itemStyle: { color: '#67c23a' } },
        { value: 40, name: '已使用', itemStyle: { color: '#409eff' } },
        { value: 15, name: '已满', itemStyle: { color: '#f56c6c' } },
        { value: 10, name: '冻结', itemStyle: { color: '#909399' } }
      ]
    }]
  })
}

const handleResize = () => {
  chartReceipt?.resize()
  chartShipment?.resize()
  chartTurnover?.resize()
  chartUtilization?.resize()
}

onMounted(() => {
  handleTimeRangeChange()
  setTimeout(() => {
    handleTabChange(activeTab.value)
  }, 200)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartReceipt?.dispose()
  chartShipment?.dispose()
  chartTurnover?.dispose()
  chartUtilization?.dispose()
})
</script>

<style lang="scss" scoped>
.report-container {
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

.ml-10 {
  margin-left: 10px;
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
    
    &.primary {
      background: linear-gradient(135deg, #667eea, #764ba2);
    }
    &.success {
      background: linear-gradient(135deg, #11998e, #38ef7d);
    }
    &.warning {
      background: linear-gradient(135deg, #f093fb, #f5576c);
    }
    &.danger {
      background: linear-gradient(135deg, #ff6b6b, #ee5a52);
    }
  }
  
  .stat-info {
    flex: 1;
    
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
    .stat-trend {
      font-size: 12px;
      margin-top: 4px;
      display: flex;
      align-items: center;
      gap: 2px;
      
      &.up {
        color: #67c23a;
      }
      &.down {
        color: #f56c6c;
      }
    }
  }
}

.chart-header {
  margin-bottom: 15px;
  
  .chart-title {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
  }
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart-container-large {
  height: 400px;
  width: 100%;
}

.sub-card {
  margin-bottom: 0;
}

:deep(.el-tabs__content) {
  padding-top: 10px;
  height: calc(100vh - 400px);
  overflow-y: auto;
}

.stocktake-tab-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.stocktake-table-wrapper {
  flex: 1;
  min-height: 400px;
  overflow: hidden;
}

.profit {
  color: #67c23a;
  font-weight: 500;
}

.loss {
  color: #f56c6c;
  font-weight: 500;
}

.stocktake-summary {
  margin-top: 20px;
  
  .summary-card {
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    
    &.profit {
      background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.05));
      border: 1px solid rgba(103, 194, 58, 0.2);
    }
    &.loss {
      background: linear-gradient(135deg, rgba(245, 108, 108, 0.1), rgba(245, 108, 108, 0.05));
      border: 1px solid rgba(245, 108, 108, 0.2);
    }
    &.net {
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.05));
      border: 1px solid rgba(64, 158, 255, 0.2);
    }
    
    .summary-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }
    .summary-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }
}
</style>
