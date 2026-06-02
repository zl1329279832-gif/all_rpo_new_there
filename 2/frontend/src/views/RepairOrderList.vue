<template>
  <div class="repair-order-container">
    <el-card class="filter-card">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="工单状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
            <el-option label="待派单" :value="1" />
            <el-option label="已派单" :value="2" />
            <el-option label="维修中" :value="3" />
            <el-option label="待验收" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="filterForm.keyword" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>维修工单列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建工单
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="工单编号" min-width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="faultType" label="故障类型" min-width="100">
          <template #default="{ row }">{{ getFaultTypeText(row.faultType) }}</template>
        </el-table-column>
        <el-table-column prop="faultDescription" label="故障描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="报修人" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleDispatch(row)"
              :disabled="row.status !== 1"
            >派单</el-button>
            <el-button
              link
              type="warning"
              size="small"
              @click="handleStartRepair(row)"
              :disabled="row.status !== 2"
            >开始维修</el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleCompleteRepair(row)"
              :disabled="row.status !== 3"
            >完成维修</el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleAccept(row)"
              :disabled="row.status !== 4"
            >验收</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="createDialogVisible" title="创建维修工单" width="600px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createFormRules" ref="createFormRef" label-width="100px">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="createForm.deviceId" placeholder="请选择设备" style="width: 100%" filterable>
            <el-option v-for="device in deviceList" :key="device.id" :label="`${device.name} (${device.code})`" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="createForm.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="机械故障" :value="1" />
            <el-option label="电路故障" :value="2" />
            <el-option label="软件故障" :value="3" />
            <el-option label="其他故障" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="createForm.faultDescription" type="textarea" :rows="4" placeholder="请详细描述故障情况" />
        </el-form-item>
        <el-form-item label="紧急程度" prop="faultLevel">
          <el-radio-group v-model="createForm.faultLevel">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报修人" prop="reporterName">
          <el-input v-model="createForm.reporterName" placeholder="请输入报修人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="reporterPhone">
          <el-input v-model="createForm.reporterPhone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCreate" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dispatchDialogVisible" title="派单" width="500px" :close-on-click-modal="false">
      <el-form :model="dispatchForm" :rules="dispatchFormRules" ref="dispatchFormRef" label-width="100px">
        <el-form-item label="维修人员" prop="repairerId">
          <el-select v-model="dispatchForm.repairerId" placeholder="请选择维修人员" style="width: 100%">
            <el-option v-for="user in engineerList" :key="user.id" :label="user.realName" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dispatchForm.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDispatch" :loading="submitting">确认派单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成维修" width="600px" :close-on-click-modal="false">
      <el-form :model="completeForm" :rules="completeFormRules" ref="completeFormRef" label-width="100px">
        <el-form-item label="维修内容" prop="repairContent">
          <el-input v-model="completeForm.repairContent" type="textarea" :rows="4" placeholder="请详细描述维修内容" />
        </el-form-item>
        <el-form-item label="维修结果" prop="repairResult">
          <el-radio-group v-model="completeForm.repairResult">
            <el-radio label="已修复">已修复</el-radio>
            <el-radio label="待配件">待配件</el-radio>
            <el-radio label="无法修复">无法修复</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComplete" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="工单详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentOrder.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ getFaultTypeText(currentOrder.faultType) }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">{{ getPriorityText(currentOrder.faultLevel) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentOrder.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.reporterPhone }}</el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ currentOrder.repairerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentOrder.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentOrder.faultDescription }}</el-descriptions-item>
        <el-descriptions-item label="维修内容" :span="2">{{ currentOrder.repairContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="维修结果" :span="2">{{ currentOrder.repairResult || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getRepairOrderPage, createRepairOrder, assignOrder, startRepair, completeRepair, acceptOrder } from '@/api/repair'
import { getDeviceList } from '@/api/device'

const loading = ref(false)
const submitting = ref(false)
const createDialogVisible = ref(false)
const dispatchDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createFormRef = ref(null)
const dispatchFormRef = ref(null)
const completeFormRef = ref(null)

const filterForm = reactive({
  status: null,
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const deviceList = ref([])
const engineerList = ref([
  { id: 3, realName: '维修工程师1' },
  { id: 4, realName: '维修工程师2' }
])

const currentOrderId = ref(null)

const createForm = reactive({
  deviceId: null,
  faultType: null,
  faultDescription: '',
  faultLevel: 2,
  reporterName: '',
  reporterPhone: ''
})

const dispatchForm = reactive({
  repairerId: null,
  remark: ''
})

const completeForm = reactive({
  repairContent: '',
  repairResult: '已修复'
})

const currentOrder = reactive({
  orderNo: '',
  deviceName: '',
  faultType: null,
  faultDescription: '',
  faultLevel: null,
  reporterName: '',
  reporterPhone: '',
  repairerName: '',
  status: null,
  reportTime: '',
  completeTime: '',
  repairContent: '',
  repairResult: ''
})

const createFormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
  reporterName: [{ required: true, message: '请输入报修人', trigger: 'blur' }]
}

const dispatchFormRules = {
  repairerId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

const completeFormRules = {
  repairContent: [{ required: true, message: '请输入维修内容', trigger: 'blur' }],
  repairResult: [{ required: true, message: '请选择维修结果', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { 1: 'info', 2: 'primary', 3: 'warning', 4: 'warning', 5: 'success', 0: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '待派单', 2: '已派单', 3: '维修中', 4: '待验收', 5: '已完成', 0: '已取消' }
  return map[status] || '未知'
}

const getFaultTypeText = (type) => {
  const map = { 1: '机械故障', 2: '电路故障', 3: '软件故障', 4: '其他故障' }
  return map[type] || '未知'
}

const getPriorityText = (priority) => {
  const map = { 1: '低', 2: '中', 3: '高' }
  return map[priority] || '未知'
}

const fetchDevices = async () => {
  try {
    const res = await getDeviceList({ pageNum: 1, pageSize: 100 })
    deviceList.value = res.data?.records || []
  } catch (error) {
    console.error('获取设备列表失败', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...filterForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getRepairOrderPage(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  filterForm.status = null
  filterForm.keyword = ''
  handleSearch()
}

const handleCreate = () => {
  createForm.deviceId = null
  createForm.faultType = null
  createForm.faultDescription = ''
  createForm.faultLevel = 2
  createForm.reporterName = ''
  createForm.reporterPhone = ''
  createDialogVisible.value = true
}

const handleSubmitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createRepairOrder(createForm)
        ElMessage.success('工单创建成功')
        createDialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleView = (row) => {
  Object.assign(currentOrder, row)
  detailDialogVisible.value = true
}

const handleDispatch = (row) => {
  currentOrderId.value = row.id
  dispatchForm.repairerId = null
  dispatchForm.remark = ''
  dispatchDialogVisible.value = true
}

const handleSubmitDispatch = async () => {
  if (!dispatchFormRef.value) return
  await dispatchFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const engineer = engineerList.value.find(e => e.id === dispatchForm.repairerId)
        await assignOrder(currentOrderId.value, dispatchForm.repairerId, engineer?.realName || '')
        ElMessage.success('派单成功')
        dispatchDialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('派单失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleStartRepair = async (row) => {
  try {
    await ElMessageBox.confirm('确定开始维修吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await startRepair(row.id)
    row.status = 3
    ElMessage.success('已开始维修')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleCompleteRepair = (row) => {
  currentOrderId.value = row.id
  completeForm.repairContent = ''
  completeForm.repairResult = '已修复'
  completeDialogVisible.value = true
}

const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  await completeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await completeRepair(currentOrderId.value, completeForm.repairContent, completeForm.repairResult, [])
        ElMessage.success('维修完成，等待验收')
        completeDialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm('验收通过后工单将标记为已完成，确定验收吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await acceptOrder(row.id, 1)
    row.status = 5
    row.completeTime = new Date().toLocaleString()
    ElMessage.success('验收通过')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchDevices()
  fetchData()
})
</script>

<style scoped>
.repair-order-container {
  padding: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
