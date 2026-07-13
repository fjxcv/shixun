<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="system-title">中州养老服务平台</h1>
        <p class="system-subtitle">关爱老人 · 用心服务</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
      >
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入账号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="upwd">
          <el-input
            v-model="loginForm.upwd"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-link type="primary" :underline="false" @click="$router.push('/forgotPassword')">忘记密码</el-link>
        <span class="divider">|</span>
        <el-link type="primary" :underline="false" @click="$router.push('/register')">注册账号</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const loginFormRef = ref(null)
const loading = ref(false)
const router = useRouter()

const loginForm = reactive({
  account: '',
  upwd: ''
})

const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  upwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

function handleLogin() {
  loginFormRef.value?.validate(valid => {
    if (!valid) return
    loading.value = true
    axios.post('/login', loginForm)
      .then(response => {
        if (response.data.code === 200) {
          router.replace('/MainIndex')
          return
        }
        ElMessage.error(response.data.msg)
      })
      .catch(() => {
        ElMessage.error('登录失败，请检查网络或账号密码')
      })
      .finally(() => {
        loading.value = false
      })
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 420px;
  padding: 50px 40px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.system-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.system-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 10px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 12px;
  font-size: 16px;
}

.login-form :deep(.el-input__inner) {
  font-size: 16px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 4px;
  border-radius: 8px;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.login-footer .el-link {
  font-size: 15px;
}

.divider {
  margin: 0 12px;
  color: #dcdfe6;
}
</style>
