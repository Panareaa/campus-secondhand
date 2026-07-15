import http, { request } from './http'
import type { AuthToken } from '@/types/api'

export function login(loginName: string, password: string) {
  return request<AuthToken>(http.post('/auth/login', { loginName, password }))
}

export function register(campusId: string, loginName: string, password: string, nickname: string) {
  return request<AuthToken>(
    http.post('/auth/register', { campusId, loginName, password, nickname }),
  )
}
