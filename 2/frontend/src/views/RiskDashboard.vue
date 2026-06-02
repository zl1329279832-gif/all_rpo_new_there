<template>
  <div class="risk-dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon high-risk-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highRiskCount }}</div>
              <div class="stat-label">高风险设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon medium-risk-icon">
              <el-icon><InfoFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.mediumRiskCount }}</div>
              <div class="stat-label">中风险设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon low-risk-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowRiskCount }}</div>
              <div class="stat-label">低风险设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>风险等级分布</template>
          <div ref="riskChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>设备状态统计</template>
          <div ref="statusChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="high-risk-title">
            <el-icon class="warning-icon"><Warning /></el-icon>
            高风险设备列表
          </span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </div>
      </template>

      <el-table :data="highRiskDevices" v-loading="loading" border stripe>
        <el-table-column prop="name" label="设备名称" min-width="120" />
        <el-table-column prop="code" label="设备编号" min-width="120" />
        <el-table-column prop="model" label="型号" min-width="100" />
        <el-table-column prop="departmentName" label="所属科室" min-width="100" />
        <el-table-column prop="riskLevel" label="风险等级" min-width="100">
          <template #default="{ row }">
            <el-tag type="danger" effect="dark">{{ getRiskText(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskReason" label="风险原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="lastInspectionDate" label="上次巡检日期" min-width="130" />
        <el-table-column prop="nextInspectionDate" label="下次巡检日期" min-width="130" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="warning" size="small" @click="handleScheduleInspection(row)">安排巡检</el-button>
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

    <el-dialog v-model="inspectionDialogVisible" title="安排巡检" width="500px" :close-on-click-modal="false">
      <el-form :model="inspectionForm" :rules="inspectionFormRules" ref="inspectionFormRef" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="inspectionForm.deviceName" disabled />
        </el-form-item>
        <el-form-item label="巡检类型" prop="inspectionType">
          <el-select v-model="inspectionForm.inspectionType" placeholder="请选择巡检类型" style="width: 100%">
            <el-option label="紧急巡检" value="URGENT" />
            <el-option label="常规巡检" value="NORMAL" />
            <el-option label="专项巡检" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期" prop="plannedDate">
          <el-date-picker
            v-model="inspectionForm.plannedDate"
            type="date"
            placeholder="选择计划日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="巡检人员" prop="inspector">
          <el-select v-model="inspectionForm.inspector" placeholder="请选择巡检人员" style="width: 100%">
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
            <el-option label="王五" value="王五" />
            <el-option label="赵六" value="赵六" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="inspectionForm.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitInspection" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="设备风险详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ currentDevice.code }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentDevice.model }}</el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ currentDevice.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">{{ currentDevice.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag type="danger" effect="dark">{{ getRiskText(currentDevice.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="采购日期">{{ currentDevice.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="使用年限">{{ currentDevice.serviceYears }}年</el-descriptions-item>
        <el-descriptions-item label="风险原因" :span="2">{{ currentDevice.riskReason }}</el-descriptions-item>
        <el-descriptions-item label="风险评估" :span="2">{{ currentDevice.riskAssessment }}</el-descriptions-item>
        <el-descriptions-item label="处理建议" :span="2">{{ currentDevice.suggestion }}</el-descriptions-item>
        <el-descriptions-item label="上次巡检日期">{{ currentDevice.lastInspectionDate }}</el-descriptions-item>
        <el-descriptions-item label="下次巡检日期">{{ currentDevice.nextInspectionDate }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, InfoFilled, CircleCheck, Monitor, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const submitting = ref(false)
const riskChartRef = ref(null)
const statusChartRef = ref(null)
const inspectionDialogVisible = ref(false)
const detailVisible = ref(false)
const inspectionFormRef = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const highRiskDevices = ref([])

const stats = computed(() => {
  const high = highRiskDevices.value.length
  const medium = 15
  const low = 35
  const total = high + medium + low
  return {
    highRiskCount: high,
    mediumRiskCount: medium,
    lowRiskCount: low,
    totalCount: total
  }
})

const currentDevice = reactive({
  name: '',
  code: '',
  model: '',
  manufacturer: '',
  departmentName: '',
  riskLevel: '',
  purchaseDate: '',
  serviceYears: '',
  riskReason: '',
  riskAssessment: '',
  suggestion: '',
  lastInspectionDate: '',
  nextInspectionDate: ''
})

const inspectionForm = reactive({
  deviceId: null,
  deviceName: '',
  inspectionType: '',
  plannedDate: '',
  inspector: '',
  remark: ''
})

const inspectionFormRules = {
  inspectionType: [{ required: true, message: '请选择巡检类型', trigger: 'change' }],
  plannedDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }],
  inspector: [{ required: true, message: '请选择巡检人员', trigger: 'change' }]
}

const getRiskText = (level) => {
  const map = {
    HIGH: '高风险',
    MEDIUM: '中风险',
    LOW: '低风险'
  }
  return map[level] || level
}

const initRiskChart = () => {
  if (!riskChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(riskChartRef.value)
    chart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: '5%',
        left: 'center'
      },
      series: [
        {
          name: '风险等级',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: stats.value.highRiskCount, name: '高风险', itemStyle: { color: '#F56C6C' } },
            { value: stats.value.mediumRiskCount, name: '中风险', itemStyle: { color: '#E6A23C' } },
            { value: stats.value.lowRiskCount, name: '低风险', itemStyle: { color: '#67C23A' } }
          ]
        }
      ]
    })
  })
}

const initStatusChart = () => {
  if (!statusChartRef.value) return
  nextTick(() => {
    const chart = echarts.init(statusChartRef.value)
    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['高风险', '中风险', '低风险'],
        bottom: '0%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['内科', '外科', '急诊科', '放射科', '检验科']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '高风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#F56C6C' },
          data: [3, 2, 4, 5, 2]
        },
        {
          name: '中风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#E6A23C' },
          data: [5, 4, 3, 2, 1]
        },
        {
          name: '低风险',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#67C23A' },
          data: [10, 8, 12, 5, 0]
        }
      ]
    })
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    highRiskDevices.value = [
      { id: 1, name: 'CT扫描仪', code: 'CT-001', model: 'SOMATOM Force', departmentName: '放射科', riskLevel: 'HIGH', riskReason: '设备使用年限超过8年，多次出现图像伪影', lastInspectionDate: '2026-05-15', nextInspectionDate: '2026-06-15', manufacturer: '西门子', purchaseDate: '2018-03-10', serviceYears: 8, riskAssessment: '设备老化严重，存在故障停机风险，可能影响临床诊断', suggestion: '建议立即安排全面检修，评估更换必要性' },
      { id: 2, name: '核磁共振仪', code: 'MRI-001', model: 'Achieva 3.0T', departmentName: '放射科', riskLevel: 'HIGH', riskReason: '磁场均匀性不达标，影响诊断准确性', lastInspectionDate: '2026-05-20', nextInspectionDate: '2026-06-20', manufacturer: '飞利浦', purchaseDate: '2017-08-20', serviceYears: 9, riskAssessment: '磁场偏差可能导致误诊，属于严重质量问题', suggestion: '需立即联系厂家进行专业校准' },
      { id: 3, name: 'X光机', code: 'XR-003', model: 'DRX-Evolution', departmentName: '急诊科', riskLevel: 'HIGH', riskReason: '曝光剂量不稳定，存在安全隐患', lastInspectionDate: '2026-05-25', nextInspectionDate: '2026-06-25', manufacturer: '柯达', purchaseDate: '2019-01-05', serviceYears: 7, riskAssessment: '剂量超标可能对患者和操作人员造成伤害', suggestion: '暂停使用，立即检修高压发生器' },
      { id: 4, name: '麻醉机', code: 'AM-002', model: 'Aespire 7900', departmentName: '外科', riskLevel: 'HIGH', riskReason: '呼吸回路漏气检测失败', lastInspectionDate: '2026-05-28', nextInspectionDate: '2026-06-28', manufacturer: 'GE医疗', purchaseDate: '2018-11-15', serviceYears: 8, riskAssessment: '手术中漏气可能导致患者缺氧，属于高危问题', suggestion: '立即更换呼吸回路组件，进行压力测试' },
      { id: 5, name: '呼吸机', code: 'VM-005', model: 'Servo-i', departmentName: 'ICU', riskLevel: 'HIGH', riskReason: '潮气量校准偏差超过10%', lastInspectionDate: '2026-05-30', nextInspectionDate: '2026-06-30', manufacturer: '迈瑞', purchaseDate: '2020-06-10', serviceYears: 6, riskAssessment: '潮气量不准确可能导致通气不足或过度通气', suggestion: '进行流量传感器校准和系统测试' }
    ]
    pagination.total = 5
    initRiskChart()
    initStatusChart()
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  ElMessage.success('报告导出成功')
}

const handleView = (row) => {
  Object.keys(currentDevice).forEach(key => {
    currentDevice[key] = row[key] || ''
  })
  detailVisible.value = true
}

const handleScheduleInspection = (row) => {
  inspectionForm.deviceId = row.id
  inspectionForm.deviceName = row.name
  inspectionForm.inspectionType = 'URGENT'
  inspectionForm.plannedDate = ''
  inspectionForm.inspector = ''
  inspectionForm.remark = ''
  inspectionDialogVisible.value = true
}

const handleSubmitInspection = async () => {
  if (!inspectionFormRef.value) return
  await inspectionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        ElMessage.success('巡检安排成功')
        inspectionDialogVisible.value = false
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
.risk-dashboard-container {
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

.high-risk-icon {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
}

.medium-risk-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.low-risk-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.total-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 350px;
}

.chart {
  width: 100%;
  height: 280px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.high-risk-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #F56C6C;
}

.warning-icon {
  font-size: 18px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
