import { get } from "@/utils/request";

export function getOverview() {
  return get<{
    totalUsers: number;
    todayNewUsers: number;
    totalOrders: number;
    todayOrders: number;
    totalRevenue: number;
    todayRevenue: number;
    pendingTickets: number;
    unreadMessages: number;
  }>("/dashboard/overview");
}

export function getTrendData(days: number = 7) {
  return get<{
    dates: string[];
    userCounts: number[];
    orderCounts: number[];
    revenues: number[];
  }>(`/dashboard/trend?days=${days}`);
}

export function getOrderDistribution() {
  return get<{ name: string; value: number }[]>("/dashboard/order-distribution");
}

export function getUserDistribution() {
  return get<{ name: string; value: number }[]>("/dashboard/user-distribution");
}

export function getRecentActivities() {
  return get<Array<{
    id: number;
    user: string;
    action: string;
    module: string;
    time: string;
  }>>("/dashboard/recent-activities");
}
