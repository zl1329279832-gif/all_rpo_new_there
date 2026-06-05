import { Controller, Get, Post, Body, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { LogService } from './log.service';
@Controller('logs')
@UseGuards(JwtAuthGuard)
export class LogController {
  constructor(private readonly logService: LogService) {}
  @Get()
  async getList(@Query() query: any) { return this.logService.getList(query); }
  @Post('export')
  async exportLogs(@Body() body: any) { return this.logService.exportLogs(body); }
}
