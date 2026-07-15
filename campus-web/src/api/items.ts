import http, { request } from './http'
import type { Category, ItemDetail, ItemSummary, PageResult, PublishItemPayload } from '@/types/api'

export function listItems(params: Record<string, string | number | undefined>) {
  return request<PageResult<ItemSummary>>(http.get('/items', { params }))
}

export function getItemDetail(itemId: number) {
  return request<ItemDetail>(http.get(`/items/${itemId}`))
}

export function publishItem(payload: PublishItemPayload) {
  return request<{ itemId: number; status: number }>(http.post('/items', payload))
}

export function listCategories(parentId = 0) {
  return request<Category[]>(http.get('/categories', { params: { parentId } }))
}

export function listMyItems(page = 1, size = 10, status?: number) {
  return request<PageResult<ItemSummary>>(http.get('/items/mine', { params: { page, size, status } }))
}
