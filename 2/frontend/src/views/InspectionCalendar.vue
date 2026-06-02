<template>
  <div class="inspection-calendar-container">
    <el-card class="calendar-card">
      <template #header>
        <div class="card-header">
          <span>巡检日历</span>
          <el-button type="primary" @click="handleAddTask">
            <el-icon><Plus /></el-icon>
            新增巡检任务
          </el-button>
        </div>
      </template>

      <el-calendar v-model="selectedDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell" @click="handleDateClick(data)">
            <div class="date-number" :class="{ 'is-today': data.isCurrentMonth && data.day === today }">
              {{ data.day }}
            </div>
            <div class="task-dots">
              <span
                v-for="(task, index) in getTasksByDate(data.date)"
                :key="index"
                class="task-dot"
                :class="getTaskDotClass(task.status)"
              ></span>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-card class="task-list-card" v-if="selectedDate">
      <template #header>
        <span>{{ formatDate(selectedDate) }} - 巡检任务列表</span>
      </template>

      <el-table :data="dayTasks" v-loading="loading" border stripe>
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="planName" label="巡检计划" min-width="120" />
        <el-table-column prop="inspector" label="巡检人员" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)">{{ getTaskStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduledTime" label="计划时间" min-width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewTask(row)">详情</el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleExecuteTask(row)"
              :disabled="row.status !== 'PENDING'"
            >执行巡检</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="dayTasks.length === 0 && !loading" description="当日暂无巡检任务" />
    </el-card>

    <el-dialog v-model="executeDialogVisible" title="执行巡检" width="700px" :close-on-click-modal="false">
      <el-form :model="executeForm" :rules="executeFormRules" ref="executeFormRef" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="executeForm.deviceName" disabled />
        </el-form-item>
        <el-form-item label="巡检计划">
          <el-input v-model="executeForm.planName" disabled />
        </el-form-item>
        <el-form-item label="巡检结果" prop="result">
          <el-radio-group v-model="executeForm.result">
            <el-radio label="NORMAL">正常</el-radio>
            <el-radio label="ABNORMAL">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="巡检内容" prop="content">
          <el-input v-model="executeForm.content" type="textarea" :rows="4" placeholder="请填写巡检内容" />
        </el-form-item>
        <el-form-item label="异常描述" v-if="executeForm.result === 'ABNORMAL'" prop="abnormalDesc">
          <el-input v-model="executeForm.abnormalDesc" type="textarea" :rows="3" placeholder="请描述异常情况" />
        </el-form-item>
        <el-form-item label="处理建议" prop="suggestion">
          <el-input v-model="executeForm.suggestion" type="textarea" :rows="2" placeholder="请填写处理建议（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitExecute" :loading="submitting">提交巡检结果</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="巡检任务详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ currentTask.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="巡检计划">{{ currentTask.planName }}</el-descriptions-item>
        <el-descriptions-item label="巡检人员">{{ currentTask.inspector }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getTaskStatusType(currentTask.status)">{{ getTaskStatusText(currentTask.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划时间">{{ currentTask.scheduledTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentTask.completedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="巡检结果" :span="2">{{ currentTask.result ? (currentTask.result === 'NORMAL' ? '正常' : '异常') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="巡检内容" :span="2">{{ currentTask.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ currentTask.abnormalDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理建议" :span="2">{{ currentTask.suggestion || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const selectedDate = ref(new Date())
const today = new Date().getDate()
const executeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const executeFormRef = ref(null)

const taskList = ref([
  { id: 1, deviceName: 'CT扫描仪', planName: '日常巡检', inspector: '张三', status: 'PENDING', scheduledTime: '2026-06-02 09:00', result: null, content: '', abnormalDesc: '', suggestion: '', completedTime: null },
  { id: 2, deviceName: '核磁共振仪', planName: '周度巡检', inspector: '李四', status: 'COMPLETED', scheduledTime: '2026-06-02 14:00', result: 'NORMAL', content: '设备运行正常，各项指标符合要求', abnormalDesc: '', suggestion: '', completedTime: '2026-06-02 14:30' },
  { id: 3, deviceName: 'X光机', planName: '日常巡检', inspector: '王五', status: 'PENDING', scheduledTime: '2026-06-03 10:00', result: null, content: '', abnormalDesc: '', suggestion: '', completedTime: null },
  { id: 4, deviceName: 'B超机', planName: '月度巡检', inspector: '赵六', status: 'COMPLETED', scheduledTime: '2026-06-01 09:00', result: 'ABNORMAL', content: '检查设备各功能模块', abnormalDesc: '图像显示有轻微模糊', suggestion: '建议安排维修', completedTime: '2026-06-01 10:00' },
  { id: 5, deviceName: '心电图机', planName: '日常巡检', inspector: '张三', status: 'IN_PROGRESS', scheduledTime: '2026-06-02 15:00', result: null, content: '', abnormalDesc: '', suggestion: '', completedTime: null },
  { id: 6, deviceName: '麻醉机', planName: '周度巡检', inspector: '李四', status: 'PENDING', scheduledTime: '2026-06-05 09:00', result: null, content: '', abnormalDesc: '', suggestion: '', completedTime: null }
])

const dayTasks = computed(() => {
  const dateStr = formatDate(selectedDate.value)
  return taskList.value.filter(task => task.scheduledTime.startsWith(dateStr))
})

const executeForm = reactive({
  id: null,
  deviceName: '',
  planName: '',
  result: 'NORMAL',
  content: '',
  abnormalDesc: '',
  suggestion: ''
})

const executeFormRules = {
  result: [{ required: true, message: '请选择巡检结果', trigger: 'change' }],
  content: [{ required: true, message: '请填写巡检内容', trigger: 'blur' }],
  abnormalDesc: [{ required: true, message: '请描述异常情况', trigger: 'blur' }]
}

const currentTask = reactive({
  id: null,
  deviceName: '',
  planName: '',
  inspector: '',
  status: '',
  scheduledTime: '',
  completedTime: '',
  result: '',
  content: '',
  abnormalDesc: '',
  suggestion: ''
})

const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getTasksByDate = (dateStr) => {
  return taskList.value.filter(task => task.scheduledTime.startsWith(dateStr))
}

const getTaskDotClass = (status) => {
  const map = {
    PENDING: 'pending',
    IN_PROGRESS: 'in-progress',
    COMPLETED: 'completed'
  }
  return map[status] || 'pending'
}

const getTaskStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success'
  }
  return map[status] || 'info'
}

const getTaskStatusText = (status) => {
  const map = {
    PENDING: '待执行',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成'
  }
  return map[status] || status
}

const handleDateClick = (data) => {
  selectedDate.value = new Date(data.date)
}

const handleAddTask = () => {
  ElMessage.info('新增巡检任务功能开发中')
}

const handleViewTask = (row) => {
  Object.keys(currentTask).forEach(key => {
    currentTask[key] = row[key] || ''
  })
  detailDialogVisible.value = true
}

const handleExecuteTask = (row) => {
  executeForm.id = row.id
  executeForm.deviceName = row.deviceName
  executeForm.planName = row.planName
  executeForm.result = 'NORMAL'
  executeForm.content = ''
  executeForm.abnormalDesc = ''
  executeForm.suggestion = ''
  executeDialogVisible.value = true
}

const handleSubmitExecute = async () => {
  if (!executeFormRef.value) return
  await executeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const task = taskList.value.find(t => t.id === executeForm.id)
        if (task) {
          task.status = 'COMPLETED'
          task.result = executeForm.result
          task.content = executeForm.content
          task.abnormalDesc = executeForm.abnormalDesc
          task.suggestion = executeForm.suggestion
          task.completedTime = new Date().toLocaleString()
        }
        ElMessage.success('巡检结果提交成功')
        executeDialogVisible.value = false
      } catch (error) {
        ElMessage.error('提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  selectedDate.value = new Date()
})
</script>

<style scoped>
.inspection-calendar-container {
  padding: 0;
}

.calendar-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-cell {
  padding: 5px;
  height: 80px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.calendar-cell:hover {
  background-color: #f5f7fa;
}

.date-number {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
}

.date-number.is-today {
  color: #409eff;
  font-weight: bold;
}

.task-dots {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
}

.task-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.task-dot.pending {
  background-color: #e6a23c;
}

.task-dot.in-progress {
  background-color: #409eff;
}

.task-dot.completed {
  background-color: #67c23a;
}

.task-list-card {
  margin-bottom: 20px;
}
</style>
