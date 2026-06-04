import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/prod-plan'
  },
  {
    path: '/prod-plan',
    name: 'ProdPlan',
    component: () => import('@/views/ProdPlan.vue'),
    meta: { title: '生产计划' }
  },
  {
    path: '/batch',
    name: 'Batch',
    component: () => import('@/views/BatchLedger.vue'),
    meta: { title: '批次台账' }
  },
  {
    path: '/stock',
    name: 'Stock',
    component: () => import('@/views/StockExpiry.vue'),
    meta: { title: '库存效期' }
  },
  {
    path: '/transfer',
    name: 'Transfer',
    component: () => import('@/views/StoreTransfer.vue'),
    meta: { title: '门店调拨' }
  },
  {
    path: '/damage',
    name: 'Damage',
    component: () => import('@/views/StockDamage.vue'),
    meta: { title: '报损管理' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('@/views/BusinessAnalysis.vue'),
    meta: { title: '经营分析' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
