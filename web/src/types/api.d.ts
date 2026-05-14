/** 通用响应包络 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  traceId: string
}

/** 分页响应 */
export interface PageResp<T = any> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 分页请求参数 */
export interface PageParams {
  page?: number
  size?: number
}

/** API 参数项 */
export interface ApiParamItem {
  name: string
  location: 'QUERY' | 'BODY' | 'PATH' | 'HEADER'
  dataType: string
  required: boolean
  defaultValue?: string
  operator?: string
  validateRule?: string
  validateMin?: string
  validateMax?: string
  formatDesc?: string
  description?: string
  example?: string
  sort: number
}

/** API 返回字段项 */
export interface ApiResponseFieldItem {
  fieldName: string
  alias?: string
  dataType: string
  description?: string
  sensitive: boolean
  sensitiveRule?: string
  sort: number
}
