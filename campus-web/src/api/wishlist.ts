import http, { request } from './http'
import type { PageResult, WishlistItem } from '@/types/api'

export function listWishlist(page = 1, size = 20) {
  return request<PageResult<WishlistItem>>(http.get('/wishlist', { params: { page, size } }))
}

export function addWishlist(itemId: number) {
  return request<{ wishlistId: number; itemId: number }>(http.post('/wishlist', { itemId }))
}

export function removeWishlist(itemId: number) {
  return request<null>(http.delete(`/wishlist/${itemId}`))
}

export function moveToCart(itemId: number, keepWishlist = false) {
  return request<{ cartEntryId: number; itemId: number }>(
    http.post(`/wishlist/${itemId}/move-to-cart`, { keepWishlist }),
  )
}
