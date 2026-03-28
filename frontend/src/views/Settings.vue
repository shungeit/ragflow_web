<template>
  <div class="page">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-card class="section-card">
      <el-tabs v-model="activeTab">
        <!-- 基本设置 -->
        <el-tab-pane label="RAGFlow 配置" name="basic">
          <el-form label-position="top" class="settings-form">
            <el-form-item label="RAGFlow 服务地址">
              <el-input v-model="config.ragflowUrl" placeholder="http://your-ragflow-server" clearable>
                <template #prepend><el-icon><Link /></el-icon></template>
              </el-input>
              <div class="form-tip">例如: http://192.168.1.100 或 http://ragflow.example.com</div>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="config.apiKey" placeholder="ragflow-xxxxxxxx" clearable show-password>
                <template #prepend><el-icon><Key /></el-icon></template>
              </el-input>
              <div class="form-tip">在 RAGFlow 系统设置中获取 API 密钥</div>
            </el-form-item>
            <el-form-item label="连接状态">
              <div class="health-status">
                <el-tag :type="healthOk ? 'success' : (healthOk === null ? 'info' : 'danger')" size="large" effect="dark">
                  {{ healthOk === true ? '已连接' : (healthOk === null ? '未检测' : '连接失败') }}
                </el-tag>
                <span v-if="healthOk === true" class="health-hint success">RAGFlow 服务运行正常</span>
                <span v-else-if="healthOk === false" class="health-hint danger">请检查服务地址和 API Key 是否正确</span>
                <span v-else class="health-hint">点击"检测连接"按钮测试</span>
              </div>
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" @click="saveConfig" :loading="saving">
                保存配置
              </el-button>
              <el-button @click="saveAndCheck" :loading="checking || saving">
                保存并检测连接
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 个人中心 -->
        <el-tab-pane label="个人中心" name="profile">
          <el-form label-position="top" class="settings-form">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.username" readonly />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="设置昵称" />
            </el-form-item>
            <el-divider content-position="left">修改密码</el-divider>
            <el-form-item label="当前密码">
              <el-input v-model="profileForm.oldPassword" type="password" show-password placeholder="输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="profileForm.newPassword" type="password" show-password placeholder="输入新密码（至少6位）" />
              <div v-if="profileForm.newPassword && profileForm.newPassword.length < 6" class="form-tip danger">密码长度不能少于6位</div>
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="profileForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
              <div v-if="profileForm.confirmPassword && profileForm.confirmPassword !== profileForm.newPassword" class="form-tip danger">两次输入的密码不一致</div>
              <div v-else-if="profileForm.confirmPassword && profileForm.confirmPassword === profileForm.newPassword" class="form-tip success">密码一致</div>
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" size="small" @click="saveProfile">保存</el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 关于 -->
        <el-tab-pane label="关于" name="about">
          <div class="about-section">
            <div class="about-logo">
              <div class="about-icon">
                <el-icon size="32"><ChatDotRound /></el-icon>
              </div>
              <div>
                <h3>RAGFlow 智能助手</h3>
                <p>版本 1.0.0</p>
              </div>
            </div>
            <el-descriptions :column="1" border size="small" class="about-info">
              <el-descriptions-item label="前端框架">Vue 3 + Element Plus</el-descriptions-item>
              <el-descriptions-item label="构建工具">Vite 5</el-descriptions-item>
              <el-descriptions-item label="AI 引擎">RAGFlow</el-descriptions-item>
              <el-descriptions-item label="语音支持">Web Speech API</el-descriptions-item>
              <el-descriptions-item label="许可协议">Apache 2.0</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Link, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const route = useRoute()
const userStore = useUserStore()

const activeTab = ref(route.query?.tab || 'basic')
const healthOk = ref(null)
const checking = ref(false)
const saving = ref(false)

const config = reactive({
  ragflowUrl: '',
  apiKey: ''
})

