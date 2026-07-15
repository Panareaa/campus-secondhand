import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const TOKEN_KEY = 'campus_relife_token'
const USER_ID_KEY = 'campus_relife_user_id'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userId = ref(Number(localStorage.getItem(USER_ID_KEY) || 0))

  const isLoggedIn = computed(() => !!token.value)

  function setSession(accessToken: string, id: number) {
    token.value = accessToken
    userId.value = id
    localStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(USER_ID_KEY, String(id))
  }

  function logout() {
    token.value = ''
    userId.value = 0
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_ID_KEY)
  }

  return { token, userId, isLoggedIn, setSession, logout }
})
