import { get, post, put, del } from './request';
import type { Response, PageResult, PageParams, Ticket } from '@platform/shared-types';

export const ticketApi = {
  getList(params?: PageParams & { type?: string; priority?: string; status?: string; assigneeId?: string; reporterId?: string }): Promise<Response<PageResult<Ticket>>> {
    return get<Response<PageResult<Ticket>>>('/ticket/list', params);
  },

  getDetail(id: string): Promise<Response<Ticket>> {
    return get<Response<Ticket>>(`/ticket/${id}`);
  },

  create(params: Omit<Ticket, 'id' | 'ticketNo' | 'status' | 'createdAt' | 'updatedAt'>): Promise<Response<Ticket>> {
    return post<Response<Ticket>>('/ticket', params);
  },

  update(id: string, params: Partial<Ticket>): Promise<Response<Ticket>> {
    return put<Response<Ticket>>(`/ticket/${id}`, params);
  },

  remove(id: string): Promise<Response<void>> {
    return del<Response<void>>(`/ticket/${id}`);
  },

  updateStatus(id: string, status: Ticket['status'], remark?: string): Promise<Response<void>> {
    return put<Response<void>>(`/ticket/${id}/status`, { status, remark });
  },

  assign(id: string, assigneeId: string, assigneeName: string, remark?: string): Promise<Response<void>> {
    return post<Response<void>>(`/ticket/${id}/assign`, { assigneeId, assigneeName, remark });
  },

  resolve(id: string, params: { resolution: string; remark?: string }): Promise<Response<void>> {
    return post<Response<void>>(`/ticket/${id}/resolve`, params);
  },

  close(id: string, reason: string): Promise<Response<void>> {
    return post<Response<void>>(`/ticket/${id}/close`, { reason });
  },

  addComment(id: string, params: { content: string; attachments?: string[] }): Promise<Response<{
    id: string;
    userId: string;
    userName: string;
    content: string;
    createdAt: string;
  }>> {
    return post<Response<{
      id: string;
      userId: string;
      userName: string;
      content: string;
      createdAt: string;
    }>>(`/ticket/${id}/comments`, params);
  },

  getComments(id: string): Promise<Response<Array<{
    id: string;
    userId: string;
    userName: string;
    content: string;
    attachments?: string[];
    createdAt: string;
  }>>> {
    return get<Response<Array<{
      id: string;
      userId: string;
      userName: string;
      content: string;
      attachments?: string[];
      createdAt: string;
    }>>>(`/ticket/${id}/comments`);
  },

  getStatistics(params?: { startDate?: string; endDate?: string }): Promise<Response<{
    totalCount: number;
    pendingCount: number;
    processingCount: number;
    resolvedCount: number;
    closedCount: number;
    avgResolutionTime: number;
    satisfactionRate: number;
  }>> {
    return get<Response<{
      totalCount: number;
      pendingCount: number;
      processingCount: number;
      resolvedCount: number;
      closedCount: number;
      avgResolutionTime: number;
      satisfactionRate: number;
    }>>('/ticket/statistics', params);
  },
};
