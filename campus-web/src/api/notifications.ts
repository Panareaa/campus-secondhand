import http, { request } from './http'
import type { Notification, PageResult } from '@/types/api'

export function listNotifications(page = 1, size = 20, isRead?: boolean) {
  return request<PageResult<Notification>>(
    http.get('/notifications', { params: { page, size, isRead } }),
  )
}

export function markNotificationRead(notificationId: number) {
  return request<void>(http.put(`/notifications/${notificationId}/read`))
}

export function markAllNotificationsRead() {
  return request<void>(http.put('/notifications/read-all'))
}

export async function countUnreadNotifications() {
  const data = await listNotifications(1, 1, false)
  return data.total
}
