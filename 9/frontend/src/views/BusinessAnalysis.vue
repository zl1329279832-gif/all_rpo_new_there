<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">经营分析</h2>
      <div>
        <el-radio-group v-model="dateRange" size="default" style="margin-right: 16px">
          <el-radio-button label="7">近7天</el-radio-button>
          <el-radio-button label="30">近30天</el-radio-button>
          <el-radio-button label="90">近90天</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="6">
        <div class="stats-card">
          <div class="stats-label">销售总额</div>
          <div class="stats-value">{{ formatMoney(analysisData.salesAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card card-orange">
          <div class="stats-label">销售总量</div>
          <div class="stats-value">{{ formatNumber(analysisData.salesQty) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card card-green">
          <div class="stats-label">报损金额</div>
          <div class="stats-value">{{ formatMoney(analysisData.damageAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card card-purple">
          <div class="stats-label">库存总价值</div>
          <div class="stats-value">{{ formatMoney(analysisData.stockAmount) }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">销售趋势</div>
          <div ref="salesTrendChart" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">报损趋势</div>
          <div ref="damageTrendChart" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">分类销售占比</div>
          <div ref="categoryPieChart" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">门店销售对比</div>
          <div ref="storeBarChart" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">产品销量排行 TOP 10</div>
          <div ref="productRankChart" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">临期预警统计</div>
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="4">
              <div class="mini-stat">
                <div class="mini-stat-value" style="color: #f56c6c">
                  {{ warningStats.expiredBatches || 0 }}
                </div>
                <div class="mini-stat-label">已过期批次</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="mini-stat">
                <div class="mini-stat-value" style="color: #e6a23c">
                  {{ warningStats.warningBatches || 0 }}
                </div>
                <div class="mini-stat-label">临期批次</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="mini-stat">
                <div class="mini-stat-value" style="color: #f56c6c">
                  {{ formatNumber(warningStats.expiredQty) }}
                </div>
                <div class="mini-stat-label">已过期数量</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="mini-stat">
                <div class="mini-stat-value" style="color: #e6a23c">
                  {{ formatNumber(warningStats.warningQty) }}
                </div>
                <div class="mini-stat-label">临期数量</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="mini-stat">
                <div class="mini-stat-value" style="color: #909399">
                  {{ warningStats.warningProducts || 0 }}
                </div>
                <div class="mini-stat-label">涉及商品种类</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="mini-stat">
                <div class="mini-stat-value" style="color: #67c23a">
                  {{ warningStats.totalBatches || 0 }}
                </div>
                <div class="mini-stat-label">正常批次</div>
              </div>
            </el-col>
          </el-row>
          <div ref="warningBarChart" class="chart-container" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { analysisApi, batchApi } from '@/api'
import { formatMoney, formatNumber } from '@/utils/format'

const dateRange = ref('7')
const analysisData = ref({})
const warningStats = ref({})
const loading = ref(false)

const salesTrendChart = ref(null)
const damageTrendChart = ref(null)
const categoryPieChart = ref(null)
const storeBarChart = ref(null)
const productRankChart = ref(null)
const warningBarChart = ref(null)

let salesTrendInstance = null
let damageTrendInstance = null
let categoryPieInstance = null
let storeBarInstance = null
let productRankInstance = null
let warningBarInstance = null

const loadAnalysisData = async () => {
  loading.value = true
  try {
    const [analysisRes, warningRes] = await Promise.all([
      analysisApi.getData({ days: dateRange.value }),
      batchApi.warningStats()
    ])
    analysisData.value = analysisRes.data || {}
    warningStats.value = warningRes.data || {}
    renderCharts()
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await analysisApi.refresh()
  ElMessage.success('数据已刷新')
  loadAnalysisData()
}

const renderCharts = () => {
  renderSalesTrend()
  renderDamageTrend()
  renderCategoryPie()
  renderStoreBar()
  renderProductRank()
  renderWarningBar()
}

const renderSalesTrend = () => {
  if (!salesTrendChart.value) return
  if (!salesTrendInstance) {
    salesTrendInstance = echarts.init(salesTrendChart.value)
  }
  const data = analysisData.value.salesTrend || []
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>销售额: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map((d) => d.date)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(80, 141, 255, 0.5)' },
            { offset: 1, color: 'rgba(80, 141, 255, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#5470c6',
          width: 2
        },
        itemStyle: {
          color: '#5470c6'
        },
        data: data.map((d) => d.amount)
      }
    ]
  }
  salesTrendInstance.setOption(option)
}

const renderDamageTrend = () => {
  if (!damageTrendChart.value) return
  if (!damageTrendInstance) {
    damageTrendInstance = echarts.init(damageTrendChart.value)
  }
  const data = analysisData.value.damageTrend || []
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>报损金额: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map((d) => d.date)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '报损金额',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.5)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#f56c6c',
          width: 2
        },
        itemStyle: {
          color: '#f56c6c'
        },
        data: data.map((d) => d.amount)
      }
    ]
  }
  damageTrendInstance.setOption(option)
}

const renderCategoryPie = () => {
  if (!categoryPieChart.value) return
  if (!categoryPieInstance) {
    categoryPieInstance = echarts.init(categoryPieChart.value)
  }
  const data = analysisData.value.categorySales || []
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data.map((d) => ({
          value: d.amount,
          name: d.categoryName
        }))
      }
    ]
  }
  categoryPieInstance.setOption(option)
}

