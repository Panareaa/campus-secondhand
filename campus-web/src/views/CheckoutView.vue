<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSettlePreview } from '@/api/cart'
import { checkout } from '@/api/trade'
import type { CartItem } from '@/types/api'

const router = useRouter()

const items = ref<CartItem[]>([])
const totalAmount = ref(0)
const contactInfo = ref('')
const contactReady = ref(false)
const remark = ref('')
const loading = ref(true)
const submitting = ref(false)
const message = ref('')

async function load() {
  loading.value = true
  message.value = ''
  try {
    const data = await getSettlePreview()
    items.value = data.items ?? []
    totalAmount.value = data.totalAmount ?? 0
    contactInfo.value = data.contactInfo ?? ''
    contactReady.value = data.contactReady ?? false
  } catch (e) {
    message.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function submit() {
  if (!contactReady.value) {
    message.value = '请先在个人中心填写联系方式'
    return
  }
  submitting.value = true
  message.value = ''
  try {
    const result = await checkout(remark.value || undefined)
    router.push({ name: 'order-detail', params: { tradeNo: result.tradeNo } })
  } catch (e) {
    message.value = e instanceof Error ? e.message : '下单失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">确认订单</h1>
    <p v-if="message" class="text-red-500 text-sm mb-4">{{ message }}</p>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="items.length === 0" class="text-gray-400">
      没有可结算的商品，
      <RouterLink to="/cart" class="text-emerald-600 hover:underline">返回购物车</RouterLink>
    </div>
    <div v-else class="grid checkout-layout gap-6">
      <div class="space-y-3">
        <div
          v-for="item in items"
          :key="item.cartEntryId"
          class="page-panel list-row list-row--compact"
        >
          <div class="thumb-box">
            <img v-if="item.itemCover" :src="item.itemCover" :alt="item.itemTitle" class="thumb-img" />
          </div>
          <div class="min-w-0">
            <p class="font-medium line-clamp-2">{{ item.itemTitle }}</p>
            <p class="text-sm text-gray-400 mt-1">¥{{ item.itemPrice }} × {{ item.quantity }}</p>
          </div>
          <p class="font-medium shrink-0 col-price">¥{{ item.lineAmount }}</p>
        </div>
        <div class="page-panel p-4">
          <label class="block text-sm text-gray-500 mb-2">备注（可选）</label>
          <textarea
            v-model="remark"
            rows="2"
            class="w-full border rounded-lg px-3 py-2 text-sm"
            placeholder="给卖家留言..."
          />
        </div>
      </div>
      <div class="page-panel p-5 h-fit">
        <h2 class="font-bold mb-4">订单信息</h2>
        <div class="text-sm space-y-2 mb-4">
          <div class="flex justify-between">
            <span class="text-gray-500">商品合计</span>
            <span>¥{{ totalAmount }}</span>
          </div>
          <div>
            <span class="text-gray-500">联系方式</span>
            <p v-if="contactReady" class="mt-1">{{ contactInfo }}</p>
            <p v-else class="mt-1 text-amber-600">
              未填写，
              <RouterLink to="/profile" class="underline">去个人中心填写</RouterLink>
            </p>
          </div>
        </div>
        <p class="text-2xl text-emerald-600 font-bold mb-4">¥{{ totalAmount }}</p>
        <button
          class="w-full bg-emerald-600 text-white py-2 rounded-lg hover:bg-emerald-700 disabled:opacity-50"
          :disabled="submitting || !contactReady"
          @click="submit"
        >
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
        <RouterLink to="/cart" class="block text-center text-sm text-gray-400 mt-3 hover:text-emerald-600">
          返回购物车
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.thumb-box {
  width: 5rem;
  height: 5rem;
  border-radius: 0.9rem;
  overflow: hidden;
  background: #f1f5f9;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.col-price {
  font-weight: 600;
}
</style>
