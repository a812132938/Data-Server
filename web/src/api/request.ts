import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import { getToken, removeToken } from '@/utils/token'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    config.headers['Content-Type'] = config.headers['Content-Type'] || 'application/json'
    config.headers['X-Trace-Id'] = `tr_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code === 0) {
      return res.data as any
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || '网络错误'
    if (status === 401) {
      removeToken()
      // 动态 import 避免与 router 循环依赖
      import('@/router').then(({ default: router }) => {
        const currentRoute = router.currentRoute.value
        const redirect = currentRoute.fullPath
        if (currentRoute.name !== 'Login' && !currentRoute.meta.public) {
          router.replace({ name: 'Login', query: { redirect } })
        }
      })
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
