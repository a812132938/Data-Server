import request from './request'
import type { PageResp } from '@/types/api'
import type { RunTestReq, RunTestResp, TestCaseReq, TestCaseVO, RunAllResult } from '@/types/api-definition'

export function runTest(apiId: number, data: RunTestReq) {
  return request.post<any, RunTestResp>(`/api/v1/apis/${apiId}/test/run`, data)
}

export function createTestCase(apiId: number, data: TestCaseReq) {
  return request.post<any, { id: number }>(`/api/v1/apis/${apiId}/test-cases`, data)
}

export function getTestCases(apiId: number, params?: { page?: number; size?: number }) {
  return request.get<any, PageResp<TestCaseVO>>(`/api/v1/apis/${apiId}/test-cases`, { params })
}

export function updateTestCase(id: number, data: TestCaseReq) {
  return request.put<any, { id: number }>(`/api/v1/test-cases/${id}`, data)
}

export function deleteTestCase(id: number) {
  return request.delete<any, { success: boolean }>(`/api/v1/test-cases/${id}`)
}

export function runAllTestCases(apiId: number) {
  return request.post<any, RunAllResult>(`/api/v1/apis/${apiId}/test-cases/run-all`)
}
