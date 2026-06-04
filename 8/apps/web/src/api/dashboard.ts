import { get, post } from './request';
import type { Response, DashboardStats } from '@platform/shared-types';

export const dashboardApi = {
  getStats(params?: { startDate?: string; endDate?: string }): Promise<Response<DashboardStats>> {
    return get<Response<DashboardStats>>('/dashboard/stats', params);
  },

  getOverview(params?: { startDate?: string; endDate?: string }): Promise<Response<{
    totalUsers: number;
    totalOrders: number;
    totalRevenue: number;
    totalTickets: number;
    userGrowth: number;
    orderGrowth: number;
    revenueGrowth: number;
    ticketGrowth: number;
  }>> {
    return get<Response<{
      totalUsers: number;
      totalOrders: number;
      totalRevenue: number;
      totalTickets: number;
      userGrowth: number;
      orderGrowth: number;
      revenueGrowth: number;
      ticketGrowth: number;
    }>>('/dashboard/overview', params);
  },

  getTrendData(params: { type: 'user' | 'order' | 'revenue' | 'ticket'; period?: 'day' | 'week' | 'month'; startDate?: string; endDate?: string }): Promise<Response<Array<{ date: string; value: number }>>> {
    return get<Response<Array<{ date: string; value: number }>>>('/dashboard/trend', params);
  },

  getDistribution(params: { type: 'order' | 'ticket' | 'user'; startDate?: string; endDate?: string }): Promise<Response<Array<{ name: string; value: number }>>> {
    return get<Response<Array<{ name: string; value: number }>>>('/dashboard/distribution', params);
  },

  getTopProducts(params?: { limit?: number; startDate?: string; endDate?: string }): Promise<Response<Array<{ name: string; sales: number; amount: number }>>> {
    return get<Response<Array<{ name: string; sales: number; amount: number }>>>('/dashboard/top-products', params);
  },

  getRecentActivities(params?: { limit?: number }): Promise<Response<Array<{
    id: string;
    type: string;
    title: string;
    description: string;
    time: string;
    user: { name: string; avatar: string };
  }>>> {
    return get<Response<Array<{
      id: string;
      type: string;
      title: string;
      description: string;
      time: string;
      user: { name: string; avatar: string };
    }>>>('/dashboard/recent-activities', params);
  },

  refresh(): Promise<Response<DashboardStats>> {
    return post<Response<DashboardStats>>('/dashboard/refresh');
  },
};
