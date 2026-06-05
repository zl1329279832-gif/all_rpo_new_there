const fs = require("fs");
const path = require("path");

function wp(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

const mockUsers = "const mockUsers: any = {\n" +
  "  admin: { id: 1, username: 'admin', password: '123456', role: 'admin', roleName: '超级管理员', realName: '管理员', department: '技术部', email: 'admin@example.com', phone: '13800138000', avatar: '', permissions: ['*'] },\n" +
  "  manager: { id: 2, username: 'manager', password: '123456', role: 'manager', roleName: '运营经理', realName: '张经理', department: '运营部', email: 'manager@example.com', phone: '13800138001', avatar: '', permissions: ['user:view', 'user:create', 'user:update', 'order:view', 'order:create', 'order:update', 'ticket:view', 'ticket:create', 'ticket:update', 'message:view', 'message:send', 'log:view', 'log:export', 'dashboard:view'] },\n" +
  "  operator: { id: 3, username: 'operator', password: '123456', role: 'operator', roleName: '运营专员', realName: '李专员', department: '运营部', email: 'operator@example.com', phone: '13800138002', avatar: '', permissions: ['user:view', 'order:view', 'order:create', 'ticket:view', 'ticket:create', 'message:view', 'log:view', 'dashboard:view'] },\n" +
  "  viewer: { id: 4, username: 'viewer', password: '123456', role: 'viewer', roleName: '访客', realName: '王访客', department: '访客', email: 'viewer@example.com', phone: '13800138003', avatar: '', permissions: ['user:view', 'order:view', 'ticket:view', 'message:view', 'dashboard:view'] }\n" +
  "};\n";

const mockMenus = "const mockMenus: any = {\n" +
  "  admin: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }, { id: 6, path: '/logs', name: 'Logs', title: '操作日志', icon: 'Document', component: 'logs/index' }],\n" +
  "  manager: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }, { id: 6, path: '/logs', name: 'Logs', title: '操作日志', icon: 'Document', component: 'logs/index' }],\n" +
  "  operator: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }, { id: 6, path: '/logs', name: 'Logs', title: '操作日志', icon: 'Document', component: 'logs/index' }],\n" +
  "  viewer: [{ id: 1, path: '/dashboard', name: 'Dashboard', title: '指标看板', icon: 'DataAnalysis', component: 'dashboard/index' }, { id: 2, path: '/users', name: 'Users', title: '用户管理', icon: 'User', component: 'users/index' }, { id: 3, path: '/orders', name: 'Orders', title: '订单管理', icon: 'ShoppingCart', component: 'orders/index' }, { id: 4, path: '/tickets', name: 'Tickets', title: '工单管理', icon: 'Tickets', component: 'tickets/index' }, { id: 5, path: '/messages', name: 'Messages', title: '消息中心', icon: 'Bell', component: 'messages/index' }]\n" +
  "};\n";

wp("apps/bff/src/modules/auth/auth.module.ts",
  "import { Module } from '@nestjs/common';\n" +
  "import { JwtModule } from '@nestjs/jwt';\n" +
  "import { PassportModule } from '@nestjs/passport';\n" +
  "import { AuthService } from './auth.service';\n" +
  "import { AuthController } from './auth.controller';\n" +
  "import { JwtStrategy } from './jwt.strategy';\n" +
  "import { HttpService } from '../../common/services/http.service';\n" +
  "@Module({\n" +
  "  imports: [\n" +
  "    PassportModule,\n" +
  "    JwtModule.register({ secret: process.env.JWT_SECRET || 'platform-secret', signOptions: { expiresIn: '2h' }})\n" +
  "  ],\n" +
  "  providers: [AuthService, JwtStrategy, HttpService],\n" +
  "  controllers: [AuthController]\n" +
  "})\n" +
  "export class AuthModule {}\n"
);

wp("apps/bff/src/modules/auth/auth.controller.ts",
  "import { Controller, Post, Body, UseGuards, Request, Get } from '@nestjs/common';\n" +
  "import { AuthService } from './auth.service';\n" +
  "import { JwtAuthGuard } from './jwt-auth.guard';\n" +
  "@Controller('auth')\n" +
  "export class AuthController {\n" +
  "  constructor(private readonly authService: AuthService) {}\n" +
  "  @Post('login')\n" +
  "  async login(@Body() body: { username: string; password: string }) { return this.authService.login(body.username, body.password); }\n" +
  "  @Post('refresh')\n" +
  "  async refreshToken(@Body() body: { token: string }) { return this.authService.refreshToken(body.token); }\n" +
  "  @UseGuards(JwtAuthGuard)\n" +
  "  @Get('userinfo')\n" +
  "  async getUserInfo(@Request() req: any) { return this.authService.getUserInfo(req.user.id); }\n" +
  "  @UseGuards(JwtAuthGuard)\n" +
  "  @Get('menus')\n" +
  "  async getMenus(@Request() req: any) { return this.authService.getMenus(req.user.role); }\n" +
  "  @UseGuards(JwtAuthGuard)\n" +
  "  @Get('permissions')\n" +
  "  async getPermissions(@Request() req: any) { return this.authService.getPermissions(req.user.id); }\n" +
  "  @UseGuards(JwtAuthGuard)\n" +
  "  @Post('logout')\n" +
  "  async logout(@Request() req: any) { return this.authService.logout(req.user.id); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/auth/auth.service.ts",
  "import { Injectable, UnauthorizedException } from '@nestjs/common';\n" +
  "import { JwtService } from '@nestjs/jwt';\n" +
  "import { HttpService } from '../../common/services/http.service';\n" +
  mockUsers +
  mockMenus +
  "@Injectable()\n" +
  "export class AuthService {\n" +
  "  constructor(private jwtService: JwtService, private httpService: HttpService) {}\n" +
  "  async login(username: string, password: string) {\n" +
  "    const user = mockUsers[username];\n" +
  "    if (!user || user.password !== password) throw new UnauthorizedException('用户名或密码错误');\n" +
  "    const payload = { id: user.id, username: user.username, role: user.role, permissions: user.permissions };\n" +
  "    const token = this.jwtService.sign(payload);\n" +
  "    const refreshToken = this.jwtService.sign(payload, { expiresIn: '7d' });\n" +
  "    const { password: _, ...userInfo } = user;\n" +
  "    return { token, refreshToken, userInfo };\n" +
  "  }\n" +
  "  async refreshToken(token: string) {\n" +
  "    try {\n" +
  "      const decoded = this.jwtService.verify(token);\n" +
  "      const payload = { id: decoded.id, username: decoded.username, role: decoded.role, permissions: decoded.permissions };\n" +
  "      return { token: this.jwtService.sign(payload) };\n" +
  "    } catch (e) {\n" +
  "      throw new UnauthorizedException('Token 已过期');\n" +
  "    }\n" +
  "  }\n" +
  "  async getUserInfo(userId: number) {\n" +
  "    const user = Object.values(mockUsers).find((u: any) => u.id === userId);\n" +
  "    if (!user) throw new UnauthorizedException('用户不存在');\n" +
  "    const { password: _, ...userInfo } = user as any;\n" +
  "    return userInfo;\n" +
  "  }\n" +
  "  async getMenus(role: string) { return mockMenus[role] || mockMenus.viewer; }\n" +
  "  async getPermissions(userId: number) {\n" +
  "    const user = Object.values(mockUsers).find((u: any) => u.id === userId);\n" +
  "    return (user as any)?.permissions || [];\n" +
  "  }\n" +
  "  async logout(userId: number) { return { success: true, message: '退出成功' }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/auth/jwt.strategy.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "import { PassportStrategy } from '@nestjs/passport';\n" +
  "import { ExtractJwt, Strategy } from 'passport-jwt';\n" +
  "@Injectable()\n" +
  "export class JwtStrategy extends PassportStrategy(Strategy) {\n" +
  "  constructor() {\n" +
  "    super({ jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(), ignoreExpiration: false, secretOrKey: process.env.JWT_SECRET || 'platform-secret' });\n" +
  "  }\n" +
  "  async validate(payload: any) { return { id: payload.id, username: payload.username, role: payload.role, permissions: payload.permissions }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/auth/jwt-auth.guard.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "import { AuthGuard } from '@nestjs/passport';\n" +
  "@Injectable()\n" +
  "export class JwtAuthGuard extends AuthGuard('jwt') {}\n"
);

console.log('=== BFF Auth module generated! ===');