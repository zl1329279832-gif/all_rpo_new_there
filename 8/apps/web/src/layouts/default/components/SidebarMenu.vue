<template>
  <el-scrollbar class="sidebar-menu">
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      router
      background-color="var(--el-menu-bg-color)"
      text-color="var(--el-menu-text-color)"
      active-text-color="var(--el-color-primary)"
    >
      <template v-for="menu in visibleMenus" :key="menu.id">
        <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
          <template #title>
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </template>
          <el-menu-item
            v-for="child in filterVisibleChildren(menu.children)"
            :key="child.id"
            :index="resolvePath(menu.path, child.path)"
            v-permission="child.permission"
          >
            <el-icon><component :is="child.icon" /></el-icon>
            <span>{{ child.name }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="menu.path" v-permission="menu.permission">
          <el-icon><component :is="menu.icon" /></el-icon>
          <template #title>{{ menu.name }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </el-scrollbar>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import type { MenuItem } from '@platform/shared-types';

const props = defineProps<{
  menus: MenuItem[];
  collapsed?: boolean;
}>();

const route = useRoute();

const activeMenu = computed(() => route.path);

const visibleMenus = computed(() =>
  props.menus.filter((menu) => menu.status === 'visible')
);

function filterVisibleChildren(children: MenuItem[]): MenuItem[] {
  return children.filter((child) => child.status === 'visible');
}

function resolvePath(parent: string, child: string): string {
  if (child.startsWith('/')) return child;
  return `/${parent.replace(/^\//, '')}/${child}`;
}
</script>

<style scoped lang="scss">
.sidebar-menu {
  height: calc(100vh - 64px - 60px);
}

:deep(.el-menu) {
  border-right: none;
}
</style>
