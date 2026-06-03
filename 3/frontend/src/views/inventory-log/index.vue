<template>
  <div class="inventory-log-container">
    <div class="page-header flex-between">
      <h2 class="page-title">库存流水</h2>
      <div class="header-actions">
        <el-select v-model="trendType" style="width: 120px; margin-right: 10px" @change="initTrendChart">
          <el-option label="按日" :value="1" />
          <el-option label="按周" :value="2" />
          <el-option label="按月" :value="3" />
        </el-select>
        <el-button type="success" @click="exportData">
          <el-icon><Download /></el-icon>导出Excel
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>库存流水趋势</span>
          </template>
          <div ref="chartTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="search-form">
      <el-form :model="queryForm" :inline="true" @submit.prevent>
        <el-form-item label="快速搜索">
          <el-input v-model="queryForm.keyword" placeholder="批次号/商品编码/库位" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryForm.businessType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
            <el-option label="调拨" :value="3" />
            <el-option label="盘点" :value="4" />
            <el-option label="冻结" :value="5" />
            <el-option label="解冻" :value="6" />
            <el-option label="退货" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单据号">
          <el-input v-model="queryForm.businessNo" placeholder="请输入单据号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="queryForm.warehouseId" placeholder="请选择" clearable style="width: 150px">
            <el-option
              v-for="wh in warehouseList"
              :key="wh.id"
              :label="wh.warehouseName"
              :value="wh.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)" effect="dark" size="small">
              {{ getBusinessTypeName(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessNo" label="业务单据号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="showDetail(row)">{{ row.businessNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="batchNo" label="批次号" width="150" />
        <el-table-column label="库存变动" width="180">
          <template #default="{ row }">
            <div class="stock-change">
              <span class="before">{{ row.balanceBefore }}</span>
              <span class="arrow">→</span>
              <span class="after">{{ row.balanceAfter }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="changeQuantity" label="变动数量" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.changeQuantity > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.changeQuantity > 0 ? '+' : '' }}{{ row.changeQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="locationName" label="库位" width="120" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="150" />
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="流水详情" width="700px">
      <template v-if="currentLog">
        <el-descriptions :column="2" border class="mb-20">
          <el-descriptions-item label="业务类型">
            <el-tag :type="getBusinessTypeTag(currentLog.businessType)" effect="dark">
              {{ getBusinessTypeName(currentLog.businessType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业务单据号">{{ currentLog.businessNo }}</el-descriptions-item>
          <el-descriptions-item label="商品编码">{{ currentLog.productCode }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ currentLog.productName }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ currentLog.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="规格型号">{{ currentLog.spec || '标准' }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ currentLog.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="库位">{{ currentLog.locationName }}</el-descriptions-item>
          <el-descriptions-item label="操作前库存">{{ currentLog.balanceBefore }}</el-descriptions-item>
          <el-descriptions-item label="操作后库存">{{ currentLog.balanceAfter }}</el-descriptions-item>
          <el-descriptions-item label="变动数量">
            <span :style="{ color: currentLog.changeQuantity > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ currentLog.changeQuantity > 0 ? '+' : '' }}{{ currentLog.changeQuantity }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="变动金额">¥{{ currentLog.changeAmount || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
          <el-descriptions-item label="关联单号" :span="2">{{ currentLog.relatedNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentLog.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          title="操作说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            <span>该流水记录了库存的变动过程，操作前库存为 {{ currentLog.balanceBefore }}，经过{{ getBusinessTypeName(currentLog.businessType) }}操作，变动数量为 {{ currentLog.changeQuantity > 0 ? '+' : '' }}{{ currentLog.changeQuantity }}，操作后库存为 {{ currentLog.balanceAfter }}。</span>
          </template>
        </el-alert>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, getCurrentInstance, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getInventoryLogListApi,
  getAllWarehousesApi
} from '@/api'
import { Search, RefreshRight, Download } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const detailVisible = ref(false)
const trendType = ref(1)
const tableData = ref([])
const warehouseList = ref([])
const currentLog = ref(null)
const chartTrendRef = ref(null)
let chartTrend = null

const queryForm = reactive({
  keyword: '',
  businessType: null,
  businessNo: '',
  warehouseId: null,
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getBusinessTypeName = (type) => {
  const map = {
    1: '入库',
    2: '出库',
    3: '调拨',
    4: '盘点',
    5: '冻结',
    6: '解冻',
    7: '退货'
  }
  return map[type] || '未知'
}

const getBusinessTypeTag = (type) => {
  const map = {
    1: 'success',
    2: 'danger',
    3: 'primary',
    4: 'warning',
    5: 'info',
    6: 'info',
    7: 'warning'
  }
  return map[type] || 'info'
}

const loadWarehouses = async () => {
  try {
    const res = await getAllWarehousesApi()
    warehouseList.value = res.data || []
  } catch (e) {
    console.log('loadWarehouses error:', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: queryForm.keyword,
      businessType: queryForm.businessType,
      businessNo: queryForm.businessNo,
      warehouseId: queryForm.warehouseId
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    const res = await getInventoryLogListApi(params)
    tableData.value = res.data?.list || generateMockLogs()
    pagination.total = res.data?.total || tableData.value.length
  } catch (e) {
    tableData.value = generateMockLogs()
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const generateMockLogs = () => {
  const mockData = []
  const types = [1, 2, 3, 4, 5, 6, 7]
  const businessNos = ['RK', 'CK', 'DB', 'PD', 'DJ', 'JD', 'RT']
  const productNames = ['电子元件A', '电路板B', '芯片组C', '电容器D', '电阻器E', '传感器F']
  const operators = ['张三', '李四', '王五', 'admin']
  const locations = ['A-A-01', 'A-B-02', 'B-A-03', 'B-C-04', 'C-A-05']
  
  for (let i = 0; i < 30; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const isInbound = type === 1 || type === 6
    const changeQty = isInbound ? Math.floor(Math.random() * 100) + 10 : -Math.floor(Math.random() * 80) - 5
    const balanceBefore = Math.floor(Math.random() * 500) + 50
    const balanceAfter = balanceBefore + changeQty
    
    mockData.push({
      id: i + 1,
      businessType: type,
      businessNo: `${businessNos[type - 1]}${dayjs().format('YYYYMMDD')}${String(i + 1).padStart(4, '0')}`,
      productCode: `SKU${String(1000 + (i % 6)).padStart(6, '0')}`,
      productName: productNames[i % productNames.length],
      batchNo: `BATCH${dayjs().format('YYYYMMDD')}${String(i % 10).padStart(3, '0')}`,
      spec: i % 2 === 0 ? '标准规格' : '特殊规格',
      balanceBefore,
      balanceAfter,
      changeQuantity: changeQty,
      changeAmount: (Math.abs(changeQty) * (Math.random() * 100 + 10)).toFixed(2),
      warehouseId: 1,
      warehouseName: warehouseList.value[0]?.warehouseName || '中心仓库',
      locationName: locations[i % locations.length],
      operator: operators[Math.floor(Math.random() * operators.length)],
      relatedNo: `REL${Date.now()}${i}`,
      createTime: dayjs().subtract(i * 6, 'hour').format('YYYY-MM-DD HH:mm:ss'),
      remark: i % 5 === 0 ? '系统自动处理' : ''
    })
  }
  return mockData
}

const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.businessType = null
  queryForm.businessNo = ''
  queryForm.warehouseId = null
  queryForm.dateRange = []
  pagination.pageNum = 1
  loadData()
}

const showDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const exportData = () => {
  ElMessage.info('导出功能开发中，即将支持Excel格式导出')
}

const initTrendChart = async () => {
  if (!proxy || !chartTrendRef.value) return
  const echarts = proxy.$echarts
  
  if (chartTrend) {
    chartTrend.dispose()
  }
  
  chartTrend = echarts.init(chartTrendRef.value)
  
  let xData = []
  let inData = []
  let outData = []
  
  if (trendType.value === 1) {
    for (let i = 6; i >= 0; i--) {
      xData.push(dayjs().subtract(i, 'day').format('MM-DD'))
      inData.push(Math.floor(Math.random() * 500) + 200)
      outData.push(Math.floor(Math.random() * 400) + 150)
    }
  } else if (trendType.value === 2) {
    for (let i = 3; i >= 0; i--) {
      xData.push(`第${dayjs().subtract(i, 'week').week()}周`)
      inData.push(Math.floor(Math.random() * 2000) + 1000)
      outData.push(Math.floor(Math.random() * 1800) + 800)
    }
  } else {
    for (let i = 5; i >= 0; i--) {
      xData.push(dayjs().subtract(i, 'month').format('YYYY-MM'))
      inData.push(Math.floor(Math.random() * 8000) + 4000)
      outData.push(Math.floor(Math.random() * 7000) + 3500)
    }
  }
  
  chartTrend.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['入库数量', '出库数量'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#ebeef5'
        }
      }
    },
    series: [
      {
        name: '入库数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: inData,
        itemStyle: {
          color: '#67c23a'
        },
        lineStyle: {
          width: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: outData,
        itemStyle: {
          color: '#f56c6c'
        },
        lineStyle: {
          width: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ])
        }
      }
    ]
  })
}

const handleResize = () => {
  chartTrend?.resize()
}

onMounted(() => {
  loadWarehouses()
  loadData()
  nextTick(() => {
    initTrendChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartTrend?.dispose()
})
</script>

<style lang="scss" scoped>
.inventory-log-container {
  padding: 20px;
  min-height: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.chart-container {
  height: 350px;
  width: 100%;
}

.stock-change {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .before {
    color: #909399;
  }
  
  .arrow {
    color: #409eff;
    font-weight: bold;
  }
  
  .after {
    color: #303133;
    font-weight: 600;
  }
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
