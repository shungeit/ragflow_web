<template>
  <div class="layout-root" :class="{ 'sidebar-collapsed': collapsed }">

    <!-- ===================== PC/移动端侧边栏/抽屉 ===================== -->
    <aside class="pc-sidebar" :class="{ 'mobile-open': mobileDrawerOpen }">
      <div class="sidebar-header">
        <div class="sidebar-logo-icon"><el-icon><ChatDotRound /></el-icon></div>
        <span v-show="showLabel" class="sidebar-logo-text">RAGFlow 智能助手</span>
        <el-icon v-show="showFoldBtn" class="sidebar-toggle" @click="toggleSidebar"><Fold /></el-icon>
        <!-- 移动端关闭按钮 -->
        <el-icon v-if="isMobile" class="sidebar-close" @click="mobileDrawerOpen = false"><Close /></el-icon>
      </div>
      <div v-show="showExpandBtn" class="sidebar-expand-btn" @click="toggleSidebar">
        <el-icon><Expand /></el-icon>
      </div>

      <div class="sidebar-nav">
        <div class="nav-group">
          <div v-show="showLabel" class="nav-group-title">对话</div>
          <div class="nav-item" :class="{active: route.path==='/chat'}" @click="go('/chat')">
            <el-icon><ChatDotRound /></el-icon>
            <span v-show="showLabel" class="nav-label">智能对话</span>
          </div>
        </div>

        <div class="nav-group">
          <div v-show="showLabel" class="nav-group-title">管理</div>
          <div class="nav-item" :class="{active: route.path==='/datasets'}" @click="go('/datasets')">
            <el-icon><FolderOpened /></el-icon>
            <span v-show="showLabel" class="nav-label">知识库</span>
          </div>
          <div class="nav-item" :class="{active: route.path==='/agents'}" @click="go('/agents')">
            <el-icon><Cpu /></el-icon>
            <span v-show="showLabel" class="nav-label">智能体</span>
          </div>
        </div>

        <div class="nav-group">
          <div v-show="showLabel" class="nav-group-title">系统</div>
          <div class="nav-item" :class="{active: route.path==='/dashboard'}" @click="go('/dashboard')">
            <el-icon><DataLine /></el-icon>
            <span v-show="showLabel" class="nav-label">仪表盘</span>
          </div>
          <div class="nav-item" :class="{active: route.path==='/settings'}" @click="go('/settings')">
            <el-icon><Setting /></el-icon>
            <span v-show="showLabel" class="nav-label">系统设置</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <el-dropdown @command="handleCommand" trigger="click" placement="top-start" :teleported="true">
          <div class="sidebar-user">
            <div class="user-avatar">{{ avatarLetter }}</div>
            <div v-show="showLabel" class="user-meta">
              <span class="user-name">{{ userStore.nickname || userStore.username }}</span>
              <span class="user-email">用户</span>
            </div>
            <el-icon v-show="showLabel" class="user-more"><MoreFilled /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon> 个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- ===================== 移动端顶栏 ===================== -->
    <header class="mobile-header">
      <div class="mobile-logo">
        <div class="sidebar-logo-icon sm"><el-icon><ChatDotRound /></el-icon></div>
        <span class="mobile-title">RAGFlow</span>
      </div>
      <div class="mobile-header-right">
        <span class="mobile-page-title">{{ pageTitle }}</span>
        <el-icon class="mobile-hamburger" @click="mobileDrawerOpen = true"><Operation /></el-icon>
      </div>
    </header>

    <!-- 移动端遮罩 -->
    <transition name="fade">
      <div v-if="mobileDrawerOpen" class="mobile-overlay" @click="mobileDrawerOpen = false" />
    </transition>

    <!-- ===================== 主内容 ===================== -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- ===================== 移动端底部 TabBar ===================== -->
    <nav class="mobile-tabbar">
      <div class="tab-item" :class="{active: route.path==='/chat'}" @click="go('/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <span>对话</span>
      </div>
      <div class="tab-item" :class="{active: route.path==='/datasets'}" @click="go('/datasets')">
        <el-icon><FolderOpened /></el-icon>
        <span>知识库</span>
      </div>
      <div class="tab-item" :class="{active: route.path==='/agents'}" @click="go('/agents')">
        <el-icon><Cpu /></el-icon>
        <span>智能体</span>
      </div>
      <div class="tab-item" :class="{active: route.path==='/settings'}" @click="go('/settings')">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </div>
      <div class="tab-item" :class="{active: isMoreActive}" @click="mobileDrawerOpen = true">
        <el-icon><Grid /></el-icon>
        <span>更多</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Fold, Expand, Close, Operation, Grid,
  ChatDotRound, FolderOpened, Cpu, DataLine, Setting,
  UserFilled, MoreFilled, SwitchButton
} from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'