const profileForm = reactive({
  nickname: userStore.nickname || '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

async function loadConfig() {
  try {
    const res = await request.get('/settings/ragflow')
    config.ragflowUrl = res.data?.baseUrl || ''
    config.apiKey = res.data?.apiKey || ''
  } catch (e) {
    config.ragflowUrl = '(获取失败)'
    config.apiKey = ''
  }
}

async function saveConfig() {
  if (!config.ragflowUrl) {
    ElMessage.warning('请输入 RAGFlow 服务地址')
    return
  }
  if (!config.apiKey) {
    ElMessage.warning('请输入 API Key')
    return
  }
  saving.value = true
  try {
    await request.put('/settings/ragflow', {
      baseUrl: config.ragflowUrl,
      apiKey: config.apiKey
    })
    ElMessage.success('配置已保存')
    healthOk.value = null
    // Auto-check connection after save
    await checkHealth()
  } catch (e) {
    // Error handled by interceptor
  }
  saving.value = false
}

async function saveAndCheck() {
  await saveConfig()
}

async function checkHealth() {
  checking.value = true
  try {
    const res = await request.get('/settings/health')
    healthOk.value = res.data?.ok === true
    if (healthOk.value) {
      ElMessage.success('RAGFlow 连接正常')
    } else {
      ElMessage.error('RAGFlow 连接失败: ' + (res.data?.error || '请检查地址和密钥'))
    }
  } catch (e) {
    healthOk.value = false
    ElMessage.error('连接检测失败')
  }
  checking.value = false
}

async function saveProfile() {
  if (profileForm.newPassword && profileForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (profileForm.newPassword && profileForm.newPassword !== profileForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (profileForm.newPassword && !profileForm.oldPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  try {
    const data = {}
    if (profileForm.nickname) data.nickname = profileForm.nickname
    if (profileForm.newPassword) {
      data.old_password = profileForm.oldPassword
      data.new_password = profileForm.newPassword
    }
    await request.put('/auth/profile', data)
    if (profileForm.nickname) {
      userStore.nickname = profileForm.nickname
      localStorage.setItem('user_nickname', profileForm.nickname)
    }
    profileForm.oldPassword = ''
    profileForm.newPassword = ''
    profileForm.confirmPassword = ''
    ElMessage.success('保存成功')
  } catch (e) {
    // Error handled by interceptor
  }
}

onMounted(async () => {
  await loadConfig()
  checkHealth()
  if (route.query?.tab) {
    activeTab.value = route.query.tab
  }
})
</script>

<style scoped>
.page { max-width: 800px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 18px;
}
.page-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #18181b; }
.section-card { margin-bottom: 18px; }

.settings-form { max-width: 500px; }
.form-tip { font-size: 12px; color: #a1a1aa; margin-top: 4px; }
.form-tip.danger { color: #ef4444; }
.form-tip.success { color: #67C23A; }
.form-actions { display: flex; gap: 8px; margin-top: 8px; }

.health-status { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.health-hint { font-size: 12px; color: #a1a1aa; }
.health-hint.success { color: #67C23A; }
.health-hint.danger { color: #ef4444; }

.about-section { padding: 10px 0; }
.about-logo {
  display: flex; align-items: center; gap: 14px;
  margin-bottom: 20px;
}
.about-icon {
  width: 56px; height: 56px; border-radius: 14px;
  background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(64,158,255,.3);
}
.about-logo h3 { margin: 0; font-size: 18px; font-weight: 700; color: #18181b; }
.about-logo p { margin: 2px 0 0; font-size: 13px; color: #a1a1aa; }
.about-info { margin-top: 10px; }

@media (max-width: 767px) {
  .page { padding: 0 4px; }
  .settings-form { max-width: 100%; }
  .form-actions { flex-direction: column; }
  .form-actions .el-button { width: 100%; }
  .health-status { flex-direction: column; align-items: flex-start; gap: 6px; }
}
</style>
