import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { OrderService } from './order.service';
@Controller('orders')
@UseGuards(JwtAuthGuard)
export class OrderController {
  constructor(private readonly orderService: OrderService) {}
  @Get()
  async getList(@Query() query: any) { return this.orderService.getList(query); }
  @Get(':id')
  async getDetail(@Param('id') id: number) { return this.orderService.getDetail(id); }
  @Post()
  async create(@Body() body: any) { return this.orderService.create(body); }
  @Put(':id')
  async update(@Param('id') id: number, @Body() body: any) { return this.orderService.update(id, body); }
  @Delete(':id')
  async remove(@Param('id') id: number) { return this.orderService.remove(id); }
}
