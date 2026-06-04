import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { MenuItem, AppInfo } from '@platform/shared-types';
import { getStorage, setStorage, removeStorage } from '@platform/shared-utils';
import { menuApi, appApi } from '@/api';

export const useAppStore = defineStore('app', () => {
  const locale = ref(getStorage<string>('locale') || 'zh-CN');
  const theme = ref<'light' | 'dark'>(getStorage<'light' | 'dark'>('theme') || 'light');
  const collapsed = ref(getStorage<boolean>('sidebar_collapsed') || false);
  const menus = ref<MenuItem[]>([]);
  const apps = ref<AppInfo[]>([]);
  const currentApp = ref<AppInfo | null>(null);
  const loading = ref(false);

  const isDark = computed(() => theme.value === 'dark');

  function toggleCollapsed() {
    collapsed.value = !collapsed.value;
    setStorage('sidebar_collapsed', collapsed.value);
  }

  function setTheme(val: 'light' | 'dark') {
    theme.value = val;
    setStorage('theme', val);
  }

  function setLocale(val: string) {
    locale.value = val;
    setStorage('locale', val);
  }

  async function loadMenus(appId?: string) {
    try {
      const res = await menuApi.getMenus(appId);
      if (res.code === 0) {
        menus.value = res.data;
      }
      return res.data;
    } catch (error) {
      console.error('加载菜单失败:', error);
      return [];
    }
  }

  async function loadApps() {
    try {
      const res = await appApi.getApps();
      if (res.code === 0) {
        apps.value = res.data;
        if (res.data.length > 0 && !currentApp.value) {
          currentApp.value = res.data[0];
        }
      }
      return res.data;
    } catch (error) {
      console.error('加载应用列表失败:', error);
      return [];
    }
  }

  function setCurrentApp(app: AppInfo) {
    currentApp.value = app;
    loadMenus(app.id);
  }

  function logout() {
    menus.value = [];
    apps.value = [];
    currentApp.value = null;
    removeStorage('token');
    removeStorage('user_info');
  }

  return {
    locale,
    theme,
    collapsed,
    menus,
    apps,
    currentApp,
    loading,
    isDark,
    toggleCollapsed,
    setTheme,
    setLocale,
    loadMenus,
    loadApps,
    setCurrentApp,
    logout,
  };
});
