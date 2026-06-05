import { Injectable } from '@nestjs/common';
const orders = [];
const statuses = ['待支付', '已支付', '已发货', '已完成', '已取消'];
for (let i = 1; i <= 100; i++) {
  orders.push({ id: i, orderNo: 'ORD' + String(i).padStart(8, '0'), amount: (Math.random() * 10000 + 100).toFixed(2), status: i % 5, statusName: statuses[i % 5], payMethod: i % 3 === 1 ? '支付宝' : i % 3 === 2 ? '微信' : '银行卡', createTime: '2024-01-' + String((i % 28) + 1).padStart(2, '0') + ' ' + String((i % 24)).padStart(2, '0') + ':00:00' });
}
@Injectable()
export class OrderService {
  async getList(query: any) {
    const page = parseInt(query.page || 1);
    const pageSize = parseInt(query.pageSize || 10);
    const keyword = query.keyword || '';
    let filtered = orders;
    if (keyword) filtered = orders.filter(o => o.orderNo.includes(keyword));
    if (query.status !== undefined && query.status !== '') filtered = filtered.filter(o => o.status === parseInt(query.status));
    const start = (page - 1) * pageSize;
    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };
  }
  async getDetail(id: number) { return orders.find(o => o.id === id); }
  async create(body: any) { orders.push({ id: orders.length + 1, ...body, createTime: new Date().toISOString() }); return { success: true }; }
  async update(id: number, body: any) { const idx = orders.findIndex(o => o.id === id); if (idx > -1) orders[idx] = { ...orders[idx], ...body }; return { success: true }; }
  async remove(id: number) { const idx = orders.findIndex(o => o.id === id); if (idx > -1) orders.splice(idx, 1); return { success: true }; }
}
