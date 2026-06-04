import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Notification } from '@platform/shared-types';
import { notificationApi } from '@/api';

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([]);
  const unreadCount = ref(0);
  const loading = ref(false);

  const unreadList = computed(() => notifications.value.filter((n) => !n.read));
  const readList = computed(() => notifications.value.filter((n) => n.read));

  async function loadNotifications(params?: { page?: number; pageSize?: number; read?: boolean }) {
    loading.value = true;
    try {
      const res = await notificationApi.getList(params);
      if (res.code === 0) {
        notifications.value = res.data.list;
      }
      return res.data;
    } finally {
      loading.value = false;
    }
  }

  async function loadUnreadCount() {
    try {
      const res = await notificationApi.getUnreadCount();
      if (res.code === 0) {
        unreadCount.value = res.data.count;
      }
      return res.data.count;
    } catch (error) {
      console.error('加载未读消息数失败:', error);
      return 0;
    }
  }

  async function markAsRead(id: string) {
    try {
      const res = await notificationApi.markAsRead(id);
      if (res.code === 0) {
        const item = notifications.value.find((n) => n.id === id);
        if (item) item.read = true;
        unreadCount.value = Math.max(0, unreadCount.value - 1);
      }
      return res;
    } catch (error) {
      console.error('标记已读失败:', error);
      throw error;
    }
  }

  async function markAllAsRead() {
    try {
      const res = await notificationApi.markAllAsRead();
      if (res.code === 0) {
        notifications.value.forEach((n) => (n.read = true));
        unreadCount.value = 0;
      }
      return res;
    } catch (error) {
      console.error('标记全部已读失败:', error);
      throw error;
    }
  }

  function pushNotification(notification: Notification) {
    notifications.value.unshift(notification);
    if (!notification.read) {
      unreadCount.value++;
    }
  }

  function removeNotification(id: string) {
    const index = notifications.value.findIndex((n) => n.id === id);
    if (index > -1) {
      const item = notifications.value[index];
      if (!item.read) {
        unreadCount.value = Math.max(0, unreadCount.value - 1);
      }
      notifications.value.splice(index, 1);
    }
  }

  function clearAll() {
    notifications.value = [];
    unreadCount.value = 0;
  }

  return {
    notifications,
    unreadCount,
    loading,
    unreadList,
    readList,
    loadNotifications,
    loadUnreadCount,
    markAsRead,
    markAllAsRead,
    pushNotification,
    removeNotification,
    clearAll,
  };
});
