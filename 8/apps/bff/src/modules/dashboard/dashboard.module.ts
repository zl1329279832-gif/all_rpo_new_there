import { Module } from '@nestjs/common';
import { DashboardController } from './dashboard.controller';
import { DashboardService } from './dashboard.service';
import { HttpService } from '../../common/services/http.service';
@Module({ controllers: [DashboardController], providers: [DashboardService, HttpService] })
export class DashboardModule {}
