import { Injectable } from '@nestjs/common';
const operationLogs = [];
@Injectable()
export class OperationLogService {
  async create(log: any) { operationLogs.push({ id: operationLogs.length + 1, ...log, createTime: new Date().toISOString() }); }
  async findAll(query: any) { return { list: operationLogs, total: operationLogs.length }; }
}
