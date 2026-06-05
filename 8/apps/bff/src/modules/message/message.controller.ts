import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards, Request } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { MessageService } from './message.service';
@Controller('messages')
@UseGuards(JwtAuthGuard)
export class MessageController {
  constructor(private readonly messageService: MessageService) {}
  @Get()
  async getList(@Query() query: any, @Request() req: any) { return this.messageService.getList(query, req.user.id); }
  @Get('unread-count')
  async getUnreadCount(@Request() req: any) { return this.messageService.getUnreadCount(req.user.id); }
  @Get(':id')
  async getDetail(@Param('id') id: number, @Request() req: any) { return this.messageService.getDetail(id, req.user.id); }
  @Post()
  async create(@Body() body: any) { return this.messageService.create(body); }
  @Put(':id/read')
  async markAsRead(@Param('id') id: number, @Request() req: any) { return this.messageService.markAsRead(id, req.user.id); }
  @Put('read-all')
  async markAllAsRead(@Request() req: any) { return this.messageService.markAllAsRead(req.user.id); }
  @Delete(':id')
  async remove(@Param('id') id: number, @Request() req: any) { return this.messageService.remove(id, req.user.id); }
}
