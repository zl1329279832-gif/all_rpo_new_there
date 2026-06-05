import { Injectable } from '@nestjs/common';
const messages = [];
const types = ['system', 'todo', 'notification'];
const typeNames = ['系统通知', '待办事项', '普通通知'];
for (let i = 1; i <= 30; i++) {
  messages.push({ id: i, title: typeNames[i % 3] + ' - 消息标题' + i, content: '这是一条' + typeNames[i % 3] + '的消息内容，请及时查看处理。', type: types[i % 3], typeName: typeNames[i % 3], isRead: i % 3 === 0 ? 1 : 0, createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' 10:00:00' });
}
@Injectable()
export class MessageService {
  async getList(query: any, userId: number) {
    const page = parseInt(query.page || 1);
    const pageSize = parseInt(query.pageSize || 20);
    let filtered = messages;
    if (query.isRead !== undefined && query.isRead !== '') filtered = messages.filter(m => m.isRead === parseInt(query.isRead));
    if (query.type) filtered = filtered.filter(m => m.type === query.type);
    const start = (page - 1) * pageSize;
    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };
  }
  async getUnreadCount(userId: number) { return { count: messages.filter(m => !m.isRead).length }; }
  async getDetail(id: number, userId: number) { const msg = messages.find(m => m.id === id); if (msg) msg.isRead = 1; return msg; }
  async create(body: any) { messages.push({ id: messages.length + 1, ...body, isRead: 0, createTime: new Date().toISOString() }); return { success: true }; }
  async markAsRead(id: number, userId: number) { const msg = messages.find(m => m.id === id); if (msg) msg.isRead = 1; return { success: true }; }
  async markAllAsRead(userId: number) { messages.forEach(m => m.isRead = 1); return { success: true }; }
  async remove(id: number, userId: number) { const idx = messages.findIndex(m => m.id === id); if (idx > -1) messages.splice(idx, 1); return { success: true }; }
}
