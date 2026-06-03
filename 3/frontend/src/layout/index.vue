<template>
  <div class="layout-container">
    <Sidebar class="sidebar-container" />
    <div class="main-container" :class="{ 'is-collapsed': !appStore.sidebar.opened }">
      <Navbar />
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import { useAppStore } from '@/store/modules/app'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'

const appStore = useAppStore()
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100%;
  width: 100%;
  display: flex;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  transition: width 0.3s;
  flex-shrink: 0;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  transition: margin-left 0.3s;
}

:deep(.el-menu--collapse) {
  width: 64px;
}
</style>
