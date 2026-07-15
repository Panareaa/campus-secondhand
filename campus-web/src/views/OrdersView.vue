<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { listBuyerOrders, listSellerOrders } from '@/api/trade'
import type { TradeSummary } from '@/types/api'
import { tradeStatusLabel } from '@/utils/trade'

const tab = ref<'buyer' | 'seller'>('buyer')
const orders = ref<TradeSummary[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const fn = tab.value === 'buyer' ? listBuyerOrders : listSellerOrders
    const data = await fn(page.value, 10)
    orders.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(tab, () => {
  page.value = 1
  load()
})
</script>

<template>
  <div>
    <h1 class="page-title">我的订单</h1>
    <div class="flex gap-4 mb-4 border-b">
      <button
        class="pb-2 text-sm"
        :class="tab === 'buyer' ? 'border-b-2 border-emerald-600 text-emerald-600 font-medium' : 'text-gray-500'"
        @click="tab = 'buyer'"
      >
        我买到的
      </button>
      <button
        class="pb-2 text-sm"
        :class="tab === 'seller' ? 'border-b-2 border-emerald-600 text-emerald-600 font-medium' : 'text-gray-500'"
        @click="tab = 'seller'"
      >
        我卖出的
      </button>
    </div>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="orders.length === 0" class="text-gray-400">暂无订单</div>
    <div v-else class="space-y-3">
      <RouterLink
        v-for="order in orders"
        :key="order.tradeNo"
        :to="`/orders/${order.tradeNo}`"
        class="order-row page-panel list-row list-row--compact"
      >
        <div class="order-thumb">
          <img v-if="order.coverUrl" :src="order.coverUrl" alt="" class="w-full h-full object-cover" />
        </div>
        <div class="min-w-0">
          <p class="text-sm text-gray-400">{{ order.tradeNo }}</p>
          <p class="font-medium mt-1">{{ order.itemCount }} 件商品 · ¥{{ order.totalAmount }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ order.createdAt }}</p>
        </div>
        <span
          class="status-badge"
          :class="{
            'status-badge--pending': order.status === 0,
            'status-badge--confirmed': order.status === 1,
            'status-badge--done': order.status === 2,
            'status-badge--closed': order.status >= 3,
          }"
        >
          {{ tradeStatusLabel(order.status, order.statusText) }}
        </span>
      </RouterLink>
    </div>
    <p v-if="total > 10" class="text-sm text-gray-400 mt-4">共 {{ total }} 条订单</p>
  </div>
</template>

<style scoped>
.order-row {
  display: grid;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.order-row:hover {
  border-color: rgba(16, 185, 129, 0.35);
  box-shadow: 0 12px 28px rgba(5, 150, 105, 0.08);
}

.order-thumb {
  width: 4rem;
  height: 4rem;
  border-radius: 0.75rem;
  overflow: hidden;
  background: #f1f5f9;
}

.status-badge {
  font-size: 0.85rem;
  padding: 0.25rem 0.55rem;
  border-radius: 9999px;
  white-space: nowrap;
}

.status-badge--pending {
  background: #fffbeb;
  color: #d97706;
}

.status-badge--confirmed {
  background: #eff6ff;
  color: #2563eb;
}

.status-badge--done {
  background: #ecfdf5;
  color: #059669;
}

.status-badge--closed {
  background: #f1f5f9;
  color: #64748b;
}
</style>
