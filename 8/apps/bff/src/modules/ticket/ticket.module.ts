import { Module } from '@nestjs/common';
import { TicketController } from './ticket.controller';
import { TicketService } from './ticket.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [TicketController], providers: [TicketService, HttpService] })
export class TicketModule {}
