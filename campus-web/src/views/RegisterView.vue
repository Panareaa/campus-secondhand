<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const campusId = ref('')
const loginName = ref('')
const password = ref('')
const nickname = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const data = await register(campusId.value, loginName.value, password.value, nickname.value)
    auth.setSession(data.accessToken, data.userId)
    router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 px-4">
    <div class="w-full max-w-md bg-white rounded-xl shadow p-8">
      <h1 class="text-2xl font-bold text-center text-emerald-600 mb-6">注册账号</h1>
      <form class="space-y-4" @submit.prevent="submit">
        <div>
          <label class="block text-sm mb-1">学号</label>
          <input v-model="campusId" class="w-full border rounded-lg px-3 py-2" required />
        </div>
        <div>
          <label class="block text-sm mb-1">登录名</label>
          <input v-model="loginName" class="w-full border rounded-lg px-3 py-2" required />
        </div>
        <div>
          <label class="block text-sm mb-1">昵称</label>
          <input v-model="nickname" class="w-full border rounded-lg px-3 py-2" required />
        </div>
        <div>
          <label class="block text-sm mb-1">密码（至少 8 位）</label>
          <input v-model="password" type="password" minlength="8" class="w-full border rounded-lg px-3 py-2" required />
        </div>
        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
        <button
          type="submit"
          class="w-full bg-emerald-600 text-white py-2 rounded-lg hover:bg-emerald-700 disabled:opacity-50"
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="text-center text-sm mt-4 text-gray-500">
        已有账号？
        <RouterLink to="/login" class="text-emerald-600">登录</RouterLink>
      </p>
    </div>
  </div>
</template>
