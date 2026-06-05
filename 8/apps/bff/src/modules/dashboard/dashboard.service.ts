import { Injectable } from '@nestjs/common';
@Injectable()
export class DashboardService {
  async getStats() { return { totalUsers: 12586, totalOrders: 89456, totalTickets: 3256, totalRevenue: 1258000, todayNewUsers: 128, todayNewOrders: 356, pendingTickets: 158, monthGrowth: 12.5 }; }
  async getTrendChart() {
    const dates = [];
    const userData = [];
    const orderData = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date(); d.setDate(d.getDate() - i);
      dates.push((d.getMonth() + 1) + '/' + d.getDate());
      userData.push(Math.floor(Math.random() * 200) + 100);
      orderData.push(Math.floor(Math.random() * 500) + 200);
    }
    return { dates, userData, orderData };
  }
  async getPieChart() { return [{ name: '支付宝', value: 45 }, { name: '微信', value: 35 }, { name: '银行卡', value: 20 }]; }
  async getBarChart() { return { categories: ['1月', '2月', '3月', '4月', '5月', '6月'], values: [120, 200, 150, 80, 70, 110] }; }
  async getActivities() {
    return [
      { id: 1, user: 'admin', action: '创建了新用户', time: '5分钟前' },
      { id: 2, user: 'manager', action: '处理了工单', time: '10分钟前' },
      { id: 3, user: 'operator', action: '更新了订单状态', time: '30分钟前' },
      { id: 4, user: 'admin', action: '导出了操作日志', time: '1小时前' },
      { id: 5, user: 'manager', action: '发送了系统通知', time: '2小时前' }
    ];
  }
}
