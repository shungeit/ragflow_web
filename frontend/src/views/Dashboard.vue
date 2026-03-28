<template>
  <div class="page">
    <div class="page-header">
      <h2>仪表盘</h2>
      <div class="header-right">
        <el-button size="small" :loading="loading" @click="loadData">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="12" class="stat-row">
      <el-col :xs="12" :sm="6" :md="6">
        <div class="stat-card">
          <div class="stat-icon si-blue"><el-icon><ChatDotRound /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">总助手</div>
            <div class="stat-value">{{ stats.totalAssistants }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="stat-card">
          <div class="stat-icon si-green"><el-icon><Cpu /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">总智能体</div>
            <div class="stat-value green">{{ stats.totalAgents }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="stat-card">
          <div class="stat-icon si-purple"><el-icon><FolderOpened /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">知识库</div>
            <div class="stat-value purple">{{ stats.totalDatasets }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="stat-card">
          <div class="stat-icon si-orange"><el-icon><Comment /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">系统状态</div>
            <div class="stat-value orange" style="font-size:16px">运行中</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="12" class="action-row">
      <el-col :xs="12" :sm="6" :md="6">
        <div class="action-card ac-blue" @click="$router.push('/chat')">
          <div class="action-icon"><el-icon size="28"><ChatDotRound /></el-icon></div>
          <div class="action-label">开始对话</div>
          <div class="action-desc">与助手或智能体交流</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="action-card ac-purple" @click="$router.push('/datasets')">
          <div class="action-icon"><el-icon size="28"><FolderOpened /></el-icon></div>
          <div class="action-label">知识库管理</div>
          <div class="action-desc">上传与管理文档</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="action-card ac-green" @click="$router.push('/agents')">
          <div class="action-icon"><el-icon size="28"><Cpu /></el-icon></div>
          <div class="action-label">智能体列表</div>
          <div class="action-desc">查看可用智能体</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="action-card ac-gray" @click="$router.push('/settings')">
          <div class="action-icon"><el-icon size="28"><Setting /></el-icon></div>
          <div class="action-label">系统设置</div>
          <div class="action-desc">配置连接与偏好</div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近使用 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近使用</span>
        </div>
      </template>
      <div class="recent-placeholder">
        <el-icon size="32" color="#d4d4d8"><Comment /></el-icon>
        <p>暂无最近使用记录</p>
        <el-button type="primary" size="small" round @click="$router.push('/chat')">开始第一次对话</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh, ChatDotRound, Cpu, FolderOpened, Comment, Setting } from '@element-plus/icons-vue'
import { listAssistants, listAgents } from '@/api/chat'
import { listDatasets } from '@/api/dataset'

const loading = ref(false)
const stats = reactive({
  totalAssistants: 0,
  totalAgents: 0,
  totalDatasets: 0
})

async function loadData() {
  loading.value = true
  try {
    const [assistRes, agentRes, datasetRes] = await Promise.allSettled([
      listAssistants(),
      listAgents(),
      listDatasets()
    ])

    if (assistRes.status === 'fulfilled') {
      const d = assistRes.value?.data
      stats.totalAssistants = Array.isArray(d) ? d.length : (d?.list?.length || d?.total || 0)
    }
    if (agentRes.status === 'fulfilled') {
      const d = agentRes.value?.data
      stats.totalAgents = Array.isArray(d) ? d.length : (d?.list?.length || d?.total || 0)
    }
    if (datasetRes.status === 'fulfilled') {
      const d = datasetRes.value?.data
      stats.totalDatasets = Array.isArray(d) ? d.length : (d?.list?.length || d?.total || 0)
    }
  } finally {
    loading.value = false
  }
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

.stat-row { margin-bottom: 18px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 12px; padding: 18px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  transition: transform .2s, box-shadow .2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.1); }

.stat-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; color: #fff; flex-shrink: 0;
}
.si-blue   { background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%); box-shadow: 0 4px 10px rgba(64,158,255,.3); }
.si-green  { background: linear-gradient(135deg, #67C23A 0%, #409EFF 100%); box-shadow: 0 4px 10px rgba(103,194,58,.3); }
.si-purple { background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); box-shadow: 0 4px 10px rgba(99,102,241,.3); }
.si-orange { background: linear-gradient(135deg, #E6A23C 0%, #F56C6C 100%); box-shadow: 0 4px 10px rgba(230,162,60,.3); }

.stat-body { flex: 1; min-width: 0; }
.stat-label { font-size: 12px; color: #a1a1aa; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: #18181b; line-height: 1.2; }
.stat-value.green  { color: #67C23A; }
.stat-value.purple { color: #6366f1; }
.stat-value.orange { color: #E6A23C; }

.section-card { margin-bottom: 18px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #18181b; }

.action-row { margin-bottom: 18px; }
.action-card {
  display: flex; flex-direction: column; align-items: center;
  padding: 24px 12px 20px; border-radius: 14px; cursor: pointer;
  transition: transform .2s, box-shadow .2s;
  color: #fff; text-align: center;
}
.action-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,.15); }
.ac-blue   { background: linear-gradient(135deg, #409EFF 0%, #2563eb 100%); }
.ac-purple { background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); }
.ac-green  { background: linear-gradient(135deg, #67C23A 0%, #10b981 100%); }
.ac-gray   { background: linear-gradient(135deg, #71717a 0%, #52525b 100%); }
.action-icon { margin-bottom: 10px; opacity: .9; }
.action-label { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.action-desc { font-size: 11px; opacity: .75; }

.recent-placeholder {
  display: flex; flex-direction: column; align-items: center;
  padding: 32px 16px; color: #a1a1aa;
}
.recent-placeholder p { margin: 12px 0 16px; font-size: 13px; }

@media (max-width: 767px) {
  .stat-card { padding: 14px 12px; gap: 10px; }
  .stat-icon { width: 38px; height: 38px; font-size: 18px; border-radius: 10px; }
  .stat-value { font-size: 20px; }
  .stat-row .el-col { margin-bottom: 8px; }
  .action-row .el-col { margin-bottom: 8px; }
  .action-card { padding: 18px 10px 16px; border-radius: 12px; }
}
</style>
