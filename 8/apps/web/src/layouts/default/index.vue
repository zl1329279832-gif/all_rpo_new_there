<template>
  <div class="layout-container" :class="{ collapsed: appStore.collapsed, dark: appStore.isDark }">
    <el-container class="layout-wrapper">
      <el-aside :width="sidebarWidth" class="layout-sidebar">
        <Logo :collapsed="appStore.collapsed" />
        <AppSwitcher />
        <SidebarMenu :menus="appStore.menus" :collapsed="appStore.collapsed" />
      </el-aside>

      <el-container class="layout-main">
        <el-header class="layout-header">
          <div class="header-left">
            <el-icon class="toggle-btn" @click="appStore.toggleCollapsed">
              <Fold v-if="!appStore.collapsed" />
              <Expand v-else />
            </el-icon>
            <Breadcrumb />
          </div>
          <div class="header-right">
            <SearchBar />
            <Fullscreen />
            <NotificationCenter />
            <UserDropdown />
          </div>
        </el-header>

        <el-main class="layout-content">
          <router-view v-slot="{ Component, route }">
            <transition name="fade" mode="out-in">
              <component :is="Component" :key="route.fullPath" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Fold, Expand } from '@element-plus/icons-vue';
import { useAppStore } from '@/stores';
import Logo from './components/Logo.vue';
import AppSwitcher from './components/AppSwitcher.vue';
import SidebarMenu from './components/SidebarMenu.vue';
import Breadcrumb from './components/Breadcrumb.vue';
import SearchBar from './components/SearchBar.vue';
import Fullscreen from './components/Fullscreen.vue';
import NotificationCenter from './components/NotificationCenter.vue';
import UserDropdown from './components/UserDropdown.vue';

const appStore = useAppStore();

const sidebarWidth = computed(() => (appStore.collapsed ? '64px' : '220px'));
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  overflow: hidden;
  transition: all 0.3s ease;

  &.collapsed {
    .layout-sidebar {
      width: 64px;
    }
  }

  &.dark {
    background: #141414;
  }
}

.layout-wrapper {
  height: 100%;
}

.layout-sidebar {
  background: var(--el-menu-bg-color);
  border-right: 1px solid var(--el-border-color-lighter);
  transition: width 0.3s ease;
  overflow: hidden;
}

.layout-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 64px;
  padding: 0 16px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.layout-content {
  padding: 0;
  background: var(--el-bg-color-page);
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
