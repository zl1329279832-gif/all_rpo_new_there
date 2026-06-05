const fs = require("fs");
const path = require("path");

function wp(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

wp("apps/bff/src/modules/message/message.module.ts",
  "import { Module } from '@nestjs/common';\nimport { MessageController } from './message.controller';\nimport { MessageService } from './message.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [MessageController], providers: [MessageService, HttpService] })\nexport class MessageModule {}\n"
);

wp("apps/bff/src/modules/message/message.controller.ts",
  "import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards, Request } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { MessageService } from './message.service';\n" +
  "@Controller('messages')\n@UseGuards(JwtAuthGuard)\nexport class MessageController {\n" +
  "  constructor(private readonly messageService: MessageService) {}\n" +
  "  @Get()\n  async getList(@Query() query: any, @Request() req: any) { return this.messageService.getList(query, req.user.id); }\n" +
  "  @Get('unread-count')\n  async getUnreadCount(@Request() req: any) { return this.messageService.getUnreadCount(req.user.id); }\n" +
  "  @Get(':id')\n  async getDetail(@Param('id') id: number, @Request() req: any) { return this.messageService.getDetail(id, req.user.id); }\n" +
  "  @Post()\n  async create(@Body() body: any) { return this.messageService.create(body); }\n" +
  "  @Put(':id/read')\n  async markAsRead(@Param('id') id: number, @Request() req: any) { return this.messageService.markAsRead(id, req.user.id); }\n" +
  "  @Put('read-all')\n  async markAllAsRead(@Request() req: any) { return this.messageService.markAllAsRead(req.user.id); }\n" +
  "  @Delete(':id')\n  async remove(@Param('id') id: number, @Request() req: any) { return this.messageService.remove(id, req.user.id); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/message/message.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "const messages = [];\n" +
  "const types = ['system', 'todo', 'notification'];\n" +
  "const typeNames = ['系统通知', '待办事项', '普通通知'];\n" +
  "for (let i = 1; i <= 30; i++) {\n" +
  "  messages.push({ id: i, title: typeNames[i % 3] + ' - 消息标题' + i, content: '这是一条' + typeNames[i % 3] + '的消息内容，请及时查看处理。', type: types[i % 3], typeName: typeNames[i % 3], isRead: i % 3 === 0 ? 1 : 0, createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' 10:00:00' });\n" +
  "}\n" +
  "@Injectable()\nexport class MessageService {\n" +
  "  async getList(query: any, userId: number) {\n" +
  "    const page = parseInt(query.page || 1);\n" +
  "    const pageSize = parseInt(query.pageSize || 20);\n" +
  "    let filtered = messages;\n" +
  "    if (query.isRead !== undefined && query.isRead !== '') filtered = messages.filter(m => m.isRead === parseInt(query.isRead));\n" +
  "    if (query.type) filtered = filtered.filter(m => m.type === query.type);\n" +
  "    const start = (page - 1) * pageSize;\n" +
  "    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };\n" +
  "  }\n" +
  "  async getUnreadCount(userId: number) { return { count: messages.filter(m => !m.isRead).length }; }\n" +
  "  async getDetail(id: number, userId: number) { const msg = messages.find(m => m.id === id); if (msg) msg.isRead = 1; return msg; }\n" +
  "  async create(body: any) { messages.push({ id: messages.length + 1, ...body, isRead: 0, createTime: new Date().toISOString() }); return { success: true }; }\n" +
  "  async markAsRead(id: number, userId: number) { const msg = messages.find(m => m.id === id); if (msg) msg.isRead = 1; return { success: true }; }\n" +
  "  async markAllAsRead(userId: number) { messages.forEach(m => m.isRead = 1); return { success: true }; }\n" +
  "  async remove(id: number, userId: number) { const idx = messages.findIndex(m => m.id === id); if (idx > -1) messages.splice(idx, 1); return { success: true }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/log/log.module.ts",
  "import { Module } from '@nestjs/common';\nimport { LogController } from './log.controller';\nimport { LogService } from './log.service';\nimport { OperationLogService } from './operation-log.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [LogController], providers: [LogService, OperationLogService, HttpService], exports: [OperationLogService] })\nexport class LogModule {}\n"
);

wp("apps/bff/src/modules/log/log.controller.ts",
  "import { Controller, Get, Post, Body, Query, UseGuards } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { LogService } from './log.service';\n" +
  "@Controller('logs')\n@UseGuards(JwtAuthGuard)\nexport class LogController {\n" +
  "  constructor(private readonly logService: LogService) {}\n" +
  "  @Get()\n  async getList(@Query() query: any) { return this.logService.getList(query); }\n" +
  "  @Post('export')\n  async exportLogs(@Body() body: any) { return this.logService.exportLogs(body); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/log/log.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "const logs = [];\n" +
  "const modules = ['user', 'order', 'ticket', 'message', 'dashboard'];\n" +
  "const actions = ['创建', '更新', '删除', '查询', '导出'];\n" +
  "const methods = ['GET', 'POST', 'PUT', 'DELETE'];\n" +
  "for (let i = 1; i <= 100; i++) {\n" +
  "  logs.push({ id: i, userId: (i % 4) + 1, username: ['admin', 'manager', 'operator', 'viewer'][i % 4], module: modules[i % 5], action: actions[i % 5], method: methods[i % 4], params: JSON.stringify({ id: i }), ip: '192.168.1.' + (i % 255), userAgent: 'Mozilla/5.0', duration: Math.floor(Math.random() * 500), status: i % 5 === 0 ? 0 : 1, errorMsg: i % 5 === 0 ? '操作失败' : null, createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' ' + String((i % 24)).padStart(2, '0') + ':00:00' });\n" +
  "}\n" +
  "@Injectable()\nexport class LogService {\n" +
  "  async getList(query: any) {\n" +
  "    const page = parseInt(query.page || 1);\n" +
  "    const pageSize = parseInt(query.pageSize || 10);\n" +
  "    const keyword = query.keyword || '';\n" +
  "    let filtered = logs;\n" +
  "    if (keyword) filtered = logs.filter(l => l.username.includes(keyword) || l.module.includes(keyword));\n" +
  "    if (query.status !== undefined && query.status !== '') filtered = filtered.filter(l => l.status === parseInt(query.status));\n" +
  "    const start = (page - 1) * pageSize;\n" +
  "    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };\n" +
  "  }\n" +
  "  async exportLogs(body: any) { return { url: '/export/logs_' + Date.now() + '.xlsx' }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/log/operation-log.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "const operationLogs = [];\n" +
  "@Injectable()\nexport class OperationLogService {\n" +
  "  async create(log: any) { operationLogs.push({ id: operationLogs.length + 1, ...log, createTime: new Date().toISOString() }); }\n" +
  "  async findAll(query: any) { return { list: operationLogs, total: operationLogs.length }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/dashboard/dashboard.module.ts",
  "import { Module } from '@nestjs/common';\nimport { DashboardController } from './dashboard.controller';\nimport { DashboardService } from './dashboard.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [DashboardController], providers: [DashboardService, HttpService] })\nexport class DashboardModule {}\n"
);

wp("apps/bff/src/modules/dashboard/dashboard.controller.ts",
  "import { Controller, Get, UseGuards } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { DashboardService } from './dashboard.service';\n" +
  "@Controller('dashboard')\n@UseGuards(JwtAuthGuard)\nexport class DashboardController {\n" +
  "  constructor(private readonly dashboardService: DashboardService) {}\n" +
  "  @Get('stats')\n  async getStats() { return this.dashboardService.getStats(); }\n" +
  "  @Get('chart/trend')\n  async getTrendChart() { return this.dashboardService.getTrendChart(); }\n" +
  "  @Get('chart/pie')\n  async getPieChart() { return this.dashboardService.getPieChart(); }\n" +
  "  @Get('chart/bar')\n  async getBarChart() { return this.dashboardService.getBarChart(); }\n" +
  "  @Get('activities')\n  async getActivities() { return this.dashboardService.getActivities(); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/dashboard/dashboard.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "@Injectable()\nexport class DashboardService {\n" +
  "  async getStats() { return { totalUsers: 12586, totalOrders: 89456, totalTickets: 3256, totalRevenue: 1258000, todayNewUsers: 128, todayNewOrders: 356, pendingTickets: 158, monthGrowth: 12.5 }; }\n" +
  "  async getTrendChart() {\n" +
  "    const dates = [];\n" +
  "    const userData = [];\n" +
  "    const orderData = [];\n" +
  "    for (let i = 6; i >= 0; i--) {\n" +
  "      const d = new Date(); d.setDate(d.getDate() - i);\n" +
  "      dates.push((d.getMonth() + 1) + '/' + d.getDate());\n" +
  "      userData.push(Math.floor(Math.random() * 200) + 100);\n" +
  "      orderData.push(Math.floor(Math.random() * 500) + 200);\n" +
  "    }\n" +
  "    return { dates, userData, orderData };\n" +
  "  }\n" +
  "  async getPieChart() { return [{ name: '支付宝', value: 45 }, { name: '微信', value: 35 }, { name: '银行卡', value: 20 }]; }\n" +
  "  async getBarChart() { return { categories: ['1月', '2月', '3月', '4月', '5月', '6月'], values: [120, 200, 150, 80, 70, 110] }; }\n" +
  "  async getActivities() {\n" +
  "    return [\n" +
  "      { id: 1, user: 'admin', action: '创建了新用户', time: '5分钟前' },\n" +
  "      { id: 2, user: 'manager', action: '处理了工单', time: '10分钟前' },\n" +
  "      { id: 3, user: 'operator', action: '更新了订单状态', time: '30分钟前' },\n" +
  "      { id: 4, user: 'admin', action: '导出了操作日志', time: '1小时前' },\n" +
  "      { id: 5, user: 'manager', action: '发送了系统通知', time: '2小时前' }\n" +
  "    ];\n" +
  "  }\n" +
  "}\n"
);

wp("apps/bff/src/modules/aggregate/aggregate.module.ts",
  "import { Module } from '@nestjs/common';\nimport { AggregateController } from './aggregate.controller';\nimport { AggregateService } from './aggregate.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [AggregateController], providers: [AggregateService, HttpService] })\nexport class AggregateModule {}\n"
);

wp("apps/bff/src/modules/aggregate/aggregate.controller.ts",
  "import { Controller, Get, UseGuards, Query } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { AggregateService } from './aggregate.service';\n" +
  "@Controller('aggregate')\n@UseGuards(JwtAuthGuard)\nexport class AggregateController {\n" +
  "  constructor(private readonly aggregateService: AggregateService) {}\n" +
  "  @Get('home')\n  async getHomeData() { return this.aggregateService.getHomeData(); }\n" +
  "  @Get('user-detail')\n  async getUserDetail(@Query('id') id: number) { return this.aggregateService.getUserDetail(id); }\n" +
  "  @Get('order-detail')\n  async getOrderDetail(@Query('id') id: number) { return this.aggregateService.getOrderDetail(id); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/aggregate/aggregate.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "@Injectable()\nexport class AggregateService {\n" +
  "  async getHomeData() { return { stats: {}, chart: {}, recentOrders: [], recentTickets: [] }; }\n" +
  "  async getUserDetail(id: number) { return { user: {}, orders: [], tickets: [] }; }\n" +
  "  async getOrderDetail(id: number) { return { order: {}, user: {}, logs: [] }; }\n" +
  "}\n"
);

console.log('=== BFF business modules (message, log, dashboard, aggregate) generated! ===');