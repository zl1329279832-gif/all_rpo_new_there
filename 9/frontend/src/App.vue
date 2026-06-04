<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="28" color="#fff"><Crop /></el-icon>
        <span class="title">烘焙管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="false"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#ffd04b"
        router
      >
        <el-menu-item index="/prod-plan">
          <el-icon><Tickets /></el-icon>
          <span>生产计划</span>
        </el-menu-item>
        <el-menu-item index="/batch">
          <el-icon><Goods /></el-icon>
          <span>批次台账</span>
        </el-menu-item>
        <el-menu-item index="/stock">
          <el-icon><Warning /></el-icon>
          <span>库存效期</span>
        </el-menu-item>
        <el-menu-item index="/transfer">
          <el-icon><SwitchButton /></el-icon>
          <span>门店调拨</span>
        </el-menu-item>
        <el-menu-item index="/damage">
          <el-icon><Delete /></el-icon>
          <span>报损管理</span>
        </el-menu-item>
        <el-menu-item index="/analysis">
          <el-icon><DataAnalysis /></el-icon>
          <span>经营分析</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="user-info">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="username">管理员</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)

const pageTitleMap = {
  '/prod-plan': '生产计划',
  '/batch': '批次台账',
  '/stock': '库存效期',
  '/transfer': '门店调拨',
  '/damage': '报损管理',
  '/analysis': '经营分析'
}

const currentPageTitle = computed(() => pageTitleMap[route.path] || '首页')
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  overflow: hidden;

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background: rgba(255, 255, 255, 0.1);

    .title {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }
  }

  :deep(.el-menu) {
    border-right: none;
  }
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;

  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .username {
      color: #666;
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
