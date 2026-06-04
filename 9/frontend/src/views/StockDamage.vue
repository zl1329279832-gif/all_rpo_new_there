<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">报损管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建报损单
      </el-button>
    </div>

    <div class="search-bar">
      <div class="search-item">
        <label>报损单号:</label>
        <el-input v-model="query.damageNo" placeholder="请输入" clearable style="width: 160px" />
      </div>
      <div class="search-item">
        <label>状态:</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="待审核" :value="0" />
          <el-option label="已审核" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </div>
      <div class="search-item">
        <label>报损类型:</label>
        <el-select v-model="query.damageType" placeholder="全部" clearable style="width: 140px">
          <el-option label="临期过期" :value="1" />
          <el-option label="质量问题" :value="2" />
          <el-option label="破损" :value="3" />
          <el-option label="其他" :value="4" />
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
      <el-table-column prop="damageNo" label="报损单号" width="180" />
      <el-table-column prop="storeName" label="门店" width="140" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ damageTypeMap[row.damageType] }}</template>
      </el-table-column>
      <el-table-column prop="productName" label="产品名称" width="160" />
      <el-table-column prop="batchNo" label="批次号" width="180" />
      <el-table-column prop="damageQty" label="报损数量" width="100">
        <template #default="{ row }">{{ formatNumber(row.damageQty) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="120">
        <template #default="{ row }">{{ formatMoney(row.damageAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :class="['status-tag', damageStatusMap[row.status].class]">
            {{ damageStatusMap[row.status].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="success"
            link
            @click="handleAudit(row, true)"
          >
            审核通过
          </el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="danger"
            link
            @click="handleAudit(row, false)"
          >
            驳回
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

    <el-dialog v-model="createDialogVisible" title="新建报损单" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="form.storeId" placeholder="请选择" style="width: 100%" @change="loadStoreBatches">
            <el-option
              v-for="store in storeList"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报损类型" prop="damageType">
          <el-select v-model="form.damageType" placeholder="请选择" style="width: 100%">
            <el-option label="临期过期" :value="1" />
            <el-option label="质量问题" :value="2" />
            <el-option label="破损" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次" prop="batchId">
          <el-select v-model="form.batchId" placeholder="请选择批次" style="width: 100%" @change="onBatchChange">
            <el-option
              v-for="batch in storeBatches"
              :key="batch.id"
              :label="`${batch.productName} - ${batch.batchNo}`"
              :value="batch.id"
            >
              <span>{{ batch.productName }}</span>
              <span style="float: right; color: #999; font-size: 12px">
                库存: {{ formatNumber(batch.remainQty) }} | 过期: {{ formatDateTime(batch.expireTime) }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="报损数量" prop="damageQty">
          <el-input-number
            v-model="form.damageQty"
            :min="0.01"
            :max="form.maxQty || 9999"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
          <div v-if="form.maxQty" style="font-size: 12px; color: #999; margin-top: 5px">
            可报损最大数量: {{ formatNumber(form.maxQty) }}
          </div>
        </el-form-item>
        <el-form-item label="报损原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请详细说明报损原因" />
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

    <el-dialog v-model="detailDialogVisible" title="报损单详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报损单号">{{ damageDetail.damageNo }}</el-descriptions-item>
        <el-descriptions-item label="门店">{{ damageDetail.storeName }}</el-descriptions-item>
        <el-descriptions-item label="报损类型">{{ damageTypeMap[damageDetail.damageType] }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :class="['status-tag', damageStatusMap[damageDetail.status].class]">
            {{ damageStatusMap[damageDetail.status].label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ damageDetail.productName }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ damageDetail.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="报损数量">{{ formatNumber(damageDetail.damageQty) }}</el-descriptions-item>
        <el-descriptions-item label="报损金额">{{ formatMoney(damageDetail.damageAmount) }}</el-descriptions-item>
        <el-descriptions-item label="报损原因" :span="2">{{ damageDetail.reason }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" v-if="damageDetail.auditRemark" :span="2">
          {{ damageDetail.auditRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(damageDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ formatDateTime(damageDetail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ damageDetail.remark || '-' }}</el-descriptions-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { damageApi, storeApi, batchApi } from '@/api'
import {
  damageStatusMap,
  damageTypeMap,
  formatDateTime,
  formatNumber,
  formatMoney
} from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const storeList = ref([])
const storeBatches = ref([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  damageNo: '',
  status: null,
  damageType: null
})

const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref(null)
const damageDetail = ref({})

const form = reactive({
  storeId: null,
  damageType: 1,
  batchId: null,
  damageQty: 0,
  maxQty: 0,
  reason: '',
  remark: ''
})

const rules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  damageType: [{ required: true, message: '请选择报损类型', trigger: 'change' }],
  batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
  damageQty: [{ required: true, message: '请输入报损数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入报损原因', trigger: 'blur' }]
}

const loadStoreList = async () => {
  const res = await storeApi.list()
  storeList.value = res.data || []
}

const loadStoreBatches = async () => {
  if (!form.storeId) {
    storeBatches.value = []
    return
  }
  const res = await batchApi.page({
    storeId: form.storeId,
    status: [1, 2],
    pageSize: 100
  })
  storeBatches.value = res.data.list || []
}

const onBatchChange = () => {
  const batch = storeBatches.value.find((b) => b.id === form.batchId)
  if (batch) {
    form.maxQty = batch.remainQty
    form.damageQty = Math.min(form.damageQty || 0, batch.remainQty)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await damageApi.page(query)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.damageNo = ''
  query.status = null
  query.damageType = null
  query.pageNum = 1
  loadData()
}

const openCreateDialog = () => {
  form.storeId = null
  form.damageType = 1
  form.batchId = null
  form.damageQty = 0
  form.maxQty = 0
  form.reason = ''
  form.remark = ''
  storeBatches.value = []
  createDialogVisible.value = true
}

const submitCreate = async () => {
  await formRef.value.validate()
  await damageApi.create(form)
  ElMessage.success('创建成功，请等待审核')
  createDialogVisible.value = false
  loadData()
}

const viewDetail = async (row) => {
  const res = await damageApi.detail(row.id)
  damageDetail.value = res.data || {}
  detailDialogVisible.value = true
}

const handleAudit = (row, passed) => {
  const title = passed ? '确认审核通过？' : '确认驳回？'
  const message = passed ? '审核通过后将扣减对应库存' : ''
  ElMessageBox.prompt(message, title, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
    inputPlaceholder: passed ? '填写审核意见（可选）' : '填写驳回原因',
    inputRequired: !passed
  })
    .then(async ({ value }) => {
      await damageApi.audit(row.id, {
        passed,
        remark: value
      })
      ElMessage.success(passed ? '审核通过' : '已驳回')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => {
  loadStoreList()
  loadData()
})
</script>
