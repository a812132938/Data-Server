import request from './request'
import type { TrendItem, StatusDistItem, TopApiItem, TopAppItem } from '@/types/dashboard'

export function getDashboardTrend(timeRange: '7d' | '30d') {
  return request.get<any, { records: TrendItem[] }>('/api/v1/dashboard/trend', { params: { timeRange } })
}

export function getStatusDist(params?: { date?: string; timeRange?: string; apiId?: number }) {
  return request.get<any, { records: StatusDistItem[] }>('/api/v1/dashboard/status-dist', { params })
}

export function getTopApi(params?: { timeRange?: string; limit?: number }) {
  return request.get<any, { records: TopApiItem[] }>('/api/v1/dashboard/top-api', { params })
}

export function getTopApp(params?: { timeRange?: string; limit?: number }) {
  return request.get<any, { records: TopAppItem[] }>('/api/v1/dashboard/top-app', { params })
}
