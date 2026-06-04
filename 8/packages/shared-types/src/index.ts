export interface User {
  id: string;
  username: string;
  realName: string;
  avatar: string;
  email: string;
  phone: string;
  department: string;
  roles: string[];
  permissions: string[];
  status: 'active' | 'disabled';
  createdAt: string;
  lastLoginAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  captcha?: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

export interface MenuItem {
  id: string;
  parentId: string | null;
  name: string;
  path: string;
  component: string;
  icon: string;
  sort: number;
  permission?: string;
  children?: MenuItem[];
  appId?: string;
  status: 'visible' | 'hidden';
}

export interface AppInfo {
  id: string;
  name: string;
  code: string;
  icon: string;
  description: string;
  baseRoute: string;
  status: 'active' | 'inactive';
}

export interface Response<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  traceId?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface PageParams {
  page?: number;
  pageSize?: number;
  keyword?: string;
  [key: string]: any;
}

export interface OperationLog {
  id: string;
  userId: string;
  username: string;
  module: string;
  operation: string;
  method: string;
  params: string;
  ip: string;
  location: string;
  duration: number;
  status: 'success' | 'fail';
  errorMsg?: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  userId: string;
  title: string;
  content: string;
  type: 'system' | 'business' | 'warning' | 'error';
  read: boolean;
  source: string;
  createdAt: string;
}

export interface Order {
  id: string;
  orderNo: string;
  customerName: string;
  customerPhone: string;
  totalAmount: number;
  payAmount: number;
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled' | 'refunded';
  paymentMethod: string;
  items: OrderItem[];
  address: string;
  remark?: string;
  createdAt: string;
  paidAt?: string;
  shippedAt?: string;
  completedAt?: string;
}

export interface OrderItem {
  id: string;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Ticket {
  id: string;
  ticketNo: string;
  title: string;
  description: string;
  type: 'bug' | 'feature' | 'consult' | 'complaint';
  priority: 'low' | 'medium' | 'high' | 'urgent';
  status: 'pending' | 'processing' | 'resolved' | 'closed';
  reporterId: string;
  reporterName: string;
  assigneeId?: string;
  assigneeName?: string;
  department: string;
  tags: string[];
  attachments?: string[];
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
}

export interface DashboardStats {
  totalUsers: number;
  totalOrders: number;
  totalRevenue: number;
  totalTickets: number;
  todayUsers: number;
  todayOrders: number;
  todayRevenue: number;
  pendingTickets: number;
  orderTrend: TrendItem[];
  userTrend: TrendItem[];
  revenueTrend: TrendItem[];
  ticketTrend: TrendItem[];
  topProducts: ProductRank[];
  orderDistribution: { name: string; value: number }[];
  ticketDistribution: { name: string; value: number }[];
}

export interface TrendItem {
  date: string;
  value: number;
}

export interface ProductRank {
  name: string;
  sales: number;
  amount: number;
}

export interface GlobalState {
  user: User | null;
  token: string | null;
  menus: MenuItem[];
  apps: AppInfo[];
  currentApp: AppInfo | null;
  filters: Record<string, any>;
  notifications: Notification[];
  unreadCount: number;
  theme: 'light' | 'dark';
  collapsed: boolean;
}
