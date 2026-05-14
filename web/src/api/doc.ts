import request from './request'
import type { DocApiTreeNode, DocApiDetailVO } from '@/types/doc'

export function getDocApiTree() {
  return request.get<any, { records: DocApiTreeNode[] }>('/api/v1/docs/apis')
}

export function getDocApiDetail(id: number) {
  return request.get<any, DocApiDetailVO>(`/api/v1/docs/apis/${id}`)
}

export function tryDocApi(id: number, params: Record<string, any>, appCode: string) {
  return request.post<any, { httpStatus: number; bizCode: number; costMs: number; data: any }>(
    `/api/v1/docs/apis/${id}/try`,
    { params },
    { headers: { 'X-App-Code': appCode } }
  )
}
