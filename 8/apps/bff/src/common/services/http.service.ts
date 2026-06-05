import { Injectable, Logger } from '@nestjs/common';
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
@Injectable()
export class HttpService {
  private readonly logger = new Logger(HttpService.name);
  private readonly axiosInstance: AxiosInstance;
  constructor() {
    this.axiosInstance = axios.create({ timeout: 5000, headers: { 'Content-Type': 'application/json' } });
  }
  async request<T>(config: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.axiosInstance.request(config);
    return response.data;
  }
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'GET', url }); }
  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'POST', url, data }); }
  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'PUT', url, data }); }
  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> { return this.request<T>({ ...config, method: 'DELETE', url }); }
}
