import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { DashboardService } from './dashboard.service';
@Controller('dashboard')
@UseGuards(JwtAuthGuard)
export class DashboardController {
  constructor(private readonly dashboardService: DashboardService) {}
  @Get('stats')
  async getStats() { return this.dashboardService.getStats(); }
  @Get('chart/trend')
  async getTrendChart() { return this.dashboardService.getTrendChart(); }
  @Get('chart/pie')
  async getPieChart() { return this.dashboardService.getPieChart(); }
  @Get('chart/bar')
  async getBarChart() { return this.dashboardService.getBarChart(); }
  @Get('activities')
  async getActivities() { return this.dashboardService.getActivities(); }
}
