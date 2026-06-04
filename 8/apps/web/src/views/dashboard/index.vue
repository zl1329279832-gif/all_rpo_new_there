<template>
  <PageContainer title="指标看板" :show-filter="true" @search="handleSearch" @reset="handleReset">
    <template #filter>
      <el-form :model="filterForm" inline @submit.prevent>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <div class="stats-overview">
        <el-row :gutter="16">
          <el-col :span="6" v-for="stat in statsCards" :key="stat.key">
            <div class="stat-card" :class="stat.color">
              <div class="stat-icon">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
                  <el-icon><CaretTop v-if="stat.trend > 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(stat.trend) }}%
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </template>

    <div class="dashboard-content" v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>数据趋势</span>
                <el-radio-group v-model="trendType" size="small">
                  <el-radio-button value="order">订单</el-radio-button>
                  <el-radio-button value="user">用户</el-radio-button>
                  <el-radio-button value="revenue">营收</el-radio-button>
                  <el-radio-button value="ticket">工单</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>订单分布</span>
            </template>
            <div ref="orderPieChartRef" class="chart-container pie-chart" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mt-16">
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>工单分布</span>
            </template>
            <div ref="ticketPieChartRef" class="chart-container pie-chart" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>热销商品</span>
            </template>
            <div class="rank-list">
              <div v-for="(item, index) in topProducts" :key="index" class="rank-item">
                <span class="rank-no" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                <span class="rank-name">{{ item.name }}</span>
                <span class="rank-sales">{{ formatNumber(item.sales) }}件</span>
                <span class="rank-amount">{{ formatMoney(item.amount) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>最近动态</span>
            </template>
            <div class="activity-list">
              <div v-for="item in recentActivities" :key="item.id" class="activity-item">
                <el-avatar :size="32" :src="item.user.avatar">
                  {{ item.user.name.charAt(0) }}
                </el-avatar>
                <div class="activity-content">
                  <div class="activity-title">
                    <span class="activity-user">{{ item.user.name }}</span>
                    <span>{{ item.title }}</span>
                  </div>
                  <div class="activity-desc">{{ item.description }}</div>
                  <div class="activity-time">{{ formatRelativeTime(item.time) }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import { User, Goods, Money, Tickets, CaretTop, CaretBottom } from '@element-plus/icons-vue';
import { useFilterStore } from '@/stores';
import { dashboardApi } from '@/api';
import { formatMoney, formatNumber, formatRelativeTime } from '@platform/shared-utils';
import type { DashboardStats, TrendItem, ProductRank } from '@platform/shared-types';

const filterStore = useFilterStore();
const savedFilter = filterStore.getFilter('dashboard');

const loading = ref(false);
const trendType = ref('order');
const trendChartRef = ref<HTMLElement>();
const orderPieChartRef = ref<HTMLElement>();
const ticketPieChartRef = ref<HTMLElement>();

let trendChart: echarts.ECharts | null = null;
let orderPieChart: echarts.ECharts | null = null;
let ticketPieChart: echarts.ECharts | null = null;

const filterForm = reactive({
  dateRange: savedFilter.dateRange || [],
});

const statsCards = ref([
  { key: 'users', label: '总用户数', value: 0, trend: 12.5, icon: User, color: 'blue' },
  { key: 'orders', label: '总订单数', value: 0, trend: 8.3, icon: Goods, color: 'green' },
  { key: 'revenue', label: '总营收', value: '¥0', trend: 15.7, icon: Money, color: 'orange' },
  { key: 'tickets', label: '总工单', value: 0, trend: -3.2, icon: Tickets, color: 'purple' },
]);

const topProducts = ref<ProductRank[]>([]);
const recentActivities = ref<Array<{
  id: string;
  type: string;
  title: string;
  description: string;
  time: string;
  user: { name: string; avatar: string };
}>>([]);

async function loadData() {
  loading.value = true;
  try {
    const params: any = {};
    if (filterForm.dateRange?.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }

    const res = await dashboardApi.getStats(params);
    if (res.code === 0) {
      const data: DashboardStats = res.data;
      updateStatsCards(data);
      topProducts.value = data.topProducts;
      updateTrendChart(data.orderTrend);
      updatePieCharts(data.orderDistribution, data.ticketDistribution);
    }

    const activitiesRes = await dashboardApi.getRecentActivities({ limit: 5 });
    if (activitiesRes.code === 0) {
      recentActivities.value = activitiesRes.data;
    }
  } finally {
    loading.value = false;
  }
}

function updateStatsCards(data: DashboardStats) {
  statsCards.value[0].value = formatNumber(data.totalUsers);
  statsCards.value[1].value = formatNumber(data.totalOrders);
  statsCards.value[2].value = formatMoney(data.totalRevenue);
  statsCards.value[3].value = formatNumber(data.totalTickets);
}

function updateTrendChart(trendData: TrendItem[]) {
  if (!trendChartRef.value) return;

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: {c}',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.map((d) => d.date),
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: trendType.value,
        type: 'line',
        smooth: true,
        data: trendData.map((d) => d.value),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
          ]),
        },
        lineStyle: {
          color: '#409eff',
          width: 2,
        },
        itemStyle: {
          color: '#409eff',
        },
      },
    ],
  };

  trendChart?.setOption(option);
}

