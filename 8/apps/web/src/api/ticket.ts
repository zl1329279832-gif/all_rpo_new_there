import { get, post, put } from "@/utils/request";
import type { Ticket, PageParams, PageResult } from "@/types";

export function getTicketList(params: PageParams & { type?: string; priority?: string }) {
  return get<PageResult<Ticket>>("/tickets", params);
}

export function getTicketDetail(id: number) {
  return get<Ticket>(`/tickets/${id}`);
}

export function createTicket(data: Partial<Ticket>) {
  return post("/tickets", data);
}

export function updateTicketStatus(id: number, status: number, remark?: string) {
  return put(`/tickets/${id}/status`, { status, remark });
}

export function assignTicket(id: number, handler: string) {
  return put(`/tickets/${id}/assign`, { handler });
}

export function getTicketStatistics() {
  return get<{
    pendingCount: number;
    processingCount: number;
    resolvedCount: number;
    closedCount: number;
    todayCount: number;
  }>("/tickets/statistics");
}
