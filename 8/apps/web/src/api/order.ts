import { get, post, put, del } from './request';
import type { Response, PageResult, PageParams, Order } from '@platform/shared-types';

export const orderApi = {
  getList(params?: PageParams & { status?: string; startDate?: string; endDate?: string; payMethod?: string }): Promise<Response<PageResult<Order>>> {
    return get<Response<PageResult<Order>>>('/order/list', params);
  },

  getDetail(id: string): Promise<Response<Order>> {
    return get<Response<Order>>(`/order/${id}`);
  },

  create(params: Partial<Order>): Promise<Response<Order>> {
    return post<Response<Order>>('/order', params);
  },

  update(id: string, params: Partial<Order>): Promise<Response<Order>> {
    return put<Response<Order>>(`/order/${id}`, params);
  },

  remove(id: string): Promise<Response<void>> {
    return del<Response<void>>(`/order/${id}`);
  },

  updateStatus(id: string, status: Order['status'], remark?: string): Promise<Response<void>> {
    return put<Response<void>>(`/order/${id}/status`, { status, remark });
  },

  ship(id: string, params: { company: string; trackingNo: string; remark?: string }): Promise<Response<void>> {
    return post<Response<void>>(`/order/${id}/ship`, params);
  },

  refund(id: string, params: { amount: number; reason: string; remark?: string }): Promise<Response<void>> {
    return post<Response<void>>(`/order/${id}/refund`, params);
  },

  export(params?: PageParams): Promise<Response<{ url: string }>> {
    return post<Response<{ url: string }>>('/order/export', params);
  },

  getStatistics(params?: { startDate?: string; endDate?: string }): Promise<Response<{
    totalCount: number;
    totalAmount: number;
    paidCount: number;
    paidAmount: number;
    shippedCount: number;
    completedCount: number;
    cancelledCount: number;
    refundedCount: number;
    refundedAmount: number;
  }>> {
    return get<Response<{
      totalCount: number;
      totalAmount: number;
      paidCount: number;
      paidAmount: number;
      shippedCount: number;
      completedCount: number;
      cancelledCount: number;
      refundedCount: number;
      refundedAmount: number;
    }>>('/order/statistics', params);
  },
};
