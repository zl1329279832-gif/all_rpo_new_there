import { Injectable } from '@nestjs/common';
@Injectable()
export class AggregateService {
  async getHomeData() { return { stats: {}, chart: {}, recentOrders: [], recentTickets: [] }; }
  async getUserDetail(id: number) { return { user: {}, orders: [], tickets: [] }; }
  async getOrderDetail(id: number) { return { order: {}, user: {}, logs: [] }; }
}
