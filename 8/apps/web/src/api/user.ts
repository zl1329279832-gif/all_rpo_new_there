import { get, post } from "@/utils/request";
import type { LoginParams, LoginResult, User, PageParams, PageResult } from "@/types";

export function login(params: LoginParams) {
  return post<LoginResult>("/auth/login", params);
}

export function getUserInfo() {
  return get<LoginResult>("/auth/userinfo");
}

export function logout() {
  return post("/auth/logout");
}

export function refreshToken() {
  return post<{ token: string }>("/auth/refresh");
}

export function getUserList(params: PageParams) {
  return get<PageResult<User>>("/users", params);
}

export function getUserDetail(id: number) {
  return get<User>(`/users/${id}`);
}

export function createUser(data: Partial<User>) {
  return post("/users", data);
}

export function updateUser(id: number, data: Partial<User>) {
  return put(`/users/${id}`, data);
}

export function deleteUser(id: number) {
  return del(`/users/${id}`);
}

export function updateUserStatus(id: number, status: number) {
  return put(`/users/${id}/status`, { status });
}
