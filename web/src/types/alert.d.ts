import type { PageParams } from './api'

export type TargetType = 'API' | 'APP' | 'GLOBAL'
export type Metric = 'ERROR_RATE' | 'AVG_LATENCY' | 'QPS' | 'FAIL_COUNT'
export type Operator = 'GT' | 'GE' | 'LT' | 'LE'
export type AlertEventStatus = 'FIRING' | 'RESOLVED'

export interface AlertRuleReq {
  name: string
  targetType: TargetType
  targetId?: number
  metric: Metric
  operator: Operator
  threshold: number
  windowSec: number
  silenceSec: number
  channels: string[]
  receivers: number[]
  enabled: boolean
}

export interface AlertRuleVO extends AlertRuleReq {
  id: number
}

export interface AlertRuleQuery extends PageParams {
  targetType?: TargetType
  metric?: Metric
  enabled?: boolean
}

export interface AlertEventVO {
  id: number
  ruleId: number
  ruleName: string
  metricValue: string
  status: AlertEventStatus
  firedAt: string
  resolvedAt: string
  notifyStatus: 'PENDING' | 'SUCCESS' | 'FAIL'
}

export interface AlertEventQuery extends PageParams {
  status?: AlertEventStatus
  ruleId?: number
}
