import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/chat',
    meta: { requiresAuth: true },
    children: [
      { path: 'chat',      name: 'Chat',      component: () => import('@/views/Chat.vue') },
      { path: 'dashboard',  name: 'Dashboard',  component: () => import('@/views/Dashboard.vue') },
      { path: 'datasets',   name: 'Datasets',   component: () => import('@/views/Datasets.vue') },
      { path: 'agents',     name: 'Agents',     component: () => import('@/views/Agents.vue') },
      { path: 'settings',   name: 'Settings',   component: () => import('@/views/Settings.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('user_token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
