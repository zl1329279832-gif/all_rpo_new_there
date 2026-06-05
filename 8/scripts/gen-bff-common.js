const fs = require("fs");
const path = require("path");
const BQ = String.fromCharCode(96);

function wp(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

wp("apps/bff/src/common/types/index.ts", 
  "export interface ApiResponse<T = any> {\n" +
  "  code: number;\n" +
  "  message: string;\n" +
  "  data: T;\n" +
  "}\n"
);

wp("apps/bff/src/common/filters/exception.filter.ts",
  "import { ExceptionFilter, Catch, ArgumentsHost, HttpException, HttpStatus, Logger } from '@nestjs/common';\n" +
  "import { Request, Response } from 'express';\n" +
  "@Catch()\n" +
  "export class AllExceptionsFilter implements ExceptionFilter {\n" +
  "  private readonly logger = new Logger(AllExceptionsFilter.name);\n" +
  "  catch(exception: unknown, host: ArgumentsHost) {\n" +
  "    const ctx = host.switchToHttp();\n" +
  "    const response = ctx.getResponse<Response>();\n" +
  "    const request = ctx.getRequest<Request>();\n" +
  "    let status = HttpStatus.INTERNAL_SERVER_ERROR;\n" +
  "    let message = '服务器内部错误';\n" +
  "    let code = 500;\n" +
  "    if (exception instanceof HttpException) {\n" +
  "      status = exception.getStatus();\n" +
  "      const r = exception.getResponse() as any;\n" +
  "      message = typeof r === 'string' ? r : r.message || exception.message;\n" +
  "      code = status;\n" +
  "    } else if (exception instanceof Error) {\n" +
  "      message = exception.message;\n" +
  "    }\n" +
  "    this.logger.error('[' + request.method + '] ' + request.url + ' - ' + status + ' - ' + message);\n" +
  "    response.status(status).json({ code, message, data: null, timestamp: new Date().toISOString(), path: request.url });\n" +
  "  }\n" +
  "}\n"
);

wp("apps/bff/src/common/interceptors/transform.interceptor.ts",
  "import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';\n" +
  "import { Observable } from 'rxjs';\n" +
  "import { map } from 'rxjs/operators';\n" +
  "@Injectable()\n" +
  "export class TransformInterceptor<T> implements NestInterceptor<T, any> {\n" +
  "  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {\n" +
  "    return next.handle().pipe(map((data) => ({ code: 200, message: 'success', data: data !== undefined ? data : null })));\n" +
  "  }\n" +
  "}\n"
);

wp("apps/bff/src/common/interceptors/timeout.interceptor.ts",
  "import { Injectable, NestInterceptor, ExecutionContext, CallHandler, RequestTimeoutException } from '@nestjs/common';\n" +
  "import { Observable, throwError, TimeoutError } from 'rxjs';\n" +
  "import { catchError, timeout } from 'rxjs/operators';\n" +
  "@Injectable()\n" +
  "export class TimeoutInterceptor implements NestInterceptor {\n" +
  "  private readonly defaultTimeout = 5000;\n" +
  "  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {\n" +
  "    return next.handle().pipe(\n" +
  "      timeout(this.defaultTimeout),\n" +
  "      catchError((err) => {\n" +
  "        if (err instanceof TimeoutError) return throwError(() => new RequestTimeoutException('请求超时'));\n" +
  "        return throwError(() => err);\n" +
  "      })\n" +
  "    );\n" +
  "  }\n" +
  "}\n"
);

wp("apps/bff/src/common/services/http.service.ts",
  "import { Injectable, Logger } from '@nestjs/common';\n" +
  "import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';\n" +
  "@Injectable()\n" +
  "export class HttpService {\n" +
  "  private readonly logger = new Logger(HttpService.name);\n" +
  "  private readonly axiosInstance: AxiosInstance;\n" +
  "  constructor() {\n" +
  "    this.axiosInstance = axios.create({ timeout: 5000, headers: { 'Content-Type': 'application/json' } });\n" +
  "  }\n" +
  "  async request<T>(config: AxiosRequestConfig): Promise<T> {\n" +
  "    const response: AxiosResponse<T> = await this.axiosInstance.request(config);\n" +
  "    return response.data;\n" +
  "  }\n" +
  "  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'GET', url }); }\n" +
  "  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'POST', url, data }); }\n" +
  "  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'PUT', url, data }); }\n" +
  "  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'DELETE', url }); }\n" +
  "}\n"
);

console.log('=== BFF common files generated! ===');