import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { JwtStrategy } from './jwt.strategy';
import { HttpService } from '../../common/services/http.service';
@Module({
  imports: [
    PassportModule,
    JwtModule.register({ secret: process.env.JWT_SECRET || 'platform-secret', signOptions: { expiresIn: '2h' }})
  ],
  providers: [AuthService, JwtStrategy, HttpService],
  controllers: [AuthController]
})
export class AuthModule {}
