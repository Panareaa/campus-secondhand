<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listItems } from '@/api/items'
import type { ItemSummary } from '@/types/api'
import ItemCard from '@/components/ItemCard.vue'

const items = ref<ItemSummary[]>([])
const loading = ref(true)

const features = [
  { title: '校园同城', desc: '面向在校师生的 C2C 闲置交易' },
  { title: 'AI 帮写', desc: '发布页一键生成商品描述' },
  { title: '安全交易', desc: '订单确认、通知与积分闭环' },
]

onMounted(async () => {
  try {
    const data = await listItems({ page: 1, size: 8, status: 1 })
    items.value = data.list
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-content">
        <p class="hero-kicker">CampusRelife · 绿色循环市集</p>
        <h1 class="hero-title">让闲置物品<br class="sm:hidden" />在校园里找到新主人</h1>
        <p class="hero-desc">
          发布、搜索、收藏、下单、确认收货——完整校园二手交易链路，支持 AI 语义搜索与描述生成。
        </p>
        <div class="hero-actions">
          <RouterLink to="/items" class="hero-btn hero-btn--primary">开始逛逛</RouterLink>
          <RouterLink to="/publish" class="hero-btn hero-btn--ghost">发布闲置</RouterLink>
        </div>
      </div>
      <div class="hero-panel" aria-hidden="true">
        <div class="hero-stat">
          <span class="hero-stat-value">6</span>
          <span class="hero-stat-label">微服务协同</span>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">AI</span>
          <span class="hero-stat-label">智能搜索与帮写</span>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">100%</span>
          <span class="hero-stat-label">Docker 可部署</span>
        </div>
      </div>
    </section>

    <section class="feature-grid">
      <article v-for="feature in features" :key="feature.title" class="feature-card">
        <h3>{{ feature.title }}</h3>
        <p>{{ feature.desc }}</p>
      </article>
    </section>

    <section class="items-section">
      <div class="section-head">
        <div>
          <p class="section-kicker">Fresh Listings</p>
          <h2 class="section-title">最新上架</h2>
        </div>
        <RouterLink to="/items" class="section-link">查看全部 →</RouterLink>
      </div>

      <div v-if="loading" class="skeleton-grid item-grid">
        <div v-for="n in 4" :key="n" class="skeleton-card" />
      </div>
      <div v-else-if="items.length === 0" class="empty-state">
        <p>还没有物品上架</p>
        <RouterLink to="/publish" class="empty-link">成为第一个发布者</RouterLink>
      </div>
      <div v-else class="item-grid">
        <ItemCard v-for="item in items" :key="item.itemId" :item="item" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.hero {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 1.5rem;
  padding: 2rem;
  border-radius: 1.75rem;
  color: #fff;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.18), transparent 30%),
    linear-gradient(135deg, #047857 0%, #0f766e 45%, #115e59 100%);
  box-shadow: 0 24px 60px rgba(4, 120, 87, 0.22);
  overflow: hidden;
  position: relative;
}

.hero::after {
  content: '';
  position: absolute;
  inset: auto -3rem -3rem auto;
  width: 12rem;
  height: 12rem;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.08);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.75);
}

.hero-title {
  margin-top: 0.75rem;
  font-size: clamp(1.85rem, 4vw, 2.6rem);
  font-weight: 800;
  line-height: 1.15;
}

.hero-desc {
  margin-top: 1rem;
  max-width: 34rem;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.25rem;
  border-radius: 9999px;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.hero-btn--primary {
  color: #065f46;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.hero-btn--ghost {
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.08);
}

.hero-btn:hover {
  transform: translateY(-1px);
}

.hero-panel {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 0.85rem;
  align-content: center;
}

.hero-stat {
  padding: 1rem 1.1rem;
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
}

.hero-stat-value {
  display: block;
  font-size: 1.35rem;
  font-weight: 800;
}

.hero-stat-label {
  display: block;
  margin-top: 0.2rem;
  font-size: 0.82rem;
  color: rgba(255, 255, 255, 0.78);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.feature-card {
  padding: 1.1rem 1.2rem;
  border-radius: 1.1rem;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.feature-card h3 {
  font-size: 0.98rem;
  font-weight: 700;
  color: #065f46;
}

.feature-card p {
  margin-top: 0.35rem;
  font-size: 0.85rem;
  line-height: 1.55;
  color: #64748b;
}

.section-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.section-kicker {
  font-size: 0.72rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #14b8a6;
  font-weight: 700;
}

.section-title {
  margin-top: 0.25rem;
  font-size: 1.5rem;
  font-weight: 800;
  color: #0f172a;
}

.section-link {
  font-size: 0.9rem;
  font-weight: 600;
  color: #059669;
}

.skeleton-card {
  aspect-ratio: 0.78;
  border-radius: 1.25rem;
  background: linear-gradient(90deg, #eef2f7 25%, #f8fafc 50%, #eef2f7 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.empty-state {
  padding: 3rem 1rem;
  text-align: center;
  border-radius: 1.25rem;
  background: rgba(255, 255, 255, 0.85);
  border: 1px dashed #cbd5e1;
  color: #64748b;
}

.empty-link {
  display: inline-block;
  margin-top: 0.75rem;
  color: #059669;
  font-weight: 600;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
