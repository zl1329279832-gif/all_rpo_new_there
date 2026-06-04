import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import NProgress from 'nprogress';

const Layout = () => import('@/layouts/default/index.vue');
const Login = () => import('@/views/login/index.vue');
const Dashboard = () => import('@/views/dashboard/index.vue');
const UserList = () => import('@/views/user/list.vue');
const UserDetail = () => import('@/views/user/detail.vue');
const OrderList = () => import('@/views/order/list.vue');
const OrderDetail = () => import('@/views/order/detail.vue');
const TicketList = () => import('@/views/ticket/list.vue');
const TicketDetail = () => import('@/views/ticket/detail.vue');
const NotificationList = () => import('@/views/notification/list.vue');
const OperationLog = () => import('@/views/log/operation.vue');
const NotFound = () => import('@/views/error/404.vue');
const Forbidden = () => import('@/views/error/403.vue');
const ServerError = () => import('@/views/error/500.vue');

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', public: true, hidden: true },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: Forbidden,
    meta: { title: '无权限', public: true, hidden: true },
  },
  {
    path: '/500',
    name: 'ServerError',
    component: ServerError,
    meta: { title: '服务器错误', public: true, hidden: true },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '指标看板',
          icon: 'DataLine',
          affix: true,
          permission: 'dashboard:view',
        },
      },
    ],
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    meta: { title: '用户管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: UserList,
        meta: {
          title: '用户列表',
          icon: 'UserFilled',
          permission: 'user:list',
        },
      },
      {
        path: 'detail/:id',
        name: 'UserDetail',
        component: UserDetail,
        meta: {
          title: '用户详情',
          hidden: true,
          permission: 'user:detail',
        },
      },
    ],
  },
  {
    path: '/order',
    component: Layout,
    redirect: '/order/list',
    meta: { title: '订单管理', icon: 'Goods' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: OrderList,
        meta: {
          title: '订单列表',
          icon: 'List',
          permission: 'order:list',
        },
      },
      {
        path: 'detail/:id',
        name: 'OrderDetail',
        component: OrderDetail,
        meta: {
          title: '订单详情',
          hidden: true,
          permission: 'order:detail',
        },
      },
    ],
  },
  {
    path: '/ticket',
    component: Layout,
    redirect: '/ticket/list',
    meta: { title: '工单管理', icon: 'Tickets' },
    children: [
      {
        path: 'list',
        name: 'TicketList',
        component: TicketList,
        meta: {
          title: '工单列表',
          icon: 'Document',
          permission: 'ticket:list',
        },
      },
      {
        path: 'detail/:id',
        name: 'TicketDetail',
        component: TicketDetail,
        meta: {
          title: '工单详情',
          hidden: true,
          permission: 'ticket:detail',
        },
      },
    ],
  },
  {
    path: '/notification',
    component: Layout,
    redirect: '/notification/list',
    meta: { title: '通知中心', icon: 'Bell' },
    children: [
      {
        path: 'list',
        name: 'NotificationList',
        component: NotificationList,
        meta: {
          title: '消息列表',
          icon: 'Message',
          permission: 'notification:list',
        },
      },
    ],
  },
  {
    path: '/log',
    component: Layout,
    redirect: '/log/operation',
    meta: { title: '日志管理', icon: 'Notebook' },
    children: [
      {
        path: 'operation',
        name: 'OperationLog',
        component: OperationLog,
        meta: {
          title: '操作日志',
          icon: 'Operation',
          permission: 'log:operation',
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面不存在', public: true, hidden: true },
  },
];

export const dynamicRoutes: RouteRecordRaw[] = [];

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach((to, from, next) => {
  NProgress.start();
  document.title = to.meta.title ? `${to.meta.title} - 集团运营中台` : '集团运营中台';
  next();
});

router.afterEach(() => {
  NProgress.done();
});

router.onError((error) => {
  NProgress.done();
  console.error('路由错误:', error);
});

export default router;
