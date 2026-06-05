import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { HttpService } from '../../common/services/http.service';
const mockUsers: any = {
  admin: { id: 1, username: 'admin', password: '123456', role: 'admin', roleName: '超级管理员', realName: '管理员', department: '技术部', email: 'admin@example.com', phone: '13800138000', avatar: '', permissions: ['*'] },
  manager: { id: 2, username: 'manager', password: '123456', role: 'manager', roleName: '运营经理', realName: '张经理', department: '运营部', email: 'manager@example.com', phone: '13800138001', avatar: '', permissions: ['user:view', 'user:create', 'user:update', 'order:view', 'order:create', 'order:update', 'ticket:view', 'ticket:create', 'ticket:update', 'message:view', 'message:send', 'log:view', 'log:export', 'dashboard:view'] },
  operator: { id: 3, username: 'operator', password: '123456', role: 'operator', roleName: '运营专员', realName: '李专员', department: '运营部', email: 'operator@example.com', phone: '13800138002', avatar: '', permissions: ['user:view', 'order:view', 'order:create', 'ticket:view', 'ticket:create', 'message:view', 'log:view', 'dashboard:view'] },
  viewer: { id: 4, username: 'viewer', password: '123456', role: 'viewer', roleName: '访客', realName: '王访客', department: '访客', email: 'viewer@example.com', phone: '13800138003', avatar: '', permissions: ['user:view', 'order:view', 'ticket:view', 'message:view', 'dashboard:view'] }
};
const mockMenus: any = {
  admin: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }, { id: 6, path: '/logs', name: 'Logs', title: '操作日志', icon: 'Document', component: 'logs/index' }],
  manager: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }, { id: 6, path: '/logs', name: 'Logs', title: '操作日志', icon: 'Document', component: 'logs/index' }],
  operator: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }, { id: 6, path: '/logs', name: 'Logs', title: '操作日志', icon: 'Document', component: 'logs/index' }],
  viewer: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }]
};
@Injectable()
export class AuthService {
  constructor(private jwtService: JwtService, private httpService: HttpService) {}
  async login(username: string, password: string) {
    const user = mockUsers[username];
    if (!user || user.password !== password) throw new UnauthorizedException('用户名或密码错误');
    const payload = { id: user.id, username: user.username, role: user.role, permissions: user.permissions };
    const token = this.jwtService.sign(payload);
    const refreshToken = this.jwtService.sign(payload, { expiresIn: '7d' });
    const { password: _, ...userInfo } = user;
    return { token, refreshToken, userInfo };
  }
  async refreshToken(token: string) {
    try {
      const decoded = this.jwtService.verify(token);
      const payload = { id: decoded.id, username: decoded.username, role: decoded.role, permissions: decoded.permissions };
      return { token: this.jwtService.sign(payload) };
    } catch (e) {
      throw new UnauthorizedException('Token 已过期');
    }
  }
  async getUserInfo(userId: number) {
    const user = Object.values(mockUsers).find((u: any) => u.id === userId);
    if (!user) throw new UnauthorizedException('用户不存在');
    const { password: _, ...userInfo } = user as any;
    return userInfo;
  }
  async getMenus(role: string) { return mockMenus[role] || mockMenus.viewer; }
  async getPermissions(userId: number) {
    const user = Object.values(mockUsers).find((u: any) => u.id === userId);
    return (user as any)?.permissions || [];
  }
  async logout(userId: number) { return { success: true, message: '退出成功' }; }
}
