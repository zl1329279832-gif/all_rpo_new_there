import { Module } from '@nestjs/common';
import { MessageController } from './message.controller';
import { MessageService } from './message.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [MessageController], providers: [MessageService, HttpService] })
export class MessageModule {}
