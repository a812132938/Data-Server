import request from './request'
import type { PageResp } from '@/types/api'
import type { LogQuery, LogVO, LogDetailVO } from '@/types/log'

export function getLogList(params: LogQuery) {
  return request.get<any, PageResp<LogVO>>('/api/v1/logs', { params })
}

export function getLogDetail(id: number) {
  return request.get<any, LogDetailVO>(`/api/v1/logs/${id}`)
}
