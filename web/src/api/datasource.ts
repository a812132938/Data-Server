import request from './request'
import type { PageResp } from '@/types/api'
import type {
  DatasourceUpsertReq,
  DatasourceQuery,
  DatasourceVO,
  DatasourceSummary,
  TestConnectionResult,
  SchemaInfo,
} from '@/types/datasource'

export function getDatasourceList(params: DatasourceQuery) {
  return request.get<any, PageResp<DatasourceVO>>('/api/v1/datasources', { params })
}

export function getDatasource(id: number) {
  return request.get<any, DatasourceVO>(`/api/v1/datasources/${id}`)
}

export function createDatasource(data: DatasourceUpsertReq) {
  return request.post<any, { id: number }>('/api/v1/datasources', data)
}

export function updateDatasource(id: number, data: DatasourceUpsertReq) {
  return request.put<any, { id: number }>(`/api/v1/datasources/${id}`, data)
}

export function deleteDatasource(id: number) {
  return request.delete<any, { success: boolean }>(`/api/v1/datasources/${id}`)
}

export function testConnection(data: DatasourceUpsertReq) {
  return request.post<any, TestConnectionResult>('/api/v1/datasources/test', data)
}

export function testExistingConnection(id: number) {
  return request.post<any, TestConnectionResult>(`/api/v1/datasources/${id}/test`)
}

export function getDatasourceSchemas(id: number) {
  return request.get<any, { schemas: SchemaInfo[] }>(`/api/v1/datasources/${id}/schemas`)
}

export function getDatasourceSummary() {
  return request.get<any, DatasourceSummary>('/api/v1/datasources/summary')
}

export function disableDatasource(id: number) {
  return request.post<any, { success: boolean }>(`/api/v1/datasources/${id}/disable`)
}

export function enableDatasource(id: number) {
  return request.post<any, { success: boolean }>(`/api/v1/datasources/${id}/enable`)
}
