<template>
  <div class="page">
    <div class="page-header">
      <h2>智能体</h2>
      <div class="header-right">
        <el-button size="small" :loading="loading" @click="loadData">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Desktop 表格 -->
    <el-card v-if="!isMobile" class="section-card">
      <el-table :data="agents" v-loading="loading" size="small" style="width:100%">
        <el-table-column type="index" width="50" align="center" />
        <el-table-column label="名称" min-width="160">
          <template #default="{ row }">
            <div class="fw6">{{ row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="200">
          <template #default="{ row }">
            <div class="txt-muted">{{ row.description || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="goChat(row)">对话</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Mobile 卡片列表 -->
    <template v-else>
      <div v-if="loading" style="text-align:center;padding:40px"><el-icon class="is-loading" size="24"><Loading /></el-icon></div>
      <div v-for="row in agents" :key="row.id" class="mobile-card" @click="goChat(row)">
        <div class="mc-header">
          <div class="mc-icon">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="mc-info">
            <div class="mc-name">{{ row.name }}</div>
            <div class="mc-desc">{{ row.description || '智能体' }}</div>
          </div>
        </div>
        <div class="mc-time">{{ formatTime(row.create_time) }}</div>
      </div>
      <div v-if="!loading && !agents.length" class="empty-state">
        <el-icon size="40" color="#ddd"><Cpu /></el-icon>
        <p>暂无智能体</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Cpu, Loading } from '@element-plus/icons-vue'
import { listAgents } from '@/api/chat'
import { useDevice } from '@/composables/useDevice'

const router = useRouter()
const { isMobile } = useDevice()
const loading = ref(false)
const agents = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await listAgents()
    agents.value = res.data?.list || res.data || []
  } catch (e) {
    agents.value = []
  } finally {
    loading.value = false
  }
}

function goChat(row) {
  router.push('/chat')
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(typeof t === 'number' ? t * 1000 : t)
  if (isNaN(d.getTime())) return '-'
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => loadData())
</script>

<style scoped>
.page { max-width: 1200px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 18px;
}
.page-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #18181b; }
.header-right { display: flex; align-items: center; gap: 8px; }
.section-card { margin-bottom: 18px; }
.fw6 { font-weight: 600; }
.txt-muted { color: #a1a1aa; }

.mobile-card {
  background: #fff; border-radius: 10px; padding: 14px 16px;
  margin-bottom: 8px; box-shadow: 0 1px 3px rgba(0,0,0,.06);
  cursor: pointer; transition: transform .15s;
}
.mobile-card:active { transform: scale(.99); }
.mc-header { display: flex; align-items: center; gap: 10px; }
.mc-icon {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #67C23A 0%, #409EFF 100%);
  color: #fff; font-size: 18px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.mc-info { flex: 1; min-width: 0; }
.mc-name { font-size: 14px; font-weight: 600; color: #18181b; }
.mc-desc { font-size: 12px; color: #a1a1aa; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mc-time { font-size: 11px; color: #a1a1aa; margin-top: 8px; }

.empty-state { text-align: center; padding: 40px 20px; color: #a1a1aa; }
.empty-state p { margin: 8px 0 0; font-size: 13px; }
</style>
