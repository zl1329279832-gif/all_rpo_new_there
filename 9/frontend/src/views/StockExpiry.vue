<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">库存效期管理</h2>
      <el-button type="warning" @click="loadWarningData">
        <el-icon><Warning /></el-icon>
        刷新预警
      </el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="8">
        <div class="stats-card">
          <div class="stats-label">预警商品种类</div>
          <div class="stats-value">{{ warningStats.warningProducts || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stats-card card-orange">
          <div class="stats-label">临期预警数量</div>
          <div class="stats-value">{{ formatNumber(warningStats.warningQty) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stats-card card-green">
          <div class="stats-label">已过期数量</div>
          <div class="stats-value">{{ formatNumber(warningStats.expiredQty) }}</div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="临期预警列表" name="warning">
        <div class="search-bar">
          <div class="search-item">
            <label>产品名称:</label>
            <el-input v-model="warningQuery.productName" placeholder="请输入" clearable style="width: 160px" />
          </div>
          <div class="search-item">
            <label>门店:</label>
            <el-select v-model="warningQuery.storeId" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="store in storeList" :key="store.id" :label="store.storeName" :value="store.id" />
            </el-select>
          </div>
          <div class="search-item">
            <label>预警级别:</label>
            <el-select v-model="warningQuery.warningLevel" placeholder="全部" clearable style="width: 140px">
              <el-option label="已过期" value="expired" />
              <el-option label="严重" value="severe" />
              <el-option label="一般" value="normal" />
            </el-select>
          </div>
          <div class="search-item">
            <el-button type="primary" @click="loadWarningData">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </div>

        <el-table :data="warningList" v-loading="warningLoading" border stripe>
          <el-table-column label="预警级别" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.warningLevel === 'expired'" type="danger">已过期</el-tag>
              <el-tag v-else-if="row.warningLevel === 'severe'" type="warning">严重</el-tag>
              <el-tag v-else type="info">一般</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="180" />
          <el-table-column prop="productName" label="产品名称" width="160" />
          <el-table-column prop="storeName" label="门店" width="140" />
          <el-table-column prop="remainQty" label="剩余数量" width="100">
            <template #default="{ row }">{{ formatNumber(row.remainQty) }}</template>
          </el-table-column>
          <el-table-column prop="expireTime" label="过期时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.expireTime) }}</template>
          </el-table-column>
          <el-table-column label="剩余时间" width="140">
            <template #default="{ row }">
              <span :style="{ color: row.warningLevel === 'expired' ? '#f56c6c' : row.warningLevel === 'severe' ? '#e6a23c' : '' }">
                {{ row.remainTimeDesc }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="warning" link @click="quickDamage(row)">报损</el-button>
              <el-button size="small" type="primary" link @click="quickTransfer(row)">调拨</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="warningQuery.pageNum"
            v-model:page-size="warningQuery.pageSize"
            :total="warningTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadWarningData"
            @current-change="loadWarningData"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="库存效期总览" name="overview">
        <div class="search-bar">
          <div class="search-item">
            <label>产品名称:</label>
            <el-input v-model="overviewQuery.productName" placeholder="请输入" clearable style="width: 160px" />
          </div>
          <div class="search-item">
            <label>门店:</label>
            <el-select v-model="overviewQuery.storeId" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="store in storeList" :key="store.id" :label="store.storeName" :value="store.id" />
            </el-select>
          </div>
          <div class="search-item">
            <el-button type="primary" @click="loadOverviewData">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </div>

        <el-table :data="overviewList" v-loading="overviewLoading" border stripe>
          <el-table-column prop="productName" label="产品名称" width="180" />
          <el-table-column prop="storeName" label="门店" width="140" />
          <el-table-column prop="totalQty" label="总库存" width="100">
            <template #default="{ row }">{{ formatNumber(row.totalQty) }}</template>
          </el-table-column>
          <el-table-column prop="normalQty" label="正常库存" width="100">
            <template #default="{ row }">
              <span style="color: #67c23a">{{ formatNumber(row.normalQty) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="warningQty" label="临期库存" width="100">
            <template #default="{ row }">
              <span style="color: #e6a23c">{{ formatNumber(row.warningQty) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="expiredQty" label="过期库存" width="100">
            <template #default="{ row }">
              <span style="color: #f56c6c">{{ formatNumber(row.expiredQty) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最早过期时间" width="170">
            <template #default="{ row }">
              <span :style="{ color: row.expiredQty > 0 ? '#f56c6c' : row.warningQty > 0 ? '#e6a23c' : '' }">
                {{ formatDateTime(row.earliestExpireTime) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.expiredQty > 0" type="danger">有过期</el-tag>
              <el-tag v-else-if="row.warningQty > 0" type="warning">有临期</el-tag>
              <el-tag v-else type="success">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="viewBatches(row)">查看批次</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="overviewQuery.pageNum"
            v-model:page-size="overviewQuery.pageSize"
            :total="overviewTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadOverviewData"
            @current-change="loadOverviewData"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="batchListDialogVisible" title="效期批次明细" width="800px">
      <el-table :data="currentProductBatches" border stripe>
        <el-table-column prop="batchNo" label="批次号" width="180" />
        <el-table-column prop="remainQty" label="剩余数量" width="100">
          <template #default="{ row }">{{ formatNumber(row.remainQty) }}</template>
        </el-table-column>
        <el-table-column prop="produceTime" label="生产时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.produceTime) }}</template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="170">
          <template #default="{ row }">
            <span :style="{ color: getWarningStatus(row.expireTime) === 'expired' ? '#f56c6c' : getWarningStatus(row.expireTime) === 'expiring' ? '#e6a23c' : '' }">
              {{ formatDateTime(row.expireTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="效期状态" width="100">
          <template #default="{ row }">
            <el-tag :class="['warning-tag', getWarningStatus(row.expireTime)]">
              {{ getWarningLabel(getWarningStatus(row.expireTime)) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { batchApi, storeApi } from '@/api'
import { formatDateTime, formatNumber, getWarningStatus, getWarningLabel } from '@/utils/format'

const router = useRouter()

const activeTab = ref('warning')
const storeList = ref([])
const warningStats = ref({})

const warningLoading = ref(false)
const warningList = ref([])
const warningTotal = ref(0)

const overviewLoading = ref(false)
const overviewList = ref([])
const overviewTotal = ref(0)

const batchListDialogVisible = ref(false)
const currentProductBatches = ref([])

const warningQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  productName: '',
  storeId: null,
  warningLevel: ''
})

const overviewQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  productName: '',
  storeId: null
})

const loadWarningStats = async () => {
  const res = await batchApi.warningStats()
  warningStats.value = res.data || {}
}

const loadStoreList = async () => {
  const res = await storeApi.list()
  storeList.value = res.data || []
}

const loadWarningData = async () => {
  warningLoading.value = true
  try {
    const res = await batchApi.warningList(warningQuery)
    warningList.value = res.data.list || []
    warningTotal.value = res.data.total || 0
  } finally {
    warningLoading.value = false
  }
}

const loadOverviewData = async () => {
  overviewLoading.value = true
  try {
    const res = await batchApi.page({ ...overviewQuery, groupByProduct: true })
    overviewList.value = res.data.list || []
    overviewTotal.value = res.data.total || 0
  } finally {
    overviewLoading.value = false
  }
}

const quickDamage = (row) => {
  ElMessage.info('请前往报损管理页面创建报损单')
  router.push('/damage')
}

const quickTransfer = (row) => {
  ElMessage.info('请前往门店调拨页面创建调拨单')
  router.push('/transfer')
}

const viewBatches = async (row) => {
  const res = await batchApi.available({
    recipeId: row.recipeId,
    storeId: row.storeId
  })
  currentProductBatches.value = res.data || []
  batchListDialogVisible.value = true
}

onMounted(() => {
  loadWarningStats()
  loadStoreList()
  loadWarningData()
})
</script>
