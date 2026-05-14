import request from './request'
import type { PageResp } from '@/types/api'
import type {
  ApiUpsertReq,
  ApiQuery,
  ApiVO,
  ApiSummary,
  QueryDesign,
  QueryDesignRenderResp,
  SqlPreviewReq,
  SqlPreviewResp,
} from '@/types/api-definition'

export function getApiList(params: ApiQuery) {
  return request.get<any, PageResp<ApiVO>>('/api/v1/apis', { params })
}

export function getApi(id: number) {
  return request.get<any, ApiVO>(`/api/v1/apis/${id}`)
}

export function createApi(data: ApiUpsertReq) {
  return request.post<any, { id: number; code: string; status: string }>('/api/v1/apis', data)
}

export function updateApi(id: number, data: ApiUpsertReq) {
  return request.put<any, { id: number; status: string }>(`/api/v1/apis/${id}`, data)
}

export function deleteApi(id: number) {
  return request.delete<any, { success: boolean }>(`/api/v1/apis/${id}`)
}

export function cloneApi(id: number, data: { name: string; path: string }) {
  return request.post<any, { id: number; code: string; status: string }>(`/api/v1/apis/${id}/clone`, data)
}

export function getApiSummary() {
  return request.get<any, ApiSummary>('/api/v1/apis/summary')
}

export function renderQueryDesign(data: QueryDesign) {
  return request.post<any, QueryDesignRenderResp>('/api/v1/apis/query-design/render', data)
}

export function previewSql(data: SqlPreviewReq) {
  return request.post<any, SqlPreviewResp>('/api/v1/apis/sql/preview', data)
}
