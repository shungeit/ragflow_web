<template>
  <div class="chat-page" :class="{ 'is-mobile': isMobile }">

    <!-- ========== 左侧面板 (Desktop) / 底部弹出 (Mobile) ========== -->
    <div v-if="!isMobile" class="chat-sidebar">
      <!-- 助手/智能体 选项卡 -->
      <div class="sidebar-tabs">
        <div class="sidebar-tab" :class="{ active: sidebarTab === 'assistant' }" @click="sidebarTab = 'assistant'">助手</div>
        <div class="sidebar-tab" :class="{ active: sidebarTab === 'agent' }" @click="sidebarTab = 'agent'">智能体</div>
      </div>

      <!-- 助手/智能体列表 -->
      <div class="entity-list">
        <template v-if="sidebarTab === 'assistant'">
          <div
            v-for="item in assistants"
            :key="item.id"
            class="entity-item"
            :class="{ active: selectedEntity?.id === item.id && selectedType === 'assistant' }"
            @click="selectEntity(item, 'assistant')"
          >
            <div class="entity-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="entity-info">
              <div class="entity-name">{{ item.name }}</div>
              <div class="entity-desc">{{ item.description || '智能助手' }}</div>
            </div>
          </div>
          <div v-if="!assistants.length" class="empty-hint">暂无助手</div>
        </template>
        <template v-else>
          <div
            v-for="item in agents"
            :key="item.id"
            class="entity-item"
            :class="{ active: selectedEntity?.id === item.id && selectedType === 'agent' }"
            @click="selectEntity(item, 'agent')"
          >
            <div class="entity-avatar agent">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="entity-info">
              <div class="entity-name">{{ item.name }}</div>
              <div class="entity-desc">{{ item.description || '智能体' }}</div>
            </div>
          </div>
          <div v-if="!agents.length" class="empty-hint">暂无智能体</div>
        </template>
      </div>

      <!-- 新建会话 -->
      <div class="new-session-btn" @click="handleNewSession">
        <el-icon><Plus /></el-icon>
        <span>新建会话</span>
      </div>

      <!-- 会话列表 -->
      <div class="session-list-header">会话列表</div>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: currentSession?.id === s.id }"
          @click="selectSession(s)"
          @dblclick.stop="handleRenameSession(s)"
        >
          <div class="session-name">{{ s.name || '新会话' }}</div>
          <div class="session-time">{{ formatTime(s.create_time || s.update_time) }}</div>
          <el-icon class="session-rename" @click.stop="handleRenameSession(s)"><Edit /></el-icon>
          <el-icon class="session-delete" @click.stop="handleDeleteSession(s)"><Delete /></el-icon>
        </div>
        <div v-if="!sessions.length && selectedEntity" class="empty-hint">暂无会话，点击上方创建</div>
        <div v-if="!selectedEntity" class="empty-hint">请先选择助手或智能体</div>
      </div>
    </div>

    <!-- ========== 移动端顶部选择栏 ========== -->
    <div v-if="isMobile" class="mobile-top-bar">
      <div class="mobile-top-left" @click="showMobilePanel = true">
        <el-icon :size="16" v-if="selectedType === 'agent'"><Cpu /></el-icon>
        <el-icon :size="16" v-else><ChatDotRound /></el-icon>
        <span class="mobile-entity-name">{{ selectedEntity?.name || '选择助手' }}</span>
        <el-icon :size="12"><ArrowDown /></el-icon>
      </div>
      <div class="mobile-top-right">
        <div v-if="isSynthSupported" class="mobile-top-btn" :class="{ active: voiceOutputEnabled }" @click="toggleVoiceOutput">
          <el-icon :size="18"><template v-if="voiceOutputEnabled"><Open /></template><template v-else><Mute /></template></el-icon>
        </div>
        <div v-if="selectedEntity" class="mobile-top-btn" @click="handleNewSession">
          <el-icon :size="18"><Plus /></el-icon>
        </div>
      </div>
    </div>

    <!-- 移动端选择面板 -->
    <el-drawer
      v-if="isMobile"
      v-model="showMobilePanel"
      direction="btt"
      size="75%"
      :show-close="true"
      title="选择助手与会话"
    >
      <div class="mobile-panel-content">
        <div class="sidebar-tabs">
          <div class="sidebar-tab" :class="{ active: sidebarTab === 'assistant' }" @click="sidebarTab = 'assistant'">助手</div>
          <div class="sidebar-tab" :class="{ active: sidebarTab === 'agent' }" @click="sidebarTab = 'agent'">智能体</div>
        </div>
        <div class="entity-list mobile">
          <template v-if="sidebarTab === 'assistant'">
            <div
              v-for="item in assistants"
              :key="item.id"
              class="entity-item"
              :class="{ active: selectedEntity?.id === item.id && selectedType === 'assistant' }"
              @click="selectEntity(item, 'assistant'); showMobilePanel = false"
            >
              <div class="entity-avatar"><el-icon><ChatDotRound /></el-icon></div>
              <div class="entity-info">
                <div class="entity-name">{{ item.name }}</div>
                <div class="entity-desc">{{ item.description || '智能助手' }}</div>
              </div>
            </div>
          </template>
          <template v-else>
            <div
              v-for="item in agents"
              :key="item.id"
              class="entity-item"
              :class="{ active: selectedEntity?.id === item.id && selectedType === 'agent' }"
              @click="selectEntity(item, 'agent'); showMobilePanel = false"
            >
              <div class="entity-avatar agent"><el-icon><Cpu /></el-icon></div>
              <div class="entity-info">
                <div class="entity-name">{{ item.name }}</div>
                <div class="entity-desc">{{ item.description || '智能体' }}</div>
              </div>
            </div>
          </template>
          <div v-if="(sidebarTab === 'assistant' && !assistants.length) || (sidebarTab !== 'assistant' && !agents.length)" class="empty-hint">暂无{{ sidebarTab === 'assistant' ? '助手' : '智能体' }}</div>
        </div>
        <div v-if="selectedEntity" class="mobile-session-section">
          <div class="mobile-section-header">
            <span>会话列表</span>
            <el-button type="primary" size="small" round @click="handleNewSession(); showMobilePanel = false">
              <el-icon><Plus /></el-icon> 新建
            </el-button>
          </div>
          <div class="session-list mobile">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="session-item mobile"
              :class="{ active: currentSession?.id === s.id }"
              @click="selectSession(s); showMobilePanel = false"
            >
              <div class="session-item-main">
                <div class="session-name">{{ s.name || '新会话' }}</div>
                <div class="session-time">{{ formatTime(s.create_time || s.update_time) }}</div>
              </div>
              <div class="session-item-actions" @click.stop>
                <el-icon class="session-action-icon" @click="handleRenameSession(s)"><Edit /></el-icon>
                <el-icon class="session-action-icon danger" @click="handleDeleteSession(s)"><Delete /></el-icon>
              </div>
            </div>
            <div v-if="!sessions.length" class="empty-hint">暂无会话</div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- ========== 右侧聊天区 ========== -->
    <div class="chat-main">
      <!-- 聊天头部 (仅桌面端) -->
      <div v-if="!isMobile" class="chat-header">
        <div class="chat-header-left">
          <el-icon v-if="selectedType === 'agent'" class="header-icon agent"><Cpu /></el-icon>
          <el-icon v-else class="header-icon"><ChatDotRound /></el-icon>
          <div class="header-info">
            <div class="header-name">{{ selectedEntity?.name || 'RAGFlow 智能助手' }}</div>
            <div class="header-session" v-if="currentSession">{{ currentSession.name || '新会话' }}</div>
          </div>
        </div>
        <div class="chat-header-right">
          <span v-if="voiceOutputEnabled" class="voice-status-text">语音播报已开启</span>
          <el-tooltip :content="voiceOutputEnabled ? '关闭语音播报' : '开启语音播报'" placement="bottom">
            <div class="header-btn" :class="{ active: voiceOutputEnabled }" @click="toggleVoiceOutput">
              <el-icon><template v-if="voiceOutputEnabled"><Open /></template><template v-else><Mute /></template></el-icon>
            </div>
          </el-tooltip>
        </div>
      </div>
      <!-- 移动端会话名 -->
      <div v-if="isMobile && currentSession" class="mobile-session-bar">
        <span class="mobile-session-name">{{ currentSession.name || '新会话' }}</span>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="!messages.length && (!currentSession || !selectedEntity)" class="chat-welcome">
          <div class="welcome-icon"><el-icon :size="40"><ChatDotRound /></el-icon></div>
          <h3>{{ selectedEntity?.name || 'RAGFlow 智能助手' }}</h3>
          <p v-if="selectedEntity">选择或创建会话开始对话</p>
          <p v-else>请从左侧选择助手或智能体</p>
          <div v-if="selectedEntity && !currentSession" class="welcome-actions">
            <el-button type="primary" round @click="handleNewSession">
              <el-icon><Plus /></el-icon> 开始新对话
            </el-button>
          </div>
        </div>
        <div v-if="currentSession && !messages.length && !selectedEntity" class="chat-welcome">
          <p>请选择助手开始对话</p>
        </div>

        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="message-row"
          :class="{ 'is-user': msg.role === 'user', 'is-assistant': msg.role === 'assistant' }"
        >
          <!-- 助手头像 -->
          <div v-if="msg.role === 'assistant'" class="msg-avatar">
            <el-icon><ChatDotRound /></el-icon>
          </div>

          <div class="msg-bubble">
            <div class="msg-content markdown-body" v-html="renderMarkdown(msg.content)"></div>
            <div v-if="msg.role === 'assistant' && msg.loading" class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
            <div v-if="msg.role === 'assistant' && msg.content && !msg.loading && isSynthSupported" class="msg-actions">
              <el-tooltip content="朗读此消息" placement="bottom">
                <button class="msg-action-btn" @click="speakMessage(msg)">
                  <el-icon :size="14"><VideoPlay /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </div>

          <!-- 用户头像 -->
          <div v-if="msg.role === 'user'" class="msg-avatar user">
            <el-icon><User /></el-icon>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <!-- 录音状态指示 -->
        <div v-if="isListening" class="recording-indicator">
          <div class="rec-dot"></div>
          <span>正在录音... 点击麦克风停止</span>
        </div>
        <!-- 播报状态指示 -->
        <div v-if="isSpeaking" class="speaking-indicator">
          <el-icon><Microphone /></el-icon>
          <span>正在播报...</span>
          <el-button size="small" text @click="stopSpeaking">停止</el-button>
        </div>
        <div class="input-wrapper">
          <textarea
            ref="inputRef"
            v-model="inputText"
            class="chat-textarea"
            :placeholder="isMobile ? '输入消息...' : '输入消息... (Enter 发送, Shift+Enter 换行)'"
            rows="1"
            @keydown="handleKeydown"
            @input="autoResize"
          ></textarea>
          <div class="input-actions">
            <!-- 语音输入按钮 -->
            <div
              v-if="isRecognitionSupported"
              class="action-btn mic-btn"
              :class="{ recording: isListening }"
              @click="toggleListening"
            >
              <el-icon><Microphone /></el-icon>
              <div v-if="isListening" class="pulse-ring"></div>
            </div>
            <!-- 发送按钮 -->
            <div
              class="action-btn send-btn"
              :class="{ active: inputText.trim() }"
              @click="handleSend"
            >
              <el-icon><Promotion /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound, Cpu, Plus, Delete, Edit, ArrowDown,
  Microphone, Mute, Promotion, User, Open, VideoPlay
} from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { useSpeech } from '@/composables/useSpeech'
import { listAssistants, listAgents, createSession, listSessions, updateSession, deleteSessions, streamChat } from '@/api/chat'
import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import sql from 'highlight.js/lib/languages/sql'
import bash from 'highlight.js/lib/languages/bash'
import json from 'highlight.js/lib/languages/json'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import typescript from 'highlight.js/lib/languages/typescript'
import markdown from 'highlight.js/lib/languages/markdown'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('py', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sh', bash)
hljs.registerLanguage('json', json)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('markdown', markdown)
hljs.registerLanguage('md', markdown)

const marked = new Marked(
  markedHighlight({
    highlight(code, lang) {
      if (lang && hljs.getLanguage(lang)) {
        try { return hljs.highlight(code, { language: lang }).value } catch {}
      }
      return code
    }
  }),
  { breaks: true, gfm: true }
)

const { isMobile } = useDevice()
const {
  isListening, transcript, isSpeaking,
  isRecognitionSupported, isSynthSupported,
  startListening, stopListening, speak, stopSpeaking
} = useSpeech()

// State
const sidebarTab = ref('assistant')
const assistants = ref([])
const agents = ref([])
const selectedEntity = ref(null)
const selectedType = ref('assistant')
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputText = ref('')
const inputRef = ref(null)
const messagesContainer = ref(null)
const voiceOutputEnabled = ref(false)
const showMobilePanel = ref(false)
const isSending = ref(false)

// Watch transcript from speech recognition - show realtime in input
watch(transcript, (val) => {
  if (val) {
    inputText.value = val
  }
})

// When speech recognition ends and we have text, auto-send
watch(isListening, (val, oldVal) => {
  if (!val && oldVal) {
    const text = transcript.value?.trim()
    if (text) {
      inputText.value = text
      voiceOutputEnabled.value = true
      nextTick(() => {
        handleSend()
      })
    }
  }
})

// Auto-scroll to bottom
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

watch(messages, () => scrollToBottom(), { deep: true })

// Load data
async function loadAssistants() {
  try {
    const res = await listAssistants()
    assistants.value = res.data?.list || res.data || []
  } catch (e) {
    assistants.value = []
  }
}

async function loadAgents() {
  try {
    const res = await listAgents()
    agents.value = res.data?.list || res.data || []
  } catch (e) {
    agents.value = []
  }
}

async function loadSessions() {
  if (!selectedEntity.value) {
    sessions.value = []
    return
  }
  try {
    const params = {
      chatId: selectedType.value === 'assistant' ? selectedEntity.value.id : undefined,
      agentId: selectedType.value === 'agent' ? selectedEntity.value.id : undefined
    }
    const res = await listSessions(params)
    sessions.value = res.data?.list || res.data || []
  } catch (e) {
    sessions.value = []
  }
}

function selectEntity(item, type) {
  selectedEntity.value = item
  selectedType.value = type
  currentSession.value = null
  messages.value = []
  loadSessions()
}

function selectSession(s) {
  currentSession.value = s
  // Load messages from session if available
  messages.value = s.messages || []
  scrollToBottom()
}

async function handleNewSession() {
  if (!selectedEntity.value) {
    ElMessage.warning('请先选择助手或智能体')
    return
  }
  try {
    const data = {
      chatId: selectedType.value === 'assistant' ? selectedEntity.value.id : undefined,
      agentId: selectedType.value === 'agent' ? selectedEntity.value.id : undefined,
      type: selectedType.value === 'agent' ? 'agent' : 'chat'
    }
    const res = await createSession(data)
    const newSession = res.data || {}
    await loadSessions()
    // Select the new session
    const found = sessions.value.find(s => s.id === newSession.id)
    if (found) {
      selectSession(found)
    } else if (sessions.value.length) {
      selectSession(sessions.value[0])
    }
    messages.value = []
  } catch (e) {
    // Error already handled by interceptor
  }
}

async function handleDeleteSession(s) {
  try {
    await ElMessageBox.confirm('确定删除该会话？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSessions({ ids: [s.id] })
    if (currentSession.value?.id === s.id) {
      currentSession.value = null
      messages.value = []
    }
    await loadSessions()
    ElMessage.success('已删除')
  } catch (e) {
    // cancelled or error
  }
}

async function handleRenameSession(s) {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新名称', '重命名会话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: s.name || '',
      inputPlaceholder: '会话名称'
    })
    if (!newName || !newName.trim()) return
    const chatId = selectedType.value === 'assistant' ? selectedEntity.value.id : undefined
    await updateSession({ chatId, sessionId: s.id, name: newName.trim() })
    s.name = newName.trim()
    ElMessage.success('已重命名')
  } catch (e) {
    // cancelled or error
  }
}