function updatePieCharts(
  orderDist: { name: string; value: number }[],
  ticketDist: { name: string; value: number }[]
) {
  const orderOption: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
        },
        data: orderDist,
      },
    ],
  };

  const ticketOption: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
        },
        data: ticketDist,
      },
    ],
  };

  orderPieChart?.setOption(orderOption);
  ticketPieChart?.setOption(ticketOption);
}

function handleSearch() {
  filterStore.setFilter('dashboard', filterForm);
  loadData();
}

function handleReset() {
  filterForm.dateRange = [];
  filterStore.clearFilter('dashboard');
  loadData();
}

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
  }
  if (orderPieChartRef.value) {
    orderPieChart = echarts.init(orderPieChartRef.value);
  }
  if (ticketPieChartRef.value) {
    ticketPieChart = echarts.init(ticketPieChartRef.value);
  }
}

function handleResize() {
  trendChart?.resize();
  orderPieChart?.resize();
  ticketPieChart?.resize();
}

watch(trendType, async () => {
  const params: any = { type: trendType.value as any };
  if (filterForm.dateRange?.length === 2) {
    params.startDate = filterForm.dateRange[0];
    params.endDate = filterForm.dateRange[1];
  }
  const res = await dashboardApi.getTrendData(params);
  if (res.code === 0) {
    updateTrendChart(res.data);
  }
});

onMounted(async () => {
  await nextTick();
  initCharts();
  loadData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  trendChart?.dispose();
  orderPieChart?.dispose();
  ticketPieChart?.dispose();
});
</script>

<style scoped lang="scss">
.stats-overview {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  &.blue .stat-icon {
    background: linear-gradient(135deg, #409eff, #66b1ff);
  }

  &.green .stat-icon {
    background: linear-gradient(135deg, #67c23a, #85ce61);
  }

  &.orange .stat-icon {
    background: linear-gradient(135deg, #e6a23c, #ebb563);
  }

  &.purple .stat-icon {
    background: linear-gradient(135deg, #909399, #a6a9ad);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.stat-trend {
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 2px;

  &.up {
    color: var(--el-color-success);
  }

  &.down {
    color: var(--el-color-danger);
  }
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;

  &.pie-chart {
    height: 280px;
  }
}

.rank-list {
  .rank-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }
  }

  .rank-no {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    background: var(--el-fill-color-light);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
    margin-right: 12px;
    flex-shrink: 0;

    &.top3 {
      background: var(--el-color-primary);
      color: #fff;
    }
  }

  .rank-name {
    flex: 1;
    color: var(--el-text-color-primary);
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .rank-sales {
    margin: 0 16px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .rank-amount {
    color: var(--el-color-primary);
    font-weight: 600;
    font-size: 13px;
  }
}

.activity-list {
  .activity-item {
    display: flex;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }
  }

  .activity-content {
    flex: 1;
    min-width: 0;
  }

  .activity-title {
    font-size: 13px;
    color: var(--el-text-color-primary);
    margin-bottom: 2px;

    .activity-user {
      color: var(--el-color-primary);
      margin-right: 4px;
    }
  }

  .activity-desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin: 0 0 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .activity-time {
    font-size: 11px;
    color: var(--el-text-color-placeholder);
  }
}

.mt-16 {
  margin-top: 16px;
}
</style>
