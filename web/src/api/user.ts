import request from './request'
import type { RegisterReq, LoginReq, LoginVO, UserVO } from '@/types/user'

/** 注册 */
export function register(data: RegisterReq) {
  return request.post<any, UserVO>('/api/v1/user/register', data)
}

/** 登录 */
export function login(data: LoginReq) {
  return request.post<any, LoginVO>('/api/v1/user/login', data)
}

/** 退出登录 */
export function logout() {
  return request.post<any, void>('/api/v1/user/logout')
}

/** 获取当前用户信息 */
export function getCurrentUser() {
  return request.get<any, UserVO>('/api/v1/user/me')
}
