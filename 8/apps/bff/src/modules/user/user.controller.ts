import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { UserService } from './user.service';
@Controller('users')
@UseGuards(JwtAuthGuard)
export class UserController {
  constructor(private readonly userService: UserService) {}
  @Get()
  async getList(@Query() query: any) { return this.userService.getList(query); }
  @Get(':id')
  async getDetail(@Param('id') id: number) { return this.userService.getDetail(id); }
  @Post()
  async create(@Body() body: any) { return this.userService.create(body); }
  @Put(':id')
  async update(@Param('id') id: number, @Body() body: any) { return this.userService.update(id, body); }
  @Delete(':id')
  async remove(@Param('id') id: number) { return this.userService.remove(id); }
}
