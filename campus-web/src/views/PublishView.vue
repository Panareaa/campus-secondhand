<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { aiDescribe } from '@/api/ai'
import { listCategories, publishItem } from '@/api/items'

const router = useRouter()

const categories = ref<{ categoryId: number; name: string }[]>([])
const categoryId = ref<number>()
const title = ref('')
const summary = ref('')
const description = ref('')
const salePrice = ref<number>()
const conditionLevel = ref(3)
const loading = ref(false)
const aiLoading = ref(false)
const error = ref('')
const aiHint = ref('')

const categoryName = computed(() =>
  categories.value.find((c) => c.categoryId === categoryId.value)?.name ?? '',
)

onMounted(async () => {
  const data = await listCategories(0)
  categories.value = data
  if (data.length > 0) {
    categoryId.value = data[0].categoryId
  }
})

async function generateDescription() {
  if (!title.value) {
    error.value = '请先填写标题'
    return
  }
  aiLoading.value = true
  error.value = ''
  aiHint.value = ''
  try {
    const result = await aiDescribe({
      title: title.value,
      categoryName: categoryName.value,
      conditionLevel: conditionLevel.value,
      originalPrice: salePrice.value,
    })
    description.value = result.description
    if (result.suggestedTags?.length && !summary.value) {
      summary.value = result.suggestedTags.slice(0, 3).join(' · ')
    }
    aiHint.value = result.degraded ? '已使用模板降级生成' : `由 ${result.modelName} 生成`
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'AI 生成失败'
  } finally {
    aiLoading.value = false
  }
}

async function submit(publish: boolean) {
  if (!categoryId.value || !title.value || !salePrice.value) {
    error.value = '请填写分类、标题和价格'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const result = await publishItem({
      categoryId: categoryId.value,
      title: title.value,
      summary: summary.value,
      description: description.value,
      salePrice: salePrice.value,
      conditionLevel: conditionLevel.value,
      publish,
    })
    router.push(`/items/${result.itemId}`)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发布失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-shell--wide">
    <h1 class="page-title">发布闲置</h1>
    <div class="page-panel p-6 sm:p-8">
    <form class="space-y-4" @submit.prevent="submit(true)">
      <div>
        <label class="block text-sm mb-1">分类</label>
        <select v-model="categoryId" class="w-full border rounded-lg px-3 py-2">
          <option v-for="c in categories" :key="c.categoryId" :value="c.categoryId">{{ c.name }}</option>
        </select>
      </div>
      <div>
        <label class="block text-sm mb-1">标题</label>
        <input v-model="title" class="w-full border rounded-lg px-3 py-2" required />
      </div>
      <div>
        <label class="block text-sm mb-1">简介</label>
        <input v-model="summary" class="w-full border rounded-lg px-3 py-2" />
      </div>
      <div>
        <div class="flex items-center justify-between mb-1">
          <label class="block text-sm">详细描述</label>
          <button
            type="button"
            class="text-sm text-emerald-600 hover:text-emerald-700 disabled:opacity-50"
            :disabled="aiLoading"
            @click="generateDescription"
          >
            {{ aiLoading ? '生成中...' : 'AI 帮我写' }}
          </button>
        </div>
        <textarea v-model="description" rows="5" class="w-full border rounded-lg px-3 py-2" />
        <p v-if="aiHint" class="text-xs text-gray-400 mt-1">{{ aiHint }}</p>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm mb-1">售价</label>
          <input v-model.number="salePrice" type="number" min="0.01" step="0.01" class="w-full border rounded-lg px-3 py-2" required />
        </div>
        <div>
          <label class="block text-sm mb-1">成色 (1-5)</label>
          <input v-model.number="conditionLevel" type="number" min="1" max="5" class="w-full border rounded-lg px-3 py-2" />
        </div>
      </div>
      <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
      <div class="flex gap-3">
        <button type="submit" class="bg-emerald-600 text-white px-6 py-2 rounded-lg" :disabled="loading">
          发布上架
        </button>
        <button type="button" class="border px-6 py-2 rounded-lg" :disabled="loading" @click="submit(false)">
          保存草稿
        </button>
      </div>
    </form>
    </div>
  </div>
</template>
