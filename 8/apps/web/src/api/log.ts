import { get, post } from "@/utils/request";
import type { OperationLog, PageParams, PageResult } from "@/types";

export function getLogList(params: PageParams & { module?: string; startDate?: string; endDate?: string }) {
  return get<PageResult<OperationLog>>("/logs", params);
}

export function getLogDetail(id: number) {
  return get<OperationLog>(`/logs/${id}`);
}

export function exportLogs(params: any) {
  return post("/logs/export", params);
}

export function getLogStatistics() {
  return get<{
    todayCount: number;
    weekCount: number;
    monthCount: number;
    successRate: number;
    topModules: { module: string; count: number }[];
  }>("/logs/statistics");
}
