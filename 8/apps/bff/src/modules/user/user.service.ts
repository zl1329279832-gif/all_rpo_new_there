import { Injectable } from '@nestjs/common';
const users = [];
for (let i = 1; i <= 50; i++) {
  users.push({ id: i, username: 'user' + i, realName: '用户' + i, email: 'user' + i + '@example.com', phone: '13800' + String(i).padStart(5, '0'), role: i % 4 === 1 ? 'admin' : i % 4 === 2 ? 'manager' : i % 4 === 3 ? 'operator' : 'viewer', roleName: i % 4 === 1 ? '超级管理员' : i % 4 === 2 ? '运营经理' : i % 4 === 3 ? '运营专员' : '访客', department: i % 2 === 1 ? '技术部' : '运营部', status: i % 5 === 0 ? 0 : 1, createTime: '2024-01-0' + ((i % 9) + 1) + ' 10:00:00' });
}
@Injectable()
export class UserService {
  async getList(query: any) {
    const page = parseInt(query.page || 1);
    const pageSize = parseInt(query.pageSize || 10);
    const keyword = query.keyword || '';
    let filtered = users;
    if (keyword) filtered = users.filter(u => u.realName.includes(keyword) || u.username.includes(keyword));
    const start = (page - 1) * pageSize;
    return { list: filtered.slice(start, start + pageSize), total: filtered.length, page, pageSize };
  }
  async getDetail(id: number) { return users.find(u => u.id === id); }
  async create(body: any) { users.push({ id: users.length + 1, ...body, createTime: new Date().toISOString() }); return { success: true }; }
  async update(id: number, body: any) { const idx = users.findIndex(u => u.id === id); if (idx > -1) users[idx] = { ...users[idx], ...body }; return { success: true }; }
  async remove(id: number) { const idx = users.findIndex(u => u.id === id); if (idx > -1) users.splice(idx, 1); return { success: true }; }
}
