<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">成品批次台账</h2>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="6">
        <div class="stats-card">
          <div class="stats-label">库存总批次</div>
          <div class="stats-value">{{ warningStats.totalBatches || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card card-orange">
          <div class="stats-label">临期预警</div>
          <div class="stats-value">{{ warningStats.warningBatches || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card card-green">
          <div class="stats-label">已过期</div>
          <div class="stats-value">{{ warningStats.expiredBatches || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card card-purple">
          <div class="stats-label">当前库存总量</div>
          <div class="stats-value">{{ formatNumber(warningStats.totalRemainQty) }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <div class="search-item">
        <label>批次号:</label>
        <el-input v-model="query.batchNo" placeholder="请输入" clearable style="width: 160px" />
      </div>
      <div class="search-item">
        <label>产品名称:</label>
        <el-input v-model="query.productName" placeholder="请输入" clearable style="width: 160px" />
      </div>
      <div class="search-item">
        <label>门店:</label>
        <el-select v-model="query.storeId" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="store in storeList" :key="store.id" :label="store.storeName" :value="store.id" />
        </el-select>
      </div>
      <div class="search-item">
        <label>批次状态:</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="在库" :value="1" />
          <el-option label="部分出库" :value="2" />
          <el-option label="已售罄" :value="3" />
          <el-option label="已报损" :value="4" />
        </el-select>
      </div>
      <div class="search-item">
        <label>效期状态:</label>
        <el-select v-model="query.warningStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" value="normal" />
          <el-option label="临期" value="expiring" />
          <el-option label="已过期" value="expired" />
        </el-select>
      </div>
      <div class="search-item">
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="resetQuery">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="batchNo" label="批次号" width="180" />
      <el-table-column prop="productName" label="产品名称" width="160" />
      <el-table-column prop="storeName" label="所属门店" width="140" />
      <el-table-column prop="totalQty" label="生产数量" width="100">
        <template #default="{ row }">{{ formatNumber(row.totalQty) }}</template>
      </el-table-column>
      <el-table-column prop="remainQty" label="剩余数量" width="100">
        <template #default="{ row }">{{ formatNumber(row.remainQty) }}</template>
      </el-table-column>
      <el-table-column prop="outboundQty" label="已出库" width="90">
        <template #default="{ row }">{{ formatNumber(row.outboundQty) }}</template>
      </el-table-column>
      <el-table-column prop="produceTime" label="生产时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.produceTime) }}</template>
      </el-table-column>
      <el-table-column prop="expireTime" label="过期时间" width="170">
        <template #default="{ row }">
          <span :style="{ color: getWarningStatus(row.expireTime, row.warningHours) === 'expired' ? '#f56c6c' : getWarningStatus(row.expireTime, row.warningHours) === 'expiring' ? '#e6a23c' : '' }">
            {{ formatDateTime(row.expireTime) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="效期状态" width="90">
        <template #default="{ row }">
          <el-tag :class="['warning-tag', getWarningStatus(row.expireTime, row.warningHours)]">
            {{ getWarningLabel(getWarningStatus(row.expireTime, row.warningHours)) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="库存状态" width="100">
        <template #default="{ row }">
          <el-tag :class="['status-tag', batchStatusMap[row.status].class]">
            {{ batchStatusMap[row.status].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="detailDialogVisible" title="批次详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="批次号">{{ batchDetail.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ batchDetail.productName }}</el-descriptions-item>
        <el-descriptions-item label="所属门店">{{ batchDetail.storeName }}</el-descriptions-item>
        <el-descriptions-item label="关联生产计划">{{ batchDetail.planNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产数量">{{ formatNumber(batchDetail.totalQty) }}</el-descriptions-item>
        <el-descriptions-item label="剩余数量">{{ formatNumber(batchDetail.remainQty) }}</el-descriptions-item>
        <el-descriptions-item label="已出库数量">{{ formatNumber(batchDetail.outboundQty) }}</el-descriptions-item>
        <el-descriptions-item label="已报损数量">{{ formatNumber(batchDetail.damageQty) }}</el-descriptions-item>
        <el-descriptions-item label="生产时间">{{ formatDateTime(batchDetail.produceTime) }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">
          <span :style="{ color: getWarningStatus(batchDetail.expireTime, batchDetail.warningHours) === 'expired' ? '#f56c6c' : '' }">
            {{ formatDateTime(batchDetail.expireTime) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="生产班组">{{ batchDetail.produceTeam || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ batchDetail.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="库存状态" :span="2">
          <el-tag :class="['status-tag', batchStatusMap[batchDetail.status].class]">
            {{ batchStatusMap[batchDetail.status].label }}
          </el-tag>
          <el-tag
            :class="['warning-tag', getWarningStatus(batchDetail.expireTime, batchDetail.warningHours)]"
            style="margin-left: 10px"
          >
            {{ getWarningLabel(getWarningStatus(batchDetail.expireTime, batchDetail.warningHours)) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ batchDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="form-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { batchApi, storeApi } from '@/api'
import {
  batchStatusMap,
  formatDateTime,
  formatNumber,
  getWarningStatus,
  getWarningLabel
} from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const storeList = ref([])
const warningStats = ref({})

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  batchNo: '',
  productName: '',
  storeId: null,
  status: null,
  warningStatus: ''
})

const detailDialogVisible = ref(false)
const batchDetail = ref({})

const loadWarningStats = async () => {
  const res = await batchApi.warningStats()
  warningStats.value = res.data || {}
}

const loadStoreList = async () => {
  const res = await storeApi.list()
  storeList.value = res.data || []
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await batchApi.page(query)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.batchNo = ''
  query.productName = ''
  query.storeId = null
  query.status = null
  query.warningStatus = ''
  query.pageNum = 1
  loadData()
}

const viewDetail = async (row) => {
  const res = await batchApi.detail(row.id)
  batchDetail.value = res.data || {}
  detailDialogVisible.value = true
}

onMounted(() => {
  loadWarningStats()
  loadStoreList()
  loadData()
})
</script>
