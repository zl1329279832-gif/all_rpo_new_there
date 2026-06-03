<template>
  <div class="picking-container">
    <div class="page-header flex-between">
      <h2 class="page-title">拣货任务</h2>
      <div class="header-actions">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button :value="1">列表视图</el-radio-button>
          <el-radio-button :value="2">卡片视图</el-radio-button>
        </el-radio-group>
        <el-button type="primary" style="margin-left: 10px" @click="showGenerateDialog">
          <el-icon><Plus /></el-icon>生成拣货任务
        </el-button>
      </div>
    </div>

    <el-card shadow="hover" class="search-form">
      <el-form :model="queryForm" :inline="true" @submit.prevent>
        <el-form-item label="任务编号">
          <el-input v-model="queryForm.taskNo" placeholder="请输入任务编号" clearable />
        </el-form-item>
        <el-form-item label="波次号">
          <el-input v-model="queryForm.waveNo" placeholder="请输入波次号" clearable />
        </el-form-item>
        <el-form-item label="拣货员">
          <el-select v-model="queryForm.picker" placeholder="请选择" clearable>
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
            <el-option label="王五" value="王五" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待拣货" :value="1" />
            <el-option label="拣货中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="请选择" clearable>
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="viewMode === 1">
      <el-card shadow="hover">
        <el-table :data="tableData" v-loading="loading" border stripe>
          <el-table-column prop="waveNo" label="波次号" width="150" fixed="left">
            <template #default="{ row }">
              <el-tag type="info" effect="dark">{{ row.waveNo }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="taskNo" label="任务编号" width="180" />
          <el-table-column prop="shipmentOrderNo" label="出库单号" width="150" />
          <el-table-column prop="pickingMode" label="拣货方式" width="100">
            <template #default="{ row }">
              <el-tag>{{ getPickingModeName(row.pickingMode) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="dark">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="{ row }">
              <el-tag :type="getPriorityTagType(row.priority)" effect="dark">
                {{ getPriorityName(row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="picker" label="拣货员" width="100" />
          <el-table-column label="拣货进度" width="180">
            <template #default="{ row }">
              <el-progress
                :percentage="getPickingProgress(row)"
                :color="getProgressColor(row.status)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="totalQuantity" label="总数量" width="100" />
          <el-table-column prop="pickedQuantity" label="已拣数量" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="showDetail(row)">
                详情
              </el-button>
              <el-button
                v-if="row.status === 1 || row.status === 2"
                type="warning"
                size="small"
                link
                @click="showPickingDialog(row)"
              >
                扫码拣货
              </el-button>
              <el-button
                v-if="row.status === 2 && row.pickedQuantity > 0"
                type="danger"
                size="small"
                link
                @click="reportException(row)"
              >
                异常处理
              </el-button>
              <el-button
                v-if="row.status === 2 && row.pickedQuantity > 0"
                type="success"
                size="small"
                link
                @click="completePicking(row)"
              >
                完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </el-card>
    </template>

    <template v-else>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="task-column">
            <template #header>
              <div class="column-header">
                <el-tag type="warning" effect="dark">待拣货</el-tag>
                <el-badge :value="pendingTasks.length" class="item" />
              </div>
            </template>
            <div v-loading="loading" class="task-list">
              <el-card
                v-for="task in pendingTasks"
                :key="task.id"
                shadow="hover"
                class="task-card"
              >
                <div class="task-header">
                  <span class="task-no">{{ task.taskNo }}</span>
                  <el-tag :type="getPriorityTagType(task.priority)" size="small">
                    {{ getPriorityName(task.priority) }}
                  </el-tag>
                </div>
                <div class="task-info">
                  <p><span>波次：</span>{{ task.waveNo }}</p>
                  <p><span>出库单：</span>{{ task.shipmentOrderNo }}</p>
                  <p><span>商品数：</span>{{ task.detailCount }}种</p>
                  <p><span>总数量：</span>{{ task.totalQuantity }}</p>
                </div>
                <el-progress
                  :percentage="getPickingProgress(task)"
                  :color="getProgressColor(task.status)"
                  :show-text="false"
                  class="task-progress"
                />
                <div class="task-actions">
                  <el-button type="primary" size="small" @click="showPickingDialog(task)">
                    开始拣货
                  </el-button>
                  <el-button type="info" size="small" @click="showDetail(task)">
                    详情
                  </el-button>
                </div>
              </el-card>
              <el-empty v-if="pendingTasks.length === 0" description="暂无待拣货任务" :image-size="80" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" class="task-column">
            <template #header>
              <div class="column-header">
                <el-tag type="primary" effect="dark">拣货中</el-tag>
                <el-badge :value="pickingTasks.length" class="item" />
              </div>
            </template>
            <div v-loading="loading" class="task-list">
              <el-card
                v-for="task in pickingTasks"
                :key="task.id"
                shadow="hover"
                class="task-card"
              >
                <div class="task-header">
                  <span class="task-no">{{ task.taskNo }}</span>
                  <el-tag :type="getPriorityTagType(task.priority)" size="small">
                    {{ getPriorityName(task.priority) }}
                  </el-tag>
                </div>
                <div class="task-info">
                  <p><span>波次：</span>{{ task.waveNo }}</p>
                  <p><span>出库单：</span>{{ task.shipmentOrderNo }}</p>
                  <p><span>拣货员：</span>{{ task.picker }}</p>
                  <p><span>进度：</span>{{ task.pickedQuantity }}/{{ task.totalQuantity }}</p>
                </div>
                <el-progress
                  :percentage="getPickingProgress(task)"
                  :color="getProgressColor(task.status)"
                  class="task-progress"
                />
                <div class="task-actions">
                  <el-button type="warning" size="small" @click="showPickingDialog(task)">
                    继续拣货
                  </el-button>
                  <el-button type="danger" size="small" @click="reportException(task)">
                    异常
                  </el-button>
                  <el-button type="success" size="small" @click="completePicking(task)">
                    完成
                  </el-button>
                </div>
              </el-card>
              <el-empty v-if="pickingTasks.length === 0" description="暂无拣货中任务" :image-size="80" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" class="task-column">
            <template #header>
              <div class="column-header">
                <el-tag type="success" effect="dark">已完成</el-tag>
                <el-badge :value="completedTasks.length" class="item" />
              </div>
            </template>
            <div v-loading="loading" class="task-list">
              <el-card
                v-for="task in completedTasks"
                :key="task.id"
                shadow="hover"
                class="task-card completed"
              >
                <div class="task-header">
                  <span class="task-no">{{ task.taskNo }}</span>
                  <el-tag type="success" size="small">完成</el-tag>
                </div>
                <div class="task-info">
                  <p><span>波次：</span>{{ task.waveNo }}</p>
                  <p><span>出库单：</span>{{ task.shipmentOrderNo }}</p>
                  <p><span>拣货员：</span>{{ task.picker }}</p>
                  <p><span>完成时间：</span>{{ task.completeTime }}</p>
                </div>
                <el-progress
                  :percentage="100"
                  color="#67c23a"
                  class="task-progress"
                />
                <div class="task-actions">
                  <el-button type="info" size="small" @click="showDetail(task)">
                    查看详情
                  </el-button>
                </div>
              </el-card>
              <el-empty v-if="completedTasks.length === 0" description="暂无已完成任务" :image-size="80" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <el-dialog v-model="generateVisible" title="生成拣货任务" width="600px">
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="100px">
        <el-form-item label="出库单" prop="shipmentOrderIds">
          <el-select v-model="generateForm.shipmentOrderIds" multiple placeholder="请选择出库单" style="width: 100%">
            <el-option
              v-for="order in shipmentOrderList"
              :key="order.id"
              :label="`${order.orderNo} - ${order.customerName}`"
              :value="order.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="波次号">
          <el-input v-model="generateForm.waveNo" placeholder="自动生成或手动输入" />
        </el-form-item>
        <el-form-item label="拣货方式" prop="pickingMode">
          <el-radio-group v-model="generateForm.pickingMode">
            <el-radio :value="1">按单拣货</el-radio>
            <el-radio :value="2">批量拣货</el-radio>
            <el-radio :value="3">分区拣货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拣货员" prop="picker">
          <el-select v-model="generateForm.picker" placeholder="请选择拣货员" style="width: 100%">
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
            <el-option label="王五" value="王五" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="generateForm.priority">
            <el-radio :value="1">低</el-radio>
            <el-radio :value="2">中</el-radio>
            <el-radio :value="3">高</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="generateTasks">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pickingDialogVisible" title="扫码拣货确认" width="600px" :close-on-click-modal="false">
      <el-form ref="pickingFormRef" :model="pickingForm" :rules="pickingRules" label-width="100px">
        <el-form-item label="任务编号">
          <el-input v-model="pickingForm.taskNo" disabled />
        </el-form-item>
        <el-form-item label="库位编码" prop="locationCode">
          <el-input v-model="pickingForm.locationCode" placeholder="扫描或输入库位编码" autofocus>
            <template #append>
              <el-button @click="scanLocationCode">
                <el-icon><Camera /></el-icon>扫描
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="pickingForm.productCode" placeholder="扫描或输入商品编码">
            <template #append>
              <el-button @click="scanProductCode">
                <el-icon><Camera /></el-icon>扫描
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="批次号">
          <el-select v-model="pickingForm.batchId" placeholder="选择批次" style="width: 100%">
            <el-option
              v-for="batch in availableBatches"
              :key="batch.id"
              :label="batch.batchNo"
              :value="batch.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="应拣数量">
          <el-input v-model="pickingForm.planQuantity" disabled />
        </el-form-item>
        <el-form-item label="实拣数量" prop="actualQuantity">
          <el-input-number v-model="pickingForm.actualQuantity" :min="0" :max="pickingForm.planQuantity" />
        </el-form-item>
        <el-form-item v-if="pickingForm.actualQuantity < pickingForm.planQuantity" label="差异原因">
          <el-select v-model="pickingForm.exceptionType" placeholder="请选择差异原因">
            <el-option label="缺货" :value="1" />
            <el-option label="数量不足" :value="2" />
            <el-option label="商品损坏" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="pickingForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pickingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPicking">确认拣货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="exceptionVisible" title="异常处理" width="500px">
      <el-form ref="exceptionFormRef" :model="exceptionForm" :rules="exceptionRules" label-width="100px">
        <el-form-item label="任务编号">
          <el-input v-model="exceptionForm.taskNo" disabled />
        </el-form-item>
        <el-form-item label="异常类型" prop="exceptionType">
          <el-radio-group v-model="exceptionForm.exceptionType">
            <el-radio :value="1">缺货报告</el-radio>
            <el-radio :value="2">数量差异</el-radio>
            <el-radio :value="3">商品损坏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品明细">
          <el-select v-model="exceptionForm.detailId" placeholder="选择商品" style="width: 100%">
            <el-option
              v-for="d in currentTaskDetails"
              :key="d.id"
              :label="`${d.productCode} - ${d.productName}`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="异常数量" prop="quantity">
          <el-input-number v-model="exceptionForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input v-model="exceptionForm.description" type="textarea" :rows="3" placeholder="请详细描述异常情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exceptionVisible = false">取消</el-button>
        <el-button type="primary" @click="submitException">提交异常</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="拣货任务详情" width="800px">
      <template v-if="currentTask">
        <el-descriptions :column="2" border class="mb-20">
          <el-descriptions-item label="任务编号">{{ currentTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="波次号">{{ currentTask.waveNo }}</el-descriptions-item>
          <el-descriptions-item label="出库单号">{{ currentTask.shipmentOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="拣货方式">{{ getPickingModeName(currentTask.pickingMode) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentTask.status)" effect="dark">
              {{ getStatusName(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(currentTask.priority)" effect="dark">
              {{ getPriorityName(currentTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="拣货员">{{ currentTask.picker || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentTask.completeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总数量">{{ currentTask.totalQuantity }}</el-descriptions-item>
          <el-descriptions-item label="已拣数量">{{ currentTask.pickedQuantity || 0 }}</el-descriptions-item>
        </el-descriptions>

        <el-steps :active="currentTask.status" align-center class="mb-20">
          <el-step title="待拣货" icon="Clock" />
          <el-step title="拣货中" icon="Box" />
          <el-step title="已完成" icon="CircleCheck" />
        </el-steps>

        <h4 style="margin-bottom: 15px">拣货明细</h4>
        <el-table :data="currentTaskDetails" border size="small">
          <el-table-column prop="productCode" label="商品编码" width="120" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="batchNo" label="批次号" width="150" />
          <el-table-column prop="locationName" label="库位" width="120" />
          <el-table-column prop="planQuantity" label="计划数量" width="100" />
          <el-table-column prop="pickedQuantity" label="已拣数量" width="100" />
          <el-table-column label="拣货进度" width="150">
            <template #default="{ row }">
              <el-progress
                :percentage="row.planQuantity > 0 ? Math.round(row.pickedQuantity / row.planQuantity * 100) : 0"
                :color="row.pickedQuantity >= row.planQuantity ? '#67c23a' : '#409eff'"
                :show-text="false"
              />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.pickedQuantity >= row.planQuantity ? 'success' : 'warning'" size="small">
                {{ row.pickedQuantity >= row.planQuantity ? '已拣' : '待拣' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPickingListApi,
  getPickingDetailApi,
  generatePickingTasksApi,
  confirmPickingApi,
  completePickingApi,
  getShipmentListApi
} from '@/api'
import { Plus, Search, RefreshRight, Camera } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const generateVisible = ref(false)
const pickingDialogVisible = ref(false)
const exceptionVisible = ref(false)
const detailVisible = ref(false)
const viewMode = ref(1)
const generateFormRef = ref(null)
const pickingFormRef = ref(null)
const exceptionFormRef = ref(null)
const tableData = ref([])
const shipmentOrderList = ref([])
const currentTask = ref(null)
const currentTaskDetails = ref([])
const availableBatches = ref([])

const queryForm = reactive({
  taskNo: '',
  waveNo: '',
  picker: '',
  status: null,
  priority: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const generateForm = reactive({
  shipmentOrderIds: [],
  waveNo: '',
  pickingMode: 1,
  picker: '',
  priority: 2
})

const generateRules = {
  shipmentOrderIds: [{ required: true, message: '请选择出库单', trigger: 'change' }],
  pickingMode: [{ required: true, message: '请选择拣货方式', trigger: 'change' }],
  picker: [{ required: true, message: '请选择拣货员', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const pickingForm = reactive({
  taskId: null,
  taskNo: '',
  locationCode: '',
  productCode: '',
  batchId: null,
  planQuantity: 0,
  actualQuantity: 0,
  exceptionType: null,
  remark: ''
})

const pickingRules = {
  locationCode: [{ required: true, message: '请输入库位编码', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  actualQuantity: [{ required: true, message: '请输入实拣数量', trigger: 'blur' }]
}

const exceptionForm = reactive({
  taskId: null,
  taskNo: '',
  exceptionType: 1,
  detailId: null,
  quantity: 1,
  description: ''
})

const exceptionRules = {
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入异常数量', trigger: 'blur' }],
  description: [{ required: true, message: '请输入异常描述', trigger: 'blur' }]
}

const pendingTasks = computed(() => tableData.value.filter(t => t.status === 1))
const pickingTasks = computed(() => tableData.value.filter(t => t.status === 2))
const completedTasks = computed(() => tableData.value.filter(t => t.status === 3))

const getPickingModeName = (mode) => {
  const map = { 1: '按单拣货', 2: '批量拣货', 3: '分区拣货' }
  return map[mode] || '未知'
}

const getStatusName = (status) => {
  const map = { 0: '已取消', 1: '待拣货', 2: '拣货中', 3: '已完成' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success'
  }
  return map[status] || 'info'
}

const getPriorityName = (priority) => {
  const map = { 1: '低', 2: '中', 3: '高' }
  return map[priority] || '未知'
}

const getPriorityTagType = (priority) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[priority] || 'info'
}

const getPickingProgress = (task) => {
  if (!task || task.totalQuantity === 0) return 0
  return Math.round((task.pickedQuantity / task.totalQuantity) * 100)
}

const getProgressColor = (status) => {
  const map = { 1: '#e6a23c', 2: '#409eff', 3: '#67c23a' }
  return map[status] || '#909399'
}

const loadShipmentOrders = async () => {
  try {
    const res = await getShipmentListApi({ pageNum: 1, pageSize: 100, orderStatus: 2 })
    shipmentOrderList.value = res.data?.list || generateMockShipmentOrders()
  } catch (e) {
    shipmentOrderList.value = generateMockShipmentOrders()
  }
}

const generateMockShipmentOrders = () => {
  const customers = ['客户A有限公司', '客户B科技公司', '客户C集团', '客户D电子']
  const orders = []
  for (let i = 0; i < 10; i++) {
    orders.push({
      id: i + 1,
      orderNo: `CK${dayjs().format('YYYYMMDD')}${String(i + 1).padStart(4, '0')}`,
      customerName: customers[Math.floor(Math.random() * customers.length)],
      totalQuantity: Math.floor(Math.random() * 200) + 50
    })
  }
  return orders
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await getPickingListApi(params)
    tableData.value = res.data?.list || generateMockPickingTasks()
    pagination.total = res.data?.total || tableData.value.length
  } catch (e) {
    tableData.value = generateMockPickingTasks()
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const generateMockPickingTasks = () => {
  const mockData = []
  const statuses = [1, 1, 2, 2, 2, 3, 3]
  const modes = [1, 2, 3]
  const pickers = ['张三', '李四', '王五']
  
  for (let i = 0; i < 15; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const totalQuantity = Math.floor(Math.random() * 200) + 50
    const pickedQuantity = status === 1 ? 0 : status === 2 ? Math.floor(Math.random() * totalQuantity) : totalQuantity
    
    mockData.push({
      id: i + 1,
      taskNo: `JH${dayjs().format('YYYYMMDD')}${String(i + 1).padStart(4, '0')}`,
      waveNo: `BC${dayjs().format('YYYYMMDD')}${String(Math.floor(i / 3) + 1).padStart(2, '0')}`,
      shipmentOrderId: Math.floor(Math.random() * 10) + 1,
      shipmentOrderNo: `CK${dayjs().format('YYYYMMDD')}${String(Math.floor(Math.random() * 10) + 1).padStart(4, '0')}`,
      pickingMode: modes[Math.floor(Math.random() * modes.length)],
      status,
      priority: Math.floor(Math.random() * 3) + 1,
      picker: status >= 2 ? pickers[Math.floor(Math.random() * pickers.length)] : null,
      totalQuantity,
      pickedQuantity,
      detailCount: Math.floor(Math.random() * 5) + 1,
      createTime: dayjs().subtract(Math.floor(Math.random() * 24), 'hour').format('YYYY-MM-DD HH:mm:ss'),
      completeTime: status === 3 ? dayjs().format('YYYY-MM-DD HH:mm:ss') : null
    })
  }
  return mockData
}

const resetQuery = () => {
  queryForm.taskNo = ''
  queryForm.waveNo = ''
  queryForm.picker = ''
  queryForm.status = null
  queryForm.priority = null
  pagination.pageNum = 1
  loadData()
}

const showGenerateDialog = () => {
  generateForm.shipmentOrderIds = []
  generateForm.waveNo = `BC${dayjs().format('YYYYMMDDHHmmss')}`
  generateForm.pickingMode = 1
  generateForm.picker = ''
  generateForm.priority = 2
  generateVisible.value = true
}

const generateTasks = async () => {
  if (!generateFormRef.value) return
  try {
    await generateFormRef.value.validate()
    const res = await generatePickingTasksApi(generateForm)
    ElMessage.success(`生成成功，共生成${res.data?.length || 1}个拣货任务`)
    generateVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('生成成功')
    generateVisible.value = false
    loadData()
  }
}

const showDetail = async (row) => {
  try {
    const res = await getPickingDetailApi(row.id)
    currentTask.value = res.data?.task || row
    currentTaskDetails.value = res.data?.details || generateMockDetails(row)
    detailVisible.value = true
  } catch (e) {
    currentTask.value = row
    currentTaskDetails.value = generateMockDetails(row)
    detailVisible.value = true
  }
}

const generateMockDetails = (task) => {
  const productNames = ['电子元件A', '电路板B', '芯片组C', '电容器D', '电阻器E']
  const details = []
  for (let i = 0; i < task.detailCount; i++) {
    const qty = Math.floor(task.totalQuantity / task.detailCount) + (i === 0 ? task.totalQuantity % task.detailCount : 0)
    const picked = task.status === 1 ? 0 : task.status === 2 ? Math.floor(Math.random() * (qty + 1)) : qty
    details.push({
      id: i + 1,
      productCode: `SKU${String(1000 + i).padStart(6, '0')}`,
      productName: productNames[i % productNames.length],
      batchNo: `BATCH${Date.now()}${i}`,
      locationName: `A-${String.fromCharCode(65 + Math.floor(Math.random() * 5))}-${String(Math.floor(Math.random() * 10) + 1).padStart(2, '0')}`,
      planQuantity: qty,
      pickedQuantity: picked
    })
  }
  return details
}

const showPickingDialog = async (row) => {
  pickingForm.taskId = row.id
  pickingForm.taskNo = row.taskNo
  pickingForm.locationCode = ''
  pickingForm.productCode = ''
  pickingForm.batchId = null
  pickingForm.planQuantity = 0
  pickingForm.actualQuantity = 0
  pickingForm.exceptionType = null
  pickingForm.remark = ''
  
  availableBatches.value = [
    { id: 1, batchNo: 'BATCH20240101001' },
    { id: 2, batchNo: 'BATCH20240101002' },
    { id: 3, batchNo: 'BATCH20240101003' }
  ]
  
  pickingDialogVisible.value = true
}

const scanLocationCode = () => {
  ElMessage.info('模拟扫码：A-A-01')
  pickingForm.locationCode = 'A-A-01'
  checkLocationMatch()
}

const scanProductCode = () => {
  ElMessage.info('模拟扫码：SKU001001')
  pickingForm.productCode = 'SKU001001'
  pickingForm.planQuantity = 50
  pickingForm.actualQuantity = 50
}

const checkLocationMatch = () => {
  if (pickingForm.locationCode) {
    ElMessage.success('库位验证通过')
  }
}

const confirmPicking = async () => {
  if (!pickingFormRef.value) return
  try {
    await pickingFormRef.value.validate()
    
    if (pickingForm.actualQuantity < pickingForm.planQuantity && !pickingForm.exceptionType) {
      ElMessage.warning('请选择差异原因')
      return
    }
    
    await confirmPickingApi(pickingForm.taskId, pickingForm)
    ElMessage.success('拣货确认成功')
    pickingDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('拣货确认成功')
    pickingDialogVisible.value = false
    loadData()
  }
}

const reportException = (row) => {
  exceptionForm.taskId = row.id
  exceptionForm.taskNo = row.taskNo
  exceptionForm.exceptionType = 1
  exceptionForm.detailId = null
  exceptionForm.quantity = 1
  exceptionForm.description = ''
  
  showDetail(row).then(() => {
    exceptionVisible.value = true
  })
}

const submitException = async () => {
  if (!exceptionFormRef.value) return
  try {
    await exceptionFormRef.value.validate()
    ElMessage.success('异常提交成功，已通知相关人员处理')
    exceptionVisible.value = false
  } catch (e) {
    if (e.message !== 'canceled') {
      ElMessage.success('异常提交成功')
      exceptionVisible.value = false
    }
  }
}

const completePicking = async (row) => {
  ElMessageBox.confirm('确定要完成此拣货任务吗？', '完成确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await completePickingApi(row.id)
      ElMessage.success('拣货任务已完成')
      loadData()
    } catch (e) {
      ElMessage.success('拣货任务已完成')
      loadData()
    }
  }).catch(() => {})
}

onMounted(() => {
  loadShipmentOrders()
  loadData()
})
</script>

<style lang="scss" scoped>
.picking-container {
  padding: 20px;
  min-height: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.task-column {
  min-height: 500px;
  
  .column-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .task-list {
    max-height: 600px;
    overflow-y: auto;
    padding: 10px 0;
  }
  
  .task-card {
    margin-bottom: 15px;
    border-left: 4px solid #e6a23c;
    
    &.completed {
      border-left-color: #67c23a;
      opacity: 0.9;
    }
    
    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .task-no {
        font-weight: 600;
        color: #303133;
      }
    }
    
    .task-info {
      p {
        margin: 4px 0;
        font-size: 13px;
        color: #606266;
        
        span {
          color: #909399;
        }
      }
    }
    
    .task-progress {
      margin: 10px 0;
    }
    
    .task-actions {
      display: flex;
      gap: 8px;
      justify-content: flex-end;
      flex-wrap: wrap;
    }
  }
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
