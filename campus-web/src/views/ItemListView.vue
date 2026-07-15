<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { aiSearch } from '@/api/ai'
import { listCategories, listItems } from '@/api/items'
import type { Category, ItemSummary } from '@/types/api'
import ItemCard from '@/components/ItemCard.vue'

const items = ref<ItemSummary[]>([])
const categories = ref<Category[]>([])
const keyword = ref('')
const categoryId = ref<number | undefined>()
const searchMode = ref<'keyword' | 'ai'>('keyword')
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const aiDegraded = ref(false)
const parsedHint = ref('')

async function loadKeywordSearch() {
  loading.value = true
  aiDegraded.value = false
  parsedHint.value = ''
  try {
    const data = await listItems({
      page: page.value,
      size: 12,
      status: 1,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
    })
    items.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadAiSearch() {
  if (!keyword.value.trim()) {
    parsedHint.value = '请输入语义搜索描述'
    return
  }
  loading.value = true
  try {
    const data = await aiSearch(keyword.value, page.value, 12)
    items.value = data.items.list
    total.value = data.items.total
    aiDegraded.value = data.degraded
    const cond = data.parsedCondition
    const parts: string[] = []
    if (cond.categoryId) parts.push(`分类#${cond.categoryId}`)
    if (cond.keywords?.length) parts.push(`关键词:${cond.keywords.join('/')}`)
    if (cond.maxPrice != null) parts.push(`≤¥${cond.maxPrice}`)
    parsedHint.value = parts.length ? `AI 解析：${parts.join(' · ')}` : 'AI 搜索完成'
  } catch (e) {
    parsedHint.value = e instanceof Error ? e.message : 'AI 搜索失败'
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function load() {
  if (searchMode.value === 'ai') {
    await loadAiSearch()
  } else {
    await loadKeywordSearch()
  }
}

onMounted(async () => {
  categories.value = await listCategories(0)
  await load()
})

watch([page, categoryId], () => {
  if (searchMode.value === 'keyword') {
    load()
  }
})

function search() {
  page.value = 1
  load()
}

function switchMode(mode: 'keyword' | 'ai') {
  searchMode.value = mode
  page.value = 1
  if (mode === 'keyword') {
    loadKeywordSearch()
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">逛逛</h1>
    <div class="flex flex-wrap gap-2 mb-4">
      <button
        class="px-3 py-1 rounded-full text-sm border"
        :class="searchMode === 'keyword' ? 'bg-emerald-600 text-white border-emerald-600' : ''"
        @click="switchMode('keyword')"
      >
        关键词搜索
      </button>
      <button
        class="px-3 py-1 rounded-full text-sm border"
        :class="searchMode === 'ai' ? 'bg-emerald-600 text-white border-emerald-600' : ''"
        @click="switchMode('ai')"
      >
        AI 语义搜索
      </button>
      <span v-if="aiDegraded" class="text-xs text-amber-600 self-center">（规则降级）</span>
    </div>

    <div class="flex flex-wrap gap-3 mb-6">
      <input
        v-model="keyword"
        :placeholder="searchMode === 'ai' ? '例如：宿舍用的小台灯，便宜点的' : '搜索闲置...'"
        class="flex-1 min-w-48 border rounded-lg px-3 py-2"
        @keyup.enter="search"
      />
      <select
        v-if="searchMode === 'keyword'"
        v-model="categoryId"
        class="border rounded-lg px-3 py-2"
      >
        <option :value="undefined">全部分类</option>
        <option v-for="c in categories" :key="c.categoryId" :value="c.categoryId">{{ c.name }}</option>
      </select>
      <button class="bg-emerald-600 text-white px-4 py-2 rounded-lg" @click="search">搜索</button>
    </div>

    <p v-if="parsedHint" class="text-sm text-gray-500 mb-4">{{ parsedHint }}</p>

    <div v-if="loading" class="text-gray-400">加载中...</div>
    <div v-else-if="items.length === 0" class="text-gray-400">没有找到相关物品</div>
    <div v-else class="item-grid">
      <ItemCard v-for="item in items" :key="item.itemId" :item="item" />
    </div>

    <div v-if="total > 12" class="flex justify-center gap-2 mt-6">
      <button class="px-3 py-1 border rounded" :disabled="page <= 1" @click="page--; load()">上一页</button>
      <span class="px-3 py-1">{{ page }}</span>
      <button class="px-3 py-1 border rounded" :disabled="page * 12 >= total" @click="page++; load()">下一页</button>
    </div>
  </div>
</template>
