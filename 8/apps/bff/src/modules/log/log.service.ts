import { Injectable } from '@nestjs/common';
const logs = [];
const modules = ['user', 'order', 'ticket', 'message', 'dashboard'];
const actions = ['创建', '更新', '删除', '查询', '导出'];
const methods = ['GET', 'POST', 'PUT', 'DELETE'];
for (let i = 1; i <= 100; i++) {
  logs.push({ id: i, userId: (i % 4) + 1, username: ['admin', 'manager', 'operator', 'viewer'][i % 4], module: modules[i % 5], action: actions[i % 5], method: methods[i % 4], params: JSON.stringify({ id: i }), ip: '192.168.1.' + (i % 255), userAgent: 'Mozilla/5.0', duration: Math.floor(Math.random() * 500), status: i % 5 === 0 ? 0 : 1, errorMsg: i % 5 === 0 ? '操作失败' : null, createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' ' + String((i % 24)).padStart(2, '0') + ':00:00' });
}
@Injectable()
export class LogService {
  async getList(query: any) {
    const page = parseInt(query.page || 1);
    const pageSize = parseInt(query.pageSize || 10);
    const keyword = query.keyword || '';
    let filtered = logs;
    if (keyword) filtered = logs.filter(l => l.username.includes(keyword) || l.module.includes(keyword));
    if (query.status !== undefined && query.status !== '') filtered = filtered.filter(l => l.status === parseInt(query.status));
    const start = (page - 1) * pageSize;
    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };
  }
  async exportLogs(body: any) { return { url: '/export/logs_' + Date.now() + '.xlsx' }; }
}
