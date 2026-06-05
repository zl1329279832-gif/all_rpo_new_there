import { Module } from '@nestjs/common';
import { OrderController } from './order.controller';
import { OrderService } from './order.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [OrderController], providers: [OrderService, HttpService] })
export class OrderModule {}
