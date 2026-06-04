import { get, post, put, del } from './request';
import type { Response, MenuItem } from '@platform/shared-types';

export const menuApi = {
  getMenus(appId?: string): Promise<Response<MenuItem[]>> {
    return get<Response<MenuItem[]>>('/menu/tree', { appId });
  },

  getFlatMenus(appId?: string): Promise<Response<MenuItem[]>> {
    return get<Response<MenuItem[]>>('/menu/flat', { appId });
  },

  getDetail(id: string): Promise<Response<MenuItem>> {
    return get<Response<MenuItem>>(`/menu/${id}`);
  },

  create(params: Omit<MenuItem, 'id' | 'children'>): Promise<Response<MenuItem>> {
    return post<Response<MenuItem>>('/menu', params);
  },

  update(id: string, params: Partial<MenuItem>): Promise<Response<MenuItem>> {
    return put<Response<MenuItem>>(`/menu/${id}`, params);
  },

  remove(id: string): Promise<Response<void>> {
    return del<Response<void>>(`/menu/${id}`);
  },

  updateSort(ids: string[]): Promise<Response<void>> {
    return put<Response<void>>('/menu/sort', { ids });
  },

  getUserMenus(userId: string, appId?: string): Promise<Response<MenuItem[]>> {
    return get<Response<MenuItem[]>>(`/menu/user/${userId}`, { appId });
  },
};
