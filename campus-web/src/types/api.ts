export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  page: number
  size: number
  total: number
  pages: number
}

export interface AuthToken {
  userId: number
  accessToken: string
  expiresIn: number
  tokenType: string
}

export interface ItemSummary {
  itemId: number
  title: string
  summary: string
  coverUrl: string
  salePrice: number
  conditionLevel: number
  categoryId: number
  categoryName: string
  sellerId: number
  status: number
  publishedAt: string
}

export interface ItemDetail extends ItemSummary {
  sellerNickname: string
  description: string
  originalPrice: number
  viewCount: number
  availableQty: number
  images: { imageUrl: string; isCover: boolean }[]
}

export interface Category {
  categoryId: number
  name: string
  parentId: number
}

export interface WishlistItem {
  wishlistId: number
  itemId: number
  itemTitle: string
  itemCover: string
  salePrice: number
  itemStatus: number
  valid: boolean
  createdAt: string
}

export interface PublishItemPayload {
  categoryId: number
  title: string
  summary?: string
  description?: string
  conditionLevel?: number
  originalPrice?: number
  salePrice: number
  publish?: boolean
}

export interface CartItem {
  cartEntryId: number
  itemId: number
  itemTitle: string
  itemCover: string
  itemPrice: number
  quantity: number
  lineAmount: number
  valid: boolean
  invalidReason?: string
}

export interface Cart {
  items: CartItem[]
  totalAmount: number
  totalQuantity: number
}

export interface SettlePreview {
  items: CartItem[]
  totalAmount: number
  contactInfo: string
  contactReady: boolean
}

export interface CheckoutResult {
  tradeNo: string
  tradeId: number
  totalAmount: number
  status: number
  statusText: string
  createdAt: string
}

export interface TradeLine {
  lineId: number
  itemId: number
  itemTitle: string
  itemCover: string
  unitPrice: number
  quantity: number
  lineAmount: number
}

export interface TradeSummary {
  tradeNo: string
  tradeId: number
  buyerId: number
  sellerId: number
  totalAmount: number
  status: number
  statusText: string
  itemCount: number
  coverUrl: string
  createdAt: string
}

export interface TradeDetail {
  tradeNo: string
  tradeId: number
  buyerId: number
  sellerId: number
  buyerContact: string
  totalAmount: number
  status: number
  statusText: string
  remark: string
  lines: TradeLine[]
  confirmedAt?: string
  completedAt?: string
  createdAt: string
}

export interface Notification {
  notificationId: number
  notifyType: string
  title: string
  content: string
  tradeNo: string
  isRead: boolean
  createdAt: string
}

export interface UserProfile {
  userId: number
  campusId: string
  loginName: string
  nickname: string
  contactInfo: string
  avatarUrl: string
  role: string
  certStatus: number
  reputation: number
  greenPoints: number
  createdAt: string
}

export interface PointBalance {
  balance: number
  updatedAt: string
}
