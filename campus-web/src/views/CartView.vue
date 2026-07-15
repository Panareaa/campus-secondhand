<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getCart, removeCartItem, updateCartItem } from '@/api/cart'
import type { CartItem } from '@/types/api'

const items = ref<CartItem[]>([])
const totalAmount = ref(0)
const totalQuantity = ref(0)
const loading = ref(true)
const message = ref('')

async function load() {
  loading.value = true
  message.value = ''
  try {
    const data = await getCart()
    items.value = data.items ?? []
    totalAmount.value = data.totalAmount ?? 0
    totalQuantity.value = data.totalQuantity ?? 0
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function changeQty(item: CartItem, delta: number) {
  const next = item.quantity + delta
  if (next < 1) return
  message.value = ''
  try {
    await updateCartItem(item.itemId, next)
    await load()
  } catch (e) {
    message.value = e instanceof Error ? e.message : '更新失败'
  }
}

async function remove(itemId: number) {
  message.value = ''
  try {
    await removeCartItem(itemId)
    await load()
  } catch (e) {
    message.value = e instanceof Error ? e.message : '删除失败'
  }
}

const hasValidItems = () => items.value.some((i) => i.valid)
</script>

<template>
  <div>
    <h1 class="page-title">购物车</h1>
    <p v-if="message" class="text-red-500 text-sm mb-4">{{ message }}</p>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="items.length === 0" class="page-panel p-8 text-center text-gray-400">
      购物车是空的，
      <RouterLink to="/items" class="text-emerald-600 hover:underline">去逛逛</RouterLink>
    </div>
    <div v-else class="space-y-4">
      <div v-for="item in items" :key="item.cartEntryId" class="page-panel list-row">
        <RouterLink :to="`/items/${item.itemId}`" class="thumb-link">
          <img v-if="item.itemCover" :src="item.itemCover" :alt="item.itemTitle" class="thumb-img" />
          <span v-else class="thumb-empty">无图</span>
        </RouterLink>
        <div class="min-w-0">
          <RouterLink :to="`/items/${item.itemId}`" class="font-medium hover:text-emerald-600 line-clamp-2">
            {{ item.itemTitle }}
          </RouterLink>
          <p class="text-emerald-600 font-bold mt-1">¥{{ item.itemPrice }}</p>
          <p v-if="!item.valid" class="text-xs text-red-400 mt-1">{{ item.invalidReason || '已失效' }}</p>
        </div>
        <div class="qty-controls">
          <button class="qty-btn" :disabled="!item.valid" @click="changeQty(item, -1)">-</button>
          <span class="qty-value">{{ item.quantity }}</span>
          <button class="qty-btn" :disabled="!item.valid" @click="changeQty(item, 1)">+</button>
        </div>
        <div class="line-actions">
          <p class="font-medium text-right">¥{{ item.lineAmount }}</p>
          <button class="text-xs text-gray-400 hover:text-red-500 mt-2" @click="remove(item.itemId)">删除</button>
        </div>
      </div>
      <div class="page-panel p-5 flex flex-wrap items-center justify-between gap-4">
        <div>
          <span class="text-gray-500">共 {{ totalQuantity }} 件，合计</span>
          <span class="text-2xl text-emerald-600 font-bold ml-2">¥{{ totalAmount }}</span>
        </div>
        <RouterLink
          v-if="hasValidItems()"
          to="/checkout"
          class="bg-emerald-600 text-white px-6 py-2.5 rounded-lg hover:bg-emerald-700 shrink-0"
        >
          去结算
        </RouterLink>
        <span v-else class="text-gray-400 text-sm">请移除失效商品后再结算</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.thumb-link {
  width: 5rem;
  height: 5rem;
  border-radius: 0.9rem;
  overflow: hidden;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  shrink: 0;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-empty {
  font-size: 0.75rem;
  color: #94a3b8;
}

.qty-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.qty-btn {
  width: 2rem;
  height: 2rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background: #fff;
}

.qty-value {
  min-width: 2rem;
  text-align: center;
}

.line-actions {
  min-width: 5rem;
}
</style>
