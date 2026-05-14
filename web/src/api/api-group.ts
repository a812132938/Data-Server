import request from './request'
import type { ApiGroupReq, ApiGroupVO } from '@/types/api-definition'

export function getApiGroups() {
  return request.get<any, ApiGroupVO[]>('/api/v1/api-groups')
}

export function createApiGroup(data: ApiGroupReq) {
  return request.post<any, { id: number }>('/api/v1/api-groups', data)
}

export function updateApiGroup(id: number, data: ApiGroupReq) {
  return request.put<any, { id: number }>(`/api/v1/api-groups/${id}`, data)
}

export function deleteApiGroup(id: number) {
  return request.delete<any, { success: boolean }>(`/api/v1/api-groups/${id}`)
}
