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
        <el-table-column prop="recordNo" label="记录编号" min-width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="qcType" label="质控类型" min-width="100">
          <template #default="{ row }">{{ getQcTypeText(row.qcType) }}</template>
        </el-table-column>
        <el-table-column prop="qcItem" label="质控项目" min-width="150" />
        <el-table-column prop="result" label="质控结果" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.result)">{{ getResultText(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inspector" label="质控人员" min-width="100" />
        <el-table-column prop="qcTime" label="质控时间" min-width="160" />
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
            <el-option v-for="device in deviceList" :key="device.id" :label="device.name" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="质控类型" prop="qcType">
          <el-select v-model="formData.qcType" placeholder="请选择质控类型" style="width: 100%">
            <el-option label="日常质控" value="DAILY" />
            <el-option label="周度质控" value="WEEKLY" />
            <el-option label="月度质控" value="MONTHLY" />
            <el-option label="年度质控" value="YEARLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="质控项目" prop="qcItem">
          <el-input v-model="formData.qcItem" placeholder="请输入质控项目" />
        </el-form-item>
        <el-form-item label="质控标准" prop="standard">
          <el-input v-model="formData.standard" type="textarea" :rows="2" placeholder="请输入质控标准" />
        </el-form-item>
        <el-form-item label="实测值" prop="measuredValue">
          <el-input v-model="formData.measuredValue" placeholder="请输入实测值" />
        </el-form-item>
        <el-form-item label="质控结果" prop="result">
          <el-radio-group v-model="formData.result">
            <el-radio label="PASS">合格</el-radio>
            <el-radio label="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质控人员" prop="inspector">
          <el-input v-model="formData.inspector" placeholder="请输入质控人员姓名" />
        </el-form-item>
        <el-form-item label="质控时间" prop="qcTime">
          <el-date-picker
            v-model="formData.qcTime"
            type="datetime"
            placeholder="选择质控时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="不合格说明" v-if="formData.result === 'FAIL'" prop="failDesc">
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
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录编号">{{ detailData.recordNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detailData.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="质控类型">{{ getQcTypeText(detailData.qcType) }}</el-descriptions-item>
        <el-descriptions-item label="质控人员">{{ detailData.inspector }}</el-descriptions-item>
        <el-descriptions-item label="质控结果">
          <el-tag :type="getResultType(detailData.result)">{{ getResultText(detailData.result) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="质控时间">{{ detailData.qcTime }}</el-descriptions-item>
        <el-descriptions-item label="质控项目" :span="2">{{ detailData.qcItem }}</el-descriptions-item>
        <el-descriptions-item label="质控标准" :span="2">{{ detailData.standard }}</el-descriptions-item>
        <el-descriptions-item label="实测值" :span="2">{{ detailData.measuredValue }}</el-descriptions-item>
        <el-descriptions-item label="不合格说明" :span="2" v-if="detailData.result === 'FAIL'">{{ detailData.failDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改建议" :span="2">{{ detailData.suggestion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, Document, TrendCharts, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

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

const formData = reactive({
  id: null,
  deviceId: '',
  qcType: '',
  qcItem: '',
  standard: '',
  measuredValue: '',
  result: 'PASS',
  inspector: '',
  qcTime: '',
  failDesc: '',
  suggestion: '',
  remark: ''
})

const detailData = reactive({
  recordNo: '',
  deviceName: '',
  qcType: '',
  qcItem: '',
  standard: '',
  measuredValue: '',
  result: '',
  inspector: '',
  qcTime: '',
  failDesc: '',
  suggestion: '',
  remark: ''
})

const formRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  qcType: [{ required: true, message: '请选择质控类型', trigger: 'change' }],
  qcItem: [{ required: true, message: '请输入质控项目', trigger: 'blur' }],
  standard: [{ required: true, message: '请输入质控标准', trigger: 'blur' }],
  measuredValue: [{ required: true, message: '请输入实测值', trigger: 'blur' }],
  result: [{ required: true, message: '请选择质控结果', trigger: 'change' }],
  inspector: [{ required: true, message: '请输入质控人员', trigger: 'blur' }],
  qcTime: [{ required: true, message: '请选择质控时间', trigger: 'change' }],
  failDesc: [{ required: true, message: '请填写不合格说明', trigger: 'blur' }]
}

const stats = computed(() => {
  const total = tableData.value.length
  const pass = tableData.value.filter(r => r.result === 'PASS').length
  const fail = total - pass
  const passRate = total > 0 ? ((pass / total) * 100).toFixed(1) : 0
  return {
    totalCount: total,
    passCount: pass,
    failCount: fail,
    passRate: passRate
  }
})

const getQcTypeText = (type) => {
  const map = {
    DAILY: '日常质控',
    WEEKLY: '周度质控',
    MONTHLY: '月度质控',
    YEARLY: '年度质控'
  }
  return map[type] || type
}

const getResultType = (result) => {
  return result === 'PASS' ? 'success' : 'danger'
}

const getResultText = (result) => {
  return result === 'PASS' ? '合格' : '不合格'
}

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = [
      { id: 1, recordNo: 'QC20260602001', deviceName: 'CT扫描仪', qcType: 'DAILY', qcItem: '图像质量检测', standard: 'CT值误差≤5HU', measuredValue: '3HU', result: 'PASS', inspector: '质检员A', qcTime: '2026-06-02 09:00:00', failDesc: '', suggestion: '', remark: '' },
      { id: 2, recordNo: 'QC20260602002', deviceName: '核磁共振仪', qcType: 'WEEKLY', qcItem: '磁场均匀性', standard: '≤0.5ppm', measuredValue: '0.6ppm', result: 'FAIL', inspector: '质检员B', qcTime: '2026-06-02 10:30:00', failDesc: '磁场均匀性超出标准范围', suggestion: '建议联系厂家进行校准', remark: '已通知维修部门' },
      { id: 3, recordNo: 'QC20260601001', deviceName: 'X光机', qcType: 'DAILY', qcItem: '剂量检测', standard: '空气比释动能≤0.1mGy', measuredValue: '0.08mGy', result: 'PASS', inspector: '质检员A', qcTime: '2026-06-01 08:30:00', failDesc: '', suggestion: '', remark: '' },
      { id: 4, recordNo: 'QC20260601002', deviceName: 'B超机', qcType: 'MONTHLY', qcItem: '探头灵敏度', standard: '全部通道正常', measuredValue: '全部正常', result: 'PASS', inspector: '质检员C', qcTime: '2026-06-01 14:00:00', failDesc: '', suggestion: '', remark: '' },
      { id: 5, recordNo: 'QC20260531001', deviceName: '心电图机', qcType: 'DAILY', qcItem: '波形准确性', standard: '误差≤5%', measuredValue: '3%', result: 'PASS', inspector: '质检员A', qcTime: '2026-05-31 09:00:00', failDesc: '', suggestion: '', remark: '' },
      { id: 6, recordNo: 'QC20260530001', deviceName: '麻醉机', qcType: 'WEEKLY', qcItem: '氧浓度检测', standard: '95%-100%', measuredValue: '92%', result: 'FAIL', inspector: '质检员B', qcTime: '2026-05-30 11:00:00', failDesc: '氧浓度偏低', suggestion: '检查氧气供应系统', remark: '已安排维修' }
    ]
    pagination.total = 6
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增质控记录'
  Object.keys(formData).forEach(key => {
    formData[key] = key === 'result' ? 'PASS' : ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑质控记录'
  Object.keys(formData).forEach(key => {
    formData[key] = row[key] || ''
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  Object.keys(detailData).forEach(key => {
    detailData[key] = row[key] || ''
  })
  detailVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          const index = tableData.value.findIndex(r => r.id === formData.id)
          if (index > -1) {
            tableData.value[index] = { ...tableData.value[index], ...formData }
          }
          ElMessage.success('更新成功')
        } else {
          const newRecord = {
            id: Date.now(),
            recordNo: 'QC' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + String(tableData.value.length + 1).padStart(3, '0'),
            deviceName: deviceList.value.find(d => d.id === formData.deviceId)?.name || '',
            ...formData
          }
          tableData.value.unshift(newRecord)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
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
