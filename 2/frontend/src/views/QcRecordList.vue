<template>
  <div class="qc-record-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pass-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.passCount }}</div>
              <div class="stat-label">合格</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon fail-icon">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.failCount }}</div>
              <div class="stat-label">不合格</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.passRate }}%</div>
              <div class="stat-label">合格率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>质控记录列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增质控记录
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="recordCode" label="记录编号" min-width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="qcType" label="质控类型" min-width="100">
          <template #default="{ row }">{{ getQcTypeText(row.qcType) }}</template>
        </el-table-column>
        <el-table-column prop="qcContent" label="质控内容" min-width="150" />
        <el-table-column prop="qcResult" label="质控结果" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.qcResult)">{{ getResultText(row.qcResult) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executorName" label="质控人员" min-width="100" />
        <el-table-column prop="qcDate" label="质控时间" min-width="160" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="设备名称" prop="deviceId">
          <el-select v-model="formData.deviceId" placeholder="请选择设备" style="width: 100%">
            <el-option v-for="device in deviceList" :key="device.id" :label="device.deviceName" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="质控类型" prop="qcType">
          <el-select v-model="formData.qcType" placeholder="请选择质控类型" style="width: 100%">
            <el-option label="日常质控" :value="1" />
            <el-option label="周度质控" :value="2" />
            <el-option label="月度质控" :value="3" />
            <el-option label="年度质控" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="质控内容" prop="qcContent">
          <el-input v-model="formData.qcContent" placeholder="请输入质控内容" />
        </el-form-item>
        <el-form-item label="质控标准" prop="standard">
          <el-input v-model="formData.standard" type="textarea" :rows="2" placeholder="请输入质控标准" />
        </el-form-item>
        <el-form-item label="实测值" prop="measuredValue">
          <el-input v-model="formData.measuredValue" placeholder="请输入实测值" />
        </el-form-item>
        <el-form-item label="质控结果" prop="qcResult">
          <el-radio-group v-model="formData.qcResult">
            <el-radio :label="1">合格</el-radio>
            <el-radio :label="2">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质控人员" prop="executorName">
          <el-input v-model="formData.executorName" placeholder="请输入质控人员姓名" />
        </el-form-item>
        <el-form-item label="质控时间" prop="qcDate">
          <el-date-picker
            v-model="formData.qcDate"
            type="datetime"
            placeholder="选择质控时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="不合格说明" v-if="formData.qcResult === 2" prop="failDesc">
          <el-input v-model="formData.failDesc" type="textarea" :rows="3" placeholder="请详细说明不合格原因" />
        </el-form-item>
        <el-form-item label="整改建议" prop="suggestion">
          <el-input v-model="formData.suggestion" type="textarea" :rows="2" placeholder="请输入整改建议（可选）" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="质控记录详情" width="700px">
      <el-descriptions :column="2" border v-loading="detailLoading">
        <el-descriptions-item label="记录编号">{{ detailData.recordCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detailData.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="质控类型">{{ getQcTypeText(detailData.qcType) }}</el-descriptions-item>
        <el-descriptions-item label="质控人员">{{ detailData.executorName }}</el-descriptions-item>
        <el-descriptions-item label="质控结果">
          <el-tag :type="getResultType(detailData.qcResult)">{{ getResultText(detailData.qcResult) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="质控时间">{{ detailData.qcDate }}</el-descriptions-item>
        <el-descriptions-item label="质控内容" :span="2">{{ detailData.qcContent }}</el-descriptions-item>
        <el-descriptions-item label="质控标准" :span="2">{{ detailData.standard }}</el-descriptions-item>
        <el-descriptions-item label="实测值" :span="2">{{ detailData.measuredValue }}</el-descriptions-item>
        <el-descriptions-item label="不合格说明" :span="2" v-if="detailData.qcResult === 2">{{ detailData.failDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改建议" :span="2">{{ detailData.suggestion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, Document, TrendCharts, Plus } from '@element-plus/icons-vue'
import { getQcRecordPage, createQcRecord, updateQcRecord, getQcRecordById, getQcRecordsByDeviceId } from '@/api/qcRecord'
import { getDeviceList } from '@/api/device'

const STORAGE_KEY = 'qc_record_list_state'

const loading = ref(false)
const submitting = ref(false)
const detailLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const loadStateFromStorage = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      return JSON.parse(saved)
    }
  } catch (e) {
    console.error('Failed to load state from localStorage:', e)
  }
  return null
}

const savedState = loadStateFromStorage()

const pagination = reactive({
  pageNum: savedState?.pagination?.pageNum ?? 1,
  pageSize: savedState?.pagination?.pageSize ?? 10,
  total: 0
})

const saveStateToStorage = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      pagination: { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    }))
  } catch (e) {
    console.error('Failed to save state to localStorage:', e)
  }
}

