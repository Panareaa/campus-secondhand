import http, { request } from './http'
import type { Cart, CartItem, SettlePreview } from '@/types/api'

export function getCart() {
  return request<Cart>(http.get('/cart'))
}

export function getSettlePreview() {
  return request<SettlePreview>(http.get('/cart/settle-preview'))
}

export function addCartItem(itemId: number, quantity = 1) {
  return request<{ cartEntryId: number; quantity: number }>(
    http.post('/cart/items', { itemId, quantity }),
  )
}

export function updateCartItem(itemId: number, quantity: number) {
  return request<{ cartEntryId: number; quantity: number }>(
    http.put(`/cart/items/${itemId}`, { quantity }),
  )
}

export function removeCartItem(itemId: number) {
  return request<void>(http.delete(`/cart/items/${itemId}`))
}

export function clearCart() {
  return request<void>(http.delete('/cart'))
}

export type { CartItem }
