import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { TicketService } from './ticket.service';
@Controller('tickets')
@UseGuards(JwtAuthGuard)
export class TicketController {
  constructor(private readonly ticketService: TicketService) {}
  @Get()
  async getList(@Query() query: any) { return this.ticketService.getList(query); }
  @Get(':id')
  async getDetail(@Param('id') id: number) { return this.ticketService.getDetail(id); }
  @Post()
  async create(@Body() body: any) { return this.ticketService.create(body); }
  @Put(':id')
  async update(@Param('id') id: number, @Body() body: any) { return this.ticketService.update(id, body); }
  @Delete(':id')
  async remove(@Param('id') id: number) { return this.ticketService.remove(id); }
}
