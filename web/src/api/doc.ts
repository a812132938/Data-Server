import axios from 'axios'
import request from './request'
import type { DocApiTreeNode, DocApiDetailVO } from '@/types/doc'

export function getDocApiTree() {
  return request.get<any, DocApiTreeNode[]>('/api/v1/docs/tree')
}

export function getDocApiDetail(id: number) {
  return request.get<any, DocApiDetailVO>(`/api/v1/docs/apis/${id}`)
}

export function tryDocApi(id: number, params: Record<string, any>, appCode: string) {
  return request.post<any, { httpStatus: number; bizCode: number; costMs: number; data: any }>(
    `/api/v1/docs/apis/${id}/try`,
    params,
    { headers: { 'X-App-Code': appCode } }
  )
}

export function callGatewayApi(options: {
  url: string
  method: string
  query?: Record<string, any>
  body?: Record<string, any>
  headers?: Record<string, string>
}) {
  return axios.request({
    url: options.url,
    method: options.method,
    params: options.query,
    data: options.body,
    headers: options.headers,
  }).then(response => response.data)
}
