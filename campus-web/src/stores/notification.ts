import { defineStore } from 'pinia'
import { ref } from 'vue'
import { countUnreadNotifications } from '@/api/notifications'
import { useAuthStore } from '@/stores/auth'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function refresh() {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) {
      unreadCount.value = 0
      return
    }
    try {
      unreadCount.value = await countUnreadNotifications()
    } catch {
      unreadCount.value = 0
    }
  }

  function decrement() {
    if (unreadCount.value > 0) unreadCount.value -= 1
  }

  function clear() {
    unreadCount.value = 0
  }

  return { unreadCount, refresh, decrement, clear }
})
