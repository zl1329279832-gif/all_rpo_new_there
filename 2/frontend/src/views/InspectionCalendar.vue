<template>
  <div class="inspection-calendar-container">
    <el-card class="calendar-card">
      <template #header>
        <div class="card-header">
          <span>巡检日历</span>
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
        <template #empty>
          <el-empty description="当日暂无巡检任务" />
        </template>
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="taskName" label="巡检任务" min-width="120" />
        <el-table-column prop="inspectorName" label="巡检人员" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)">{{ getTaskStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planDate" label="计划日期" min-width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewTask(row)">详情</el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleExecuteTask(row)"
              :disabled="row.status !== 1"
            >执行巡检</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="executeDialogVisible" title="执行巡检" width="700px" :close-on-click-modal="false">
      <el-form :model="executeForm" :rules="executeFormRules" ref="executeFormRef" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="executeForm.deviceName" disabled />
        </el-form-item>
        <el-form-item label="巡检结果" prop="checkResult">
          <el-radio-group v-model="executeForm.checkResult">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="异常描述" v-if="executeForm.checkResult === 2" prop="abnormalDesc">
          <el-input v-model="executeForm.abnormalDesc" type="textarea" :rows="3" placeholder="请描述异常情况" />
        </el-form-item>
        <el-form-item label="处理建议" prop="handleSuggestion">
          <el-input v-model="executeForm.handleSuggestion" type="textarea" :rows="2" placeholder="请填写处理建议（可选）" />
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
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="巡检人员">{{ currentTask.inspectorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getTaskStatusType(currentTask.status)">{{ getTaskStatusText(currentTask.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划日期">{{ currentTask.planDate }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentTask.executeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="巡检结果" :span="2">{{ getCheckResultText(currentTask.checkResult) }}</el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ currentTask.abnormalDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理建议" :span="2">{{ currentTask.handleSuggestion || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getCalendarTasks, executeTask } from '@/api/inspection'

const STORAGE_KEY = 'inspection_calendar_state'

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

const loading = ref(false)
const submitting = ref(false)
const selectedDate = ref(savedState?.selectedDate ? new Date(savedState.selectedDate) : new Date())
const today = new Date().getDate()
const executeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const executeFormRef = ref(null)

const saveStateToStorage = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      selectedDate: selectedDate.value.toISOString()
    }))
  } catch (e) {
    console.error('Failed to save state to localStorage:', e)
  }
}

watch(selectedDate, () => {
  saveStateToStorage()
})

const taskList = ref([])

const dayTasks = computed(() => {
  const dateStr = formatDate(selectedDate.value)
  return taskList.value.filter(task => task.planDate === dateStr)
})

const executeForm = reactive({
  id: null,
  deviceName: '',
  checkResult: 1,
  abnormalDesc: '',
  handleSuggestion: ''
})

const executeFormRules = {
  checkResult: [{ required: true, message: '请选择巡检结果', trigger: 'change' }],
  abnormalDesc: [{ required: true, message: '请描述异常情况', trigger: 'blur' }]
}

const currentTask = reactive({
  id: null,
  deviceName: '',
  taskName: '',
  inspectorName: '',
  status: null,
  planDate: '',
  executeTime: '',
  checkResult: null,
  abnormalDesc: '',
  handleSuggestion: ''
})

const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getMonthRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month + 2, 0)
  return {
    start: formatDate(firstDay),
    end: formatDate(lastDay)
  }
}

const fetchTasks = async () => {
  loading.value = true
  try {
    const range = getMonthRange()
    const res = await getCalendarTasks(range.start, range.end)
    taskList.value = res.data || []
  } catch (error) {
    console.error('获取巡检任务失败', error)
    ElMessage.error('获取巡检任务失败')
    taskList.value = []
  } finally {
    loading.value = false
  }
}

const refreshTaskById = (taskId, updatedData) => {
  const index = taskList.value.findIndex(t => t.id === taskId)
  if (index !== -1) {
    taskList.value[index] = { ...taskList.value[index], ...updatedData }
  }
}

const getTasksByDate = (dateStr) => {
  return taskList.value.filter(task => task.planDate === dateStr)
}

const getTaskDotClass = (status) => {
  const map = { 1: 'pending', 2: 'in-progress', 3: 'completed' }
  return map[status] || 'pending'
}

const getTaskStatusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success' }
  return map[status] || 'info'
}

const getTaskStatusText = (status) => {
  const map = { 1: '待执行', 2: '进行中', 3: '已完成' }
  return map[status] || '未知'
}

const getCheckResultText = (result) => {
  const map = { 1: '正常', 2: '异常' }
  return map[result] || '-'
}

const handleDateClick = (data) => {
  selectedDate.value = new Date(data.date)
}

const handleViewTask = (row) => {
  Object.assign(currentTask, row)
  detailDialogVisible.value = true
}

const handleExecuteTask = (row) => {
  executeForm.id = row.id
  executeForm.deviceName = row.deviceName
  executeForm.checkResult = 1
  executeForm.abnormalDesc = ''
  executeForm.handleSuggestion = ''
  executeDialogVisible.value = true
}

const handleSubmitExecute = async () => {
  if (!executeFormRef.value) return
  await executeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await executeTask(
          executeForm.id,
          executeForm.checkResult,
          executeForm.abnormalDesc,
          executeForm.handleSuggestion
        )
        refreshTaskById(executeForm.id, {
          status: 3,
          checkResult: executeForm.checkResult,
          abnormalDesc: executeForm.abnormalDesc,
          handleSuggestion: executeForm.handleSuggestion,
          executeTime: new Date().toLocaleString()
        })
        ElMessage.success({
          message: '巡检结果提交成功',
          type: 'success'
        })
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
  fetchTasks()
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
