import http, { request } from './http'
import type { CheckoutResult, PageResult, TradeDetail, TradeSummary } from '@/types/api'

export function checkout(remark?: string) {
  const idempotencyKey = crypto.randomUUID()
  return request<CheckoutResult>(
    http.post('/trades/checkout', { remark }, { headers: { 'Idempotency-Key': idempotencyKey } }),
  )
}

export function listBuyerOrders(page = 1, size = 10, status?: number) {
  return request<PageResult<TradeSummary>>(
    http.get('/trades/buyer', { params: { page, size, status } }),
  )
}

export function listSellerOrders(page = 1, size = 10, status?: number) {
  return request<PageResult<TradeSummary>>(
    http.get('/trades/seller', { params: { page, size, status } }),
  )
}

export function getTradeDetail(tradeNo: string) {
  return request<TradeDetail>(http.get(`/trades/${tradeNo}`))
}

export function confirmTrade(tradeNo: string) {
  return request<TradeDetail>(http.put(`/trades/${tradeNo}/confirm`))
}

export function completeTrade(tradeNo: string) {
  return request<TradeDetail>(http.put(`/trades/${tradeNo}/complete`))
}

export function cancelTrade(tradeNo: string, reason?: string) {
  return request<TradeDetail>(http.put(`/trades/${tradeNo}/cancel`, { reason }))
}
