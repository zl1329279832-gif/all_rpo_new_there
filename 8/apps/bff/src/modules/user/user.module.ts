import { Module } from '@nestjs/common';
import { UserController } from './user.controller';
import { UserService } from './user.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [UserController], providers: [UserService, HttpService] })
export class UserModule {}
