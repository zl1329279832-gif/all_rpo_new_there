<template>
  <div class="repair-order-container">
    <el-card class="filter-card">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="工单状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
            <el-option label="待派单" value="PENDING" />
            <el-option label="已派单" value="ASSIGNED" />
            <el-option label="维修中" value="IN_PROGRESS" />
            <el-option label="待验收" value="FOR_CHECK" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="filterForm.deviceName" placeholder="请输入设备名称" clearable />
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
        <el-table-column prop="description" label="故障描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reporter" label="报修人" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleDispatch(row)"
              :disabled="row.status !== 'PENDING'"
            >派单</el-button>
            <el-button
              link
              type="warning"
              size="small"
              @click="handleStartRepair(row)"
              :disabled="row.status !== 'ASSIGNED'"
            >开始维修</el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleCompleteRepair(row)"
              :disabled="row.status !== 'IN_PROGRESS'"
            >完成维修</el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleAccept(row)"
              :disabled="row.status !== 'FOR_CHECK'"
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
        <el-form-item label="设备名称" prop="deviceId">
          <el-select v-model="createForm.deviceId" placeholder="请选择设备" style="width: 100%">
            <el-option v-for="device in deviceList" :key="device.id" :label="device.name" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="createForm.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="机械故障" value="MECHANICAL" />
            <el-option label="电路故障" value="ELECTRICAL" />
            <el-option label="软件故障" value="SOFTWARE" />
            <el-option label="其他故障" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="请详细描述故障情况" />
        </el-form-item>
        <el-form-item label="紧急程度" prop="priority">
          <el-radio-group v-model="createForm.priority">
            <el-radio label="LOW">低</el-radio>
            <el-radio label="MEDIUM">中</el-radio>
            <el-radio label="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报修人" prop="reporter">
          <el-input v-model="createForm.reporter" placeholder="请输入报修人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="createForm.contactPhone" placeholder="请输入联系电话" />
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
            <el-option label="张三" :value="1" />
            <el-option label="李四" :value="2" />
            <el-option label="王五" :value="3" />
            <el-option label="赵六" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计完成时间" prop="expectedCompleteTime">
          <el-date-picker
            v-model="dispatchForm.expectedCompleteTime"
            type="datetime"
            placeholder="选择预计完成时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
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
        <el-form-item label="更换配件" prop="partsReplaced">
          <el-input v-model="completeForm.partsReplaced" type="textarea" :rows="2" placeholder="请列出更换的配件（如无则填无）" />
        </el-form-item>
        <el-form-item label="维修费用" prop="repairCost">
          <el-input-number v-model="completeForm.repairCost" :min="0" :precision="2" placeholder="请输入维修费用" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="completeForm.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
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
        <el-descriptions-item label="紧急程度">{{ getPriorityText(currentOrder.priority) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentOrder.reporter }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ currentOrder.repairer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentOrder.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentOrder.description }}</el-descriptions-item>
        <el-descriptions-item label="维修内容" :span="2">{{ currentOrder.repairContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更换配件" :span="2">{{ currentOrder.partsReplaced || '-' }}</el-descriptions-item>
        <el-descriptions-item label="维修费用" :span="2">{{ currentOrder.repairCost ? '¥' + currentOrder.repairCost : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getRepairOrderPage, createRepairOrder, updateRepairOrderStatus } from '@/api/repair'

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
  status: '',
  deviceName: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const deviceList = ref([
  { id: 1, name: 'CT扫描仪' },
  { id: 2, name: '核磁共振仪' },
  { id: 3, name: 'X光机' },
  { id: 4, name: 'B超机' },
  { id: 5, name: '心电图机' }
])

const currentOrderId = ref(null)

const createForm = reactive({
  deviceId: '',
  faultType: '',
  description: '',
  priority: 'MEDIUM',
  reporter: '',
  contactPhone: ''
})

const dispatchForm = reactive({
  repairerId: '',
  expectedCompleteTime: '',
  remark: ''
})

const completeForm = reactive({
  repairContent: '',
  partsReplaced: '',
  repairCost: 0,
  remark: ''
})

const currentOrder = reactive({
  orderNo: '',
  deviceName: '',
  faultType: '',
  description: '',
  priority: '',
  reporter: '',
  contactPhone: '',
  repairer: '',
  status: '',
  createTime: '',
  completeTime: '',
  repairContent: '',
  partsReplaced: '',
  repairCost: null
})

const createFormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
  reporter: [{ required: true, message: '请输入报修人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const dispatchFormRules = {
  repairerId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

const completeFormRules = {
  repairContent: [{ required: true, message: '请输入维修内容', trigger: 'blur' }],
  partsReplaced: [{ required: true, message: '请填写更换配件', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'info',
    ASSIGNED: 'primary',
    IN_PROGRESS: 'warning',
    FOR_CHECK: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待派单',
    ASSIGNED: '已派单',
    IN_PROGRESS: '维修中',
    FOR_CHECK: '待验收',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getFaultTypeText = (type) => {
  const map = {
    MECHANICAL: '机械故障',
    ELECTRICAL: '电路故障',
    SOFTWARE: '软件故障',
    OTHER: '其他故障'
  }
  return map[type] || type
}

const getPriorityText = (priority) => {
  const map = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高'
  }
  return map[priority] || priority
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRepairOrderPage({
      ...filterForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data?.records || [
      { id: 1, orderNo: 'WO20260602001', deviceName: 'CT扫描仪', faultType: 'MECHANICAL', description: '设备扫描时发出异常噪音', reporter: '王医生', status: 'PENDING', priority: 'HIGH', contactPhone: '13800138001', createTime: '2026-06-02 09:30:00' },
      { id: 2, orderNo: 'WO20260602002', deviceName: 'B超机', faultType: 'SOFTWARE', description: '图像显示系统偶尔卡顿', reporter: '李医生', status: 'ASSIGNED', repairer: '张三', priority: 'MEDIUM', contactPhone: '13800138002', createTime: '2026-06-02 10:15:00' },
      { id: 3, orderNo: 'WO20260601001', deviceName: 'X光机', faultType: 'ELECTRICAL', description: '曝光按钮失灵', reporter: '张医生', status: 'IN_PROGRESS', repairer: '李四', priority: 'HIGH', contactPhone: '13800138003', createTime: '2026-06-01 14:20:00' },
      { id: 4, orderNo: 'WO20260601002', deviceName: '心电图机', faultType: 'OTHER', description: '打印纸无法正常输出', reporter: '刘医生', status: 'FOR_CHECK', repairer: '王五', repairContent: '更换了打印电机', partsReplaced: '打印电机 x1', repairCost: 500, priority: 'LOW', contactPhone: '13800138004', createTime: '2026-06-01 16:45:00' },
      { id: 5, orderNo: 'WO20260531001', deviceName: '核磁共振仪', faultType: 'MECHANICAL', description: '门体开关不顺畅', reporter: '陈医生', status: 'COMPLETED', repairer: '赵六', repairContent: '润滑门体轨道，调整门体位置', partsReplaced: '无', repairCost: 200, completeTime: '2026-06-01 11:00:00', priority: 'MEDIUM', contactPhone: '13800138005', createTime: '2026-05-31 09:00:00' }
    ]
    pagination.total = res.data?.total || 5
  } catch (error) {
    tableData.value = [
      { id: 1, orderNo: 'WO20260602001', deviceName: 'CT扫描仪', faultType: 'MECHANICAL', description: '设备扫描时发出异常噪音', reporter: '王医生', status: 'PENDING', priority: 'HIGH', contactPhone: '13800138001', createTime: '2026-06-02 09:30:00' },
      { id: 2, orderNo: 'WO20260602002', deviceName: 'B超机', faultType: 'SOFTWARE', description: '图像显示系统偶尔卡顿', reporter: '李医生', status: 'ASSIGNED', repairer: '张三', priority: 'MEDIUM', contactPhone: '13800138002', createTime: '2026-06-02 10:15:00' },
      { id: 3, orderNo: 'WO20260601001', deviceName: 'X光机', faultType: 'ELECTRICAL', description: '曝光按钮失灵', reporter: '张医生', status: 'IN_PROGRESS', repairer: '李四', priority: 'HIGH', contactPhone: '13800138003', createTime: '2026-06-01 14:20:00' },
      { id: 4, orderNo: 'WO20260601002', deviceName: '心电图机', faultType: 'OTHER', description: '打印纸无法正常输出', reporter: '刘医生', status: 'FOR_CHECK', repairer: '王五', repairContent: '更换了打印电机', partsReplaced: '打印电机 x1', repairCost: 500, priority: 'LOW', contactPhone: '13800138004', createTime: '2026-06-01 16:45:00' },
      { id: 5, orderNo: 'WO20260531001', deviceName: '核磁共振仪', faultType: 'MECHANICAL', description: '门体开关不顺畅', reporter: '陈医生', status: 'COMPLETED', repairer: '赵六', repairContent: '润滑门体轨道，调整门体位置', partsReplaced: '无', repairCost: 200, completeTime: '2026-06-01 11:00:00', priority: 'MEDIUM', contactPhone: '13800138005', createTime: '2026-05-31 09:00:00' }
    ]
    pagination.total = 5
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = ''
  })
  handleSearch()
}

const handleCreate = () => {
  Object.keys(createForm).forEach(key => {
    createForm[key] = key === 'priority' ? 'MEDIUM' : ''
  })
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
  Object.keys(currentOrder).forEach(key => {
    currentOrder[key] = row[key] || ''
  })
  detailDialogVisible.value = true
}

const handleDispatch = (row) => {
  currentOrderId.value = row.id
  Object.keys(dispatchForm).forEach(key => {
    dispatchForm[key] = ''
  })
  dispatchDialogVisible.value = true
}

const handleSubmitDispatch = async () => {
  if (!dispatchFormRef.value) return
  await dispatchFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await updateRepairOrderStatus(currentOrderId.value, 'ASSIGNED')
        const order = tableData.value.find(o => o.id === currentOrderId.value)
        if (order) {
          order.status = 'ASSIGNED'
          order.repairer = ['张三', '李四', '王五', '赵六'][dispatchForm.repairerId - 1]
        }
        ElMessage.success('派单成功')
        dispatchDialogVisible.value = false
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
    await updateRepairOrderStatus(row.id, 'IN_PROGRESS')
    row.status = 'IN_PROGRESS'
    ElMessage.success('已开始维修')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleCompleteRepair = (row) => {
  currentOrderId.value = row.id
  Object.keys(completeForm).forEach(key => {
    completeForm[key] = key === 'repairCost' ? 0 : ''
  })
  completeDialogVisible.value = true
}

const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  await completeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await updateRepairOrderStatus(currentOrderId.value, 'FOR_CHECK')
        const order = tableData.value.find(o => o.id === currentOrderId.value)
        if (order) {
          order.status = 'FOR_CHECK'
          order.repairContent = completeForm.repairContent
          order.partsReplaced = completeForm.partsReplaced
          order.repairCost = completeForm.repairCost
        }
        ElMessage.success('维修完成，等待验收')
        completeDialogVisible.value = false
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
    await updateRepairOrderStatus(row.id, 'COMPLETED')
    row.status = 'COMPLETED'
    row.completeTime = new Date().toLocaleString()
    ElMessage.success('验收通过')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
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
