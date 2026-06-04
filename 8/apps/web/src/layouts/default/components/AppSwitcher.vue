<template>
  <div class="app-switcher">
    <el-dropdown trigger="click" @command="handleSwitch">
      <div class="app-switcher-trigger">
        <el-icon><Grid /></el-icon>
        <span v-if="!appStore.collapsed" class="app-name">
          {{ appStore.currentApp?.name || '选择应用' }}
        </span>
        <el-icon class="arrow"><ArrowDown /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="app in appStore.apps"
            :key="app.id"
            :command="app"
            :class="{ active: appStore.currentApp?.id === app.id }"
          >
            <el-icon><component :is="app.icon" /></el-icon>
            <span>{{ app.name }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { Grid, ArrowDown } from '@element-plus/icons-vue';
import { useAppStore } from '@/stores';
import type { AppInfo } from '@platform/shared-types';

const appStore = useAppStore();

function handleSwitch(app: AppInfo) {
  appStore.setCurrentApp(app);
}
</script>

<style scoped lang="scss">
.app-switcher {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.app-switcher-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--el-text-color-primary);
  font-size: 14px;

  &:hover {
    background: var(--el-fill-color-light);
  }

  .app-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .arrow {
    font-size: 12px;
  }
}

.active {
  color: var(--el-color-primary);
}
</style>
