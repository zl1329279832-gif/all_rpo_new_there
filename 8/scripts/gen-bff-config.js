const fs = require("fs");
const path = require("path");

function writeFile(filePath, content) {
  const fullPath = path.join(process.cwd(), filePath);
  const dir = path.dirname(fullPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  fs.writeFileSync(fullPath, content, "utf8");
  console.log("Created:", filePath);
}

// ========== Types ==========
writeFile("apps/web/src/types/index.ts", `export interface User {
  id: number;
  username: string;
  realName: string;
  email: string;
  phone: string;
  avatar: string;
  role: string;
  roleName: string;
  department: string;
  status: number;
  createTime: string;
}

export interface MenuItem {
  id: number;
  name: string;
  path: string;
  icon: string;
  parentId: number;
  sort: number;
  children?: MenuItem[];
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  user: User;
  permissions: string[];
  menus: MenuItem[];
}

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface Order {
  id: number;
  orderNo: string;
  userId: number;
  userName: string;
  amount: number;
  status: number;
  statusName: string;
  payType: string;
  createTime: string;
  updateTime: string;
}

export interface Ticket {
  id: number;
  ticketNo: string;
  title: string;
  content: string;
  type: string;
  typeName: string;
  priority: string;
  priorityName: string;
  status: number;
  statusName: string;
  creator: string;
  handler: string;
  createTime: string;
  updateTime: string;
}

export interface Message {
  id: number;
  title: string;
  content: string;
  type: string;
  typeName: string;
  isRead: number;
  createTime: string;
}

export interface OperationLog {
  id: number;
  userId: number;
  username: string;
  module: string;
  action: string;
  method: string;
  params: string;
  ip: string;
  userAgent: string;
  duration: number;
  status: number;
  errorMsg: string;
  createTime: string;
}

export interface PageParams {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: number;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}
`);

// ========== HTTP Request ==========
writeFile("apps/web/src/utils/request.ts", `import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
import router from "@/router";
import type { ApiResponse } from "@/types";

const service: AxiosInstance = axios.create({
  baseURL: "/api",
  timeout: 15000,
  headers: {
    "Content-Type": "application/json;charset=utf-8"
  }
});

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = \`Bearer \${userStore.token}\`;
    }
    return config;
  },
  (error) => {
    console.error("Request error:", error);
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data;
    
    if (res.code !== 200) {
      ElMessage.error(res.message || "请求失败");
      
      if (res.code === 401) {
        ElMessageBox.confirm("登录状态已过期，请重新登录", "提示", {
          confirmButtonText: "重新登录",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          const userStore = useUserStore();
          userStore.clearUser();
          router.push("/login");
        });
      }
      
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    
    return res;
  },
  (error) => {
    console.error("Response error:", error);
    
    if (error.response) {
      const status = error.response.status;
      const messages: Record<number, string> = {
        400: "请求参数错误",
        401: "未授权，请重新登录",
        403: "拒绝访问",
        404: "请求地址不存在",
        500: "服务器内部错误",
        502: "网关错误",
        503: "服务不可用",
        504: "网关超时"
      };
      
      const message = messages[status] || error.message || "网络错误";
      
      if (status === 401) {
        ElMessageBox.confirm("登录状态已过期，请重新登录", "提示", {
          confirmButtonText: "重新登录",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          const userStore = useUserStore();
          userStore.clearUser();
          router.push("/login");
        });
      } else {
        ElMessage.error(message);
      }
    } else if (error.code === "ECONNABORTED") {
      ElMessage.error("请求超时，请稍后重试");
    } else {
      ElMessage.error("网络错误，请检查网络连接");
    }
    
    return Promise.reject(error);
  }
);

export function request<T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service(config) as unknown as Promise<ApiResponse<T>>;
}

export function get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
  return request({ method: "GET", url, params });
}

export function post<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
  return request({ method: "POST", url, data });
}

export function put<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
  return request({ method: "PUT", url, data });
}

export function del<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
  return request({ method: "DELETE", url, params });
}

export default service;
`);

// ========== User API ==========
writeFile("apps/web/src/api/user.ts", `import { get, post } from "@/utils/request";
import type { LoginParams, LoginResult, User, PageParams, PageResult } from "@/types";

export function login(params: LoginParams) {
  return post<LoginResult>("/auth/login", params);
}

export function getUserInfo() {
  return get<LoginResult>("/auth/userinfo");
}

export function logout() {
  return post("/auth/logout");
}

export function refreshToken() {
  return post<{ token: string }>("/auth/refresh");
}

export function getUserList(params: PageParams) {
  return get<PageResult<User>>("/users", params);
}

export function getUserDetail(id: number) {
  return get<User>(\`/users/\${id}\`);
}

export function createUser(data: Partial<User>) {
  return post("/users", data);
}

export function updateUser(id: number, data: Partial<User>) {
  return put(\`/users/\${id}\`, data);
}

export function deleteUser(id: number) {
  return del(\`/users/\${id}\`);
}

export function updateUserStatus(id: number, status: number) {
  return put(\`/users/\${id}/status\`, { status });
}
`);

// ========== Order API ==========
writeFile("apps/web/src/api/order.ts", `import { get, post, put } from "@/utils/request";
import type { Order, PageParams, PageResult } from "@/types";

export function getOrderList(params: PageParams & { startDate?: string; endDate?: string }) {
  return get<PageResult<Order>>("/orders", params);
}

export function getOrderDetail(id: number) {
  return get<Order>(\`/orders/\${id}\`);
}

export function createOrder(data: Partial<Order>) {
  return post("/orders", data);
}

export function updateOrderStatus(id: number, status: number) {
  return put(\`/orders/\${id}/status\`, { status });
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
`);

// ========== Ticket API ==========
writeFile("apps/web/src/api/ticket.ts", `import { get, post, put } from "@/utils/request";
import type { Ticket, PageParams, PageResult } from "@/types";

export function getTicketList(params: PageParams & { type?: string; priority?: string }) {
  return get<PageResult<Ticket>>("/tickets", params);
}

export function getTicketDetail(id: number) {
  return get<Ticket>(\`/tickets/\${id}\`);
}

export function createTicket(data: Partial<Ticket>) {
  return post("/tickets", data);
}

export function updateTicketStatus(id: number, status: number, remark?: string) {
  return put(\`/tickets/\${id}/status\`, { status, remark });
}

export function assignTicket(id: number, handler: string) {
  return put(\`/tickets/\${id}/assign\`, { handler });
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
`);

// ========== Message API ==========
writeFile("apps/web/src/api/message.ts", `import { get, post, put } from "@/utils/request";
import type { Message, PageParams, PageResult } from "@/types";

export function getMessageList(params: PageParams & { type?: string; isRead?: number }) {
  return get<PageResult<Message>>("/messages", params);
}

export function getUnreadCount() {
  return get<{ count: number }>("/messages/unread/count");
}

export function markAsRead(id: number) {
  return put(\`/messages/\${id}/read\`);
}

export function markAllAsRead() {
  return put("/messages/read-all");
}

export function sendMessage(data: { title: string; content: string; type: string; userIds: number[] }) {
  return post("/messages", data);
}
`);

// ========== Log API ==========
writeFile("apps/web/src/api/log.ts", `import { get, post } from "@/utils/request";
import type { OperationLog, PageParams, PageResult } from "@/types";

export function getLogList(params: PageParams & { module?: string; startDate?: string; endDate?: string }) {
  return get<PageResult<OperationLog>>("/logs", params);
}

export function getLogDetail(id: number) {
  return get<OperationLog>(\`/logs/\${id}\`);
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
`);

// ========== Dashboard API ==========
writeFile("apps/web/src/api/dashboard.ts", `import { get } from "@/utils/request";

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
  }>(\`/dashboard/trend?days=\${days}\`);
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
`);

console.log("=== API layer generated! ===");
