import request from './request'

export function revokeAuth(id: number) {
  return request.post<any, { id: number; status: string }>(`/api/v1/auths/${id}/revoke`)
}
