<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const loginName = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const data = await login(loginName.value, password.value)
    auth.setSession(data.accessToken, data.userId)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-shell">
      <section class="auth-brand">
        <div class="brand-badge" aria-hidden="true">♻</div>
        <h1>拾光校园</h1>
        <p class="brand-tagline">绿色循环市集 · 让闲置继续发光</p>
        <ul class="brand-points">
          <li>完整 C2C 交易闭环</li>
          <li>AI 搜索与描述生成</li>
          <li>微服务架构可演示部署</li>
        </ul>
      </section>

      <section class="auth-form-panel">
        <div class="form-card">
          <div class="form-head">
            <h2>欢迎回来</h2>
            <p>登录后继续浏览、发布与交易</p>
          </div>

          <form class="auth-form" @submit.prevent="submit">
            <div class="field">
              <label for="loginName">登录名</label>
              <input
                id="loginName"
                v-model="loginName"
                class="field-input"
                placeholder="请输入登录名"
                autocomplete="username"
                required
              />
            </div>
            <div class="field">
              <label for="password">密码</label>
              <input
                id="password"
                v-model="password"
                type="password"
                class="field-input"
                placeholder="请输入密码"
                autocomplete="current-password"
                required
              />
            </div>

            <p v-if="error" class="form-error">{{ error }}</p>

            <button type="submit" class="submit-btn" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <p class="form-footer">
            还没有账号？
            <RouterLink to="/register" class="form-link">立即注册</RouterLink>
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  background:
    radial-gradient(circle at top left, rgba(16, 185, 129, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(13, 148, 136, 0.1), transparent 32%),
    #f8fafc;
}

.auth-shell {
  width: 100%;
  max-width: 920px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
  border-radius: 1.75rem;
  background: #fff;
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(226, 232, 240, 0.95);
}

.auth-brand {
  padding: 2.5rem 2rem;
  color: #fff;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.16), transparent 28%),
    linear-gradient(160deg, #047857 0%, #0f766e 55%, #134e4a 100%);
}

.brand-badge {
  width: 3rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  font-size: 1.5rem;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.auth-brand h1 {
  margin-top: 1.25rem;
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.2;
}

.brand-tagline {
  margin-top: 0.65rem;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.6;
}

.brand-points {
  margin-top: 1.75rem;
  display: grid;
  gap: 0.75rem;
  list-style: none;
  padding: 0;
}

.brand-points li {
  position: relative;
  padding-left: 1.25rem;
  color: rgba(255, 255, 255, 0.88);
  font-size: 0.92rem;
}

.brand-points li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #bbf7d0;
  font-weight: 700;
}

.auth-form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.form-card {
  width: 100%;
  max-width: 22rem;
}

.form-head h2 {
  font-size: 1.6rem;
  font-weight: 800;
  color: #0f172a;
}

.form-head p {
  margin-top: 0.35rem;
  color: #64748b;
  font-size: 0.92rem;
}

.auth-form {
  margin-top: 1.5rem;
  display: grid;
  gap: 1rem;
}

.field label {
  display: block;
  margin-bottom: 0.4rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.field-input {
  width: 100%;
  padding: 0.75rem 0.9rem;
  border-radius: 0.9rem;
  border: 1px solid #dbeafe;
  background: #f8fafc;
  color: #0f172a;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field-input:focus {
  outline: none;
  border-color: #34d399;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.12);
}

.form-error {
  padding: 0.65rem 0.8rem;
  border-radius: 0.75rem;
  font-size: 0.85rem;
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.submit-btn {
  width: 100%;
  margin-top: 0.25rem;
  padding: 0.8rem 1rem;
  border-radius: 0.95rem;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #059669, #0d9488);
  box-shadow: 0 12px 24px rgba(5, 150, 105, 0.22);
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.65;
}

.form-footer {
  margin-top: 1.25rem;
  text-align: center;
  font-size: 0.9rem;
  color: #64748b;
}

.form-link {
  color: #059669;
  font-weight: 700;
}

@media (max-width: 768px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-brand {
    padding: 1.75rem 1.5rem 1.5rem;
  }

  .brand-points {
    display: none;
  }

  .auth-form-panel {
    padding: 1.5rem;
  }
}
</style>
