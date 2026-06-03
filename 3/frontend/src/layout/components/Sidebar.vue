<template>
  <div class="sidebar">
    <div class="logo-container">
      <el-icon v-if="appStore.sidebar.opened" class="logo-icon"><Box /></el-icon>
      <h1 v-if="appStore.sidebar.opened" class="logo-text">{{ appTitle }}</h1>
      <el-icon v-else class="logo-icon"><Box /></el-icon>
    </div>
    <el-scrollbar class="scrollbar-container">
      <el-menu
        :default-active="route.path"
        :collapse="!appStore.sidebar.opened"
        :unique-opened="true"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item
          v-for="route in menuRoutes"
          :key="route.path"
          :index="route.path"
        >
          <el-icon><component :is="route.meta.icon" /></el-icon>
          <template #title>{{ route.meta.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import {
  DataAnalysis,
  OfficeBuilding,
  Download,
  List,
  Tickets,
  Document,
  TrendCharts,
  Box
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const appTitle = import.meta.env.VITE_APP_TITLE

const menuRoutes = computed(() => {
  const layoutRoute = route.matched.find(r => r.name === 'Layout')
  return layoutRoute?.children || []
})
</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;
  width: 100%;
  background-color: #304156;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 15px;
  background-color: #2b2f3a;
  border-bottom: 1px solid #1f2d3d;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 28px;
  color: #409eff;
}

.logo-text {
  margin-left: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.scrollbar-container {
  flex: 1;
  overflow: hidden;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item:hover) {
  background-color: #263445 !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #263445 !important;
}
</style>
