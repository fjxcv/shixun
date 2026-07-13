<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="system-title">注册账号</h1>
        <p class="system-subtitle">中州养老服务平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        size="large"
        label-position="top"
      >
        <el-form-item prop="account" :label="'用户名'">
          <el-input v-model="form.account" :placeholder="'请输入用户名'" clearable />
        </el-form-item>
        <el-form-item prop="realname" :label="'真实姓名'">
          <el-input v-model="form.realname" :placeholder="'请输入真实姓名'" clearable />
        </el-form-item>
        <el-form-item prop="phone" :label="'手机号'">
          <el-input v-model="form.phone" :placeholder="'请输入手机号'" clearable />
        </el-form-item>
        <el-form-item prop="upwd" :label="'密码'">
          <el-input v-model="form.upwd" type="password" :placeholder="'请输入密码（至少6位）'" show-password />
        </el-form-item>
        <el-form-item prop="confirmPwd" :label="'确认密码'">
          <el-input v-model="form.confirmPwd" type="password" :placeholder="'请再次输入密码'" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleSubmit">
            {{ loading ? '提交中...' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-link type="primary" :underline="false" @click="$router.push('/')">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const formRef = ref(null)
const loading = ref(false)
const router = useRouter()

const form = reactive({
  account: '',
  realname: '',
  phone: '',
  upwd: '',
  confirmPwd: ''
})

const validateConfirm = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.upwd) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  account: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少三个字符', trigger: 'blur' }
  ],
  realname: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  upwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少6位', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ]
}

function handleSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    loading.value = true
    axios.post('/register', {
      account: form.account.trim(),
      realname: form.realname.trim(),
      phone: form.phone.trim(),
      upwd: form.upwd
    }).then(response => {
      if (response.data.code === 200) {
        ElMessage.success(response.data.msg || '注册成功')
        router.replace('/')
        return
      }
      ElMessage.error(response.data.msg || '注册失败')
    }).catch(() => {
      ElMessage.error('注册失败，请检查网络')
    }).finally(() => {
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
  padding: 40px 36px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}
.login-header { text-align: center; margin-bottom: 24px; }
.system-title { font-size: 26px; font-weight: 600; color: #2c3e50; margin: 0 0 8px 0; }
.system-subtitle { font-size: 14px; color: #909399; margin: 0; }
.login-form :deep(.el-form-item__label) { padding-bottom: 2px; }
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
  border-radius: 8px;
  margin-top: 6px;
}
.login-footer { text-align: center; margin-top: 16px; }
</style>
