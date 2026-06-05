import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import NProgress from "nprogress";
import { useUserStore } from "@/stores/user";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录", requiresAuth: false }
  },
  {
    path: "/",
    component: () => import("@/layout/index.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/index.vue"),
        meta: { title: "指标看板", icon: "DataLine", requiresAuth: true }
      },
      {
        path: "users",
        name: "Users",
        component: () => import("@/views/users/index.vue"),
        meta: { title: "用户管理", icon: "User", requiresAuth: true, permission: "user:view" }
      },
      {
        path: "orders",
        name: "Orders",
        component: () => import("@/views/orders/index.vue"),
        meta: { title: "订单管理", icon: "List", requiresAuth: true, permission: "order:view" }
      },
      {
        path: "tickets",
        name: "Tickets",
        component: () => import("@/views/tickets/index.vue"),
        meta: { title: "工单管理", icon: "Tickets", requiresAuth: true, permission: "ticket:view" }
      },
      {
        path: "messages",
        name: "Messages",
        component: () => import("@/views/messages/index.vue"),
        meta: { title: "消息中心", icon: "Bell", requiresAuth: true }
      },
      {
        path: "logs",
        name: "Logs",
        component: () => import("@/views/logs/index.vue"),
        meta: { title: "操作日志", icon: "Document", requiresAuth: true, permission: "log:view" }
      },
      {
        path: "profile",
        name: "Profile",
        component: () => import("@/views/profile/index.vue"),
        meta: { title: "个人中心", icon: "UserFilled", requiresAuth: true }
      }
    ]
  },
  {
    path: "/403",
    name: "403",
    component: () => import("@/views/error/403.vue"),
    meta: { title: "无权限" }
  },
  {
    path: "/404",
    name: "404",
    component: () => import("@/views/error/404.vue"),
    meta: { title: "页面不存在" }
  },
  {
    path: "/500",
    name: "500",
    component: () => import("@/views/error/500.vue"),
    meta: { title: "服务器错误" }
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/404"
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  NProgress.start();
  const userStore = useUserStore();
  
  if (to.meta.requiresAuth && !userStore.token) {
    next({ path: "/login", query: { redirect: to.fullPath } });
  } else if (to.meta.permission && !userStore.hasPermission(to.meta.permission as string)) {
    next("/403");
  } else {
    next();
  }
});

router.afterEach((to) => {
  NProgress.done();
  if (to.meta.title) {
    document.title = `${to.meta.title} - 集团运营中台`;
  }
});

export default router;
