<template>
  <el-popover
    placement="bottom-end"
    trigger="click"
    :width="360"
    popper-class="notification-popover"
    @show="loadNotifications"
  >
    <template #reference>
      <div class="notification-wrapper">
        <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" class="notification-badge">
          <el-icon class="header-icon">
            <Bell />
          </el-icon>
        </el-badge>
      </div>
    </template>

    <div class="notification-container">
      <div class="notification-header">
        <span class="notification-title">消息中心</span>
        <el-button type="primary" link @click="handleMarkAllRead" v-if="notificationStore.unreadCount > 0">
          全部已读
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="notification-tabs">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane :label="`未读 (${notificationStore.unreadCount})`" name="unread" />
      </el-tabs>

      <div class="notification-list" v-loading="notificationStore.loading">
        <el-empty v-if="displayNotifications.length === 0" description="暂无消息" />
        <div
          v-for="item in displayNotifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.read }"
          @click="handleItemClick(item)"
        >
          <div class="item-icon" :class="item.type">
            <el-icon>
              <InfoFilled v-if="item.type === 'system'" />
              <BellFilled v-else-if="item.type === 'business'" />
              <WarningFilled v-else-if="item.type === 'warning'" />
              <CircleCloseFilled v-else />
            </el-icon>
          </div>
          <div class="item-content">
            <div class="item-header">
              <span class="item-title">{{ item.title }}</span>
              <span class="item-time">{{ formatRelativeTime(item.createdAt) }}</span>
            </div>
            <p class="item-desc">{{ item.content }}</p>
          </div>
          <div v-if="!item.read" class="unread-dot" />
        </div>
      </div>

      <div class="notification-footer">
        <el-button type="primary" link @click="goToNotificationList">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, InfoFilled, BellFilled, WarningFilled, CircleCloseFilled, ArrowRight } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useNotificationStore } from '@/stores';
import { formatRelativeTime } from '@platform/shared-utils';
import type { Notification } from '@platform/shared-types';

const router = useRouter();
const notificationStore = useNotificationStore();
const activeTab = ref('all');

let timer: NodeJS.Timeout | null = null;

const displayNotifications = computed(() => {
  if (activeTab.value === 'unread') {
    return notificationStore.unreadList.slice(0, 10);
  }
  return notificationStore.notifications.slice(0, 10);
});

async function loadNotifications() {
  await notificationStore.loadNotifications({ pageSize: 20 });
  await notificationStore.loadUnreadCount();
}

async function handleMarkAllRead() {
  await notificationStore.markAllAsRead();
  ElMessage.success('已全部标记为已读');
}

async function handleItemClick(item: Notification) {
  if (!item.read) {
    await notificationStore.markAsRead(item.id);
  }
  router.push('/notification/list');
}

function goToNotificationList() {
  router.push('/notification/list');
}

function startPolling() {
  timer = setInterval(() => {
    notificationStore.loadUnreadCount();
  }, 30000);
}

onMounted(() => {
  notificationStore.loadUnreadCount();
  startPolling();
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped lang="scss">
.notification-wrapper {
  position: relative;
}

.notification-badge {
  cursor: pointer;
}

.header-icon {
  font-size: 18px;
  padding: 8px;
  border-radius: 4px;
  color: var(--el-text-color-regular);
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}

.notification-container {
  max-height: 480px;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .notification-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.notification-tabs {
  padding: 0 16px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 320px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.unread {
    background: var(--el-color-primary-light-9);
  }

  .item-icon {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: #fff;

    &.system {
      background: var(--el-color-primary);
    }

    &.business {
      background: var(--el-color-success);
    }

    &.warning {
      background: var(--el-color-warning);
    }

    &.error {
      background: var(--el-color-danger);
    }
  }

  .item-content {
    flex: 1;
    min-width: 0;
  }

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 4px;
  }

  .item-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .item-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    flex-shrink: 0;
  }

  .item-desc {
    font-size: 12px;
    color: var(--el-text-color-regular);
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
  }

  .unread-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--el-color-danger);
    flex-shrink: 0;
    margin-top: 8px;
  }
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  text-align: center;
}
</style>
