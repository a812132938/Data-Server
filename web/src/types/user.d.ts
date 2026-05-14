/** 注册请求 */
export interface RegisterReq {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
}

/** 登录请求 */
export interface LoginReq {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginVO {
  token: string
  tokenType: string
  expiresIn: number
}

/** 用户信息 */
export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  status: number
  createdAt: string
}
