const fs = require("fs");
const path = require("path");

function writeFile(filePath, content) {
  const fullPath = path.join(process.cwd(), filePath);
  const dir = path.dirname(fullPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  fs.writeFileSync(fullPath, content, "utf8");
  console.log("Created:", filePath);
}

// ========== Router ==========
writeFile("apps/web/src/router/index.ts", `import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
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
    document.title = \`\${to.meta.title} - 集团运营中台\`;
  }
});

export default router;
`);

// ========== User Store ==========
writeFile("apps/web/src/stores/user.ts", `import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { login, getUserInfo, logout } from "@/api/user";
import type { User, MenuItem } from "@/types";

export const useUserStore = defineStore(
  "user",
  () => {
    const token = ref<string>("");
    const userInfo = ref<User | null>(null);
    const permissions = ref<string[]>([]);
    const menus = ref<MenuItem[]>([]);

    const isLoggedIn = computed(() => !!token.value);

    function hasPermission(permission: string): boolean {
      return permissions.value.includes("*") || permissions.value.includes(permission);
    }

    async function doLogin(username: string, password: string) {
      const res = await login({ username, password });
      token.value = res.data.token;
      await fetchUserInfo();
      return res;
    }

    async function fetchUserInfo() {
      const res = await getUserInfo();
      userInfo.value = res.data.user;
      permissions.value = res.data.permissions;
      menus.value = res.data.menus;
      return res;
    }

    async function doLogout() {
      try {
        await logout();
      } finally {
        clearUser();
      }
    }

    function clearUser() {
      token.value = "";
      userInfo.value = null;
      permissions.value = [];
      menus.value = [];
    }

    return {
      token,
      userInfo,
      permissions,
      menus,
      isLoggedIn,
      hasPermission,
      doLogin,
      fetchUserInfo,
      doLogout,
      clearUser
    };
  },
  {
    persist: {
      key: "platform-user",
      paths: ["token", "userInfo", "permissions", "menus"]
    }
  }
);
`);

// ========== App Store (shared state) ==========
writeFile("apps/web/src/stores/app.ts", `import { defineStore } from "pinia";
import { ref } from "vue";

export const useAppStore = defineStore(
  "app",
  () => {
    const sidebarCollapsed = ref(false);
    const globalFilters = ref<Record<string, any>>({
      dateRange: [],
      status: "",
      keyword: ""
    });
    const unreadMessageCount = ref(0);

    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value;
    }

    function setGlobalFilters(filters: Record<string, any>) {
      globalFilters.value = { ...globalFilters.value, ...filters };
    }

    function resetGlobalFilters() {
      globalFilters.value = {
        dateRange: [],
        status: "",
        keyword: ""
      };
    }

    function setUnreadCount(count: number) {
      unreadMessageCount.value = count;
    }

    return {
      sidebarCollapsed,
      globalFilters,
      unreadMessageCount,
      toggleSidebar,
      setGlobalFilters,
      resetGlobalFilters,
      setUnreadCount
    };
  },
  {
    persist: {
      key: "platform-app",
      paths: ["sidebarCollapsed", "globalFilters"]
    }
  }
);
`);

console.log("=== Router and stores generated! ===");
