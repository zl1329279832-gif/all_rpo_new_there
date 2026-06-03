import { defineStore } from 'pinia'
import { loginApi, logoutApi } from '@/api'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  actions: {
    async login(loginData) {
      const res = await loginApi(loginData)
      const user = res.data
      this.token = user.token || 'mock-token-' + Date.now()
      this.userInfo = user
      setToken(this.token)
      return res
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        console.log('logout error:', e)
      }
      this.token = ''
      this.userInfo = null
      removeToken()
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    }
  },
  persist: {
    key: 'wms-user',
    storage: localStorage,
    paths: ['token', 'userInfo']
  }
})
