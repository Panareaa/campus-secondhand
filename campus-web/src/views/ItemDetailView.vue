<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemDetail } from '@/api/items'
import { addCartItem } from '@/api/cart'
import { addWishlist } from '@/api/wishlist'
import type { ItemDetail } from '@/types/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const item = ref<ItemDetail | null>(null)
const loading = ref(true)
const message = ref('')

const itemId = computed(() => Number(route.params.id))

onMounted(async () => {
  try {
    item.value = await getItemDetail(itemId.value)
  } finally {
    loading.value = false
  }
})

async function collect() {
  if (!auth.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  message.value = ''
  try {
    await addWishlist(itemId.value)
    message.value = '已加入收藏'
  } catch (e) {
    message.value = e instanceof Error ? e.message : '收藏失败'
  }
}

async function addToCart() {
  if (!auth.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  message.value = ''
  try {
    await addCartItem(itemId.value, 1)
    message.value = '已加入购物车'
  } catch (e) {
    message.value = e instanceof Error ? e.message : '加购失败'
  }
}
</script>

<template>
  <div v-if="loading" class="text-gray-400">加载中...</div>
  <div v-else-if="!item" class="text-gray-400">物品不存在</div>
  <div v-else class="grid detail-layout">
    <div class="bg-gray-100 rounded-xl aspect-square flex items-center justify-center">
      <img
        v-if="item.coverUrl"
        :src="item.coverUrl"
        :alt="item.title"
        class="w-full h-full object-cover rounded-xl"
      />
      <span v-else class="text-gray-400">暂无图片</span>
    </div>
    <div>
      <h1 class="text-2xl font-bold mb-2">{{ item.title }}</h1>
      <p class="text-3xl text-emerald-600 font-bold mb-4">¥{{ item.salePrice }}</p>
      <p class="text-gray-600 mb-2">{{ item.summary }}</p>
      <p class="text-sm text-gray-400 mb-4">
        分类：{{ item.categoryName }} · 成色 {{ item.conditionLevel }}/5 · 库存 {{ item.availableQty }}
      </p>
      <div class="prose max-w-none text-gray-700 whitespace-pre-wrap mb-6">{{ item.description }}</div>
      <div class="flex gap-3">
        <button
          class="bg-emerald-600 text-white px-6 py-2 rounded-lg hover:bg-emerald-700"
          @click="addToCart"
        >
          加入购物车
        </button>
        <button
          class="border border-emerald-600 text-emerald-600 px-6 py-2 rounded-lg hover:bg-emerald-50"
          @click="collect"
        >
          收藏
        </button>
      </div>
      <p v-if="message" class="text-sm mt-3 text-emerald-600">{{ message }}</p>
    </div>
  </div>
</template>
