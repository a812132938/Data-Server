import request from './request'
import type { StatsOverview } from '@/types/log'

export function getStatsOverview(date?: string) {
  return request.get<any, StatsOverview>('/api/v1/stats/overview', { params: { date } })
}

export function getStatsByApi(params: { dateFrom?: string; dateTo?: string; limit?: number }) {
  return request.get<any, { records: any[] }>('/api/v1/stats/api', { params })
}

export function getStatsByApp(params: { dateFrom?: string; dateTo?: string; limit?: number }) {
  return request.get<any, { records: any[] }>('/api/v1/stats/app', { params })
}
