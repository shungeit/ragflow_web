import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, getMe } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const username = ref(localStorage.getItem('user_username') || '')
  const nickname = ref(localStorage.getItem('user_nickname') || '')

  async function login(user, pass) {
    const res = await apiLogin(user, pass)
    token.value = res.data?.token || res.data?.access_token || ''
    username.value = res.data?.username || user
    nickname.value = res.data?.nickname || res.data?.username || user
    localStorage.setItem('user_token', token.value)
    localStorage.setItem('user_username', username.value)
    localStorage.setItem('user_nickname', nickname.value)
  }

  async function fetchMe() {
    const res = await getMe()
    username.value = res.data?.username || username.value
    nickname.value = res.data?.nickname || res.data?.username || nickname.value
    localStorage.setItem('user_username', username.value)
    localStorage.setItem('user_nickname', nickname.value)
  }

  function logout() {
    token.value = ''
    username.value = ''
    nickname.value = ''
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_username')
    localStorage.removeItem('user_nickname')
  }

  return { token, username, nickname, login, fetchMe, logout }
})
