import request from './request'

export interface NotificationItem {
  id: number
  title: string
  content: string
  type: string
  status: 'UNREAD' | 'READ'
  senderName: string
  createdAt: string
  bizType: string
  bizId: number
}

export interface NotificationListResp {
  unreadCount: number
  records: NotificationItem[]
}

export function getNotifications(params?: { status?: string; type?: string; limit?: number }) {
  return request.get<any, NotificationListResp>('/api/v1/notifications/insite', { params })
}

export function markAsRead(id: number) {
  return request.put<any, { id: number; status: string }>(`/api/v1/notifications/${id}/read`)
}

export function markAllAsRead() {
  return request.put<any, { count: number }>('/api/v1/notifications/read-all')
}
