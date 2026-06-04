import type { RouteRecordRaw } from 'vue-router';
import type { MenuItem } from '@platform/shared-types';

const modules = import.meta.glob('../views/**/**.vue');

export function generateRoutes(menus: MenuItem[], parentPath = ''): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = [];

  menus.forEach((menu) => {
    if (menu.status === 'hidden') return;

    const fullPath = parentPath + menu.path;
    let component: any;

    if (menu.component && modules[`../views/${menu.component}.vue`]) {
      component = modules[`../views/${menu.component}.vue`];
    } else if (menu.children && menu.children.length > 0) {
      component = () => import('@/layouts/default/index.vue');
    } else {
      component = () => import('@/views/error/404.vue');
    }

    const route: RouteRecordRaw = {
      path: menu.path.startsWith('/') ? menu.path : fullPath,
      name: menu.name,
      component,
      meta: {
        title: menu.name,
        icon: menu.icon,
        permission: menu.permission,
        hidden: menu.status === 'hidden',
      },
    };

    if (menu.children && menu.children.length > 0) {
      route.children = generateRoutes(menu.children, fullPath + '/');
      route.redirect = fullPath + '/' + menu.children[0].path;
    }

    routes.push(route);
  });

  return routes;
}

export function addDynamicRoutes(menus: MenuItem[]): void {
  const routes = generateRoutes(menus);
  routes.forEach((route) => {
    if (!router.hasRoute(route.name as string)) {
      router.addRoute(route);
    }
  });
}

import router from './index';
