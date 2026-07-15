<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listWishlist, moveToCart, removeWishlist } from '@/api/wishlist'
import type { WishlistItem } from '@/types/api'
import { RouterLink } from 'vue-router'

const items = ref<WishlistItem[]>([])
const loading = ref(true)
const message = ref('')

async function load() {
  loading.value = true
  try {
    const data = await listWishlist()
    items.value = data.list
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function remove(itemId: number) {
  await removeWishlist(itemId)
  await load()
}

async function toCart(itemId: number) {
  message.value = ''
  try {
    await moveToCart(itemId)
    message.value = '已移入购物车'
    await load()
  } catch (e) {
    message.value = e instanceof Error ? e.message : '操作失败'
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">我的收藏</h1>
    <p v-if="message" class="text-emerald-600 text-sm mb-4">{{ message }}</p>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="items.length === 0" class="page-panel p-8 text-center text-gray-400">还没有收藏，去逛逛吧</div>
    <div v-else class="space-y-3">
      <div v-for="item in items" :key="item.wishlistId" class="page-panel list-row list-row--actions">
        <RouterLink :to="`/items/${item.itemId}`" class="thumb-link">
          <img v-if="item.itemCover" :src="item.itemCover" :alt="item.itemTitle" class="thumb-img" />
          <span v-else class="thumb-empty">无图</span>
        </RouterLink>
        <div class="min-w-0">
          <RouterLink :to="`/items/${item.itemId}`" class="font-medium hover:text-emerald-600 line-clamp-2">
            {{ item.itemTitle || `物品 #${item.itemId}` }}
          </RouterLink>
          <p class="text-emerald-600 font-bold mt-1">¥{{ item.salePrice ?? '-' }}</p>
          <p v-if="!item.valid" class="text-xs text-red-400 mt-1">已失效</p>
        </div>
        <div class="action-group">
          <button
            class="text-sm bg-emerald-600 text-white px-4 py-2 rounded-lg"
            :disabled="!item.valid"
            @click="toCart(item.itemId)"
          >
            移入购物车
          </button>
          <button class="text-sm text-gray-400 hover:text-red-500" @click="remove(item.itemId)">取消收藏</button>
        </div>
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

.action-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}
</style>
