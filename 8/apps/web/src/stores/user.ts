import { defineStore } from "pinia";
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
