<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notifications'
import { useNotificationStore } from '@/stores/notification'
import type { Notification } from '@/types/api'

const notifyStore = useNotificationStore()

const items = ref<Notification[]>([])
const loading = ref(true)
const message = ref('')

async function load() {
  loading.value = true
  try {
    const data = await listNotifications(1, 50)
    items.value = data.list
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function readOne(n: Notification) {
  if (n.isRead) return
  await markNotificationRead(n.notificationId)
  n.isRead = true
  notifyStore.decrement()
}

async function readAll() {
  await markAllNotificationsRead()
  items.value.forEach((n) => { n.isRead = true })
  notifyStore.clear()
  message.value = '已全部标为已读'
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h1 class="page-title">消息通知</h1>
      <button
        v-if="items.some((n) => !n.isRead)"
        class="text-sm text-emerald-600 hover:underline"
        @click="readAll"
      >
        全部已读
      </button>
    </div>
    <p v-if="message" class="text-emerald-600 text-sm mb-4">{{ message }}</p>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="items.length === 0" class="text-gray-400">暂无通知</div>
    <div v-else class="space-y-3">
      <div
        v-for="n in items"
        :key="n.notificationId"
        class="page-panel p-4 cursor-pointer transition"
        :class="n.isRead ? 'opacity-70' : 'border-emerald-200 bg-emerald-50/30'"
        @click="readOne(n)"
      >
        <div class="flex items-start justify-between gap-2">
          <div class="flex-1 min-w-0">
            <p class="font-medium">{{ n.title }}</p>
            <p class="text-sm text-gray-600 mt-1">{{ n.content }}</p>
            <p class="text-xs text-gray-400 mt-2">{{ n.createdAt }}</p>
          </div>
          <span v-if="!n.isRead" class="w-2 h-2 bg-emerald-500 rounded-full shrink-0 mt-2" />
        </div>
        <RouterLink
          v-if="n.tradeNo"
          :to="`/orders/${n.tradeNo}`"
          class="text-sm text-emerald-600 hover:underline mt-2 inline-block"
          @click.stop
        >
          查看订单 →
        </RouterLink>
      </div>
    </div>
  </div>
</template>
