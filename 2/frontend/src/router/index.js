import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'device',
        name: 'DeviceList',
        component: () => import('@/views/DeviceList.vue')
      },
      {
        path: 'inspection',
        name: 'InspectionCalendar',
        component: () => import('@/views/InspectionCalendar.vue')
      },
      {
        path: 'repair',
        name: 'RepairOrderList',
        component: () => import('@/views/RepairOrderList.vue')
      },
      {
        path: 'qc-record',
        name: 'QcRecordList',
        component: () => import('@/views/QcRecordList.vue')
      },
      {
        path: 'risk',
        name: 'RiskDashboard',
        component: () => import('@/views/RiskDashboard.vue')
      },
      {
        path: 'statistics',
        name: 'StatisticsReport',
        component: () => import('@/views/StatisticsReport.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