const renderStoreBar = () => {
  if (!storeBarChart.value) return
  if (!storeBarInstance) {
    storeBarInstance = echarts.init(storeBarChart.value)
  }
  const data = analysisData.value.storeSales || []
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>销售额: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.storeName),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: data.map((d) => d.amount)
      }
    ]
  }
  storeBarInstance.setOption(option)
}

const renderProductRank = () => {
  if (!productRankChart.value) return
  if (!productRankInstance) {
    productRankInstance = echarts.init(productRankChart.value)
  }
  const data = (analysisData.value.productRank || []).slice(0, 10).reverse()
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>销量: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.map((d) => d.productName)
    },
    series: [
      {
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#43e97b' },
            { offset: 1, color: '#38f9d7' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        data: data.map((d) => d.qty)
      }
    ]
  }
  productRankInstance.setOption(option)
}

const renderWarningBar = () => {
  if (!warningBarChart.value) return
  if (!warningBarInstance) {
    warningBarInstance = echarts.init(warningBarChart.value)
  }
  const data = analysisData.value.warningByProduct || []
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['临期数量', '过期数量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.productName),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '临期数量',
        type: 'bar',
        stack: 'total',
        itemStyle: {
          color: '#e6a23c'
        },
        data: data.map((d) => d.warningQty)
      },
      {
        name: '过期数量',
        type: 'bar',
        stack: 'total',
        itemStyle: {
          color: '#f56c6c'
        },
        data: data.map((d) => d.expiredQty)
      }
    ]
  }
  warningBarInstance.setOption(option)
}

const handleResize = () => {
  salesTrendInstance?.resize()
  damageTrendInstance?.resize()
  categoryPieInstance?.resize()
  storeBarInstance?.resize()
  productRankInstance?.resize()
  warningBarInstance?.resize()
}

watch(dateRange, () => {
  loadAnalysisData()
})

onMounted(() => {
  loadAnalysisData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  salesTrendInstance?.dispose()
  damageTrendInstance?.dispose()
  categoryPieInstance?.dispose()
  storeBarInstance?.dispose()
  productRankInstance?.dispose()
  warningBarInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;

  .chart-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }

  .chart-container {
    width: 100%;
    height: 350px;
  }
}

.mini-stat {
  text-align: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;

  .mini-stat-value {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .mini-stat-label {
    font-size: 13px;
    color: #909399;
  }
}
</style>
