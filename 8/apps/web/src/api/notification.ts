import { get, post, put, del } from './request';
import type { Response, PageResult, PageParams, Notification } from '@platform/shared-types';

export const notificationApi = {
  getList(params?: PageParams & { type?: string; read?: boolean }): Promise<Response<PageResult<Notification>>> {
    return get<Response<PageResult<Notification>>>('/notification/list', params);
  },

  getDetail(id: string): Promise<Response<Notification>> {
    return get<Response<Notification>>(`/notification/${id}`);
  },

  create(params: Omit<Notification, 'id' | 'read' | 'createdAt'>): Promise<Response<Notification>> {
    return post<Response<Notification>>('/notification', params);
  },

  update(id: string, params: Partial<Notification>): Promise<Response<Notification>> {
    return put<Response<Notification>>(`/notification/${id}`, params);
  },

  remove(id: string): Promise<Response<void>> {
    return del<Response<void>>(`/notification/${id}`);
  },

  markAsRead(id: string): Promise<Response<void>> {
    return put<Response<void>>(`/notification/${id}/read`);
  },

  markAllAsRead(): Promise<Response<void>> {
    return post<Response<void>>('/notification/mark-all-read');
  },

  getUnreadCount(): Promise<Response<{ count: number }>> {
    return get<Response<{ count: number }>>('/notification/unread-count');
  },

  pushToUser(userId: string, params: { title: string; content: string; type: Notification['type']; source?: string }): Promise<Response<void>> {
    return post<Response<void>>('/notification/push', { userId, ...params });
  },

  pushToAll(params: { title: string; content: string; type: Notification['type']; source?: string }): Promise<Response<void>> {
    return post<Response<void>>('/notification/push-all', params);
  },

  getTypes(): Promise<Response<Array<{ value: Notification['type']; label: string }>>> {
    return get<Response<Array<{ value: Notification['type']; label: string }>>>('/notification/types');
  },
};
