import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { clearAuth, getToken, getUser, setToken, setUser } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    user: getUser(),
  }),
  getters: {
    displayName: (state) =>
      state.user?.realName || state.user?.username || '用户',
  },
  actions: {
    async login(form) {
      const res = await loginApi(form)
      const user = res.data
      this.user = user
      setUser(user)
      // token 由响应头写入；若已有则同步
      this.token = getToken()
      return user
    },
    async logout() {
      try {
        await logoutApi()
      } catch {
        // ignore
      }
      this.token = ''
      this.user = null
      clearAuth()
    },
    syncToken(token) {
      this.token = token
      setToken(token)
    },
  },
})
