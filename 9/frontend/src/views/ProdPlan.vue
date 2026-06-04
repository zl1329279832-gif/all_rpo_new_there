<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">生产计划管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建计划
      </el-button>
    </div>

    <div class="search-bar">
      <div class="search-item">
        <label>计划编号:</label>
        <el-input v-model="query.planNo" placeholder="请输入" clearable style="width: 160px" />
      </div>
      <div class="search-item">
        <label>计划状态:</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="待审核" :value="0" />
          <el-option label="已审核" :value="1" />
          <el-option label="生产中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </div>
      <div class="search-item">
        <label>计划日期:</label>
        <el-date-picker
          v-model="query.planDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 160px"
        />
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
      <el-table-column prop="planNo" label="计划编号" width="160" />
      <el-table-column prop="planName" label="计划名称" width="160" />
      <el-table-column prop="planDate" label="计划日期" width="120" />
      <el-table-column prop="totalQty" label="计划总数" width="100">
        <template #default="{ row }">{{ formatNumber(row.totalQty) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :class="['status-tag', planStatusMap[row.status].class]">
            {{ planStatusMap[row.status].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">
            详情
          </el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="success"
            link
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="warning"
            link
            @click="handleStart(row)"
          >
            开始生产
          </el-button>
          <el-button
            v-if="row.status === 2"
            size="small"
            type="success"
            link
            @click="openCompleteDialog(row)"
          >
            完成生产
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

    <el-dialog v-model="createDialogVisible" title="新建生产计划" width="800px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="form.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划日期" prop="planDate">
              <el-date-picker
                v-model="form.planDate"
                type="date"
                placeholder="选择计划日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="生产明细">
          <el-table :data="form.details" border size="small">
            <el-table-column prop="productName" label="产品名称" width="160">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.recipeId"
                  placeholder="选择产品"
                  style="width: 100%"
                  @change="onRecipeChange($index)"
                >
                  <el-option
                    v-for="recipe in recipeList"
                    :key="recipe.id"
                    :label="recipe.productName"
                    :value="recipe.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="planQty" label="计划数量(个)" width="140">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.planQty"
                  :min="1"
                  :precision="2"
                  :step="10"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="shelfLifeHours" label="保质期(小时)" width="120">
              <template #default="{ row }">{{ row.shelfLifeHours || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeDetail($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link style="margin-top: 10px" @click="addDetail">
            <el-icon><Plus /></el-icon>
            添加产品
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="form-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="生产计划详情" width="800px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="计划编号">{{ planDetail.planNo }}</el-descriptions-item>
        <el-descriptions-item label="计划名称">{{ planDetail.planName }}</el-descriptions-item>
        <el-descriptions-item label="计划日期">{{ planDetail.planDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :class="['status-tag', planStatusMap[planDetail.status].class]">
            {{ planStatusMap[planDetail.status].label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划总数">{{ formatNumber(planDetail.totalQty) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(planDetail.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin: 20px 0 10px">生产明细</h4>
      <el-table :data="planDetail.details || []" border size="small">
        <el-table-column prop="productName" label="产品名称" width="180" />
        <el-table-column prop="planQty" label="计划数量" width="120">
          <template #default="{ row }">{{ formatNumber(row.planQty) }}</template>
        </el-table-column>
        <el-table-column prop="actualQty" label="实际数量" width="120">
          <template #default="{ row }">{{ formatNumber(row.actualQty) }}</template>
        </el-table-column>
        <el-table-column prop="shelfLifeHours" label="保质期(小时)" width="120" />
      </el-table>
      <template #footer>
        <div class="form-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成生产" width="600px">
      <el-form :model="completeForm" :rules="completeRules" ref="completeFormRef" label-width="120px">
        <el-table :data="completeForm.details" border size="small">
          <el-table-column prop="productName" label="产品名称" width="180" />
          <el-table-column prop="planQty" label="计划数量" width="120">
            <template #default="{ row }">{{ formatNumber(row.planQty) }}</template>
          </el-table-column>
          <el-table-column prop="actualQty" label="实际数量" width="160">
            <template #default="{ row }">
              <el-input-number
                v-model="row.actualQty"
                :min="0"
                :precision="2"
                :step="10"
                style="width: 100%"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="form-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitComplete">确认完成</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { planApi, recipeApi } from '@/api'
import { planStatusMap, formatDateTime, formatNumber } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const recipeList = ref([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  planNo: '',
  status: null,
  planDate: ''
})

const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const formRef = ref(null)
const completeFormRef = ref(null)

const planDetail = ref({})

const form = reactive({
  planName: '',
  planDate: '',
  remark: '',
  details: []
})

const completeForm = reactive({
  planId: null,
  details: []
})

const rules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
}

const completeRules = {}

const loadRecipeList = async () => {
  const res = await recipeApi.list()
  recipeList.value = res.data || []
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await planApi.page(query)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.planNo = ''
  query.status = null
  query.planDate = ''
  query.pageNum = 1
  loadData()
}

const openCreateDialog = () => {
  form.planName = ''
  form.planDate = ''
  form.remark = ''
  form.details = [{ recipeId: null, productName: '', planQty: 100, shelfLifeHours: '' }]
  createDialogVisible.value = true
}

const addDetail = () => {
  form.details.push({ recipeId: null, productName: '', planQty: 100, shelfLifeHours: '' })
}

const removeDetail = (index) => {
  form.details.splice(index, 1)
}

const onRecipeChange = (index) => {
  const recipeId = form.details[index].recipeId
  const recipe = recipeList.value.find((r) => r.id === recipeId)
  if (recipe) {
    form.details[index].productName = recipe.productName
    form.details[index].shelfLifeHours = recipe.shelfLifeHours
  }
}

const submitCreate = async () => {
  await formRef.value.validate()
  if (form.details.length === 0) {
    ElMessage.warning('请至少添加一条生产明细')
    return
  }
  for (const detail of form.details) {
    if (!detail.recipeId || !detail.planQty) {
      ElMessage.warning('请完善所有生产明细')
      return
    }
  }
  await planApi.create(form)
  ElMessage.success('创建成功')
  createDialogVisible.value = false
  loadData()
}

const viewDetail = async (row) => {
  const res = await planApi.detail(row.id)
  planDetail.value = res.data || {}
  detailDialogVisible.value = true
}

const handleAudit = (row) => {
  ElMessageBox.confirm('确认审核该生产计划？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await planApi.audit(row.id)
    ElMessage.success('审核成功')
    loadData()
  })
}

const handleStart = (row) => {
  ElMessageBox.confirm('确认开始生产？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await planApi.start(row.id)
    ElMessage.success('已开始生产')
    loadData()
  })
}

const openCompleteDialog = async (row) => {
  const res = await planApi.detail(row.id)
  const details = (res.data.details || []).map((d) => ({
    planDetailId: d.id,
    recipeId: d.recipeId,
    productName: d.productName,
    planQty: d.planQty,
    actualQty: d.planQty
  }))
  completeForm.planId = row.id
  completeForm.details = details
  completeDialogVisible.value = true
}

const submitComplete = async () => {
  for (const detail of completeForm.details) {
    if (detail.actualQty === null || detail.actualQty < 0) {
      ElMessage.warning('请填写正确的实际数量')
      return
    }
  }
  await planApi.complete(completeForm)
  ElMessage.success('生产完成，已生成成品批次')
  completeDialogVisible.value = false
  loadData()
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该生产计划？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await planApi.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  })
}

onMounted(() => {
  loadRecipeList()
  loadData()
})
</script>
