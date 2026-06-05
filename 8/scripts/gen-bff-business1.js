const fs = require("fs");
const path = require("path");

function wp(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

function genMockList(count, prefix, extra) {
  const list = [];
  for (let i = 1; i <= count; i++) {
    list.push({ id: i, ...extra(i) });
  }
  return JSON.stringify({ list, total: count });
}

const now = new Date().toISOString();

wp("apps/bff/src/modules/user/user.module.ts",
  "import { Module } from '@nestjs/common';\nimport { UserController } from './user.controller';\nimport { UserService } from './user.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [UserController], providers: [UserService, HttpService] })\nexport class UserModule {}\n"
);

wp("apps/bff/src/modules/user/user.controller.ts",
  "import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { UserService } from './user.service';\n" +
  "@Controller('users')\n@UseGuards(JwtAuthGuard)\nexport class UserController {\n" +
  "  constructor(private readonly userService: UserService) {}\n" +
  "  @Get()\n  async getList(@Query() query: any) { return this.userService.getList(query); }\n" +
  "  @Get(':id')\n  async getDetail(@Param('id') id: number) { return this.userService.getDetail(id); }\n" +
  "  @Post()\n  async create(@Body() body: any) { return this.userService.create(body); }\n" +
  "  @Put(':id')\n  async update(@Param('id') id: number, @Body() body: any) { return this.userService.update(id, body); }\n" +
  "  @Delete(':id')\n  async remove(@Param('id') id: number) { return this.userService.remove(id); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/user/user.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "const users = [];\n" +
  "for (let i = 1; i <= 50; i++) {\n" +
  "  users.push({ id: i, username: 'user' + i, realName: '用户' + i, email: 'user' + i + '@example.com', phone: '13800' + String(i).padStart(5, '0'), role: i % 4 === 1 ? 'admin' : i % 4 === 2 ? 'manager' : i % 4 === 3 ? 'operator' : 'viewer', roleName: i % 4 === 1 ? '超级管理员' : i % 4 === 2 ? '运营经理' : i % 4 === 3 ? '运营专员' : '访客', department: i % 2 === 1 ? '技术部' : '运营部', status: i % 5 === 0 ? 0 : 1, createTime: '2024-01-0' + ((i % 9) + 1) + ' 10:00:00' });\n" +
  "}\n" +
  "@Injectable()\nexport class UserService {\n" +
  "  async getList(query: any) {\n" +
  "    const page = parseInt(query.page || 1);\n" +
  "    const pageSize = parseInt(query.pageSize || 10);\n" +
  "    const keyword = query.keyword || '';\n" +
  "    let filtered = users;\n" +
  "    if (keyword) filtered = users.filter(u => u.realName.includes(keyword) || u.username.includes(keyword));\n" +
  "    const start = (page - 1) * pageSize;\n" +
  "    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };\n" +
  "  }\n" +
  "  async getDetail(id: number) { return users.find(u => u.id === id); }\n" +
  "  async create(body: any) { users.push({ id: users.length + 1, ...body, createTime: new Date().toISOString() }); return { success: true }; }\n" +
  "  async update(id: number, body: any) { const idx = users.findIndex(u => u.id === id); if (idx > -1) users[idx] = { ...users[idx], ...body }; return { success: true }; }\n" +
  "  async remove(id: number) { const idx = users.findIndex(u => u.id === id); if (idx > -1) users.splice(idx, 1); return { success: true }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/order/order.module.ts",
  "import { Module } from '@nestjs/common';\nimport { OrderController } from './order.controller';\nimport { OrderService } from './order.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [OrderController], providers: [OrderService, HttpService] })\nexport class OrderModule {}\n"
);

wp("apps/bff/src/modules/order/order.controller.ts",
  "import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { OrderService } from './order.service';\n" +
  "@Controller('orders')\n@UseGuards(JwtAuthGuard)\nexport class OrderController {\n" +
  "  constructor(private readonly orderService: OrderService) {}\n" +
  "  @Get()\n  async getList(@Query() query: any) { return this.orderService.getList(query); }\n" +
  "  @Get(':id')\n  async getDetail(@Param('id') id: number) { return this.orderService.getDetail(id); }\n" +
  "  @Post()\n  async create(@Body() body: any) { return this.orderService.create(body); }\n" +
  "  @Put(':id')\n  async update(@Param('id') id: number, @Body() body: any) { return this.orderService.update(id, body); }\n" +
  "  @Delete(':id')\n  async remove(@Param('id') id: number) { return this.orderService.remove(id); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/order/order.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "const orders = [];\n" +
  "const statuses = ['待支付', '已支付', '已发货', '已完成', '已取消'];\n" +
  "for (let i = 1; i <= 100; i++) {\n" +
  "  orders.push({ id: i, orderNo: 'ORD' + String(i).padStart(8, '0'), amount: (Math.random() * 10000 + 100).toFixed(2), status: i % 5, statusName: statuses[i % 5], payMethod: i % 3 === 1 ? '支付宝' : i % 3 === 2 ? '微信' : '银行卡', createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' ' + String((i % 24)).padStart(2, '0') + ':00:00' });\n" +
  "}\n" +
  "@Injectable()\nexport class OrderService {\n" +
  "  async getList(query: any) {\n" +
  "    const page = parseInt(query.page || 1);\n" +
  "    const pageSize = parseInt(query.pageSize || 10);\n" +
  "    const keyword = query.keyword || '';\n" +
  "    let filtered = orders;\n" +
  "    if (keyword) filtered = orders.filter(o => o.orderNo.includes(keyword));\n" +
  "    if (query.status !== undefined && query.status !== '') filtered = filtered.filter(o => o.status === parseInt(query.status));\n" +
  "    const start = (page - 1) * pageSize;\n" +
  "    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };\n" +
  "  }\n" +
  "  async getDetail(id: number) { return orders.find(o => o.id === id); }\n" +
  "  async create(body: any) { orders.push({ id: orders.length + 1, ...body, createTime: new Date().toISOString() }); return { success: true }; }\n" +
  "  async update(id: number, body: any) { const idx = orders.findIndex(o => o.id === id); if (idx > -1) orders[idx] = { ...orders[idx], ...body }; return { success: true }; }\n" +
  "  async remove(id: number) { const idx = orders.findIndex(o => o.id === id); if (idx > -1) orders.splice(idx, 1); return { success: true }; }\n" +
  "}\n"
);

wp("apps/bff/src/modules/ticket/ticket.module.ts",
  "import { Module } from '@nestjs/common';\nimport { TicketController } from './ticket.controller';\nimport { TicketService } from './ticket.service';\nimport { HttpService } from '../../common/services/http.service';\n@Module({ controllers: [TicketController], providers: [TicketService, HttpService] })\nexport class TicketModule {}\n"
);

wp("apps/bff/src/modules/ticket/ticket.controller.ts",
  "import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';\n" +
  "import { JwtAuthGuard } from '../auth/jwt-auth.guard';\n" +
  "import { TicketService } from './ticket.service';\n" +
  "@Controller('tickets')\n@UseGuards(JwtAuthGuard)\nexport class TicketController {\n" +
  "  constructor(private readonly ticketService: TicketService) {}\n" +
  "  @Get()\n  async getList(@Query() query: any) { return this.ticketService.getList(query); }\n" +
  "  @Get(':id')\n  async getDetail(@Param('id') id: number) { return this.ticketService.getDetail(id); }\n" +
  "  @Post()\n  async create(@Body() body: any) { return this.ticketService.create(body); }\n" +
  "  @Put(':id')\n  async update(@Param('id') id: number, @Body() body: any) { return this.ticketService.update(id, body); }\n" +
  "  @Delete(':id')\n  async remove(@Param('id') id: number) { return this.ticketService.remove(id); }\n" +
  "}\n"
);

wp("apps/bff/src/modules/ticket/ticket.service.ts",
  "import { Injectable } from '@nestjs/common';\n" +
  "const tickets = [];\n" +
  "const types = ['咨询', '投诉', '建议', '故障'];\n" +
  "const statuses = ['待处理', '处理中', '已解决', '已关闭'];\n" +
  "for (let i = 1; i <= 80; i++) {\n" +
  "  tickets.push({ id: i, ticketNo: 'TK' + String(i).padStart(8, '0'), title: types[i % 4] + '工单' + i, content: '这是一个' + types[i % 4] + '工单的内容...', type: i % 4, typeName: types[i % 4], status: i % 4, statusName: statuses[i % 4], priority: i % 3, priorityName: i % 3 === 0 ? '高' : i % 3 === 1 ? '中' : '低', creator: '用户' + i, createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' 09:00:00' });\n" +
  "}\n" +
  "@Injectable()\nexport class TicketService {\n" +
  "  async getList(query: any) {\n" +
  "    const page = parseInt(query.page || 1);\n" +
  "    const pageSize = parseInt(query.pageSize || 10);\n" +
  "    const keyword = query.keyword || '';\n" +
  "    let filtered = tickets;\n" +
  "    if (keyword) filtered = tickets.filter(t => t.title.includes(keyword) || t.ticketNo.includes(keyword));\n" +
  "    if (query.status !== undefined && query.status !== '') filtered = filtered.filter(t => t.status === parseInt(query.status));\n" +
  "    const start = (page - 1) * pageSize;\n" +
  "    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };\n" +
  "  }\n" +
  "  async getDetail(id: number) { return tickets.find(t => t.id === id); }\n" +
  "  async create(body: any) { tickets.push({ id: tickets.length + 1, ...body, createTime: new Date().toISOString() }); return { success: true }; }\n" +
  "  async update(id: number, body: any) { const idx = tickets.findIndex(t => t.id === id); if (idx > -1) tickets[idx] = { ...tickets[idx], ...body }; return { success: true }; }\n" +
  "  async remove(id: number) { const idx = tickets.findIndex(t => t.id === id); if (idx > -1) tickets.splice(idx, 1); return { success: true }; }\n" +
  "}\n"
);

console.log('=== BFF business modules (user, order, ticket) generated! ===');