const route  = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isMobile } = useDevice()

const collapsed = ref(localStorage.getItem('sidebarCollapsed') === 'true')
const mobileDrawerOpen = ref(false)

const showLabel    = computed(() => !collapsed.value || isMobile.value)
const showFoldBtn  = computed(() => !collapsed.value && !isMobile.value)
const showExpandBtn = computed(() => collapsed.value && !isMobile.value)

const avatarLetter = computed(() => {
  const name = userStore.nickname || userStore.username || 'U'
  return name.charAt(0).toUpperCase()
})

const morePages = ['/dashboard']
const isMoreActive = computed(() => morePages.includes(route.path))

const pageTitles = {
  '/chat': '智能对话',
  '/dashboard': '仪表盘',
  '/datasets': '知识库',
  '/agents': '智能体',
  '/settings': '系统设置'
}
const pageTitle = computed(() => pageTitles[route.path] || '')

const toggleSidebar = () => {
  collapsed.value = !collapsed.value
  localStorage.setItem('sidebarCollapsed', collapsed.value)
}

const go = (path) => {
  router.push(path)
  mobileDrawerOpen.value = false
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push({ path: '/settings', query: { tab: 'profile' } })
    mobileDrawerOpen.value = false
  }
}
</script>

<style scoped>
.layout-root { display:flex; height:100vh; overflow:hidden; background:#f0f2f5; }

/* ===================== 侧边栏 ===================== */
.pc-sidebar {
  width:220px; min-width:220px; height:100vh;
  background:#fff; display:flex; flex-direction:column;
  transition: width .25s cubic-bezier(.4,0,.2,1), min-width .25s cubic-bezier(.4,0,.2,1);
  border-right:1px solid #ebebeb; z-index:100; flex-shrink:0;
  box-shadow: 1px 0 8px rgba(0,0,0,.04);
}
.sidebar-collapsed .pc-sidebar { width:60px; min-width:60px; }

.sidebar-header {
  height:56px; display:flex; align-items:center; gap:10px;
  padding:0 14px; flex-shrink:0; user-select:none;
  border-bottom:1px solid #f4f4f5;
}
.sidebar-collapsed .sidebar-header { justify-content:center; padding:0; gap:0; }

.sidebar-logo-icon {
  width:34px; height:34px; border-radius:9px;
  background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%);
  color:#fff; font-size:18px;
  display:flex; align-items:center; justify-content:center; flex-shrink:0;
  box-shadow: 0 4px 10px rgba(64,158,255,.35);
}
.sidebar-logo-icon.sm { width:30px; height:30px; font-size:16px; border-radius:8px; }

