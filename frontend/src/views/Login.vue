<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-orb orb1"></div>
    <div class="bg-orb orb2"></div>
    <div class="bg-orb orb3"></div>

    <div class="login-card">
      <!-- Logo -->
      <div class="login-logo">
        <div class="logo-icon">
          <el-icon size="28"><ChatDotRound /></el-icon>
        </div>
        <div class="logo-text">
          <h1>RAGFlow 智能助手</h1>
          <p>Smart Assistant</p>
        </div>
      </div>

      <el-divider />

      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="onSubmit" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            clearable
            autocomplete="username"
          >
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            autocomplete="current-password"
            @keyup.enter="onSubmit"
          >
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item style="margin-bottom:0">
          <el-button
            type="primary" size="large" :loading="loading"
            style="width:100%;font-size:15px;letter-spacing:2px"
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">RAGFlow Admin &nbsp;&middot;&nbsp; v1.0</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码',   trigger: 'blur' }]
}

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/chat')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 30%, #0f3460 60%, #1a1a2e 100%);
  position: relative;
  overflow: hidden;
}

/* 背景光晕 */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: .3;
  animation: float 10s ease-in-out infinite;
}
.orb1 { width:500px; height:500px; background: radial-gradient(circle, #409EFF 0%, #2563eb 100%); top:-150px; left:-150px; animation-delay:0s; }
.orb2 { width:400px; height:400px; background: radial-gradient(circle, #764ba2 0%, #6366f1 100%); bottom:-120px; right:-120px; animation-delay:-3s; }
.orb3 { width:250px; height:250px; background: radial-gradient(circle, #67C23A 0%, #10b981 100%); bottom:25%; left:8%; animation-delay:-5s; opacity:.18; }

@keyframes float {
  0%,100% { transform: translateY(0) scale(1); }
  50%      { transform: translateY(-24px) scale(1.06); }
}

/* 卡片 */
.login-card {
  width: min(420px, 90vw);
  background: rgba(255,255,255,.98);
  border-radius: 24px;
  padding: 40px 40px 32px;
  box-shadow: 0 32px 80px rgba(0,0,0,.4), 0 0 0 1px rgba(255,255,255,.08);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px);
}

/* 顶部彩色线 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #6366f1, #764ba2, #67C23A);
  border-radius: 24px 24px 0 0;
}

/* Logo 区域 */
.login-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 6px;
}
.logo-icon {
  width: 56px; height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #409EFF 0%, #764ba2 100%);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 8px 24px rgba(64,158,255,.35);
}
.logo-text h1 { margin: 0; font-size: 20px; font-weight: 700; color: #0f172a; letter-spacing: .5px; }
.logo-text p  { margin: 4px 0 0; font-size: 12px; color: #94a3b8; letter-spacing: 1.5px; text-transform: uppercase; }

.login-form { margin-top: 4px; }

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: #c0c4cc;
  letter-spacing: .5px;
}

@media (max-width: 767px) {
  .login-card {
    padding: 32px 24px 28px;
    border-radius: 20px;
  }
  .logo-icon { width: 48px; height: 48px; border-radius: 14px; }
  .logo-text h1 { font-size: 17px; }
}
</style>
