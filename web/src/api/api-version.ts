import request from './request'
import type { PageResp } from '@/types/api'
import type { PublishReq, RollbackReq, ApiVersionVO } from '@/types/api-definition'

export function publishApi(apiId: number, data: PublishReq) {
  return request.post<any, { versionId: number; versionNo: string; apiStatus: string }>(
    `/api/v1/apis/${apiId}/publish`,
    data
  )
}

export function offlineApi(apiId: number) {
  return request.post<any, { apiStatus: string }>(`/api/v1/apis/${apiId}/offline`)
}

export function rollbackApi(apiId: number, data: RollbackReq) {
  return request.post<any, { versionId: number; versionNo: string; apiStatus: string }>(
    `/api/v1/apis/${apiId}/rollback`,
    data
  )
}

export function getVersions(apiId: number, params?: { page?: number; size?: number }) {
  return request.get<any, PageResp<ApiVersionVO>>(`/api/v1/apis/${apiId}/versions`, { params })
}
