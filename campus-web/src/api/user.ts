import http, { request } from './http'
import type { PointBalance, UserProfile } from '@/types/api'

export function getMyProfile() {
  return request<UserProfile>(http.get('/users/me'))
}

export function updateMyProfile(payload: { nickname?: string; avatarUrl?: string }) {
  return request<UserProfile>(http.put('/users/me', payload))
}

export function updateMyContact(contactInfo: string) {
  return request<UserProfile>(http.put('/users/me/contact', { contactInfo }))
}

export function getMyPoints() {
  return request<PointBalance>(http.get('/points/me'))
}