watch(
  () => [pagination.pageNum, pagination.pageSize],
  () => {
    saveStateToStorage()
  }
)

const tableData = ref([])

const deviceList = ref([])

const formData = reactive({
  id: null,
  deviceId: '',
  qcType: '',
  qcContent: '',
  standard: '',
  measuredValue: '',
  qcResult: 1,
  executorName: '',
  qcDate: '',
  failDesc: '',
  suggestion: '',
  remark: ''
})

const detailData = reactive({
  recordCode: '',
  deviceName: '',
  qcType: '',
  qcContent: '',
  standard: '',
  measuredValue: '',
  qcResult: null,
  executorName: '',
  qcDate: '',
  failDesc: '',
  suggestion: '',
  remark: ''
})

const formRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  qcType: [{ required: true, message: '请选择质控类型', trigger: 'change' }],
  qcContent: [{ required: true, message: '请输入质控内容', trigger: 'blur' }],
  standard: [{ required: true, message: '请输入质控标准', trigger: 'blur' }],
  measuredValue: [{ required: true, message: '请输入实测值', trigger: 'blur' }],
  qcResult: [{ required: true, message: '请选择质控结果', trigger: 'change' }],
  executorName: [{ required: true, message: '请输入质控人员', trigger: 'blur' }],
  qcDate: [{ required: true, message: '请选择质控时间', trigger: 'change' }],
  failDesc: [{ required: true, message: '请填写不合格说明', trigger: 'blur' }]
}

const stats = computed(() => {
  const records = tableData.value || []
  const passCount = records.filter(r => r.qcResult === 1).length
  const failCount = records.filter(r => r.qcResult === 2).length
  const totalCount = pagination.total
  const passRate = totalCount > 0 ? ((passCount / totalCount) * 100).toFixed(1) : 0
  return {
    totalCount,
    passCount,
    failCount,
    passRate
  }
})

const getQcTypeText = (type) => {
  const map = {
    1: '日常质控',
    2: '周度质控',
    3: '月度质控',
    4: '年度质控'
  }
  return map[type] || '未知'
}

const getResultType = (result) => {
  return result === 1 ? 'success' : 'danger'
}

const getResultText = (result) => {
  return result === 1 ? '合格' : '不合格'
}

const fetchDevices = async () => {
  try {
    const res = await getDeviceList({ pageNum: 1, pageSize: 100 })
    deviceList.value = res.data?.records || []
  } catch (error) {
    console.error('获取设备列表失败', error)
    ElMessage.error('获取设备列表失败')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getQcRecordPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    saveStateToStorage()
  } catch (error) {
    console.error('获取质控记录列表失败', error)
    ElMessage.error('获取质控记录列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增质控记录'
  Object.keys(formData).forEach(key => {
    formData[key] = key === 'qcResult' ? 1 : ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑质控记录'
  try {
    const res = await getQcRecordById(row.id)
    const data = res.data || {}
    Object.keys(formData).forEach(key => {
      if (key === 'failDesc') {
        formData[key] = data.deviationDescription || data[key] || ''
      } else {
        formData[key] = data[key] || ''
      }
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取质控记录详情失败', error)
    ElMessage.error('获取质控记录详情失败')
  }
}

const handleView = async (row) => {
  detailLoading.value = true
  try {
    const res = await getQcRecordById(row.id)
    const data = res.data || {}
    Object.keys(detailData).forEach(key => {
      if (key === 'failDesc') {
        detailData[key] = data.deviationDescription || data[key] || ''
      } else {
        detailData[key] = data[key] || ''
      }
    })
    detailVisible.value = true
  } catch (error) {
    console.error('获取质控记录详情失败', error)
    ElMessage.error('获取质控记录详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = { ...formData }
        submitData.deviationDescription = formData.failDesc
        if (isEdit.value) {
          await updateQcRecord(submitData)
          ElMessage.success('更新成功')
        } else {
          await createQcRecord(submitData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('操作失败', error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchDevices()
  fetchData()
})
</script>

<style scoped>
.qc-record-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.pass-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.fail-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.total-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.rate-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
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
