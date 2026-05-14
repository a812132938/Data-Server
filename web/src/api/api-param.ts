import request from './request'
import type { ApiParamItem } from '@/types/api'
import type { ParseParamsReq, ParseParamsResp } from '@/types/api-definition'

export function parseParams(apiId: number, data: ParseParamsReq) {
  return request.post<any, ParseParamsResp>(`/api/v1/apis/${apiId}/params/parse`, data)
}

export function getParams(apiId: number) {
  return request.get<any, { records: ApiParamItem[] }>(`/api/v1/apis/${apiId}/params`)
}

export function saveParams(apiId: number, params: ApiParamItem[]) {
  return request.put<any, { count: number }>(`/api/v1/apis/${apiId}/params`, { params })
}
