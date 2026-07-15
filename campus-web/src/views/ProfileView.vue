<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listMyItems } from '@/api/items'
import { getMyPoints, getMyProfile, updateMyContact, updateMyProfile } from '@/api/user'
import type { ItemSummary, UserProfile } from '@/types/api'

const tab = ref<'profile' | 'points' | 'items'>('profile')
const profile = ref<UserProfile | null>(null)
const points = ref(0)
const myItems = ref<ItemSummary[]>([])
const loading = ref(true)
const saving = ref(false)
const message = ref('')

const nickname = ref('')
const contactInfo = ref('')

async function loadProfile() {
  profile.value = await getMyProfile()
  nickname.value = profile.value.nickname ?? ''
  contactInfo.value = profile.value.contactInfo ?? ''
}

async function loadPoints() {
  const data = await getMyPoints()
  points.value = data.balance
}

async function loadItems() {
  const data = await listMyItems(1, 20)
  myItems.value = data.list
}

async function load() {
  loading.value = true
  message.value = ''
  try {
    await loadProfile()
    await loadPoints()
    await loadItems()
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function saveProfile() {
  saving.value = true
  message.value = ''
  try {
    profile.value = await updateMyProfile({ nickname: nickname.value })
    message.value = '资料已保存'
  } catch (e) {
    message.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

async function saveContact() {
  if (!contactInfo.value.trim()) {
    message.value = '请填写联系方式'
    return
  }
  saving.value = true
  message.value = ''
  try {
    profile.value = await updateMyContact(contactInfo.value.trim())
    message.value = '联系方式已保存'
  } catch (e) {
    message.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">个人中心</h1>
    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else>
      <div class="page-panel p-4 mb-4 flex items-center gap-4">
        <div class="w-14 h-14 bg-emerald-100 rounded-full flex items-center justify-center text-emerald-600 text-xl font-bold">
          {{ profile?.nickname?.[0] ?? '?' }}
        </div>
        <div>
          <p class="font-bold">{{ profile?.nickname }}</p>
          <p class="text-sm text-gray-400">@{{ profile?.loginName }} · 信誉 {{ profile?.reputation ?? 0 }}</p>
        </div>
      </div>
      <div class="flex gap-4 mb-4 border-b">
        <button
          v-for="t in [
            { key: 'profile', label: '资料' },
            { key: 'points', label: '积分' },
            { key: 'items', label: '我的发布' },
          ]"
          :key="t.key"
          class="pb-2 text-sm"
          :class="tab === t.key ? 'border-b-2 border-emerald-600 text-emerald-600 font-medium' : 'text-gray-500'"
          @click="tab = t.key as typeof tab"
        >
          {{ t.label }}
        </button>
      </div>
      <p v-if="message" class="text-emerald-600 text-sm mb-4">{{ message }}</p>
      <div v-if="tab === 'profile'" class="profile-layout">
        <div class="page-panel p-5 space-y-4">
          <div>
            <label class="block text-sm text-gray-500 mb-1">昵称</label>
            <input v-model="nickname" class="w-full border rounded-lg px-3 py-2" />
          </div>
          <div>
            <label class="block text-sm text-gray-500 mb-1">联系方式（下单必填）</label>
            <input
              v-model="contactInfo"
              class="w-full border rounded-lg px-3 py-2"
              placeholder="微信/手机号等"
            />
          </div>
          <div class="flex flex-wrap gap-3">
            <button
              class="bg-emerald-600 text-white px-4 py-2 rounded-lg hover:bg-emerald-700 disabled:opacity-50"
              :disabled="saving"
              @click="saveProfile"
            >
              保存昵称
            </button>
            <button
              class="border border-emerald-600 text-emerald-600 px-4 py-2 rounded-lg hover:bg-emerald-50 disabled:opacity-50"
              :disabled="saving"
              @click="saveContact"
            >
              保存联系方式
            </button>
          </div>
        </div>
        <div class="page-panel p-5 text-sm text-gray-500 leading-7">
          <p class="font-semibold text-gray-700 mb-2">温馨提示</p>
          <p>联系方式用于下单时展示给卖家，请填写真实可用的微信或手机号。</p>
          <p class="mt-2">昵称会展示在物品详情与个人主页中。</p>
        </div>
      </div>
      <div v-else-if="tab === 'points'" class="page-panel p-8 text-center max-w-xl">
        <p class="text-gray-500 text-sm mb-2">绿色积分</p>
        <p class="text-4xl font-bold text-emerald-600">{{ points }}</p>
        <p class="text-xs text-gray-400 mt-4">完成交易可获得积分奖励</p>
      </div>
      <div v-else class="space-y-3">
        <div v-if="myItems.length === 0" class="text-gray-400">还没有发布物品</div>
        <div
          v-for="item in myItems"
          :key="item.itemId"
          class="page-panel list-row list-row--compact"
        >
          <RouterLink :to="`/items/${item.itemId}`" class="item-thumb">
            <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" class="w-full h-full object-cover" />
          </RouterLink>
          <div class="min-w-0">
            <RouterLink :to="`/items/${item.itemId}`" class="font-medium hover:text-emerald-600 line-clamp-2">
              {{ item.title }}
            </RouterLink>
            <p class="text-emerald-600 font-bold mt-1">¥{{ item.salePrice }}</p>
          </div>
          <span class="text-xs text-gray-400 shrink-0">{{ item.publishedAt }}</span>
        </div>
        <RouterLink to="/publish" class="inline-block text-emerald-600 text-sm hover:underline">去发布新物品 →</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.item-thumb {
  width: 4rem;
  height: 4rem;
  border-radius: 0.75rem;
  overflow: hidden;
  background: #f1f5f9;
  display: block;
}
</style>
