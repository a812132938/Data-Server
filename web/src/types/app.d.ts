import type { PageParams } from './api'

export type AppStatus = 'ACTIVE' | 'DISABLED'

export interface AppCreateReq {
  name: string
  owner: number
  contact?: string
  purpose?: string
  description?: string
  callbackUrl?: string
}

export interface AppUpdateReq {
  name: string
  owner: number
  contact?: string
  purpose?: string
  description?: string
  callbackUrl?: string
}

export interface AppVO {
  id: number
  name: string
  appKey: string
  appCode: string
  owner: number
  ownerName: string
  contact: string
  purpose: string
  status: AppStatus
  authCount: number
  todayCalls?: number
  callbackUrl?: string
  description?: string
  createdAt: string
  updatedAt?: string
}

export interface AppCredential {
  id: number
  appKey: string
  appSecret: string
  appCode: string
  status: AppStatus
}

export interface AppSummary {
  total: number
  enabled: number
  disabled: number
  authCount: number
  todayCalls?: number
}

export interface AppQuery extends PageParams {
  keyword?: string
  status?: AppStatus
  owner?: number
}

export interface RotateSecretResult {
  appKey: string
  appSecret: string
}

export interface AppAuthVO {
  id: number
  apiId: number
  apiName: string
  path: string
  qpsLimit: number
  effectiveTo: string
  status: string
}
