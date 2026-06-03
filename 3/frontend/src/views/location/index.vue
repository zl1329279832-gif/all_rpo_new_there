<template>
  <div class="location-container">
    <div class="page-header flex-between">
      <h2 class="page-title">仓库库位视图</h2>
      <div class="header-actions">
        <el-select v-model="selectedWarehouse" placeholder="选择仓库" style="width: 200px; margin-right: 10px" @change="onWarehouseChange">
          <el-option
            v-for="wh in warehouseList"
            :key="wh.id"
            :label="wh.warehouseName"
            :value="wh.id"
          />
        </el-select>
        <el-button type="primary" @click="loadLocationView">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <span class="stat-label">总库位数</span>
            <span class="stat-value">{{ locationStats.total }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <span class="stat-label">已占用</span>
            <span class="stat-value" style="color: #409eff">{{ locationStats.occupied }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <span class="stat-label">空闲</span>
            <span class="stat-value" style="color: #67c23a">{{ locationStats.free }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <span class="stat-label">使用率</span>
            <span class="stat-value" style="color: #e6a23c">{{ locationStats.usageRate }}%</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <el-tabs v-model="activeArea" @tab-change="onAreaChange">
        <el-tab-pane label="A区" name="A" />
        <el-tab-pane label="B区" name="B" />
        <el-tab-pane label="C区" name="C" />
        <el-tab-pane label="D区" name="D" />
      </el-tabs>

      <div class="legend-bar">
        <div class="legend-item">
          <span class="legend-color" style="background: #67c23a"></span>
          <span>空闲</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #409eff"></span>
          <span>有货</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #f56c6c"></span>
          <span>已满</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #909399"></span>
          <span>冻结</span>
        </div>
      </div>

      <div v-loading="loading" class="location-matrix">
        <div class="matrix-header">
          <div class="header-cell">列/行</div>
          <div v-for="col in 10" :key="col" class="header-cell">{{ col }}</div>
        </div>
        <div v-for="(row, rowIndex) in 5" :key="rowIndex" class="matrix-row">
          <div class="row-header">{{ String.fromCharCode(65 + rowIndex) }}{{ activeArea }}</div>
          <div
            v-for="col in 10"
            :key="col"
            class="matrix-cell"
            :class="getLocationClass(getLocationByPosition(rowIndex + 1, col))"
            @click="showLocationDetail(getLocationByPosition(rowIndex + 1, col))"
          >
            <el-tooltip
              v-if="getLocationByPosition(rowIndex + 1, col)"
              placement="top"
              :show-after="300"
            >
              <template #content>
                <div class="tooltip-content">
                  <p><strong>库位编码：</strong>{{ getLocationByPosition(rowIndex + 1, col)?.locationCode }}</p>
                  <p><strong>容量：</strong>{{ getLocationByPosition(rowIndex + 1, col)?.maxQuantity }}</p>
                  <p><strong>已用：</strong>{{ getLocationByPosition(rowIndex + 1, col)?.currentQuantity }}</p>
                  <p><strong>商品数：</strong>{{ getLocationByPosition(rowIndex + 1, col)?.productCount || 0 }}</p>
                </div>
              </template>
              <div class="cell-content">
                <div class="cell-code">{{ getLocationByPosition(rowIndex + 1, col)?.locationCode || '—' }}</div>
                <el-progress
                  v-if="getLocationByPosition(rowIndex + 1, col)?.status === 1"
                  :percentage="getUsagePercent(getLocationByPosition(rowIndex + 1, col))"
                  :show-text="false"
                  :stroke-width="4"
                  :color="getProgressColor(getLocationByPosition(rowIndex + 1, col))"
                />
              </div>
            </el-tooltip>
            <div v-else class="cell-content">
              <div class="cell-code empty">未分配</div>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && filteredLocations.length === 0" description="暂无库位数据" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="库位详情" width="700px">
      <template v-if="currentLocation">
        <el-descriptions :column="2" border class="mb-20">
          <el-descriptions-item label="库位编码">{{ currentLocation.locationCode }}</el-descriptions-item>
          <el-descriptions-item label="库位类型">{{ getLocationTypeName(currentLocation.locationType) }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ currentLocation.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="库区">{{ currentLocation.areaName }}</el-descriptions-item>
          <el-descriptions-item label="当前数量">{{ currentLocation.currentQuantity }}</el-descriptions-item>
          <el-descriptions-item label="最大容量">{{ currentLocation.maxQuantity }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentLocation.status === 1 ? 'success' : 'info'">
              {{ currentLocation.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="占用率">
            <el-progress
              :percentage="getUsagePercent(currentLocation)"
              :color="getProgressColor(currentLocation)"
            />
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-bottom: 15px">批次库存列表</h4>
        <el-table :data="batchInventoryList" v-loading="batchLoading" border size="small">
          <el-table-column prop="batchNo" label="批次号" width="180" />
          <el-table-column prop="productCode" label="商品编码" width="120" />
          <el-table-column prop="productName" label="商品名称" min-width="150" />
          <el-table-column prop="quantity" label="库存数量" width="100" />
          <el-table-column prop="availableQuantity" label="可用数量" width="100" />
          <el-table-column prop="productionDate" label="生产日期" width="120" />
          <el-table-column prop="expireDate" label="过期日期" width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getBatchStatusTag(row)" size="small">
                {{ getBatchStatusName(row) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLocationViewApi, getAllWarehousesApi, getLocationByIdApi } from '@/api'
import { Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const batchLoading = ref(false)
const detailVisible = ref(false)
const selectedWarehouse = ref(null)
const activeArea = ref('A')
const warehouseList = ref([])
const locationList = ref([])
const currentLocation = ref(null)
const batchInventoryList = ref([])

const locationStats = reactive({
  total: 0,
  occupied: 0,
  free: 0,
  usageRate: 0
})

const filteredLocations = computed(() => {
  return locationList.value.filter(loc => loc.areaName === activeArea.value + '区')
})

const getLocationByPosition = (row, col) => {
  const rowLetter = String.fromCharCode(64 + row)
  const locationCode = `${activeArea.value}-${rowLetter}-${String(col).padStart(2, '0')}`
  return locationList.value.find(loc => loc.locationCode === locationCode) || null
}

const getLocationTypeName = (type) => {
  const map = { 1: '普通库位', 2: '冷藏库位', 3: '冷冻库位', 4: '危险品库位' }
  return map[type] || '未知'
}

const getLocationClass = (loc) => {
  if (!loc) return 'empty'
  if (loc.status !== 1) return 'frozen'
  const rate = loc.currentQuantity / loc.maxQuantity
  if (rate === 0) return 'free'
  if (rate >= 1) return 'full'
  return 'occupied'
}

const getUsagePercent = (loc) => {
  if (!loc || loc.maxQuantity === 0) return 0
  return Math.round((loc.currentQuantity / loc.maxQuantity) * 100)
}

const getProgressColor = (loc) => {
  if (!loc) return '#909399'
  const rate = loc.currentQuantity / loc.maxQuantity
  if (rate === 0) return '#67c23a'
  if (rate >= 1) return '#f56c6c'
  if (rate >= 0.8) return '#e6a23c'
  return '#409eff'
}

const getBatchStatusName = (row) => {
  const now = dayjs()
  if (row.expireDate && dayjs(row.expireDate).isBefore(now)) {
    return '过期'
  }
  if (row.expireDate && dayjs(row.expireDate).diff(now, 'day') <= 30) {
    return '临期'
  }
  return '正常'
}

const getBatchStatusTag = (row) => {
  const now = dayjs()
  if (row.expireDate && dayjs(row.expireDate).isBefore(now)) {
    return 'danger'
  }
  if (row.expireDate && dayjs(row.expireDate).diff(now, 'day') <= 30) {
    return 'warning'
  }
  return 'success'
}

const loadWarehouses = async () => {
  try {
    const res = await getAllWarehousesApi()
    warehouseList.value = res.data || []
    if (warehouseList.value.length > 0) {
      selectedWarehouse.value = warehouseList.value[0].id
      loadLocationView()
    }
  } catch (e) {
    console.log('loadWarehouses error:', e)
  }
}

const loadLocationView = async () => {
  if (!selectedWarehouse.value) return
  loading.value = true
  try {
    const res = await getLocationViewApi(selectedWarehouse.value, { area: activeArea.value })
    const data = res.data || []
    if (data.length === 0) {
      locationList.value = generateMockLocations()
    } else {
      locationList.value = data
    }
    calculateStats()
  } catch (e) {
    locationList.value = generateMockLocations()
    calculateStats()
  } finally {
    loading.value = false
  }
}

const generateMockLocations = () => {
  const mockData = []
  const areas = ['A', 'B', 'C', 'D']
  const statuses = [1, 1, 1, 1, 0]
  
  areas.forEach(area => {
    for (let row = 1; row <= 5; row++) {
      for (let col = 1; col <= 10; col++) {
        const rowLetter = String.fromCharCode(64 + row)
        const locationCode = `${area}-${rowLetter}-${String(col).padStart(2, '0')}`
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const maxQuantity = 100 + Math.floor(Math.random() * 100)
        const currentQuantity = status === 1 ? Math.floor(Math.random() * (maxQuantity + 1)) : 0
        
        mockData.push({
          id: mockData.length + 1,
          locationCode,
          locationType: [1, 2, 3, 4][Math.floor(Math.random() * 4)],
          warehouseId: selectedWarehouse.value || 1,
          warehouseName: warehouseList.value[0]?.warehouseName || '中心仓库',
          areaName: area + '区',
          rowNo: row,
          columnNo: col,
          maxQuantity,
          currentQuantity,
          productCount: currentQuantity > 0 ? Math.floor(Math.random() * 5) + 1 : 0,
          status
        })
      }
    }
  })
  return mockData
}

const calculateStats = () => {
  const list = filteredLocations.value
  locationStats.total = list.length
  locationStats.occupied = list.filter(l => l.currentQuantity > 0 && l.status === 1).length
  locationStats.free = list.filter(l => l.currentQuantity === 0 && l.status === 1).length
  locationStats.usageRate = locationStats.total > 0
    ? Math.round((locationStats.occupied / locationStats.total) * 100)
    : 0
}

const onWarehouseChange = () => {
  loadLocationView()
}

const onAreaChange = () => {
  calculateStats()
}

const showLocationDetail = async (loc) => {
  if (!loc) return
  currentLocation.value = loc
  detailVisible.value = true
  batchLoading.value = true
  
  try {
    const res = await getLocationByIdApi(loc.id)
    batchInventoryList.value = res.data?.batchInventory || generateMockBatchInventory()
  } catch (e) {
    batchInventoryList.value = generateMockBatchInventory()
  } finally {
    batchLoading.value = false
  }
}

const generateMockBatchInventory = () => {
  if (!currentLocation.value || currentLocation.value.currentQuantity === 0) return []
  
  const mockData = []
  const productNames = ['电子元件A', '电路板B', '芯片组C', '电容器D', '电阻器E']
  const count = Math.min(Math.floor(Math.random() * 3) + 1, currentLocation.value.productCount || 3)
  
  for (let i = 0; i < count; i++) {
    const quantity = Math.floor(currentLocation.value.currentQuantity / count) + (i === 0 ? currentLocation.value.currentQuantity % count : 0)
    const dateOffset = Math.floor(Math.random() * 180)
    
    mockData.push({
      id: i + 1,
      batchNo: `BATCH${Date.now()}${String(i).padStart(3, '0')}`,
      productCode: `SKU${String(1000 + i).padStart(6, '0')}`,
      productName: productNames[i % productNames.length],
      quantity,
      availableQuantity: quantity - Math.floor(Math.random() * 10),
      productionDate: dayjs().subtract(dateOffset, 'day').format('YYYY-MM-DD'),
      expireDate: dayjs().add(365 - dateOffset, 'day').format('YYYY-MM-DD')
    })
  }
  return mockData
}

onMounted(() => {
  loadWarehouses()
})
</script>

<style lang="scss" scoped>
.location-container {
  padding: 20px;
  min-height: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .stat-label {
    font-size: 14px;
    color: #909399;
  }
  .stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-top: 8px;
  }
}

.legend-bar {
  display: flex;
  gap: 30px;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #606266;
  }
  
  .legend-color {
    width: 20px;
    height: 20px;
    border-radius: 4px;
  }
}

.location-matrix {
  min-height: 500px;
  
  .matrix-header {
    display: flex;
    margin-bottom: 10px;
    
    .header-cell {
      flex: 1;
      text-align: center;
      font-weight: 600;
      color: #606266;
      padding: 8px;
      background: #f5f7fa;
      border-radius: 4px;
      margin: 0 4px;
      
      &:first-child {
        flex: 0 0 80px;
      }
    }
  }
  
  .matrix-row {
    display: flex;
    margin-bottom: 10px;
    align-items: stretch;
    
    .row-header {
      flex: 0 0 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      color: #606266;
      background: #f5f7fa;
      border-radius: 4px;
      margin-right: 4px;
    }
    
    .matrix-cell {
      flex: 1;
      margin: 0 4px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      border: 2px solid transparent;
      min-height: 70px;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
      
      &.empty {
        background: #fafafa;
        border-color: #ebeef5;
        cursor: default;
        
        .cell-code {
          color: #c0c4cc;
        }
      }
      
      &.free {
        background: #f0f9eb;
        border-color: #67c23a;
      }
      
      &.occupied {
        background: #ecf5ff;
        border-color: #409eff;
      }
      
      &.full {
        background: #fef0f0;
        border-color: #f56c6c;
      }
      
      &.frozen {
        background: #f4f4f5;
        border-color: #909399;
        cursor: not-allowed;
      }
      
      .cell-content {
        padding: 8px;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        
        .cell-code {
          font-size: 12px;
          font-weight: 600;
          text-align: center;
          margin-bottom: 4px;
          
          &.empty {
            font-weight: normal;
          }
        }
      }
    }
  }
}

.tooltip-content {
  p {
    margin: 4px 0;
    font-size: 12px;
    
    strong {
      color: #606266;
    }
  }
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
