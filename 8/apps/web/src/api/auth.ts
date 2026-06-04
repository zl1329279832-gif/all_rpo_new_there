import { get, post } from './request';
import type { Response, LoginRequest, LoginResponse, User } from '@platform/shared-types';

export const authApi = {
  login(params: LoginRequest): Promise<Response<LoginResponse>> {
    return post<Response<LoginResponse>>('/auth/login', params);
  },

  logout(): Promise<Response<void>> {
    return post<Response<void>>('/auth/logout');
  },

  refreshToken(refreshToken: string): Promise<Response<{ token: string; expiresIn: number }>> {
    return post<Response<{ token: string; expiresIn: number }>>('/auth/refresh', { refreshToken });
  },

  getUserInfo(): Promise<Response<User>> {
    return get<Response<User>>('/auth/userinfo');
  },

  changePassword(params: { oldPassword: string; newPassword: string }): Promise<Response<void>> {
    return post<Response<void>>('/auth/password', params);
  },

  getCaptcha(): Promise<Response<{ id: string; image: string }>> {
    return get<Response<{ id: string; image: string }>>('/auth/captcha');
  },
};
