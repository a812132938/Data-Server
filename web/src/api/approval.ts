import request from './request'
import type { PageResp } from '@/types/api'
import type {
  ApprovalCreateReq,
  ApprovalActionReq,
  ApprovalVO,
  ApprovalQuery,
  ApprovalSummary,
} from '@/types/approval'

export function createApproval(data: ApprovalCreateReq) {
  return request.post<any, { approvalId: number; approvalStatus: string; authId?: number }>(
    '/api/v1/approvals',
    data
  )
}

export function getApprovalList(params: ApprovalQuery) {
  return request.get<any, PageResp<ApprovalVO>>('/api/v1/approvals', { params })
}

export function getTodoList(params?: { page?: number; size?: number }) {
  return request.get<any, PageResp<ApprovalVO>>('/api/v1/approvals/todo', { params })
}

export function approveApproval(id: number, data: ApprovalActionReq) {
  return request.post<any, { approvalId: number; authId: number; status: string }>(
    `/api/v1/approvals/${id}/approve`,
    data
  )
}

export function rejectApproval(id: number, data: ApprovalActionReq) {
  return request.post<any, { approvalId: number; status: string }>(
    `/api/v1/approvals/${id}/reject`,
    data
  )
}

export function getApprovalSummary() {
  return request.get<any, ApprovalSummary>('/api/v1/approvals/summary')
}
