import { Controller, Post, Body, UseGuards, Request, Get } from '@nestjs/common';
import { AuthService } from './auth.service';
import { JwtAuthGuard } from './jwt-auth.guard';
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}
  @Post('login')
  async login(@Body() body: { username: string; password: string }) { return this.authService.login(body.username, body.password); }
  @Post('refresh')
  async refreshToken(@Body() body: { token: string }) { return this.authService.refreshToken(body.token); }
  @UseGuards(JwtAuthGuard)
  @Get('userinfo')
  async getUserInfo(@Request() req: any) { return this.authService.getUserInfo(req.user.id); }
  @UseGuards(JwtAuthGuard)
  @Get('menus')
  async getMenus(@Request() req: any) { return this.authService.getMenus(req.user.role); }
  @UseGuards(JwtAuthGuard)
  @Get('permissions')
  async getPermissions(@Request() req: any) { return this.authService.getPermissions(req.user.id); }
  @UseGuards(JwtAuthGuard)
  @Post('logout')
  async logout(@Request() req: any) { return this.authService.logout(req.user.id); }
}
