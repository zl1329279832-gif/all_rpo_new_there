import type { Router } from 'vue-router';
import { useUserStore, useAppStore } from '@/stores';
import { getStorage } from '@platform/shared-utils';

const WHITE_LIST = ['/login', '/403', '/404', '/500'];

export function setupPermission(router: Router): void {
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore();
    const appStore = useAppStore();

    const token = getStorage<string>('token');
    const hasToken = !!token;

    if (to.meta.public || WHITE_LIST.includes(to.path)) {
      next();
      return;
    }

    if (!hasToken) {
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
      return;
    }

    if (!userStore.user) {
      try {
        const res = await userStore.refreshTokenFn();
        if (!res) {
          userStore.logout();
          next({
            path: '/login',
            query: { redirect: to.fullPath },
          });
          return;
        }
      } catch (error) {
        console.error('恢复用户信息失败:', error);
        userStore.logout();
        next({
          path: '/login',
          query: { redirect: to.fullPath },
        });
        return;
      }
    }

    if (appStore.apps.length === 0) {
      await appStore.loadApps();
    }

    if (appStore.menus.length === 0) {
      await appStore.loadMenus(appStore.currentApp?.id);
    }

    if (to.meta.permission && !userStore.hasPermission(to.meta.permission as string)) {
      next('/403');
      return;
    }

    next();
  });
}
