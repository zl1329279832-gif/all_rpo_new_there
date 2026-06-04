import { get, post, del } from './request';
import type { Response, PageResult, PageParams, OperationLog } from '@platform/shared-types';

export const logApi = {
  getOperationLogs(params?: PageParams & { module?: string; operation?: string; status?: string; startDate?: string; endDate?: string; username?: string }): Promise<Response<PageResult<OperationLog>>> {
    return get<Response<PageResult<OperationLog>>>('/log/operation', params);
  },

  getOperationLogDetail(id: string): Promise<Response<OperationLog>> {
    return get<Response<OperationLog>>(`/log/operation/${id}`);
  },

  getLoginLogs(params?: PageParams & { status?: string; startDate?: string; endDate?: string; username?: string }): Promise<Response<PageResult<{
    id: string;
    userId: string;
    username: string;
    ip: string;
    location: string;
    device: string;
    browser: string;
    status: 'success' | 'fail';
    errorMsg?: string;
    createdAt: string;
  }>>> {
    return get<Response<PageResult<{
      id: string;
      userId: string;
      username: string;
      ip: string;
      location: string;
      device: string;
      browser: string;
      status: 'success' | 'fail';
      errorMsg?: string;
      createdAt: string;
    }>>>('/log/login', params);
  },

  getStatistics(params?: { startDate?: string; endDate?: string }): Promise<Response<{
    totalCount: number;
    successCount: number;
    failCount: number;
    avgDuration: number;
    topModules: Array<{ name: string; count: number }>;
    topOperations: Array<{ name: string; count: number }>;
  }>> {
    return get<Response<{
      totalCount: number;
      successCount: number;
      failCount: number;
      avgDuration: number;
      topModules: Array<{ name: string; count: number }>;
      topOperations: Array<{ name: string; count: number }>;
    }>>('/log/statistics', params);
  },

  export(params?: PageParams): Promise<Response<{ url: string }>> {
    return post<Response<{ url: string }>>('/log/export', params);
  },

  clean(params?: { days?: number; endDate?: string }): Promise<Response<{ deletedCount: number }>> {
    return post<Response<{ deletedCount: number }>>('/log/clean', params);
  },

  getModules(): Promise<Response<Array<{ value: string; label: string }>>> {
    return get<Response<Array<{ value: string; label: string }>>>('/log/modules');
  },

  getOperations(): Promise<Response<Array<{ value: string; label: string }>>> {
    return get<Response<Array<{ value: string; label: string }>>>('/log/operations');
  },
};
