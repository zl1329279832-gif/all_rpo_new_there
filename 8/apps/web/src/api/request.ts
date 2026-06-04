import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getStorage, removeStorage } from '@platform/shared-utils';
import router from '@/router';
import { useUserStore } from '@/stores';

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_ENABLE_MOCK === 'true' ? '/mock-api' : '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
});

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getStorage<string>('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-Request-Id'] = generateRequestId();
    config.headers['X-Timestamp'] = Date.now();
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;

    if (res.code === 0 || res.code === 200) {
      return res;
    }

    switch (res.code) {
      case 401:
        handleUnauthorized();
        break;
      case 403:
        ElMessage.error('抱歉，您没有权限执行此操作');
        router.push('/403');
        break;
      case 404:
        ElMessage.error('请求的资源不存在');
        break;
      case 429:
        ElMessage.warning('请求过于频繁，请稍后再试');
        break;
      case 500:
      case 502:
      case 503:
      case 504:
        ElMessage.error(res.message || '服务器错误，请稍后再试');
        break;
      default:
        ElMessage.error(res.message || '请求失败');
    }

    return Promise.reject(new Error(res.message || '请求失败'));
  },
  (error) => {
    if (axios.isCancel(error)) {
      return Promise.reject(error);
    }

    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后再试');
    } else if (!navigator.onLine) {
      ElMessage.error('网络连接失败，请检查您的网络');
    } else if (error.response) {
      const { status } = error.response;
      switch (status) {
        case 401:
          handleUnauthorized();
          break;
        case 403:
          ElMessage.error('抱歉，您没有权限访问');
          router.push('/403');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
        case 502:
        case 503:
        case 504:
          ElMessage.error('服务器错误，请稍后再试');
          break;
        default:
          ElMessage.error(error.message || '请求失败');
      }
    } else {
      ElMessage.error('网络错误，请稍后再试');
    }

    return Promise.reject(error);
  }
);

function generateRequestId(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

async function handleUnauthorized() {
  const userStore = useUserStore();
  try {
    await ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning',
    });
    userStore.logout();
    router.push(`/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`);
  } catch {
    // 用户取消
  }
}

export function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return service.request<any, T>(config);
}

export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'GET', url, params, ...config });
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'POST', url, data, ...config });
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'PUT', url, data, ...config });
}

export function del<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'DELETE', url, params, ...config });
}

export default service;
