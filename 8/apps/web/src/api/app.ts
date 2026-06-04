import { get, post, put, del } from './request';
import type { Response, PageResult, PageParams, AppInfo } from '@platform/shared-types';

export const appApi = {
  getApps(): Promise<Response<AppInfo[]>> {
    return get<Response<AppInfo[]>>('/app/list');
  },

  getList(params?: PageParams): Promise<Response<PageResult<AppInfo>>> {
    return get<Response<PageResult<AppInfo>>>('/app/page', params);
  },

  getDetail(id: string): Promise<Response<AppInfo>> {
    return get<Response<AppInfo>>(`/app/${id}`);
  },

  create(params: Omit<AppInfo, 'id'>): Promise<Response<AppInfo>> {
    return post<Response<AppInfo>>('/app', params);
  },

  update(id: string, params: Partial<AppInfo>): Promise<Response<AppInfo>> {
    return put<Response<AppInfo>>(`/app/${id}`, params);
  },

  remove(id: string): Promise<Response<void>> {
    return del<Response<void>>(`/app/${id}`);
  },

  updateStatus(id: string, status: 'active' | 'inactive'): Promise<Response<void>> {
    return put<Response<void>>(`/app/${id}/status`, { status });
  },

  getCurrentApp(): Promise<Response<AppInfo>> {
    return get<Response<AppInfo>>('/app/current');
  },

  switchApp(appId: string): Promise<Response<AppInfo>> {
    return post<Response<AppInfo>>('/app/switch', { appId });
  },
};
