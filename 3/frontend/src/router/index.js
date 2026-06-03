import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/store/modules/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', noAuth: true }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/layout',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '预警看板', icon: 'DataAnalysis' }
      },
      {
        path: '/location',
        name: 'Location',
        component: () => import('@/views/location/index.vue'),
        meta: { title: '仓库库位视图', icon: 'OfficeBuilding' }
      },
      {
        path: '/receipt',
        name: 'Receipt',
        component: () => import('@/views/receipt/index.vue'),
        meta: { title: '入库流程', icon: 'Download' }
      },
      {
        path: '/picking',
        name: 'Picking',
        component: () => import('@/views/picking/index.vue'),
        meta: { title: '拣货任务', icon: 'List' }
      },
      {
        path: '/batch',
        name: 'Batch',
        component: () => import('@/views/batch/index.vue'),
        meta: { title: '批次明细', icon: 'Tickets' }
      },
      {
        path: '/inventory-log',
        name: 'InventoryLog',
        component: () => import('@/views/inventory-log/index.vue'),
        meta: { title: '库存流水', icon: 'Document' }
      },
      {
        path: '/report',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '统计报表', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE}` : import.meta.env.VITE_APP_TITLE
  
  const userStore = useUserStore()
  const token = userStore.token
  
  if (to.meta.noAuth) {
    next()
  } else if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
