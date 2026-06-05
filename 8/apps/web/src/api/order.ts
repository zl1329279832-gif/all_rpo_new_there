import { get, post, put } from "@/utils/request";
import type { Order, PageParams, PageResult } from "@/types";

export function getOrderList(params: PageParams & { startDate?: string; endDate?: string }) {
  return get<PageResult<Order>>("/orders", params);
}

export function getOrderDetail(id: number) {
  return get<Order>(`/orders/${id}`);
}

export function createOrder(data: Partial<Order>) {
  return post("/orders", data);
}

export function updateOrderStatus(id: number, status: number) {
  return put(`/orders/${id}/status`, { status });
}

export function getOrderStatistics() {
  return get<{
    todayCount: number;
    yesterdayCount: number;
    weekCount: number;
    monthCount: number;
    totalAmount: number;
    pendingCount: number;
    paidCount: number;
    shippedCount: number;
    completedCount: number;
    cancelledCount: number;
  }>("/orders/statistics");
}
