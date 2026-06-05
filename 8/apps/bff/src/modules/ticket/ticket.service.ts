import { Injectable } from '@nestjs/common';
const tickets = [];
const types = ['咨询', '投诉', '建议', '故障'];
const statuses = ['待处理', '处理中', '已解决', '已关闭'];
for (let i = 1; i <= 80; i++) {
  tickets.push({ id: i, ticketNo: 'TK' + String(i).padStart(8, '0'), title: types[i % 4] + '工单' + i, content: '这是一个' + types[i % 4] + '工单的内容...', type: i % 4, typeName: types[i % 4], status: i % 4, statusName: statuses[i % 4], priority: i % 3, priorityName: i % 3 === 0 ? '高' : i % 3 === 1 ? '中' : '低', creator: '用户' + i, createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' 09:00:00' });
}
@Injectable()
export class TicketService {
  async getList(query: any) {
    const page = parseInt(query.page || 1);
    const pageSize = parseInt(query.pageSize || 10);
    const keyword = query.keyword || '';
    let filtered = tickets;
    if (keyword) filtered = tickets.filter(t => t.title.includes(keyword) || t.ticketNo.includes(keyword));
    if (query.status !== undefined && query.status !== '') filtered = filtered.filter(t => t.status === parseInt(query.status));
    const start = (page - 1) * pageSize;
    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };
  }
  async getDetail(id: number) { return tickets.find(t => t.id === id); }
  async create(body: any) { tickets.push({ id: tickets.length + 1, ...body, createTime: new Date().toISOString() }); return { success: true }; }
  async update(id: number, body: any) { const idx = tickets.findIndex(t => t.id === id); if (idx > -1) tickets[idx] = { ...tickets[idx], ...body }; return { success: true }; }
  async remove(id: number) { const idx = tickets.findIndex(t => t.id === id); if (idx > -1) tickets.splice(idx, 1); return { success: true }; }
}
