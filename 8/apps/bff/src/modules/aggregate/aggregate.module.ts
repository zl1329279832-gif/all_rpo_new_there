import { Module } from '@nestjs/common';
import { AggregateController } from './aggregate.controller';
import { AggregateService } from './aggregate.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [AggregateController], providers: [AggregateService, HttpService] })
export class AggregateModule {}
