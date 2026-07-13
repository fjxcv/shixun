# -*- coding: utf-8 -*-
"""Register / ForgotPassword pages + HomeView links + timeline copy + proxy/router sync."""
from pathlib import Path

ROOTS = [
    Path(r"D:/vsc-maven/zzyl-project/zzylvue"),
    Path(r"D:/vsc-maven/zzylvue"),
]


def write_rel(rel: str, content: str):
    for root in ROOTS:
        if not root.exists():
            continue
        p = root / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(content, encoding="utf-8", newline="\n")
        print("wrote", p)


def patch_file(rel: str, replacements):
    for root in ROOTS:
        if not root.exists():
            continue
        p = root / rel
        if not p.exists():
            print("skip missing", p)
            continue
        text = p.read_text(encoding="utf-8")
        for old, new in replacements:
            if old not in text:
                print("WARN not found in", p, ":", repr(old[:60]))
            else:
                text = text.replace(old, new)
                print("patched", p, "->", repr(old[:40]))
        p.write_text(text, encoding="utf-8", newline="\n")


# ---------- Register.vue ----------
REGISTER = r'''<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="system-title">''' + "\u6ce8\u518c\u8d26\u53f7" + r'''</h1>
        <p class="system-subtitle">''' + "\u4e2d\u5dde\u517b\u8001\u670d\u52a1\u5e73\u53f0" + r'''</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        size="large"
        label-position="top"
      >
        <el-form-item prop="account" :label="''' + "'\u7528\u6237\u540d'" + r'''">
          <el-input v-model="form.account" :placeholder="''' + "'\u8bf7\u8f93\u5165\u7528\u6237\u540d'" + r'''" clearable />
        </el-form-item>
        <el-form-item prop="realname" :label="''' + "'\u771f\u5b9e\u59d3\u540d'" + r'''">
          <el-input v-model="form.realname" :placeholder="''' + "'\u8bf7\u8f93\u5165\u771f\u5b9e\u59d3\u540d'" + r'''" clearable />
        </el-form-item>
        <el-form-item prop="phone" :label="''' + "'\u624b\u673a\u53f7'" + r'''">
          <el-input v-model="form.phone" :placeholder="''' + "'\u8bf7\u8f93\u5165\u624b\u673a\u53f7'" + r'''" clearable />
        </el-form-item>
        <el-form-item prop="upwd" :label="''' + "'\u5bc6\u7801'" + r'''">
          <el-input v-model="form.upwd" type="password" :placeholder="''' + "'\u8bf7\u8f93\u5165\u5bc6\u7801\uff08\u81f3\u5c116\u4f4d\uff09'" + r'''" show-password />
        </el-form-item>
        <el-form-item prop="confirmPwd" :label="''' + "'\u786e\u8ba4\u5bc6\u7801'" + r'''">
          <el-input v-model="form.confirmPwd" type="password" :placeholder="''' + "'\u8bf7\u518d\u6b21\u8f93\u5165\u5bc6\u7801'" + r'''" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleSubmit">
            {{ loading ? ''' + "'\u63d0\u4ea4\u4e2d...'" + r''' : ''' + "'\u6ce8\u518c'" + r''' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-link type="primary" :underline="false" @click="$router.push('/')">''' + "\u8fd4\u56de\u767b\u5f55" + r'''</el-link>
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
    callback(new Error(''' + "'\u8bf7\u786e\u8ba4\u5bc6\u7801'" + r'''))
  } else if (value !== form.upwd) {
    callback(new Error(''' + "'\u4e24\u6b21\u5bc6\u7801\u4e0d\u4e00\u81f4'" + r'''))
  } else {
    callback()
  }
}

const rules = {
  account: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u7528\u6237\u540d'" + r''', trigger: 'blur' },
    { min: 3, message: ''' + "'\u7528\u6237\u540d\u81f3\u5c11\u4e09\u4e2a\u5b57\u7b26'" + r''', trigger: 'blur' }
  ],
  realname: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u771f\u5b9e\u59d3\u540d'" + r''', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u624b\u673a\u53f7'" + r''', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: ''' + "'\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e'" + r''', trigger: 'blur' }
  ],
  upwd: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u5bc6\u7801'" + r''', trigger: 'blur' },
    { min: 6, message: ''' + "'\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e6\u4f4d'" + r''', trigger: 'blur' }
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
        ElMessage.success(response.data.msg || ''' + "'\u6ce8\u518c\u6210\u529f'" + r''')
        router.replace('/')
        return
      }
      ElMessage.error(response.data.msg || ''' + "'\u6ce8\u518c\u5931\u8d25'" + r''')
    }).catch(() => {
      ElMessage.error(''' + "'\u6ce8\u518c\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u7f51\u7edc'" + r''')
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
'''

# ---------- ForgotPassword.vue ----------
FORGOT = r'''<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="system-title">''' + "\u5fd8\u8bb0\u5bc6\u7801" + r'''</h1>
        <p class="system-subtitle">''' + "\u9a8c\u8bc1\u8d26\u53f7\u4e0e\u624b\u673a\u53f7\u540e\u8bbe\u7f6e\u65b0\u5bc6\u7801" + r'''</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        size="large"
        label-position="top"
      >
        <el-form-item prop="account" :label="''' + "'\u7528\u6237\u540d'" + r'''">
          <el-input v-model="form.account" :placeholder="''' + "'\u8bf7\u8f93\u5165\u6ce8\u518c\u7528\u6237\u540d'" + r'''" clearable />
        </el-form-item>
        <el-form-item prop="phone" :label="''' + "'\u624b\u673a\u53f7'" + r'''">
          <el-input v-model="form.phone" :placeholder="''' + "'\u8bf7\u8f93\u5165\u6ce8\u518c\u624b\u673a\u53f7'" + r'''" clearable />
        </el-form-item>
        <el-form-item prop="newpwd" :label="''' + "'\u65b0\u5bc6\u7801'" + r'''">
          <el-input v-model="form.newpwd" type="password" :placeholder="''' + "'\u8bf7\u8f93\u5165\u65b0\u5bc6\u7801\uff08\u81f3\u5c116\u4f4d\uff09'" + r'''" show-password />
        </el-form-item>
        <el-form-item prop="confirmPwd" :label="''' + "'\u786e\u8ba4\u65b0\u5bc6\u7801'" + r'''">
          <el-input v-model="form.confirmPwd" type="password" :placeholder="''' + "'\u8bf7\u518d\u6b21\u8f93\u5165\u65b0\u5bc6\u7801'" + r'''" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleSubmit">
            {{ loading ? ''' + "'\u63d0\u4ea4\u4e2d...'" + r''' : ''' + "'\u91cd\u7f6e\u5bc6\u7801'" + r''' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-link type="primary" :underline="false" @click="$router.push('/')">''' + "\u8fd4\u56de\u767b\u5f55" + r'''</el-link>
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
  phone: '',
  newpwd: '',
  confirmPwd: ''
})

const validateConfirm = (_rule, value, callback) => {
  if (!value) {
    callback(new Error(''' + "'\u8bf7\u786e\u8ba4\u65b0\u5bc6\u7801'" + r'''))
  } else if (value !== form.newpwd) {
    callback(new Error(''' + "'\u4e24\u6b21\u5bc6\u7801\u4e0d\u4e00\u81f4'" + r'''))
  } else {
    callback()
  }
}

const rules = {
  account: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u7528\u6237\u540d'" + r''', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u624b\u673a\u53f7'" + r''', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: ''' + "'\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e'" + r''', trigger: 'blur' }
  ],
  newpwd: [
    { required: true, message: ''' + "'\u8bf7\u8f93\u5165\u65b0\u5bc6\u7801'" + r''', trigger: 'blur' },
    { min: 6, message: ''' + "'\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e6\u4f4d'" + r''', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ]
}

function handleSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    loading.value = true
    axios.post('/resetPwd', {
      account: form.account.trim(),
      phone: form.phone.trim(),
      newpwd: form.newpwd
    }).then(response => {
      if (response.data.code === 200) {
        ElMessage.success(response.data.msg || ''' + "'\u5bc6\u7801\u91cd\u7f6e\u6210\u529f'" + r''')
        router.replace('/')
        return
      }
      ElMessage.error(response.data.msg || ''' + "'\u91cd\u7f6e\u5931\u8d25'" + r''')
    }).catch(() => {
      ElMessage.error(''' + "'\u91cd\u7f6e\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u7f51\u7edc'" + r''')
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
  letter-spacing: 2px;
  border-radius: 8px;
  margin-top: 6px;
}
.login-footer { text-align: center; margin-top: 16px; }
</style>
'''

write_rel("src/views/Register.vue", REGISTER)
write_rel("src/views/ForgotPassword.vue", FORGOT)

# ---------- router ----------
patch_file("src/router/index.js", [
    (
        "import HomeView from '../views/HomeView.vue'\nimport MainIndex from '../views/MainIndex.vue'\n\nconst routes = [\n  { path: '/', name: 'home', component: HomeView },",
        "import HomeView from '../views/HomeView.vue'\nimport MainIndex from '../views/MainIndex.vue'\n\nconst routes = [\n  { path: '/', name: 'home', component: HomeView },\n  { path: '/register', name: 'register', component: () => import('@/views/Register.vue') },\n  { path: '/forgotPassword', name: 'forgotPassword', component: () => import('@/views/ForgotPassword.vue') },",
    )
])

# ---------- HomeView links ----------
patch_file("src/views/HomeView.vue", [
    (
        '<el-link type="primary" :underline="false">\u5fd8\u8bb0\u5bc6\u7801</el-link>\n        <span class="divider">|</span>\n        <el-link type="primary" :underline="false">\u6ce8\u518c\u8d26\u53f7</el-link>',
        '<el-link type="primary" :underline="false" @click="$router.push(\'/forgotPassword\')">\u5fd8\u8bb0\u5bc6\u7801</el-link>\n        <span class="divider">|</span>\n        <el-link type="primary" :underline="false" @click="$router.push(\'/register\')">\u6ce8\u518c\u8d26\u53f7</el-link>',
    )
])

# ---------- vue.config.js proxy ----------
patch_file("vue.config.js", [
    (
        "'/login': apiProxy,\n      '/logout': apiProxy,",
        "'/login': apiProxy,\n      '/register': apiProxy,\n      '/resetPwd': apiProxy,\n      '/logout': apiProxy,",
    )
])

# ---------- OperationTimeline copy ----------
patch_file("src/components/checkout/OperationTimeline.vue", [
    (
        "name: `\uff08\u89d2\u8272\uff09\u5904\u7406-${prefix}\u5ba1\u6279`,",
        "name: `\u62a4\u7406\u7ec4\u4e3b\u7ba1\u5904\u7406-${prefix}\u5ba1\u6279`,",
    ),
    (
        "name: `\u5ba1\u6279\u89d2\u8272\u5904\u7406-${prefix}\u8bc4\u4f30`,",
        "name: `\u5ba1\u6279-${prefix}\u8bc4\u4f30`,",
    ),
    (
        "name: `\u5ba1\u6279\u89d2\u8272\u5904\u7406-${prefix}\u914d\u7f6e`,",
        "name: `\u5ba1\u6279-${prefix}\u914d\u7f6e`,",
    ),
])

print("done")
