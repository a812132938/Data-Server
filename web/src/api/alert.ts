import request from './request'
import type { PageResp } from '@/types/api'
import type { AlertRuleReq, AlertRuleVO, AlertRuleQuery, AlertEventVO, AlertEventQuery } from '@/types/alert'

export function createAlertRule(data: AlertRuleReq) {
  return request.post<any, { id: number }>('/api/v1/alert-rules', data)
}

export function getAlertRules(params: AlertRuleQuery) {
  return request.get<any, PageResp<AlertRuleVO>>('/api/v1/alert-rules', { params })
}

export function updateAlertRule(id: number, data: AlertRuleReq) {
  return request.put<any, { id: number }>(`/api/v1/alert-rules/${id}`, data)
}

export function deleteAlertRule(id: number) {
  return request.delete<any, { success: boolean }>(`/api/v1/alert-rules/${id}`)
}

export function getAlertEvents(params: AlertEventQuery) {
  return request.get<any, PageResp<AlertEventVO>>('/api/v1/alert-events', { params })
}

export function getRecentAlertEvents(limit?: number) {
  return request.get<any, PageResp<AlertEventVO>>('/api/v1/alert-events/recent', { params: { limit } })
}