.sidebar-logo-text { font-size:15px; font-weight:700; color:#18181b; white-space:nowrap; flex:1; letter-spacing:-.01em; }
.sidebar-toggle { font-size:22px; color:#a1a1aa; cursor:pointer; padding:6px; border-radius:6px; transition:.2s; flex-shrink:0; }
.sidebar-toggle:hover { background:#f4f4f5; color:#18181b; }
.sidebar-close { display:none; font-size:20px; color:#a1a1aa; cursor:pointer; padding:6px; border-radius:6px; flex-shrink:0; margin-left:auto; }

.sidebar-expand-btn {
  display:flex; align-items:center; justify-content:center;
  padding:10px 0; cursor:pointer; color:#a1a1aa; font-size:20px; transition:.2s;
}
.sidebar-expand-btn:hover { color:#409EFF; }

.sidebar-nav { flex:1; overflow-y:auto; overflow-x:hidden; padding:6px 8px; }
.sidebar-collapsed .sidebar-nav { padding:6px; }
.sidebar-nav::-webkit-scrollbar { width:3px; }
.sidebar-nav::-webkit-scrollbar-thumb { background:#e4e4e7; border-radius:2px; }

.nav-group { margin-bottom:2px; }
.nav-group-title { font-size:10.5px; font-weight:600; color:#a1a1aa; text-transform:uppercase; letter-spacing:.6px; padding:14px 10px 4px; user-select:none; }

.nav-item {
  display:flex; align-items:center; gap:10px;
  padding:0 10px; height:40px; border-radius:8px;
  cursor:pointer; color:#52525b; font-size:13.5px;
  transition: background .15s, color .15s, box-shadow .15s;
  user-select:none; white-space:nowrap; position:relative;
}
.sidebar-collapsed .nav-item { justify-content:center; padding:0; gap:0; }
.nav-item:hover { background:#f4f4f5; color:#18181b; }
.nav-item.active {
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  color:#2563eb; font-weight:600;
  box-shadow: inset 3px 0 0 #3b82f6;
}
.nav-item .el-icon { font-size:17px; flex-shrink:0; }
.nav-label { overflow:hidden; text-overflow:ellipsis; }

.sidebar-footer { flex-shrink:0; border-top:1px solid #f4f4f5; padding:8px; }
.sidebar-user {
  display:flex; align-items:center; gap:10px; padding:8px 10px;
  border-radius:8px; cursor:pointer; transition:background .18s; overflow:hidden;
}
.sidebar-user:hover { background:#f4f4f5; }
.sidebar-collapsed .sidebar-user { justify-content:center; padding:8px 4px; }

.user-avatar {
  width:32px; height:32px; border-radius:50%;
  background: linear-gradient(135deg, #409EFF 0%, #6366f1 100%);
  color:#fff; font-size:13px; font-weight:700;
  display:flex; align-items:center; justify-content:center; flex-shrink:0;
  box-shadow: 0 2px 6px rgba(64,158,255,.3);
}
.user-meta { flex:1; min-width:0; display:flex; flex-direction:column; }
.user-name { font-size:13px; font-weight:600; color:#18181b; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.user-email { font-size:11px; color:#a1a1aa; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.user-more { font-size:15px; color:#a1a1aa; flex-shrink:0; }

/* ===================== 主内容 ===================== */
.main-content {
  flex:1; overflow-y:auto; background:#f0f2f5; padding:22px 24px;
  scrollbar-width:thin; scrollbar-color:#d4d4d8 transparent;
}
.main-content::-webkit-scrollbar { width:6px; }
.main-content::-webkit-scrollbar-thumb { background:#d4d4d8; border-radius:3px; }
.main-content::-webkit-scrollbar-track { background:transparent; }

/* ===================== 移动端专属元素（PC隐藏）===================== */
.mobile-header { display:none; }
.mobile-overlay { display:none; }
.mobile-tabbar  { display:none; }

/* ===================== 移动端适配 ===================== */
@media (max-width: 767px) {

  .layout-root { overflow:visible; background:#f0f2f5; }

  /* 侧边栏 -> 抽屉 */
  .pc-sidebar {
    position:fixed; top:0; left:-100%; width:268px !important; min-width:268px !important;
    height:100vh; z-index:1001;
    transition: left .3s cubic-bezier(.4,0,.2,1), box-shadow .3s;
    box-shadow:none;
  }
  .pc-sidebar.mobile-open {
    left:0;
    box-shadow:12px 0 40px rgba(0,0,0,.18);
  }
  .sidebar-collapsed .pc-sidebar { width:268px !important; min-width:268px !important; left:-100%; }
  .sidebar-collapsed .pc-sidebar.mobile-open { left:0; }
  .sidebar-collapsed .nav-item  { justify-content:flex-start; padding:0 10px; gap:10px; }
  .sidebar-collapsed .sidebar-header { justify-content:flex-start; padding:0 14px; gap:10px; }
  .sidebar-collapsed .sidebar-user   { justify-content:flex-start; padding:8px 10px; }

  .sidebar-close { display:flex; }

  /* 移动端顶栏 - 毛玻璃效果 */
  .mobile-header {
    display:flex; align-items:center; justify-content:space-between;
    position:fixed; top:0; left:0; right:0;
    height:52px;
    padding:0 16px 0 12px;
    background:rgba(255,255,255,.92);
    backdrop-filter: blur(16px) saturate(180%);
    -webkit-backdrop-filter: blur(16px) saturate(180%);
    border-bottom:1px solid rgba(0,0,0,.07);
    z-index:999;
    padding-top:env(safe-area-inset-top);
    height:calc(52px + env(safe-area-inset-top));
  }
  .mobile-logo { display:flex; align-items:center; gap:8px; }
  .mobile-title { font-size:15px; font-weight:700; color:#18181b; letter-spacing:-.01em; }
  .mobile-header-right { display:flex; align-items:center; gap:4px; }
  .mobile-page-title { font-size:13px; color:#71717a; }
  .mobile-hamburger {
    font-size:22px; color:#3f3f46; cursor:pointer;
    width:40px; height:40px; margin-right:-4px;
    display:flex; align-items:center; justify-content:center;
    border-radius:10px; transition:background .15s;
  }
  .mobile-hamburger:active { background:#f4f4f5; }

  /* 遮罩 */
  .mobile-overlay {
    display:block; position:fixed; inset:0;
    background:rgba(0,0,0,.45); z-index:1000;
    backdrop-filter:blur(3px);
  }

  /* 底部 TabBar */
  .mobile-tabbar {
    display:flex; position:fixed; bottom:0; left:0; right:0;
    height:calc(58px + env(safe-area-inset-bottom));
    padding-bottom:env(safe-area-inset-bottom);
    background:rgba(255,255,255,.95);
    backdrop-filter: blur(16px) saturate(180%);
    -webkit-backdrop-filter: blur(16px) saturate(180%);
    border-top:1px solid rgba(0,0,0,.07);
    z-index:998;
  }
  .tab-item {
    flex:1; display:flex; flex-direction:column; align-items:center;
    justify-content:center; gap:3px; position:relative;
    color:#a1a1aa; font-size:10px; font-weight:500;
    cursor:pointer; user-select:none;
    transition: color .18s;
    min-height:44px;
  }
  .tab-item .el-icon { font-size:22px; transition: transform .18s; }
  .tab-item.active { color:#2563eb; }
  .tab-item.active .el-icon {
    transform: translateY(-1px);
    filter:drop-shadow(0 2px 5px rgba(37,99,235,.35));
  }
  /* 活跃指示点 */
  .tab-item.active::after {
    content:'';
    position:absolute;
    bottom:7px;
    width:4px; height:4px;
    border-radius:50%;
    background:#2563eb;
    box-shadow: 0 0 6px rgba(37,99,235,.5);
  }
  .tab-item:active .el-icon { transform: scale(.9); }

  /* 主内容 */
  .main-content {
    margin-left:0 !important;
    padding:calc(52px + env(safe-area-inset-top) + 14px) 12px calc(58px + env(safe-area-inset-bottom) + 14px) !important;
    background:#f0f2f5;
  }
  .sidebar-collapsed .main-content { margin-left:0 !important; }
}

/* 遮罩动画 */
.fade-enter-active, .fade-leave-active { transition:opacity .22s; }
.fade-enter-from, .fade-leave-to { opacity:0; }
</style>
