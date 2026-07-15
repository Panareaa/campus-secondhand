import http, { request } from './http'
import type { ItemSummary, PageResult } from '@/types/api'

export interface DescribePayload {
  title: string
  categoryName?: string
  conditionLevel?: number
  keywords?: string[]
  originalPrice?: number
}

export interface DescribeResult {
  description: string
  suggestedTags: string[]
  degraded: boolean
  modelName: string
}

export interface ParsedSearchCondition {
  categoryId?: number
  keywords?: string[]
  minPrice?: number
  maxPrice?: number
  sort?: string
}

export interface AiSearchResult {
  parsedCondition: ParsedSearchCondition
  items: PageResult<ItemSummary>
  degraded: boolean
}

export function aiDescribe(payload: DescribePayload) {
  return request<DescribeResult>(http.post('/ai/describe', payload))
}

export function aiSearch(query: string, page = 1, size = 12) {
  return request<AiSearchResult>(http.post('/ai/search', { query, page, size }))
}
