import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { CacheModule } from '@nestjs/cache-manager';
import { AuthModule } from './modules/auth/auth.module';
import { UserModule } from './modules/user/user.module';
import { OrderModule } from './modules/order/order.module';
import { TicketModule } from './modules/ticket/ticket.module';
import { MessageModule } from './modules/message/message.module';
import { LogModule } from './modules/log/log.module';
import { DashboardModule } from './modules/dashboard/dashboard.module';
import { AggregateModule } from './modules/aggregate/aggregate.module';
import { HttpService } from './common/services/http.service';
@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    CacheModule.register({ isGlobal: true, ttl: 300 }),
    AuthModule, UserModule, OrderModule, TicketModule, MessageModule, LogModule, DashboardModule, AggregateModule
  ],
  providers: [HttpService]
})
export class AppModule {}
