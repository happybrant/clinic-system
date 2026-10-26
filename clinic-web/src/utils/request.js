import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken, setToken } from './auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers['X-Auth-Token'] = token
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const headerToken =
      response.headers['x-auth-token'] || response.headers['X-Auth-Token']
    if (headerToken) setToken(headerToken)

    const res = response.data
    if (res && typeof res.code !== 'undefined' && res.code !== 200) {
      if (res.code === 401 || res.code === 403) {
        clearAuth()
        router.push('/login')
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      clearAuth()
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
