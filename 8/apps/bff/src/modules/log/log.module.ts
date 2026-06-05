import { Module } from '@nestjs/common';
import { LogController } from './log.controller';
import { LogService } from './log.service';
import { OperationLogService } from './operation-log.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [LogController], providers: [LogService, OperationLogService, HttpService], exports: [OperationLogService] })
export class LogModule {}
