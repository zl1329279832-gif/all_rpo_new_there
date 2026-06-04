import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User, LoginRequest, LoginResponse } from '@platform/shared-types';
import { getStorage, setStorage, removeStorage } from '@platform/shared-utils';
import { authApi } from '@/api';

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(getStorage<User>('user_info'));
  const token = ref<string | null>(getStorage<string>('token'));
  const refreshToken = ref<string | null>(getStorage<string>('refresh_token'));
  const permissions = ref<string[]>(user.value?.permissions || []);
  const roles = ref<string[]>(user.value?.roles || []);

  const isLoggedIn = computed(() => !!token.value && !!user.value);

  async function login(params: LoginRequest) {
    const res = await authApi.login(params);
    if (res.code === 0) {
      const data: LoginResponse = res.data;
      token.value = data.token;
      refreshToken.value = data.refreshToken;
      user.value = data.user;
      permissions.value = data.user.permissions;
      roles.value = data.user.roles;

      setStorage('token', data.token, { expire: data.expiresIn * 1000 });
      setStorage('refresh_token', data.refreshToken);
      setStorage('user_info', data.user);
    }
    return res;
  }

  async function logout() {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('登出API调用失败:', error);
    }
    token.value = null;
    refreshToken.value = null;
    user.value = null;
    permissions.value = [];
    roles.value = [];
    removeStorage('token');
    removeStorage('refresh_token');
    removeStorage('user_info');
  }

  async function refreshTokenFn() {
    if (!refreshToken.value) return false;
    try {
      const res = await authApi.refreshToken(refreshToken.value);
      if (res.code === 0) {
        token.value = res.data.token;
        setStorage('token', res.data.token, { expire: res.data.expiresIn * 1000 });
        return true;
      }
    } catch (error) {
      console.error('刷新Token失败:', error);
    }
    return false;
  }

  function hasPermission(permission: string | string[]): boolean {
    if (!permission) return true;
    if (Array.isArray(permission)) {
      return permission.some((p) => permissions.value.includes(p));
    }
    return permissions.value.includes(permission);
  }

  function hasRole(role: string | string[]): boolean {
    if (!role) return true;
    if (Array.isArray(role)) {
      return role.some((r) => roles.value.includes(r));
    }
    return roles.value.includes(role);
  }

  function updateUserInfo(info: Partial<User>) {
    if (user.value) {
      user.value = { ...user.value, ...info };
      setStorage('user_info', user.value);
    }
  }

  return {
    user,
    token,
    permissions,
    roles,
    isLoggedIn,
    login,
    logout,
    refreshTokenFn,
    hasPermission,
    hasRole,
    updateUserInfo,
  };
});
