import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import router from './router'
import axios from 'axios'

axios.defaults.withCredentials = true
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.timeout = 5000

function formatDateFields(data) {
  if (data == null) return data
  if (Array.isArray(data)) return data.map(formatDateFields)
  if (typeof data === 'object') {
    const out = Array.isArray(data) ? [...data] : { ...data }
    Object.keys(out).forEach(key => {
      const val = out[key]
      if (typeof val === 'string' && /^\d{4}-\d{2}-\d{2}T/.test(val)) {
        out[key] = val.replace('T', ' ').replace(/\.\d+$/, '')
      } else if (val && typeof val === 'object') {
        out[key] = formatDateFields(val)
      }
    })
    return out
  }
  return data
}

axios.interceptors.response.use(res => {
  if (res.data && res.data.data !== undefined && res.data.data !== null && typeof res.data.data === 'object') {
    res.data.data = formatDateFields(res.data.data)
  }
  return res
}, err => {
  const status = err.response?.status
  const msg = err.response?.data?.msg
  if (msg) {
    ElMessage.error(msg)
  } else if (status === 500) {
    ElMessage.error('服务器内部错误，请稍后重试')
  } else if (status === 404) {
    ElMessage.error('请求的资源不存在')
  } else if (status === 403) {
    ElMessage.error('没有访问权限')
  } else if (!err.response) {
    ElMessage.error('网络异常，请检查网络连接')
  }
  return Promise.reject(err)
})

const app = createApp(App)
  .use(router)
  .use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
