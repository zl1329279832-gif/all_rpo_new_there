import { get, post, put, del } from './request';
import type { Response, PageResult, PageParams, User } from '@platform/shared-types';

export const userApi = {
  getList(params?: PageParams & { department?: string; status?: string; role?: string }): Promise<Response<PageResult<User>>> {
    return get<Response<PageResult<User>>>('/user/list', params);
  },

  getDetail(id: string): Promise<Response<User>> {
    return get<Response<User>>(`/user/${id}`);
  },

  create(params: Omit<User, 'id' | 'createdAt' | 'lastLoginAt'>): Promise<Response<User>> {
    return post<Response<User>>('/user', params);
  },

  update(id: string, params: Partial<User>): Promise<Response<User>> {
    return put<Response<User>>(`/user/${id}`, params);
  },

  remove(id: string): Promise<Response<void>> {
    return del<Response<void>>(`/user/${id}`);
  },

  batchRemove(ids: string[]): Promise<Response<void>> {
    return post<Response<void>>('/user/batch-remove', { ids });
  },

  updateStatus(id: string, status: 'active' | 'disabled'): Promise<Response<void>> {
    return put<Response<void>>(`/user/${id}/status`, { status });
  },

  resetPassword(id: string): Promise<Response<{ password: string }>> {
    return post<Response<{ password: string }>>(`/user/${id}/reset-password`);
  },

  getDepartments(): Promise<Response<Array<{ id: string; name: string; parentId: string | null }>>> {
    return get<Response<Array<{ id: string; name: string; parentId: string | null }>>>('/user/departments');
  },

  getRoles(): Promise<Response<Array<{ id: string; name: string; code: string }>>> {
    return get<Response<Array<{ id: string; name: string; code: string }>>>('/user/roles');
  },

  export(params?: PageParams): Promise<Response<{ url: string }>> {
    return post<Response<{ url: string }>>('/user/export', params);
  },

  import(file: File): Promise<Response<{ success: number; fail: number; errors: string[] }>> {
    const formData = new FormData();
    formData.append('file', file);
    return post<Response<{ success: number; fail: number; errors: string[] }>>('/user/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
