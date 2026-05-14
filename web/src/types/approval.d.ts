import type { PageParams } from './api'

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type AuthStatus = 'EFFECTIVE' | 'EXPIRED' | 'REVOKED'

export interface ApprovalCreateReq {
  appId: number
  apiId: number
  qpsLimit: number
  effectiveTo?: string
  reason?: string
}

export interface ApprovalActionReq {
  comment?: string
}

export interface ApprovalVO {
  id: number
  applicant: number
  applicantName: string
  approver: number
  approverName: string
  appId: number
  appName: string
  apiId: number
  apiName: string
  qpsLimit: number
  effectiveTo: string
  status: ApprovalStatus
  reason: string
  comment: string
  createdAt: string
}

export interface ApprovalQuery extends PageParams {
  status?: ApprovalStatus
}

export interface ApprovalSummary {
  pending: number
  approved: number
  rejected: number
}
