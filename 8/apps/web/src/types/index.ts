export interface User {
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
