<script setup lang="ts">
import type { ItemSummary } from '@/types/api'
import { RouterLink } from 'vue-router'
import { computed } from 'vue'

const props = defineProps<{ item: ItemSummary }>()

const conditionLabel = computed(() => {
  const labels = ['', '全新', '九成新', '八成新', '七成新', '六成新']
  return labels[props.item.conditionLevel] || `成色 ${props.item.conditionLevel}/5`
})
</script>

<template>
  <RouterLink :to="`/items/${item.itemId}`" class="item-card group">
    <div class="item-cover">
      <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" class="item-image" />
      <div v-else class="item-placeholder">
        <span class="placeholder-icon" aria-hidden="true">♻</span>
        <span>暂无图片</span>
      </div>
      <span class="price-tag">¥{{ item.salePrice }}</span>
      <span v-if="item.conditionLevel" class="condition-tag">{{ conditionLabel }}</span>
    </div>
    <div class="item-body">
      <p v-if="item.categoryName" class="category-chip">{{ item.categoryName }}</p>
      <h3 class="item-title">{{ item.title }}</h3>
      <p v-if="item.summary" class="item-summary">{{ item.summary }}</p>
    </div>
  </RouterLink>
</template>

<style scoped>
.item-card {
  display: block;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  border-radius: 1.25rem;
  background: #fff;
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

.item-card:hover {
  border-color: rgba(16, 185, 129, 0.25);
  box-shadow: 0 16px 36px rgba(5, 150, 105, 0.12);
}

.item-cover {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  background: linear-gradient(145deg, #ecfdf5, #f8fafc);
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.group:hover .item-image {
  transform: scale(1.05);
}

.item-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  color: #94a3b8;
  font-size: 0.85rem;
}

.placeholder-icon {
  font-size: 1.75rem;
}

.price-tag {
  position: absolute;
  left: 0.75rem;
  bottom: 0.75rem;
  padding: 0.3rem 0.65rem;
  border-radius: 9999px;
  font-size: 0.95rem;
  font-weight: 800;
  color: #fff;
  background: rgba(5, 150, 105, 0.92);
  backdrop-filter: blur(6px);
  box-shadow: 0 6px 16px rgba(5, 150, 105, 0.25);
}

.condition-tag {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  padding: 0.2rem 0.55rem;
  border-radius: 9999px;
  font-size: 0.7rem;
  color: #065f46;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(167, 243, 208, 0.8);
}

.item-body {
  padding: 1rem 1rem 1.1rem;
}

.category-chip {
  display: inline-block;
  margin-bottom: 0.45rem;
  padding: 0.15rem 0.55rem;
  border-radius: 9999px;
  font-size: 0.72rem;
  color: #0f766e;
  background: #ecfdf5;
}

.item-title {
  min-height: 2.75rem;
  font-size: 0.98rem;
  font-weight: 700;
  line-height: 1.35;
  color: #0f172a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-summary {
  margin-top: 0.35rem;
  font-size: 0.78rem;
  line-height: 1.45;
  color: #64748b;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
