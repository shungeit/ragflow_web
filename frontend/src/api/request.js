import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('user_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    // RAGFlow style: code===0 means success; also accept code===200
    if (res.data && (res.data.code === 0 || res.data.code === 200)) return res.data
    if (res.data && res.data.code === 401 && !res.config?.url?.includes('/login')) {
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_username')
      localStorage.removeItem('user_nickname')
      router.replace('/login')
      return Promise.reject(res.data)
    }
    if (res.data && res.data.code !== undefined) {
      ElMessage.error(res.data.message || '操作失败')
      return Promise.reject(res.data)
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401 && !err.config?.url?.includes('/login')) {
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_username')
      localStorage.removeItem('user_nickname')
      router.replace('/login')
      return Promise.reject(err)
    }
    const msg = err.response?.data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
