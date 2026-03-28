<template>
  <div class="page">
    <div class="page-header">
      <h2>知识库</h2>
      <div class="header-right">
        <el-button type="primary" size="small" @click="showCreate = true">
          <el-icon><Plus /></el-icon> 创建知识库
        </el-button>
        <el-button size="small" :loading="loading" @click="loadData">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Desktop 表格 -->
    <el-card v-if="!isMobile" class="section-card">
      <el-table :data="datasets" v-loading="loading" size="small" style="width:100%">
        <el-table-column type="index" width="50" align="center" />
        <el-table-column label="名称" min-width="160">
          <template #default="{ row }">
            <div class="fw6">{{ row.name }}</div>
            <div class="txt-sm txt-muted">{{ row.description || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="分块数" width="100" align="center">
          <template #default="{ row }">{{ row.chunk_count || 0 }}</template>
        </el-table-column>
        <el-table-column label="文档数" width="100" align="center">
          <template #default="{ row }">{{ row.document_count || 0 }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="viewDocs(row)">文档</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Mobile 卡片列表 -->
    <template v-else>
      <div v-if="loading" style="text-align:center;padding:40px"><el-icon class="is-loading" size="24"><Loading /></el-icon></div>
      <div v-for="row in datasets" :key="row.id" class="mobile-card" @click="viewDocs(row)">
        <div class="mc-header">
          <div class="mc-name">
            {{ row.name }}
            <el-badge v-if="row.document_count" :value="row.document_count" type="primary" class="doc-badge" />
          </div>
          <el-button size="small" text type="danger" @click.stop="handleDelete(row)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <div class="mc-desc">{{ row.description || '-' }}</div>
        <div class="mc-stats">
          <span>分块: {{ row.chunk_count || 0 }}</span>
          <span>文档: {{ row.document_count || 0 }}</span>
          <span>{{ formatTime(row.create_time) }}</span>
        </div>
      </div>
      <div v-if="!loading && !datasets.length" class="empty-state">
        <el-icon size="40" color="#ddd"><FolderOpened /></el-icon>
        <p>暂无知识库</p>
      </div>
    </template>

    <!-- 创建知识库对话框 -->
    <el-dialog v-model="showCreate" title="创建知识库" width="440px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="createForm.name" placeholder="输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="可选描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文档列表对话框 -->
    <el-dialog v-model="showDocs" :title="'文档 - ' + (currentDataset?.name || '')" width="700px">
      <template #header>
        <div class="doc-dialog-header">
          <span class="doc-dialog-title">文档 - {{ currentDataset?.name || '' }}</span>
          <div class="doc-dialog-actions">
            <el-button type="primary" size="small" :loading="uploading" @click="triggerUpload">
              <el-icon><Upload /></el-icon> 上传文档
            </el-button>
            <el-button size="small" :loading="docsLoading" @click="refreshDocs">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <input ref="fileInput" type="file" multiple style="display:none" @change="handleFileUpload" accept=".txt,.pdf,.doc,.docx,.md,.csv,.xlsx,.xls,.ppt,.pptx,.html" />
      <el-table :data="documents" v-loading="docsLoading" size="small" style="width:100%">
        <el-table-column type="index" width="50" align="center" />
        <el-table-column label="文件名" min-width="180">
          <template #default="{ row }">{{ row.name || row.filename || '-' }}</template>
        </el-table-column>
        <el-table-column label="大小" width="90">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getRunTagType(row.run)" size="small">
              <el-icon v-if="getRunStatus(row.run) === 'RUNNING'" class="is-loading" style="margin-right:2px"><Loading /></el-icon>
              {{ getRunLabel(row.run) }}
            </el-tag>
            <div v-if="getRunStatus(row.run) === 'RUNNING' && row.progress != null" class="progress-text">
              {{ (row.progress * 100).toFixed(1) }}%
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button
              v-if="getRunStatus(row.run) === 'UNSTART' || getRunStatus(row.run) === 'FAIL' || getRunStatus(row.run) === 'CANCEL'"
              size="small" text type="primary"
              @click="handleParse(row)"
            >解析</el-button>
            <el-button
              v-if="getRunStatus(row.run) === 'RUNNING'"
              size="small" text type="warning"
              @click="handleStopParse(row)"
            >停止</el-button>
            <el-button size="small" text type="danger" @click="handleDeleteDoc(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!docsLoading && !documents.length" style="text-align:center;padding:20px;color:#a1a1aa">暂无文档</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Delete, FolderOpened, Loading, Upload } from '@element-plus/icons-vue'
import { listDatasets, createDataset, deleteDatasets, listDocuments, uploadDocuments, deleteDocuments, parseDocuments, stopParsingDocuments } from '@/api/dataset'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()
const loading = ref(false)
const datasets = ref([])

const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = ref({ name: '', description: '' })
const createRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const showDocs = ref(false)
const docsLoading = ref(false)
const currentDataset = ref(null)
const documents = ref([])
const uploading = ref(false)
const fileInput = ref(null)

// --- Status helpers ---
function getRunStatus(run) {
  const map = { '0': 'UNSTART', '1': 'RUNNING', '2': 'CANCEL', '3': 'DONE', '4': 'FAIL', 'UNSTART': 'UNSTART', 'RUNNING': 'RUNNING', 'CANCEL': 'CANCEL', 'DONE': 'DONE', 'FAIL': 'FAIL' }
  return map[run] || run || 'UNSTART'
}
function getRunLabel(run) {
  const s = getRunStatus(run)
  const map = { UNSTART: '未开始', RUNNING: '解析中', CANCEL: '已取消', DONE: '完成', FAIL: '失败' }
  return map[s] || s
}
function getRunTagType(run) {
  const s = getRunStatus(run)
  const map = { UNSTART: 'info', RUNNING: 'warning', CANCEL: 'info', DONE: 'success', FAIL: 'danger' }
  return map[s] || 'info'
}

// --- Data loading ---
async function loadData() {
  loading.value = true
  try {
    const res = await listDatasets()
    datasets.value = res.data?.list || res.data || []
  } catch (e) {
    datasets.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createDataset(createForm.value)
    ElMessage.success('创建成功')
    showCreate.value = false
    createForm.value = { name: '', description: '' }
    await loadData()
  } finally {
    creating.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除知识库「${row.name}」？`, '提示', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteDatasets({ ids: [row.id] })
    ElMessage.success('已删除')
    await loadData()
  } catch (e) {
    // cancelled
  }
}

// --- Document management ---
async function viewDocs(row) {
  currentDataset.value = row
  showDocs.value = true
  await refreshDocs()
}

async function refreshDocs() {
  if (!currentDataset.value) return
  docsLoading.value = true
  try {
    const res = await listDocuments(currentDataset.value.id)
    documents.value = res.data?.list || res.data || []
  } catch (e) {
    documents.value = []
  } finally {
    docsLoading.value = false
  }
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileUpload(e) {
  const files = e.target.files
  if (!files || !files.length) return
  uploading.value = true
  try {
    for (const file of files) {
      const formData = new FormData()
      formData.append('file', file)
      await uploadDocuments(currentDataset.value.id, formData)
    }
    ElMessage.success('上传成功')
    await refreshDocs()
  } catch (err) {
    // error handled by interceptor
  } finally {
    uploading.value = false
    e.target.value = '' // reset file input
  }
}

async function handleParse(row) {
  try {
    await parseDocuments(currentDataset.value.id, { document_ids: [row.id] })
    ElMessage.success('开始解析')
    await refreshDocs()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleStopParse(row) {
  try {
    await stopParsingDocuments(currentDataset.value.id, { document_ids: [row.id] })
    ElMessage.success('已停止解析')
    await refreshDocs()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleDeleteDoc(row) {
  try {
    await ElMessageBox.confirm(`确定删除文档「${row.name || row.filename}」？`, '提示', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteDocuments(currentDataset.value.id, { ids: [row.id] })
    ElMessage.success('已删除')
    await refreshDocs()
  } catch (e) {
    // cancelled
  }
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(typeof t === 'number' ? t * 1000 : t)
  if (isNaN(d.getTime())) return '-'
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

onMounted(() => loadData())
</script>

<style scoped>
.page { max-width: 1200px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 18px; flex-wrap: wrap; gap: 8px;
}
.page-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #18181b; }
.header-right { display: flex; align-items: center; gap: 8px; }
.section-card { margin-bottom: 18px; }
.fw6 { font-weight: 600; }
.txt-sm { font-size: 12px; }
.txt-muted { color: #a1a1aa; }

.mobile-card {
  background: #fff; border-radius: 10px; padding: 14px 16px;
  margin-bottom: 8px; box-shadow: 0 1px 3px rgba(0,0,0,.06);
  cursor: pointer; transition: transform .15s;
}
.mobile-card:active { transform: scale(.99); }
.mc-header { display: flex; align-items: center; justify-content: space-between; }
.mc-name { font-size: 14px; font-weight: 600; color: #18181b; display: flex; align-items: center; gap: 6px; }
.mc-desc { font-size: 12px; color: #a1a1aa; margin: 4px 0; }
.mc-stats { display: flex; gap: 12px; font-size: 11px; color: #71717a; }

.doc-badge :deep(.el-badge__content) { font-size: 10px; }

.empty-state { text-align: center; padding: 40px 20px; color: #a1a1aa; }
.empty-state p { margin: 8px 0 0; font-size: 13px; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 8px; }

.doc-dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding-right: 24px;
}
.doc-dialog-title { font-size: 16px; font-weight: 600; color: #18181b; }
.doc-dialog-actions { display: flex; align-items: center; gap: 8px; }

.progress-text { font-size: 11px; color: #e6a23c; margin-top: 2px; }
</style>
