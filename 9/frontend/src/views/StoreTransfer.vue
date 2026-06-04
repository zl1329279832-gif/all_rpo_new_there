<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">门店调拨管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建调拨单
      </el-button>
    </div>

    <div class="search-bar">
      <div class="search-item">
        <label>调拨单号:</label>
        <el-input v-model="query.transferNo" placeholder="请输入" clearable style="width: 160px" />
      </div>
      <div class="search-item">
        <label>状态:</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="待出库" :value="0" />
          <el-option label="已出库" :value="1" />
          <el-option label="已入库" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
      </div>
      <div class="search-item">
        <label>调拨类型:</label>
        <el-select v-model="query.transferType" placeholder="全部" clearable style="width: 140px">
          <el-option label="正常调拨" :value="1" />
          <el-option label="临期调拨" :value="2" />
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
      <el-table-column prop="transferNo" label="调拨单号" width="180" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag :class="['status-tag', transferTypeMap[row.transferType].class]">
            {{ transferTypeMap[row.transferType].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fromStoreName" label="调出门店" width="120" />
      <el-table-column prop="toStoreName" label="调入门店" width="120" />
      <el-table-column prop="productName" label="产品名称" width="160" />
      <el-table-column prop="transferQty" label="调拨数量" width="100">
        <template #default="{ row }">{{ formatNumber(row.transferQty) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :class="['status-tag', transferStatusMap[row.status].class]">
            {{ transferStatusMap[row.status].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="success"
            link
            @click="handleOutbound(row)"
          >
            确认出库
          </el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="success"
            link
            @click="handleInbound(row)"
          >
            确认入库
          </el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="danger"
            link
            @click="handleCancel(row)"
          >
            取消
          </el-button>
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

    <el-dialog v-model="createDialogVisible" title="新建调拨单" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调出门店" prop="fromStoreId">
              <el-select v-model="form.fromStoreId" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="store in storeList"
                  :key="store.id"
                  :label="store.storeName"
                  :value="store.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调入门店" prop="toStoreId">
              <el-select v-model="form.toStoreId" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="store in storeList"
                  :key="store.id"
                  :label="store.storeName"
                  :value="store.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调拨类型" prop="transferType">
              <el-radio-group v-model="form.transferType">
                <el-radio :value="1">正常调拨</el-radio>
                <el-radio :value="2">临期调拨</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调拨数量" prop="transferQty">
              <el-input-number
                v-model="form.transferQty"
                :min="1"
                :precision="2"
                :step="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="产品" prop="recipeId">
          <el-select v-model="form.recipeId" placeholder="请选择产品" style="width: 100%" @change="loadAvailableBatches">
            <el-option
              v-for="recipe in recipeList"
              :key="recipe.id"
              :label="recipe.productName"
              :value="recipe.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.recipeId && form.fromStoreId" label="可用批次(效期优先)">
          <el-alert
            title="系统将自动按效期优先原则（先过期先出）从以下批次扣减库存"
            type="info"
            :closable="false"
            style="margin-bottom: 10px"
          />
          <el-table :data="availableBatches" border size="small" max-height="200">
            <el-table-column prop="batchNo" label="批次号" width="180" />
            <el-table-column prop="remainQty" label="可用数量" width="100">
              <template #default="{ row }">{{ formatNumber(row.remainQty) }}</template>
            </el-table-column>
            <el-table-column prop="expireTime" label="过期时间" width="170">
              <template #default="{ row }">
                <span :style="{ color: getWarningStatus(row.expireTime) !== 'normal' ? '#e6a23c' : '' }">
                  {{ formatDateTime(row.expireTime) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="效期状态" width="100">
              <template #default="{ row }">
                <el-tag :class="['warning-tag', getWarningStatus(row.expireTime)]" size="small">
                  {{ getWarningLabel(getWarningStatus(row.expireTime)) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="form-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="调拨单详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="调拨单号">{{ transferDetail.transferNo }}</el-descriptions-item>
        <el-descriptions-item label="调拨类型">
          <el-tag :class="['status-tag', transferTypeMap[transferDetail.transferType].class]">
            {{ transferTypeMap[transferDetail.transferType].label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调出门店">{{ transferDetail.fromStoreName }}</el-descriptions-item>
        <el-descriptions-item label="调入门店">{{ transferDetail.toStoreName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ transferDetail.productName }}</el-descriptions-item>
        <el-descriptions-item label="调拨数量">{{ formatNumber(transferDetail.transferQty) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :class="['status-tag', transferStatusMap[transferDetail.status].class]">
            {{ transferStatusMap[transferDetail.status].label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(transferDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="出库时间">{{ formatDateTime(transferDetail.outboundTime) }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ formatDateTime(transferDetail.inboundTime) }}</el-descriptions-item>
        <el-descriptions-item label="出库批次号">{{ transferDetail.outboundBatchNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入库批次号">{{ transferDetail.inboundBatchNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ transferDetail.remark || '-' }}</el-descriptions-item>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { transferApi, storeApi, recipeApi, batchApi } from '@/api'
import {
  transferStatusMap,
  transferTypeMap,
  formatDateTime,
  formatNumber,
  getWarningStatus,
  getWarningLabel
} from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const storeList = ref([])
const recipeList = ref([])
const availableBatches = ref([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  transferNo: '',
  status: null,
  transferType: null
})

const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref(null)
const transferDetail = ref({})

const form = reactive({
  fromStoreId: null,
  toStoreId: null,
  recipeId: null,
  transferType: 1,
  transferQty: 10,
  remark: ''
})

const rules = {
  fromStoreId: [{ required: true, message: '请选择调出门店', trigger: 'change' }],
  toStoreId: [{ required: true, message: '请选择调入门店', trigger: 'change' }],
  recipeId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  transferQty: [{ required: true, message: '请输入调拨数量', trigger: 'blur' }]
}

const loadStoreList = async () => {
  const res = await storeApi.list()
  storeList.value = res.data || []
}

const loadRecipeList = async () => {
  const res = await recipeApi.list()
  recipeList.value = res.data || []
}

const loadAvailableBatches = async () => {
  if (!form.recipeId || !form.fromStoreId) {
    availableBatches.value = []
    return
  }
  const res = await batchApi.available({
    recipeId: form.recipeId,
    storeId: form.fromStoreId
  })
  availableBatches.value = res.data || []
}

watch(
  () => form.fromStoreId,
  () => {
    if (form.recipeId) {
      loadAvailableBatches()
    }
  }
)

const loadData = async () => {
  loading.value = true
  try {
    const res = await transferApi.page(query)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.transferNo = ''
  query.status = null
  query.transferType = null
  query.pageNum = 1
  loadData()
}

const openCreateDialog = () => {
  form.fromStoreId = null
  form.toStoreId = null
  form.recipeId = null
  form.transferType = 1
  form.transferQty = 10
  form.remark = ''
  availableBatches.value = []
  createDialogVisible.value = true
}

const submitCreate = async () => {
  await formRef.value.validate()
  if (form.fromStoreId === form.toStoreId) {
    ElMessage.warning('调出门店和调入门店不能相同')
    return
  }
  if (availableBatches.value.length === 0) {
    ElMessage.warning('该门店当前无可用库存')
    return
  }
  const totalAvailable = availableBatches.value.reduce((sum, b) => sum + Number(b.remainQty), 0)
  if (totalAvailable < form.transferQty) {
    ElMessage.warning(`可用库存不足，当前可用 ${totalAvailable.toFixed(2)}`)
    return
  }
  await transferApi.create(form)
  ElMessage.success('创建成功')
  createDialogVisible.value = false
  loadData()
}

const viewDetail = async (row) => {
  const res = await transferApi.detail(row.id)
  transferDetail.value = res.data || {}
  detailDialogVisible.value = true
}

const handleOutbound = (row) => {
  ElMessageBox.confirm('确认出库？系统将按效期优先原则扣减库存。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await transferApi.outbound(row.id)
    ElMessage.success('出库成功')
    loadData()
  })
}

const handleInbound = (row) => {
  ElMessageBox.confirm('确认入库？将在调入门店生成新的批次。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await transferApi.inbound(row.id)
    ElMessage.success('入库成功')
    loadData()
  })
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该调拨单？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await transferApi.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  })
}

onMounted(() => {
  loadStoreList()
  loadRecipeList()
  loadData()
})
</script>
