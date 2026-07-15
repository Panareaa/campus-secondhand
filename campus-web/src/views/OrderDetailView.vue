<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { cancelTrade, completeTrade, confirmTrade, getTradeDetail } from '@/api/trade'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import type { TradeDetail } from '@/types/api'
import { tradeStatusLabel } from '@/utils/trade'

const route = useRoute()
const auth = useAuthStore()
const notifyStore = useNotificationStore()

const trade = ref<TradeDetail | null>(null)
const loading = ref(true)
const acting = ref(false)
const message = ref('')

const tradeNo = computed(() => String(route.params.tradeNo))
const isBuyer = computed(() => trade.value && auth.userId === trade.value.buyerId)
const isSeller = computed(() => trade.value && auth.userId === trade.value.sellerId)

async function load() {
  loading.value = true
  message.value = ''
  try {
    trade.value = await getTradeDetail(tradeNo.value)
  } catch (e) {
    message.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function act(fn: () => Promise<TradeDetail>, successMsg: string) {
  acting.value = true
  message.value = ''
  try {
    trade.value = await fn()
    message.value = successMsg
    await notifyStore.refresh()
  } catch (e) {
    message.value = e instanceof Error ? e.message : '操作失败'
  } finally {
    acting.value = false
  }
}

function confirm() {
  act(() => confirmTrade(tradeNo.value), '已确认订单')
}

function complete() {
  act(() => completeTrade(tradeNo.value), '交易完成，积分已发放')
}

function cancel() {
  act(() => cancelTrade(tradeNo.value), '订单已取消')
}
</script>

<template>
  <div>
    <RouterLink to="/orders" class="text-sm text-gray-400 hover:text-emerald-600 mb-4 inline-block">
      ← 返回订单列表
    </RouterLink>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="!trade" class="text-gray-400">订单不存在</div>
    <div v-else>
      <div class="flex items-start justify-between mb-6">
        <div>
          <h1 class="text-xl font-bold">订单详情</h1>
          <p class="text-sm text-gray-400 mt-1">{{ trade.tradeNo }}</p>
        </div>
        <span
          class="text-sm px-3 py-1 rounded"
          :class="{
            'bg-amber-50 text-amber-600': trade.status === 0,
            'bg-blue-50 text-blue-600': trade.status === 1,
            'bg-emerald-50 text-emerald-600': trade.status === 2,
            'bg-gray-100 text-gray-500': trade.status >= 3,
          }"
        >
          {{ tradeStatusLabel(trade.status, trade.statusText) }}
        </span>
      </div>
      <p v-if="message" class="text-emerald-600 text-sm mb-4">{{ message }}</p>
      <div class="bg-white border rounded-xl p-4 mb-4 space-y-3">
        <div
          v-for="line in trade.lines"
          :key="line.lineId"
          class="flex gap-4 items-center"
        >
          <RouterLink :to="`/items/${line.itemId}`" class="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden shrink-0">
            <img v-if="line.itemCover" :src="line.itemCover" :alt="line.itemTitle" class="w-full h-full object-cover" />
          </RouterLink>
          <div class="flex-1 min-w-0">
            <RouterLink :to="`/items/${line.itemId}`" class="font-medium hover:text-emerald-600 line-clamp-1">
              {{ line.itemTitle }}
            </RouterLink>
            <p class="text-sm text-gray-400 mt-1">¥{{ line.unitPrice }} × {{ line.quantity }}</p>
          </div>
          <p class="font-medium">¥{{ line.lineAmount }}</p>
        </div>
      </div>
      <div class="bg-white border rounded-xl p-4 mb-4 text-sm space-y-2">
        <div class="flex justify-between">
          <span class="text-gray-500">订单金额</span>
          <span class="text-emerald-600 font-bold text-lg">¥{{ trade.totalAmount }}</span>
        </div>
        <div v-if="isSeller && trade.buyerContact" class="flex justify-between">
          <span class="text-gray-500">买家联系方式</span>
          <span>{{ trade.buyerContact }}</span>
        </div>
        <div v-if="trade.remark" class="flex justify-between">
          <span class="text-gray-500">备注</span>
          <span>{{ trade.remark }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">下单时间</span>
          <span>{{ trade.createdAt }}</span>
        </div>
        <div v-if="trade.confirmedAt" class="flex justify-between">
          <span class="text-gray-500">确认时间</span>
          <span>{{ trade.confirmedAt }}</span>
        </div>
        <div v-if="trade.completedAt" class="flex justify-between">
          <span class="text-gray-500">完成时间</span>
          <span>{{ trade.completedAt }}</span>
        </div>
      </div>
      <div v-if="trade.status === 0" class="flex gap-3">
        <button
          v-if="isSeller"
          class="bg-emerald-600 text-white px-6 py-2 rounded-lg hover:bg-emerald-700 disabled:opacity-50"
          :disabled="acting"
          @click="confirm"
        >
          确认订单
        </button>
        <button
          class="border border-gray-300 px-6 py-2 rounded-lg hover:bg-gray-50 disabled:opacity-50"
          :disabled="acting"
          @click="cancel"
        >
          取消订单
        </button>
      </div>
      <div v-else-if="trade.status === 1 && isBuyer" class="flex gap-3">
        <button
          class="bg-emerald-600 text-white px-6 py-2 rounded-lg hover:bg-emerald-700 disabled:opacity-50"
          :disabled="acting"
          @click="complete"
        >
          确认收货
        </button>
      </div>
    </div>
  </div>
</template>