// Format time
function formatTime(t) {
  if (!t) return ''
  const d = new Date(typeof t === 'number' ? t * 1000 : t)
  if (isNaN(d.getTime())) return ''
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// Markdown rendering with marked + highlight.js
function renderMarkdown(text) {
  if (!text) return ''
  try {
    return marked.parse(text)
  } catch {
    return text
  }
}

// Speak a single message
function speakMessage(msg) {
  if (!msg.content) return
  // Strip markdown for cleaner speech
  const plain = msg.content
    .replace(/```[\s\S]*?```/g, ' 代码块 ')
    .replace(/`[^`]+`/g, '')
    .replace(/[#*_~>\-|[\]()!]/g, '')
    .replace(/\n+/g, '。')
    .trim()
  speak(plain)
}

// Input handling
function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function toggleListening() {
  if (isListening.value) {
    stopListening()
  } else {
    startListening()
  }
}

function toggleVoiceOutput() {
  voiceOutputEnabled.value = !voiceOutputEnabled.value
  if (!voiceOutputEnabled.value) {
    stopSpeaking()
  }
}

// Send message
async function handleSend() {
  const text = inputText.value.trim()
  if (!text || isSending.value) return

  if (!selectedEntity.value) {
    ElMessage.warning('请先选择助手或智能体')
    return
  }

  // Auto-create session if none selected
  if (!currentSession.value) {
    try {
      const data = {
        chatId: selectedType.value === 'assistant' ? selectedEntity.value.id : undefined,
        agentId: selectedType.value === 'agent' ? selectedEntity.value.id : undefined,
        type: selectedType.value === 'agent' ? 'agent' : 'chat'
      }
      const res = await createSession(data)
      const newSession = res.data || { id: 'temp-' + Date.now(), name: text.substring(0, 30) }
      currentSession.value = newSession
      await loadSessions()
    } catch (e) {
      return
    }
  }

  // Add user message
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.style.height = 'auto'
    }
  })
  scrollToBottom()

  // Add assistant placeholder
  messages.value.push({ role: 'assistant', content: '', loading: true })
  const assistantMsg = messages.value[messages.value.length - 1]
  isSending.value = true

  try {
    const payload = {
      session_id: currentSession.value.id,
      assistant_id: selectedType.value === 'assistant' ? selectedEntity.value.id : undefined,
      agent_id: selectedType.value === 'agent' ? selectedEntity.value.id : undefined,
      messages: [{ role: 'user', content: text }],
      stream: true
    }

    const response = await streamChat(payload)
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    assistantMsg.loading = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data:')) continue
        const jsonStr = trimmed.slice(5).trim()
        if (jsonStr === '[DONE]') continue

        try {
          const parsed = JSON.parse(jsonStr)
          // Handle OpenAI-compatible SSE format
          const delta = parsed.choices?.[0]?.delta?.content
            || parsed.data?.content
            || parsed.data?.answer
            || ''
          if (delta) {
            assistantMsg.content += delta
            scrollToBottom()
          }
        } catch (e) {
          // Skip non-JSON lines
        }
      }
    }

    // Process remaining buffer
    if (buffer.trim()) {
      const trimmed = buffer.trim()
      if (trimmed.startsWith('data:')) {
        const jsonStr = trimmed.slice(5).trim()
        if (jsonStr !== '[DONE]') {
          try {
            const parsed = JSON.parse(jsonStr)
            const delta = parsed.choices?.[0]?.delta?.content
              || parsed.data?.content
              || parsed.data?.answer
              || ''
            if (delta) {
              assistantMsg.content += delta
            }
          } catch (e) {
            // ignore
          }
        }
      }
    }

    // Voice output - strip markdown for cleaner speech
    if (voiceOutputEnabled.value && assistantMsg.content) {
      speakMessage(assistantMsg)
    }
  } catch (e) {
    assistantMsg.loading = false
    assistantMsg.content = assistantMsg.content || '抱歉，请求失败，请稍后重试。'
    console.error('Stream error:', e)
  } finally {
    isSending.value = false
  }
}

onMounted(() => {
  loadAssistants()
  loadAgents()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100%;
  background: #f0f2f5;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

/* ========== 侧边栏 ========== */
.chat-sidebar {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-right: 1px solid #ebebeb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.sidebar-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 13px;
  font-weight: 500;
  color: #71717a;
  cursor: pointer;
  transition: all .2s;
  border-bottom: 2px solid transparent;
  user-select: none;
}
.sidebar-tab:hover { color: #18181b; }
.sidebar-tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
  font-weight: 600;
}

.entity-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  flex-shrink: 0;
}
.entity-list.mobile {
  max-height: 160px;
}
.entity-list::-webkit-scrollbar { width: 3px; }
.entity-list::-webkit-scrollbar-thumb { background: #e4e4e7; border-radius: 2px; }

.entity-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background .15s;
  margin-bottom: 2px;
}
.entity-item:hover { background: #f4f4f5; }
.entity-item.active {
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  box-shadow: inset 3px 0 0 #3b82f6;
}

.entity-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.entity-avatar.agent {
  background: linear-gradient(135deg, #67C23A 0%, #409EFF 100%);
}

.entity-info { flex: 1; min-width: 0; }
.entity-name { font-size: 13px; font-weight: 600; color: #18181b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.entity-desc { font-size: 11px; color: #a1a1aa; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.new-session-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 8px 8px 0;
  padding: 8px;
  border: 1px dashed #d4d4d8;
  border-radius: 8px;
  color: #71717a;
  font-size: 13px;
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.new-session-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}

.session-list-header {
  padding: 12px 12px 4px;
  font-size: 11px;
  font-weight: 600;
  color: #a1a1aa;
  text-transform: uppercase;
  letter-spacing: .5px;
  flex-shrink: 0;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;
}
.session-list::-webkit-scrollbar { width: 3px; }
.session-list::-webkit-scrollbar-thumb { background: #e4e4e7; border-radius: 2px; }

.session-item {
  position: relative;
  padding: 10px 52px 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background .15s;
  margin-bottom: 2px;
}
.session-item:hover { background: #f4f4f5; }
.session-item.active {
  background: #eff6ff;
}
.session-name { font-size: 13px; color: #18181b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-time { font-size: 11px; color: #a1a1aa; margin-top: 2px; }
.session-rename, .session-delete {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  color: #d4d4d8;
  cursor: pointer;
  opacity: 0;
  transition: opacity .15s, color .15s;
}
.session-rename { right: 28px; }
.session-delete { right: 8px; }
.session-item:hover .session-rename, .session-item:hover .session-delete { opacity: 1; }
.session-rename:hover { color: #409EFF; }
.session-delete:hover { color: #ef4444; }

.empty-hint {
  text-align: center;
  padding: 20px 10px;
  font-size: 12px;
  color: #a1a1aa;
}

/* ========== 聊天主区域 ========== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.chat-header-left { display: flex; align-items: center; gap: 10px; }
.header-icon {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%);
  color: #fff; font-size: 18px;
  display: flex; align-items: center; justify-content: center;
}
.header-icon.agent {
  background: linear-gradient(135deg, #67C23A 0%, #409EFF 100%);
}
.header-info { min-width: 0; }
.header-name { font-size: 14px; font-weight: 600; color: #18181b; }
.header-session { font-size: 11px; color: #a1a1aa; }

.chat-header-right { display: flex; align-items: center; gap: 6px; }
.voice-status-text { font-size: 11px; color: #2563eb; white-space: nowrap; }
.header-btn {
  width: 34px; height: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #71717a; font-size: 18px;
  transition: all .2s;
}
.header-btn:hover { background: #f4f4f5; color: #18181b; }
.header-btn.active { background: #eff6ff; color: #2563eb; }

/* ========== 消息区域 ========== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}
.chat-messages::-webkit-scrollbar { width: 4px; }
.chat-messages::-webkit-scrollbar-thumb { background: #e4e4e7; border-radius: 2px; }

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a1a1aa;
}
.welcome-icon {
  width: 80px; height: 80px; border-radius: 20px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  display: flex; align-items: center; justify-content: center;
  color: #409EFF;
  margin-bottom: 16px;
}
.chat-welcome h3 { font-size: 16px; color: #52525b; margin: 0 0 8px; }
.chat-welcome p { font-size: 13px; color: #a1a1aa; margin: 0; }

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
  max-width: 85%;
}
.message-row.is-user {
  margin-left: auto;
  flex-direction: row-reverse;
}
.message-row.is-assistant {
  margin-right: auto;
}

.msg-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%);
  color: #fff; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.msg-avatar.user {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.msg-bubble {
  max-width: calc(100% - 42px);
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.6;
  word-break: break-word;
}
.is-user .msg-bubble {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.is-assistant .msg-bubble {
  background: #f4f4f5;
  color: #18181b;
  border-bottom-left-radius: 4px;
}

/* Markdown body styles */
.msg-content.markdown-body :deep(p) {
  margin: 0 0 8px;
}
.msg-content.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}
.msg-content.markdown-body :deep(h1),
.msg-content.markdown-body :deep(h2),
.msg-content.markdown-body :deep(h3),
.msg-content.markdown-body :deep(h4),
.msg-content.markdown-body :deep(h5),
.msg-content.markdown-body :deep(h6) {
  margin: 12px 0 6px;
  font-weight: 600;
  line-height: 1.4;
}
.msg-content.markdown-body :deep(h1) { font-size: 1.3em; }
.msg-content.markdown-body :deep(h2) { font-size: 1.2em; }
.msg-content.markdown-body :deep(h3) { font-size: 1.1em; }
.msg-content.markdown-body :deep(ul),
.msg-content.markdown-body :deep(ol) {
  margin: 4px 0 8px;
  padding-left: 1.5em;
}
.msg-content.markdown-body :deep(li) {
  margin-bottom: 2px;
}
.msg-content.markdown-body :deep(li p) {
  margin: 0;
}
.msg-content.markdown-body :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 3px solid #d4d4d8;
  color: #71717a;
}
.msg-content.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e4e4e7;
  margin: 12px 0;
}
.msg-content.markdown-body :deep(table) {
  border-collapse: collapse;
  margin: 8px 0;
  font-size: 12.5px;
  width: 100%;
  overflow-x: auto;
  display: block;
}
.msg-content.markdown-body :deep(th),
.msg-content.markdown-body :deep(td) {
  border: 1px solid #d4d4d8;
  padding: 6px 10px;
  text-align: left;
}
.msg-content.markdown-body :deep(th) {
  background: #f4f4f5;
  font-weight: 600;
}
.msg-content.markdown-body :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 12px 14px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
  margin: 8px 0;
}
.msg-content.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
  border-radius: 0;
  font-size: inherit;
  color: inherit;
}
.msg-content.markdown-body :deep(code) {
  background: rgba(0,0,0,.08);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}
.is-user .msg-content.markdown-body :deep(code) {
  background: rgba(255,255,255,.2);
}
.msg-content.markdown-body :deep(a) {
  color: #2563eb;
  text-decoration: none;
}
.msg-content.markdown-body :deep(a:hover) {
  text-decoration: underline;
}
.is-user .msg-content.markdown-body :deep(a) {
  color: #93c5fd;
}
.msg-content.markdown-body :deep(strong) { font-weight: 700; }
.msg-content.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}

/* Highlight.js token colors (Catppuccin Mocha) */
.msg-content :deep(.hljs-keyword) { color: #cba6f7; }
.msg-content :deep(.hljs-string) { color: #a6e3a1; }
.msg-content :deep(.hljs-number) { color: #fab387; }
.msg-content :deep(.hljs-comment) { color: #6c7086; font-style: italic; }
.msg-content :deep(.hljs-function) { color: #89b4fa; }
.msg-content :deep(.hljs-title) { color: #89b4fa; }
.msg-content :deep(.hljs-built_in) { color: #f38ba8; }
.msg-content :deep(.hljs-type) { color: #f9e2af; }
.msg-content :deep(.hljs-attr) { color: #89dceb; }
.msg-content :deep(.hljs-variable) { color: #cdd6f4; }
.msg-content :deep(.hljs-params) { color: #fab387; }
.msg-content :deep(.hljs-punctuation) { color: #bac2de; }
.msg-content :deep(.hljs-meta) { color: #f5c2e7; }

/* Message action buttons */
.msg-actions {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}
.msg-bubble:hover .msg-actions {
  opacity: 1;
}
.msg-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 6px;
  background: rgba(0,0,0,.06);
  color: #71717a;
  cursor: pointer;
  transition: all 0.15s;
}
.msg-action-btn:hover {
  background: rgba(0,0,0,.12);
  color: #2563eb;
}

/* Typing indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #a1a1aa;
  animation: typingBounce .6s ease-in-out infinite;
}
.typing-indicator span:nth-child(2) { animation-delay: .15s; }
.typing-indicator span:nth-child(3) { animation-delay: .3s; }
@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* ========== 输入区域 ========== */
.chat-input-area {
  padding: 12px 20px 16px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: #f4f4f5;
  border-radius: 12px;
  padding: 8px 8px 8px 14px;
  transition: box-shadow .2s;
}
.input-wrapper:focus-within {
  box-shadow: 0 0 0 2px rgba(37,99,235,.2);
  background: #fff;
}

.chat-textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  color: #18181b;
  max-height: 120px;
  font-family: inherit;
}
.chat-textarea::placeholder { color: #a1a1aa; }

.input-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #a1a1aa;
  font-size: 18px;
  transition: all .2s;
}
.action-btn:hover { background: #e4e4e7; color: #18181b; }

.mic-btn.recording {
  background: #fecaca;
  color: #ef4444;
  position: relative;
}

.pulse-ring {
  position: absolute;
  inset: -4px;
  border-radius: 12px;
  border: 2px solid #ef4444;
  animation: pulse 1.2s ease-in-out infinite;
}
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}

.send-btn.active {
  background: #2563eb;
  color: #fff;
}
.send-btn.active:hover {
  background: #1d4ed8;
}

.recording-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 4px 6px;
  font-size: 12px;
  color: #ef4444;
}
.rec-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  animation: blink 1s ease-in-out infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.speaking-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 4px 6px;
  font-size: 12px;
  color: #2563eb;
}

/* ========== 移动端顶部栏 ========== */
.mobile-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.mobile-top-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  background: #f4f4f5;
}
.mobile-top-left:active { background: #e4e4e7; }
.mobile-entity-name {
  font-size: 14px;
  font-weight: 500;
  color: #18181b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.mobile-top-right {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
  flex-shrink: 0;
}
.mobile-top-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #71717a;
  cursor: pointer;
}
.mobile-top-btn:active { background: #f4f4f5; }
.mobile-top-btn.active { color: #2563eb; background: #eff6ff; }

/* 移动端会话名 */
.mobile-session-bar {
  padding: 4px 16px 6px;
  flex-shrink: 0;
}
.mobile-session-name {
  font-size: 11px;
  color: #a1a1aa;
}

/* 移动端面板 */
.mobile-panel-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.mobile-session-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  margin-top: 8px;
}
.mobile-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px;
  font-size: 13px;
  font-weight: 600;
  color: #52525b;
}
.mobile-panel-content .session-list.mobile {
  flex: 1;
  overflow-y: auto;
}
.session-item.mobile {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px !important;
}
.session-item-main {
  flex: 1;
  min-width: 0;
}
.session-item.mobile .session-name {
  font-size: 13px;
}
.session-item-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  margin-left: 8px;
}
.session-action-icon {
  font-size: 16px;
  color: #a1a1aa;
  cursor: pointer;
  padding: 4px;
}
.session-action-icon:active { color: #409EFF; }
.session-action-icon.danger:active { color: #ef4444; }

/* ========== 移动端适配 ========== */
.chat-page.is-mobile {
  flex-direction: column;
  border-radius: 0;
}
.chat-page.is-mobile .chat-main {
  border-radius: 0;
  overflow: hidden;
  flex: 1;
  min-height: 0;
}
.chat-page.is-mobile .chat-messages {
  padding: 12px;
}
.chat-page.is-mobile .message-row {
  max-width: 95%;
}
.chat-page.is-mobile .msg-avatar {
  width: 28px;
  height: 28px;
  font-size: 14px;
}
.chat-page.is-mobile .msg-bubble {
  padding: 8px 12px;
  font-size: 14px;
  max-width: calc(100% - 38px);
}
.chat-page.is-mobile .chat-input-area {
  padding: 6px 10px;
  padding-bottom: calc(6px + env(safe-area-inset-bottom, 0px));
}
.chat-page.is-mobile .input-wrapper {
  padding: 6px 6px 6px 12px;
  border-radius: 22px;
}
.chat-page.is-mobile .chat-textarea {
  font-size: 15px;
  min-height: 24px;
}
.chat-page.is-mobile .action-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  font-size: 19px;
}
.chat-page.is-mobile .mic-btn.recording {
  width: 38px;
  height: 38px;
  border-radius: 50%;
}
.chat-page.is-mobile .send-btn.active {
  border-radius: 50%;
}
.chat-page.is-mobile .recording-indicator {
  padding: 4px 12px;
  font-size: 12px;
}

.chat-page.is-mobile .msg-actions {
  opacity: 1;
}
.chat-page.is-mobile .msg-action-btn {
  width: 28px;
  height: 28px;
}

/* 移动端欢迎 */
.chat-page.is-mobile .chat-welcome {
  padding: 20px;
}
.chat-page.is-mobile .welcome-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
}

.welcome-actions { margin-top: 20px; }
</style>
