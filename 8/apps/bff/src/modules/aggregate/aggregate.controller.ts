import { Controller, Get, UseGuards, Query } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AggregateService } from './aggregate.service';
@Controller('aggregate')
@UseGuards(JwtAuthGuard)
export class AggregateController {
  constructor(private readonly aggregateService: AggregateService) {}
  @Get('home')
  async getHomeData() { return this.aggregateService.getHomeData(); }
  @Get('user-detail')
  async getUserDetail(@Query('id') id: number) { return this.aggregateService.getUserDetail(id); }
  @Get('order-detail')
  async getOrderDetail(@Query('id') id: number) { return this.aggregateService.getOrderDetail(id); }
}
