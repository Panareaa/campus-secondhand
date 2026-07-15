<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

const auth = useAuthStore()
const notifyStore = useNotificationStore()
const router = useRouter()
const route = useRoute()

const navItems = computed(() => {
  const items = [
    { to: '/', label: '首页', match: (p: string) => p === '/' },
    { to: '/items', label: '逛逛', match: (p: string) => p === '/items' || p.startsWith('/items/') },
  ]
  if (auth.isLoggedIn) {
    items.push(
      { to: '/wishlist', label: '收藏', match: (p: string) => p === '/wishlist' },
      { to: '/cart', label: '购物车', match: (p: string) => p === '/cart' || p === '/checkout' },
      { to: '/orders', label: '订单', match: (p: string) => p.startsWith('/orders') },
      { to: '/profile', label: '我的', match: (p: string) => p === '/profile' },
    )
  }
  return items
})

function isActive(match: (path: string) => boolean) {
  return match(route.path)
}

function logout() {
  auth.logout()
  notifyStore.clear()
  router.push('/login')
}

onMounted(() => {
  if (auth.isLoggedIn) notifyStore.refresh()
})

watch(
  () => auth.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) notifyStore.refresh()
    else notifyStore.clear()
  },
)
</script>

<template>
  <div class="app-shell min-h-screen">
    <header class="site-header sticky top-0 z-20">
      <div class="site-container px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
        <RouterLink to="/" class="brand flex items-center gap-3 shrink-0">
          <span class="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" class="w-5 h-5">
              <path
                d="M12 3c-4 3.5-7 6.5-7 10a7 7 0 0 0 14 0c0-3.5-3-6.5-7-10Z"
                fill="currentColor"
                opacity="0.9"
              />
              <path d="M12 8v8M9 11h6" stroke="#fff" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </span>
          <span>
            <span class="brand-title">拾光校园</span>
            <span class="brand-sub hidden sm:block">CampusRelife</span>
          </span>
        </RouterLink>

        <nav class="flex items-center gap-1 sm:gap-2 text-sm overflow-x-auto">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            :class="{ 'nav-link--active': isActive(item.match) }"
          >
            {{ item.label }}
          </RouterLink>

          <RouterLink
            v-if="auth.isLoggedIn"
            to="/notifications"
            class="nav-link relative"
            :class="{ 'nav-link--active': route.path === '/notifications' }"
          >
            消息
            <span v-if="notifyStore.unreadCount > 0" class="notify-badge">
              {{ notifyStore.unreadCount > 99 ? '99+' : notifyStore.unreadCount }}
            </span>
          </RouterLink>

          <RouterLink v-if="auth.isLoggedIn" to="/publish" class="publish-btn ml-1 shrink-0">
            发布闲置
          </RouterLink>

          <RouterLink v-if="!auth.isLoggedIn" to="/login" class="login-btn ml-1 shrink-0">
            登录
          </RouterLink>
          <button v-else type="button" class="logout-btn shrink-0" @click="logout">退出</button>
        </nav>
      </div>
    </header>

    <main class="site-container px-4 sm:px-6 py-8 w-full">
      <RouterView />
    </main>

    <footer class="site-footer mt-auto">
      <div class="site-container px-4 sm:px-6 py-6 text-center text-sm text-gray-500">
        拾光校园 · 绿色循环市集 · 云南大学软件服务工程期末项目
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app-shell {
  background:
    radial-gradient(circle at top right, rgba(16, 185, 129, 0.08), transparent 28%),
    radial-gradient(circle at bottom left, rgba(13, 148, 136, 0.06), transparent 32%),
    #f8fafc;
  display: flex;
  flex-direction: column;
}

.site-container {
  width: 100%;
  max-width: 82rem;
  margin-left: auto;
  margin-right: auto;
}

.site-header {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.04);
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 0.9rem;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #ecfdf5;
  box-shadow: 0 8px 20px rgba(5, 150, 105, 0.25);
}

.brand-title {
  display: block;
  font-size: 1.05rem;
  font-weight: 800;
  line-height: 1.2;
  color: #065f46;
}

.brand-sub {
  display: block;
  font-size: 0.7rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #64748b;
}

.nav-link {
  padding: 0.45rem 0.75rem;
  border-radius: 9999px;
  color: #475569;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: #059669;
  background: rgba(16, 185, 129, 0.08);
}

.nav-link--active {
  color: #047857;
  background: rgba(16, 185, 129, 0.12);
  font-weight: 600;
}

.notify-badge {
  position: absolute;
  top: -0.15rem;
  right: -0.35rem;
  min-width: 1rem;
  height: 1rem;
  padding: 0 0.25rem;
  font-size: 0.65rem;
  line-height: 1rem;
  text-align: center;
  color: #fff;
  background: #ef4444;
  border-radius: 9999px;
  box-shadow: 0 0 0 2px #fff;
}

.publish-btn,
.login-btn {
  padding: 0.5rem 0.95rem;
  border-radius: 9999px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #059669, #0d9488);
  box-shadow: 0 8px 18px rgba(5, 150, 105, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.publish-btn:hover,
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(5, 150, 105, 0.28);
}

.logout-btn {
  padding: 0.45rem 0.75rem;
  border-radius: 9999px;
  color: #64748b;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  color: #dc2626;
  background: rgba(254, 226, 226, 0.8);
}

.site-footer {
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.65);
}
</style>
