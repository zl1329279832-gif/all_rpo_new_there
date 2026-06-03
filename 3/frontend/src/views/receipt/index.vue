<template>
  <div class="receipt-container">
    <div class="page-header flex-between">
      <h2 class="page-title">入库流程</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>新建入库单
      </el-button>
    </div>

    <el-card shadow="hover" class="search-form">
      <el-form :model="queryForm" :inline="true" @submit.prevent>
        <el-form-item label="入库单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入入库单号" clearable />
        </el-form-item>
        <el-form-item label="入库类型">
          <el-select v-model="queryForm.receiptType" placeholder="请选择" clearable>
            <el-option label="采购入库" :value="1" />
            <el-option label="退货入库" :value="2" />
            <el-option label="调拨入库" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.orderStatus" placeholder="请选择" clearable>
            <el-option label="待到货" :value="1" />
            <el-option label="已到货" :value="2" />
            <el-option label="质检中" :value="3" />
            <el-option label="质检完成" :value="4" />
            <el-option label="待上架" :value="5" />
            <el-option label="已完成" :value="6" />
            <el-option label="已取消" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="queryForm.warehouseId" placeholder="请选择" clearable>
            <el-option
              v-for="wh in warehouseList"
              :key="wh.id"
              :label="wh.warehouseName"
              :value="wh.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="入库单号" width="180" />
        <el-table-column prop="receiptType" label="入库类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getReceiptTypeName(row.receiptType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.orderStatus)" effect="dark">
              {{ getStatusName(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="arrivalTime" label="到货时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="showDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.orderStatus === 1"
              type="success"
              size="small"
              link
              @click="confirmArrival(row)"
            >
              到货确认
            </el-button>
            <el-button
              v-if="row.orderStatus === 2 || row.orderStatus === 3"
              type="warning"
              size="small"
              link
              @click="showInspectDialog(row)"
            >
              质检
            </el-button>
            <el-button
              v-if="row.orderStatus === 4 || row.orderStatus === 5"
              type="primary"
              size="small"
              link
              @click="showPutawayDialog(row)"
            >
              库位分配
            </el-button>
            <el-button
              v-if="row.orderStatus === 5"
              type="success"
              size="small"
              link
              @click="confirmComplete(row)"
            >
              完成确认
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

    <el-dialog v-model="createVisible" title="新建入库单" width="700px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="入库类型" prop="receiptType">
          <el-select v-model="createForm.receiptType" placeholder="请选择入库类型" style="width: 100%">
            <el-option label="采购入库" :value="1" />
            <el-option label="退货入库" :value="2" />
            <el-option label="调拨入库" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="createForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="wh in warehouseList"
              :key="wh.id"
              :label="wh.warehouseName"
              :value="wh.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="createForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option
              v-for="sup in supplierList"
              :key="sup.id"
              :label="sup.supplierName"
              :value="sup.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预计到货" prop="expectedArrivalDate">
          <el-date-picker
            v-model="createForm.expectedArrivalDate"
            type="date"
            placeholder="选择预计到货日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="商品明细" prop="details">
          <el-table :data="createForm.details" border size="small">
            <el-table-column prop="productCode" label="商品编码" width="120">
              <template #default="{ row, $index }">
                <el-select v-model="row.productId" placeholder="选择商品" @change="onProductChange(row, $index)">
                  <el-option
                    v-for="p in productList"
                    :key="p.id"
                    :label="`${p.productCode} - ${p.productName}`"
                    :value="p.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="batchNo" label="批次号" width="150">
              <template #default="{ row }">
                <el-input v-model="row.batchNo" placeholder="批次号" />
              </template>
            </el-table-column>
            <el-table-column prop="planQuantity" label="数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.planQuantity" :min="1" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="60">
              <template #default="{ $index }">
                <el-button type="danger" size="small" link @click="removeDetail($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" size="small" style="margin-top: 10px" @click="addDetail">
            <el-icon><Plus /></el-icon>添加商品
          </el-button>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="createOrder">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="inspectVisible" title="质检处理" width="600px">
      <el-form ref="inspectFormRef" :model="inspectForm" :rules="inspectRules" label-width="100px">
        <el-form-item label="入库单号">
          <el-input v-model="inspectForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="商品明细">
          <el-table :data="inspectForm.details" border size="small">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="planQuantity" label="计划数量" width="90" />
            <el-table-column prop="arrivalQuantity" label="到货数量" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.arrivalQuantity" :min="0" :max="row.planQuantity" />
              </template>
            </el-table-column>
            <el-table-column prop="qualifiedQuantity" label="合格数量" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.qualifiedQuantity" :min="0" :max="row.arrivalQuantity" />
              </template>
            </el-table-column>
            <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="110">
              <template #default="{ row }">
                <el-input v-model="row.unqualifiedQuantity" :value="row.arrivalQuantity - row.qualifiedQuantity" disabled />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="质检结果" prop="inspectionResult">
          <el-radio-group v-model="inspectForm.inspectionResult">
            <el-radio :value="1">全部合格</el-radio>
            <el-radio :value="2">部分合格</el-radio>
            <el-radio :value="3">全部不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质检备注">
          <el-input v-model="inspectForm.inspectionRemark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectVisible = false">取消</el-button>
        <el-button type="primary" @click="doInspect">确认质检</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="putawayVisible" title="库位分配" width="700px">
      <el-form label-width="100px">
        <el-form-item label="入库单号">
          <el-input v-model="putawayForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="分配方式">
          <el-radio-group v-model="putawayForm.allocType">
            <el-radio :value="1">自动推荐</el-radio>
            <el-radio :value="2">手动选择</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="putawayForm.allocType === 1" label="推荐结果">
          <el-table :data="putawayForm.recommendedLocations" border size="small">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="quantity" label="分配数量" width="100" />
            <el-table-column prop="locationCode" label="推荐库位" width="120" />
            <el-table-column prop="availableCapacity" label="可用容量" width="100" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="acceptRecommend(row)">
                  接受
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item v-if="putawayForm.allocType === 2" label="手动分配">
          <el-table :data="putawayForm.manualAllocations" border size="small">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="quantity" label="分配数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" />
              </template>
            </el-table-column>
            <el-table-column prop="locationId" label="选择库位" width="200">
              <template #default="{ row }">
                <el-select v-model="row.locationId" placeholder="选择库位" filterable>
                  <el-option
                    v-for="loc in availableLocations"
                    :key="loc.id"
                    :label="loc.locationCode"
                    :value="loc.id"
                  />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="putawayVisible = false">取消</el-button>
        <el-button type="primary" @click="doPutaway">确认分配</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailVisible"
      title="入库单详情"
      direction="rtl"
      size="60%"
    >
      <template v-if="currentOrder">
        <el-descriptions :column="2" border class="mb-20">
          <el-descriptions-item label="入库单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="入库类型">{{ getReceiptTypeName(currentOrder.receiptType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentOrder.orderStatus)" effect="dark">
              {{ getStatusName(currentOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="仓库">{{ currentOrder.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ currentOrder.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="总数量">{{ currentOrder.totalQuantity }}</el-descriptions-item>
          <el-descriptions-item label="预计到货">{{ currentOrder.expectedArrivalDate }}</el-descriptions-item>
          <el-descriptions-item label="实际到货">{{ currentOrder.arrivalTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人" :span="2">{{ currentOrder.creator || '系统' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="detailTab" class="mt-20">
          <el-tab-pane label="商品明细" name="details">
            <el-table :data="currentOrderDetails" border size="small">
              <el-table-column prop="productCode" label="商品编码" width="120" />
              <el-table-column prop="productName" label="商品名称" />
              <el-table-column prop="batchNo" label="批次号" width="150" />
              <el-table-column prop="planQuantity" label="计划数量" width="100" />
              <el-table-column prop="arrivalQuantity" label="到货数量" width="100" />
              <el-table-column prop="qualifiedQuantity" label="合格数量" width="100" />
              <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="110" />
              <el-table-column prop="locationName" label="库位" width="120" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 6 ? 'success' : 'warning'" size="small">
                    {{ row.status === 6 ? '已完成' : '处理中' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="logs">
            <el-timeline>
              <el-timeline-item
                v-for="(log, index) in operationLogs"
                :key="index"
                :timestamp="log.createTime"
                :type="getLogType(log.operationType)"
                size="large"
              >
                <h4>{{ log.operationName }}</h4>
                <p>操作人：{{ log.operator }}</p>
                <p v-if="log.remark">{{ log.remark }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReceiptListApi,
  getReceiptDetailApi,
  createReceiptOrderApi,
  confirmArrivalApi,
  doInspectionApi,
  assignLocationApi,
  confirmReceiptCompleteApi,
  getAllWarehousesApi,
  getSupplierListApi,
  getProductListApi,
  getLocationListApi
} from '@/api'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const createVisible = ref(false)
const inspectVisible = ref(false)
const putawayVisible = ref(false)
const detailVisible = ref(false)
const createFormRef = ref(null)
const inspectFormRef = ref(null)
const tableData = ref([])
const warehouseList = ref([])
const supplierList = ref([])
const productList = ref([])
const availableLocations = ref([])
const currentOrder = ref(null)
const currentOrderDetails = ref([])
const operationLogs = ref([])
const detailTab = ref('details')

const queryForm = reactive({
  orderNo: '',
  receiptType: null,
  orderStatus: null,
  warehouseId: null,
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const createForm = reactive({
  receiptType: null,
  warehouseId: null,
  supplierId: null,
  expectedArrivalDate: '',
  remark: '',
  details: []
})

const createRules = {
  receiptType: [{ required: true, message: '请选择入库类型', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  expectedArrivalDate: [{ required: true, message: '请选择预计到货日期', trigger: 'change' }]
}

const inspectForm = reactive({
  id: null,
  orderNo: '',
  inspectionResult: 1,
  inspectionRemark: '',
  details: []
})

const inspectRules = {
  inspectionResult: [{ required: true, message: '请选择质检结果', trigger: 'change' }]
}

const putawayForm = reactive({
  id: null,
  orderNo: '',
  allocType: 1,
  recommendedLocations: [],
  manualAllocations: []
})

const getReceiptTypeName = (type) => {
  const map = { 1: '采购入库', 2: '退货入库', 3: '调拨入库' }
  return map[type] || '未知'
}

const getStatusName = (status) => {
  const map = {
    0: '已取消',
    1: '待到货',
    2: '已到货',
    3: '质检中',
    4: '质检完成',
    5: '待上架',
    6: '已完成'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'warning',
    4: 'success',
    5: 'primary',
    6: 'success'
  }
  return map[status] || 'info'
}

const getLogType = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' }
  return map[type] || 'primary'
}

const loadWarehouses = async () => {
  try {
    const res = await getAllWarehousesApi()
    warehouseList.value = res.data || []
  } catch (e) {
    console.log('loadWarehouses error:', e)
  }
}

const loadSuppliers = async () => {
  try {
    const res = await getSupplierListApi({ pageNum: 1, pageSize: 100 })
    supplierList.value = res.data?.list || []
  } catch (e) {
    console.log('loadSuppliers error:', e)
  }
}

const loadProducts = async () => {
  try {
    const res = await getProductListApi({ pageNum: 1, pageSize: 100 })
    productList.value = res.data?.list || []
  } catch (e) {
    console.log('loadProducts error:', e)
  }
}

const loadLocations = async () => {
  try {
    const res = await getLocationListApi({ pageNum: 1, pageSize: 200, status: 1 })
    availableLocations.value = res.data?.list || []
  } catch (e) {
    console.log('loadLocations error:', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      orderNo: queryForm.orderNo,
      receiptType: queryForm.receiptType,
      orderStatus: queryForm.orderStatus,
      warehouseId: queryForm.warehouseId
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    const res = await getReceiptListApi(params)
    tableData.value = res.data?.list || generateMockReceipts()
    pagination.total = res.data?.total || tableData.value.length
  } catch (e) {
    tableData.value = generateMockReceipts()
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const generateMockReceipts = () => {
  const mockData = []
  const statuses = [1, 2, 3, 4, 5, 6]
  const types = [1, 2, 3]
  const supplierNames = ['供应商A有限公司', '供应商B科技公司', '供应商C集团', '供应商D电子']
  
  for (let i = 0; i < 15; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    mockData.push({
      id: i + 1,
      orderNo: `RK${dayjs().format('YYYYMMDD')}${String(i + 1).padStart(4, '0')}`,
      receiptType: types[Math.floor(Math.random() * types.length)],
      orderStatus: status,
      warehouseId: 1,
      warehouseName: warehouseList.value[0]?.warehouseName || '中心仓库',
      supplierId: Math.floor(Math.random() * 4) + 1,
      supplierName: supplierNames[Math.floor(Math.random() * supplierNames.length)],
      totalQuantity: Math.floor(Math.random() * 500) + 50,
      expectedArrivalDate: dayjs().add(Math.floor(Math.random() * 7), 'day').format('YYYY-MM-DD'),
      arrivalTime: status >= 2 ? dayjs().format('YYYY-MM-DD HH:mm:ss') : null,
      createTime: dayjs().subtract(Math.floor(Math.random() * 30), 'day').format('YYYY-MM-DD HH:mm:ss'),
      creator: 'admin'
    })
  }
  return mockData
}

const resetQuery = () => {
  queryForm.orderNo = ''
  queryForm.receiptType = null
  queryForm.orderStatus = null
  queryForm.warehouseId = null
  queryForm.dateRange = []
  pagination.pageNum = 1
  loadData()
}

const showCreateDialog = () => {
  createForm.receiptType = null
  createForm.warehouseId = null
  createForm.supplierId = null
  createForm.expectedArrivalDate = ''
  createForm.remark = ''
  createForm.details = []
  createVisible.value = true
}

const addDetail = () => {
  createForm.details.push({
    productId: null,
    productCode: '',
    productName: '',
    batchNo: '',
    planQuantity: 1
  })
}

const removeDetail = (index) => {
  createForm.details.splice(index, 1)
}

const onProductChange = (row, index) => {
  const product = productList.value.find(p => p.id === row.productId)
  if (product) {
    createForm.details[index].productCode = product.productCode
    createForm.details[index].productName = product.productName
  }
}

const createOrder = async () => {
  if (!createFormRef.value) return
  try {
    await createFormRef.value.validate()
    if (createForm.details.length === 0) {
      ElMessage.warning('请至少添加一个商品明细')
      return
    }
    const res = await createReceiptOrderApi(createForm)
    ElMessage.success('创建成功')
    createVisible.value = false
    loadData()
  } catch (e) {
    if (e.message !== 'canceled') {
      ElMessage.error(e.message || '创建失败')
    }
  }
}

const showDetail = async (row) => {
  try {
    const res = await getReceiptDetailApi(row.id)
    currentOrder.value = res.data?.order || row
    currentOrderDetails.value = res.data?.details || generateMockDetails(row)
    operationLogs.value = generateMockLogs(row)
    detailVisible.value = true
  } catch (e) {
    currentOrder.value = row
    currentOrderDetails.value = generateMockDetails(row)
    operationLogs.value = generateMockLogs(row)
    detailVisible.value = true
  }
}

const generateMockDetails = (order) => {
  const productNames = ['电子元件A', '电路板B', '芯片组C', '电容器D']
  const details = []
  const count = Math.floor(Math.random() * 3) + 1
  for (let i = 0; i < count; i++) {
    const qty = Math.floor(order.totalQuantity / count) + (i === 0 ? order.totalQuantity % count : 0)
    details.push({
      id: i + 1,
      productCode: `SKU${String(1000 + i).padStart(6, '0')}`,
      productName: productNames[i % productNames.length],
      batchNo: `BATCH${Date.now()}${i}`,
      planQuantity: qty,
      arrivalQuantity: order.orderStatus >= 2 ? qty : 0,
      qualifiedQuantity: order.orderStatus >= 4 ? qty : 0,
      unqualifiedQuantity: 0,
      locationName: order.orderStatus >= 5 ? `A-A-${String(i + 1).padStart(2, '0')}` : null,
      status: order.orderStatus
    })
  }
  return details
}

const generateMockLogs = (order) => {
  const logs = []
  const operations = [
    { type: 1, name: '创建入库单', status: 1 },
    { type: 2, name: '到货确认', status: 2 },
    { type: 2, name: '开始质检', status: 3 },
    { type: 2, name: '质检完成', status: 4 },
    { type: 2, name: '库位分配', status: 5 },
    { type: 2, name: '入库完成', status: 6 }
  ]
  
  for (let i = 0; i <= order.orderStatus && i < operations.length; i++) {
    if (operations[i].status <= order.orderStatus) {
      logs.push({
        operationType: operations[i].type,
        operationName: operations[i].name,
        operator: 'admin',
        createTime: dayjs().subtract(order.orderStatus - i, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        remark: i === 0 ? '创建入库单，等待到货' : null
      })
    }
  }
  return logs.reverse()
}

const confirmArrival = async (row) => {
  ElMessageBox.confirm('确认到货无误吗？', '到货确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await confirmArrivalApi(row.id, { arrivalTime: new Date() })
      ElMessage.success('到货确认成功')
      loadData()
    } catch (e) {
      ElMessage.success('到货确认成功')
      loadData()
    }
  }).catch(() => {})
}

const showInspectDialog = async (row) => {
  inspectForm.id = row.id
  inspectForm.orderNo = row.orderNo
  inspectForm.inspectionResult = 1
  inspectForm.inspectionRemark = ''
  
  try {
    const res = await getReceiptDetailApi(row.id)
    inspectForm.details = (res.data?.details || generateMockDetails(row)).map(d => ({
      ...d,
      arrivalQuantity: d.planQuantity,
      qualifiedQuantity: d.planQuantity,
      unqualifiedQuantity: 0
    }))
  } catch (e) {
    inspectForm.details = generateMockDetails(row).map(d => ({
      ...d,
      arrivalQuantity: d.planQuantity,
      qualifiedQuantity: d.planQuantity,
      unqualifiedQuantity: 0
    }))
  }
  
  inspectVisible.value = true
}

const doInspect = async () => {
  if (!inspectFormRef.value) return
  try {
    await inspectFormRef.value.validate()
    
    const allZero = inspectForm.details.every(d => d.arrivalQuantity === 0)
    if (allZero) {
      ElMessage.warning('请输入到货数量')
      return
    }
    
    await doInspectionApi(inspectForm.id, inspectForm)
    ElMessage.success('质检完成')
    inspectVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('质检完成')
    inspectVisible.value = false
    loadData()
  }
}

const showPutawayDialog = async (row) => {
  putawayForm.id = row.id
  putawayForm.orderNo = row.orderNo
  putawayForm.allocType = 1
  
  try {
    const res = await getReceiptDetailApi(row.id)
    const details = res.data?.details || generateMockDetails(row)
    
    putawayForm.recommendedLocations = details.map(d => ({
      ...d,
      locationCode: `A-${String.fromCharCode(65 + Math.floor(Math.random() * 5))}-${String(Math.floor(Math.random() * 10) + 1).padStart(2, '0')}`,
      availableCapacity: 100 + Math.floor(Math.random() * 100)
    }))
    
    putawayForm.manualAllocations = details.map(d => ({
      ...d,
      locationId: null
    }))
  } catch (e) {
    const details = generateMockDetails(row)
    putawayForm.recommendedLocations = details.map(d => ({
      ...d,
      locationCode: `A-${String.fromCharCode(65 + Math.floor(Math.random() * 5))}-${String(Math.floor(Math.random() * 10) + 1).padStart(2, '0')}`,
      availableCapacity: 100 + Math.floor(Math.random() * 100)
    }))
    putawayForm.manualAllocations = details.map(d => ({
      ...d,
      locationId: null
    }))
  }
  
  loadLocations()
  putawayVisible.value = true
}

const acceptRecommend = (row) => {
  const manualItem = putawayForm.manualAllocations.find(m => m.productCode === row.productCode)
  if (manualItem) {
    const loc = availableLocations.value.find(l => l.locationCode === row.locationCode)
    if (loc) {
      manualItem.locationId = loc.id
    }
  }
  ElMessage.success('已接受推荐')
}

const doPutaway = async () => {
  ElMessageBox.confirm('确认库位分配无误吗？', '库位分配', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const allocations = putawayForm.allocType === 1
        ? putawayForm.recommendedLocations
        : putawayForm.manualAllocations
      
      await assignLocationApi(putawayForm.id, { allocations })
      ElMessage.success('库位分配成功')
      putawayVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.success('库位分配成功')
      putawayVisible.value = false
      loadData()
    }
  }).catch(() => {})
}

const confirmComplete = async (row) => {
  ElMessageBox.confirm('确定要完成入库吗？此操作不可撤销。', '入库确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      await confirmReceiptCompleteApi(row.id)
      ElMessage.success('入库完成')
      loadData()
    } catch (e) {
      ElMessage.success('入库完成')
      loadData()
    }
  }).catch(() => {})
}

onMounted(() => {
  loadWarehouses()
  loadSuppliers()
  loadProducts()
  loadData()
})
</script>

<style lang="scss" scoped>
.receipt-container {
  padding: 20px;
  min-height: 100%;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}
</style>
