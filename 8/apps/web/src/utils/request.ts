import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
import router from "@/router";
import type { ApiResponse } from "@/types";

const service: AxiosInstance = axios.create({
  baseURL: "/api",
  timeout: 15000,
  headers: {
    "Content-Type": "application/json;charset=utf-8"
  }
});

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => {
    console.error("Request error:", error);
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data;
    
    if (res.code !== 200) {
      ElMessage.error(res.message || "请求失败");
      
      if (res.code === 401) {
        ElMessageBox.confirm("登录状态已过期，请重新登录", "提示", {
          confirmButtonText: "重新登录",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          const userStore = useUserStore();
          userStore.clearUser();
          router.push("/login");
        });
      }
      
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    
    return res;
  },
  (error) => {
    console.error("Response error:", error);
    
    if (error.response) {
      const status = error.response.status;
      const messages: Record<number, string> = {
        400: "请求参数错误",
        401: "未授权，请重新登录",
        403: "拒绝访问",
        404: "请求地址不存在",
        500: "服务器内部错误",
        502: "网关错误",
        503: "服务不可用",
        504: "网关超时"
      };
      
      const message = messages[status] || error.message || "网络错误";
      
      if (status === 401) {
        ElMessageBox.confirm("登录状态已过期，请重新登录", "提示", {
          confirmButtonText: "重新登录",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          const userStore = useUserStore();
          userStore.clearUser();
          router.push("/login");
        });
      } else {
        ElMessage.error(message);
      }
    } else if (error.code === "ECONNABORTED") {
      ElMessage.error("请求超时，请稍后重试");
    } else {
      ElMessage.error("网络错误，请检查网络连接");
    }
    
    return Promise.reject(error);
  }
);

export function request<T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service(config) as unknown as Promise<ApiResponse<T>>;
}

export function get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
  return request({ method: "GET", url, params });
}

export function post<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
  return request({ method: "POST", url, data });
}

export function put<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
  return request({ method: "PUT", url, data });
}

export function del<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
  return request({ method: "DELETE", url, params });
}

export default service;
