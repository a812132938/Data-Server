import request from './request'
import type { PageResp } from '@/types/api'
import type {
  AppCreateReq,
  AppUpdateReq,
  AppVO,
  AppCredential,
  AppSummary,
  AppQuery,
  RotateSecretResult,
  AppAuthVO,
} from '@/types/app'

export function getAppList(params: AppQuery) {
  return request.get<any, PageResp<AppVO>>('/api/v1/apps', { params })
}

export function getApp(id: number) {
  return request.get<any, AppVO>(`/api/v1/apps/${id}`)
}

export function createApp(data: AppCreateReq) {
  return request.post<any, AppCredential>('/api/v1/apps', data)
}

export function updateApp(id: number, data: AppUpdateReq) {
  return request.put<any, { id: number }>(`/api/v1/apps/${id}`, data)
}

export function rotateSecret(id: number) {
  return request.post<any, RotateSecretResult>(`/api/v1/apps/${id}/reset-secret`)
}

export function disableApp(id: number) {
  return request.post<any, { status: string }>(`/api/v1/apps/${id}/disable`)
}

export function enableApp(id: number) {
  return request.post<any, { status: string }>(`/api/v1/apps/${id}/enable`)
}

export function getAppSummary() {
  return request.get<any, AppSummary>('/api/v1/apps/summary')
}

export function getAppAuths(appId: number, params?: { page?: number; size?: number }) {
  return request.get<any, PageResp<AppAuthVO>>(`/api/v1/auths/by-app/${appId}`, { params })
}
