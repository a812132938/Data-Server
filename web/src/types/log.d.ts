import type { PageParams } from './api'

export interface LogQuery extends PageParams {
  traceId?: string
  apiId?: number
  appId?: number
  httpStatus?: string
  timeFrom?: string
  timeTo?: string
}

export interface LogVO {
  id: number
  traceId: string
  apiId: number
  apiPath: string
  appId: number
  appName: string
  httpStatus: number
  bizCode: number
  costMs: number
  clientIp: string
  createdAt: string
}

export interface LogDetailVO extends LogVO {
  requestParams: string
  responseSize: number
  recordCount: number
  errorMsg: string
}

export interface StatsOverview {
  todayCount?: number
  todayCalls?: number
  successRate: number
  avgCost: number
  errorCount: number
}
