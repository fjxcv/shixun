# -*- coding: ascii -*-
"""Rewrite remaining Vue pages as UTF-8 using unicode escapes only (ASCII .py)."""
from pathlib import Path

ROOTS = [
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src"),
    Path(r"D:/zzylvue/src"),
]
DEFAULT_AVATAR = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"


def write(rel, content):
    text = content.encode("utf-8").decode("unicode_escape")
    for base in ROOTS:
        if not base.exists():
            print("skip missing root", base)
            continue
        p = base / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(text, encoding="utf-8", newline="\n")
        print("wrote", p)


MAIN_INDEX = r'''<template>
  <el-container class="main-layout">
    <el-header class="top-header" height="56px">
      <div class="logo-area">
        <div class="logo-icon">\u4e2d</div>
        <div class="logo-text">
          <div class="logo-title">\u4e2d\u5dde\u517b\u8001</div>
          <div class="logo-sub">ZHONG ZHOU YANG LAO</div>
        </div>
      </div>
      <div class="top-menu">
        <span
          v-for="item in topMenus"
          :key="item.module"
          :class="['top-menu-item', { active: activeModule === item.module }]"
          @click="switchModule(item)"
        >{{ item.name }}</span>
      </div>
      <div class="top-right">
        <el-dropdown trigger="click">
          <span class="user-dropdown">
            <el-avatar :size="28" :src="avatarUrl" />
            <span>{{ realName || '\u7ba1\u7406\u5458' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="pwdDialogVisible = true">\u4fee\u6539\u5bc6\u7801</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">\u9000\u51fa\u767b\u5f55</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <el-aside v-if="sideMenus.length" :width="collapsed ? '64px' : '200px'" class="side-aside">
        <el-menu :default-active="route.path" :collapse="collapsed" router class="side-menu">
          <template v-for="(item, idx) in sideMenus" :key="idx">
            <el-sub-menu v-if="item.children && !item.path" :index="item.name + idx">
              <template #title>{{ item.name }}</template>
              <el-menu-item v-for="sub in item.children" :key="sub.path" :index="sub.path">{{ sub.name }}</el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path">{{ item.name }}</el-menu-item>
          </template>
        </el-menu>
        <div class="aside-footer">
          <el-icon class="footer-icon" @click="handleLogout"><SwitchButton /></el-icon>
          <el-icon class="footer-icon" @click="collapsed = !collapsed"><Fold /></el-icon>
        </div>
      </el-aside>
      <el-main class="main-content"><router-view /></el-main>
    </el-container>

    <div class="page-footer">Copyright @ 2019-2020 Tencent. All Rights Reserved</div>

    <el-dialog v-model="pwdDialogVisible" title="\u4fee\u6539\u5bc6\u7801" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="\u539f\u5bc6\u7801" required>
          <el-input v-model="pwdForm.oldpwd" type="password" show-password placeholder="\u8bf7\u8f93\u5165" />
        </el-form-item>
        <el-form-item label="\u65b0\u5bc6\u7801" required>
          <el-input v-model="pwdForm.newpwd" type="password" show-password placeholder="\u8bf7\u8f93\u5165" />
        </el-form-item>
        <el-form-item label="\u786e\u8ba4\u65b0\u5bc6\u7801" required>
          <el-input v-model="pwdForm.newpwd2" type="password" show-password placeholder="\u8bf7\u8f93\u5165" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">\u53d6\u6d88</el-button>
        <el-button type="primary" @click="updateUserPwd">\u786e\u5b9a</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { topMenus, findModuleByPath, getSideMenus } from '@/config/menu'

const DEFAULT_AVATAR = 'PLACEHOLDER_AVATAR'
const route = useRoute()
const router = useRouter()
const realName = ref('\u7ba1\u7406\u5458')
const avatarUrl = ref(localStorage.getItem('zzyl_avatar') || DEFAULT_AVATAR)
const collapsed = ref(false)
const pwdDialogVisible = ref(false)
const pwdForm = reactive({ oldpwd: '', newpwd: '', newpwd2: '' })
const activeModule = ref('workbench')
const sideMenus = computed(() => getSideMenus(activeModule.value))

watch(() => route.path, (path) => {
  activeModule.value = findModuleByPath(path)
}, { immediate: true })

onMounted(() => {
  loadLoginUserInfo()
  window.addEventListener('storage', onStorageAvatar)
  window.addEventListener('zzyl-avatar-updated', onAvatarEvent)
})
onUnmounted(() => {
  window.removeEventListener('storage', onStorageAvatar)
  window.removeEventListener('zzyl-avatar-updated', onAvatarEvent)
})

function onStorageAvatar(e) {
  if (e.key === 'zzyl_avatar' && e.newValue) avatarUrl.value = e.newValue
}
function onAvatarEvent(e) {
  if (e?.detail) avatarUrl.value = e.detail
}

function switchModule(item) {
  activeModule.value = item.module
  const flat = []
  const walk = (list) => list.forEach(i => {
    if (i.path) flat.push(i)
    if (i.children) walk(i.children)
  })
  walk(item.children || [])
  router.push(item.children?.length ? (flat[0]?.path || item.path) : item.path)
}

function loadLoginUserInfo() {
  axios.get('/loadInfo').then(res => {
    if (res.data?.realname) realName.value = res.data.realname
    else if (res.data?.uname) realName.value = res.data.uname
    if (res.data?.image) {
      avatarUrl.value = res.data.image
      localStorage.setItem('zzyl_avatar', res.data.image)
    }
  }).catch(() => {})
}

function updateUserPwd() {
  if (pwdForm.newpwd !== pwdForm.newpwd2) {
    ElMessage.warning('\u4e24\u6b21\u65b0\u5bc6\u7801\u8f93\u5165\u4e0d\u4e00\u81f4')
    return
  }
  axios.post('/updatePwd', pwdForm).then(res => {
    ElMessage.success(res.data.msg || '\u4fee\u6539\u6210\u529f')
    if (res.data.code === 200) {
      pwdForm.oldpwd = ''
      pwdForm.newpwd = ''
      pwdForm.newpwd2 = ''
      pwdDialogVisible.value = false
    }
  })
}

function handleLogout() {
  axios.post('/logout').finally(() => router.replace('/'))
}
</script>

<style scoped>
.main-layout { min-height: 100vh; background: #f5f7fa; }
.top-header { display: flex; align-items: center; background: #fff; border-bottom: 1px solid #e8e8e8; padding: 0 20px; }
.logo-area { display: flex; align-items: center; min-width: 160px; }
.logo-icon { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #ff9a56, #4ecdc4); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 8px; }
.logo-title { font-size: 16px; font-weight: 600; color: #333; }
.logo-sub { font-size: 10px; color: #999; }
.top-menu { flex: 1; display: flex; flex-wrap: wrap; gap: 4px 16px; padding: 0 16px; overflow: hidden; }
.top-menu-item { cursor: pointer; font-size: 14px; color: #666; white-space: nowrap; padding: 4px 0; }
.top-menu-item.active { color: #409eff; font-weight: 600; }
.top-menu-item:hover { color: #409eff; }
.top-right { display: flex; align-items: center; gap: 12px; }
.user-dropdown { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.side-aside { background: #fff; border-right: 1px solid #e8e8e8; display: flex; flex-direction: column; }
.side-menu { border-right: none; flex: 1; }
.aside-footer { display: flex; justify-content: space-around; padding: 12px; border-top: 1px solid #f0f0f0; }
.footer-icon { cursor: pointer; font-size: 18px; color: #666; }
.main-content { padding: 16px; }
.page-footer { text-align: center; color: #999; font-size: 12px; padding: 12px; background: #f5f7fa; }
</style>
'''.replace('PLACEHOLDER_AVATAR', DEFAULT_AVATAR)

UESR_INFO = r'''<template>
  <PageCard>
    <h3 class="page-title">\u4e2a\u4eba\u4e2d\u5fc3</h3>
    <el-form :model="userInfo" label-width="100px" class="user-form">
      <el-row :gutter="40">
        <el-col :span="12">
          <el-form-item label="\u59d3\u540d" required>
            <el-input v-model="userInfo.realname" maxlength="10" show-word-limit />
          </el-form-item>
          <el-form-item label="\u90ae\u7bb1">{{ userInfo.email || '\u2014' }}</el-form-item>
          <el-form-item label="\u6240\u5c5e\u90e8\u95e8">{{ userInfo.department || '\u2014' }}</el-form-item>
          <el-form-item label="\u6240\u5c5e\u804c\u4f4d">{{ userInfo.job || '\u2014' }}</el-form-item>
          <el-form-item label="\u89d2\u8272">{{ userInfo.role || '\u2014' }}</el-form-item>
          <el-form-item label="\u624b\u673a\u53f7">
            <el-input v-model="userInfo.phone" maxlength="11" show-word-limit />
          </el-form-item>
          <el-form-item label="\u6027\u522b">
            <el-radio-group v-model="userInfo.sex">
              <el-radio value="\u7537">\u7537</el-radio>
              <el-radio value="\u5973">\u5973</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="\u5934\u50cf" required>
            <div class="avatar-block">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :http-request="uploadAvatar"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="imageUrl" :src="imageUrl" class="avatar" alt="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="avatar-tips">
                <p>\u70b9\u51fb\u5de6\u4fa7\u5706\u5708\u4e0a\u4f20\u5934\u50cf</p>
                <p>\u56fe\u7247\u5927\u5c0f\u4e0d\u8d85\u8fc72M</p>
                <p>\u4ec5\u652f\u6301 PNG/JPG/JPEG</p>
              </div>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-actions">
        <el-button type="primary" @click="saveUserInfo">\u4fdd\u5b58</el-button>
      </div>
    </el-form>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const DEFAULT_AVATAR = 'PLACEHOLDER_AVATAR'
const userInfo = reactive({
  id: null, realname: '', email: '', department: '', job: '', role: '', phone: '', sex: '', image: ''
})
const imageUrl = ref('')

onMounted(() => loadShowInfo())

function notifyAvatar(url) {
  if (!url) return
  localStorage.setItem('zzyl_avatar', url)
  window.dispatchEvent(new CustomEvent('zzyl-avatar-updated', { detail: url }))
}

function resolveImage(url) {
  if (!url || url === 'ok') return DEFAULT_AVATAR
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return (axios.defaults.baseURL || 'http://localhost:8080') + url
  return url
}

function loadShowInfo() {
  axios.get('/showInfo').then(res => {
    const obj = res.data || {}
    Object.assign(userInfo, obj)
    if (!userInfo.id) userInfo.id = 1
    imageUrl.value = resolveImage(obj.image)
  }).catch(() => {
    Object.assign(userInfo, {
      id: 1, realname: '', email: '', department: '',
      job: '', role: '', phone: '', sex: '\u7537', image: DEFAULT_AVATAR
    })
    imageUrl.value = DEFAULT_AVATAR
  })
}

function beforeAvatarUpload(file) {
  const okType = ['image/png', 'image/jpeg', 'image/jpg'].includes(file.type)
  const okSize = file.size / 1024 / 1024 < 2
  if (!okType) {
    ElMessage.error('\u4ec5\u652f\u6301 PNG/JPG/JPEG')
    return false
  }
  if (!okSize) {
    ElMessage.error('\u56fe\u7247\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc72M')
    return false
  }
  return true
}

function uploadAvatar(option) {
  const formData = new FormData()
  formData.append('mf', option.file)
  axios.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    if (res.data && res.data.code === 200 && res.data.data) {
      imageUrl.value = resolveImage(res.data.data)
      userInfo.image = imageUrl.value
      ElMessage.success('\u5934\u50cf\u4e0a\u4f20\u6210\u529f')
      option.onSuccess && option.onSuccess(res.data)
      notifyAvatar(imageUrl.value)
      if (userInfo.id) {
        axios.post('/updateUser', { id: userInfo.id, image: userInfo.image }).then(() => {
          notifyAvatar(imageUrl.value)
        }).catch(() => {})
      }
    } else {
      ElMessage.error(res.data?.msg || '\u5934\u50cf\u4e0a\u4f20\u5931\u8d25')
      option.onError && option.onError(new Error(res.data?.msg || 'upload fail'))
    }
  }).catch(err => {
    ElMessage.error('\u5934\u50cf\u4e0a\u4f20\u5931\u8d25')
    option.onError && option.onError(err)
  })
}

function saveUserInfo() {
  if (!userInfo.id) {
    ElMessage.error('\u7528\u6237\u4fe1\u606f\u672a\u52a0\u8f7d\uff0c\u8bf7\u5237\u65b0\u540e\u91cd\u8bd5')
    return
  }
  if (!userInfo.realname) {
    ElMessage.warning('\u8bf7\u586b\u5199\u59d3\u540d')
    return
  }
  axios.post('/updateUser', userInfo).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg || '\u4fdd\u5b58\u6210\u529f')
      notifyAvatar(imageUrl.value || userInfo.image)
    } else {
      ElMessage.error(res.data.msg || '\u4fdd\u5b58\u5931\u8d25')
    }
  }).catch(() => ElMessage.error('\u4fdd\u5b58\u5931\u8d25'))
}
</script>

<style scoped>
.page-title { margin: 0 0 20px; font-size: 18px; }
.user-form { max-width: 900px; }
.avatar-block { display: flex; gap: 16px; align-items: flex-start; }
.avatar { width: 120px; height: 120px; border-radius: 50%; object-fit: cover; display: block; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 120px; height: 120px; border: 1px dashed #d9d9d9; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.avatar-tips p { margin: 4px 0; color: #999; font-size: 12px; }
.form-actions { text-align: center; margin-top: 24px; }
</style>
'''.replace('PLACEHOLDER_AVATAR', DEFAULT_AVATAR)

INFO_SECTIONS = r'''<template>
  <div>
    <h4>\u57fa\u672c\u4fe1\u606f</h4>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="\u5355\u636e\u7f16\u53f7">{{ data.docNo || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u8001\u4eba\u59d3\u540d">{{ data.elderName || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7">{{ data.elderIdcard || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u8054\u7cfb\u65b9\u5f0f">{{ data.elderPhone || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u8d39\u7528\u671f\u9650">{{ feePeriod }}</el-descriptions-item>
      <el-descriptions-item label="\u62a4\u7406\u7b49\u7ea7">{{ data.nursingLevel || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u5165\u4f4f\u5e8a\u4f4d">{{ data.bedInfo || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u7b7e\u8ba2\u5408\u540c">{{ data.contractName || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u517b\u8001\u987e\u95ee">{{ data.consultant || '\u2014' }}</el-descriptions-item>
    </el-descriptions>

    <el-divider />
    <h4>\u7533\u8bf7\u4fe1\u606f</h4>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="\u9000\u4f4f\u65e5\u671f">{{ data.checkoutDate || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u9000\u4f4f\u539f\u56e0">{{ data.reason || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u5907\u6ce8">{{ data.remark || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u7533\u8bf7\u4eba">{{ data.applicant || '\u2014' }}</el-descriptions-item>
      <el-descriptions-item label="\u7533\u8bf7\u65f6\u95f4">{{ data.applyTime || '\u2014' }}</el-descriptions-item>
    </el-descriptions>

    <template v-if="showTerminate">
      <el-divider />
      <h4>\u89e3\u9664\u8bb0\u5f55</h4>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="\u63d0\u4ea4\u4eba">{{ data.consultant || data.applicant || '\u2014' }}</el-descriptions-item>
        <el-descriptions-item label="\u89e3\u9664\u65e5\u671f">{{ data.terminateDate || '\u2014' }}</el-descriptions-item>
        <el-descriptions-item label="\u89e3\u9664\u534f\u8bae">{{ data.terminateFile || '\u2014' }}</el-descriptions-item>
      </el-descriptions>
    </template>

    <template v-if="showBill">
      <el-divider />
      <h4>\u8d26\u5355\u660e\u7ec6</h4>
      <p>\u5e94\u9000 {{ billReceivable }}\u5143 \u00b7 \u6b20\u8d39 {{ billArrears }}\u5143 \u00b7 \u4f59\u989d {{ billBalance }}\u5143</p>
      <p>\u6700\u7ec8\u9000\u6b3e\u91d1\u989d = \u5e94\u9000 - \u6b20\u8d39 + \u4f59\u989d = {{ refundDisplay }}\u5143</p>
    </template>

    <template v-if="showSettlement">
      <el-divider />
      <h4>\u5b8c\u6210\u8d26\u5355\u6e05\u7b97</h4>
      <p>\u5e94\u9000 {{ billReceivable }}\u5143 \u00b7 \u6b20\u8d39 {{ billArrears }}\u5143 \u00b7 \u4f59\u989d {{ billBalance }}\u5143</p>
      <p>\u9000\u6b3e\u4fe1\u606f\uff1a\u5269\u4f59\u9884\u4ed8\u6b3e {{ refundDisplay }} \u5143</p>
      <p>\u9000\u6b3e\u65b9\u5f0f\uff1a\u73b0\u91d1 \u00b7 \u5171\u9000\u6b3e {{ refundDisplay }} \u5143\uff0c\u5df2\u7ed3\u6e05</p>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Object, default: () => ({}) },
  showTerminate: Boolean,
  showBill: Boolean,
  showSettlement: Boolean
})

const feePeriod = computed(() => {
  const { feeStart, feeEnd } = props.data
  return feeStart && feeEnd ? `${feeStart} \u2014 ${feeEnd}` : '\u2014'
})

function num(v) {
  if (v == null || v === '') return '\u2014'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : String(v)
}

const billReceivable = computed(() => num(props.data.billReceivable))
const billArrears = computed(() => num(props.data.billArrears))
const billBalance = computed(() => num(props.data.billBalance))

const refundDisplay = computed(() => {
  if (props.data.refundAmount != null && props.data.refundAmount !== '') {
    return num(props.data.refundAmount)
  }
  const a = Number(props.data.billReceivable)
  const b = Number(props.data.billArrears)
  const c = Number(props.data.billBalance)
  if ([a, b, c].every(x => Number.isFinite(x))) {
    return (a - b + c).toFixed(2)
  }
  return '\u2014'
})
</script>
'''

CHECKOUT_DETAIL = r'''<template>
  <PageCard>
    <CheckoutSteps :active="stepIndex" />
    <div v-if="readonlyView" class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" show-terminate show-bill show-settlement />
        <div class="form-actions"><el-button @click="goBack">\u8fd4\u56de</el-button></div>
      </div>
      <OperationTimeline class="detail-side" action-prefix="\u9000\u4f4f" :data="detail" />
    </div>
    <div v-else class="detail-layout">
      <div class="detail-main">
        <InfoSections
          :data="detail"
          :show-terminate="step >= 3"
          :show-bill="step >= 4"
          :show-settlement="step >= 7"
        />

        <div v-if="isApprovalStep" class="step-panel">
          <h4>{{ approvalTitle }}</h4>
          <el-radio-group v-model="approvalResult">
            <el-radio value="\u901a\u8fc7">\u901a\u8fc7</el-radio>
            <el-radio value="\u62d2\u7edd">\u62d2\u7edd</el-radio>
            <el-radio value="\u9000\u56de">\u9000\u56de</el-radio>
          </el-radio-group>
          <el-input v-model="approvalComment" type="textarea" maxlength="200" show-word-limit placeholder="\u5ba1\u6279\u610f\u89c1" style="margin-top:12px" />
        </div>

        <div v-if="step === 3" class="step-panel">
          <h4>\u89e3\u9664\u5408\u540c</h4>
          <el-form label-width="120px">
            <el-form-item label="\u89e3\u9664\u65e5\u671f" required>
              <el-date-picker v-model="detail.terminateDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
            <el-form-item label="\u89e3\u9664\u534f\u8bae" required>
              <el-upload action="#" :http-request="uploadTerminate" :show-file-list="false">
                <el-button type="primary" plain>\u4e0a\u4f20\u6587\u4ef6</el-button>
              </el-upload>
              <span v-if="detail.terminateFile" style="margin-left:8px">{{ detail.terminateFile }}</span>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="step === 4" class="step-panel">
          <h4>\u8c03\u6574\u8d26\u5355</h4>
          <el-form label-width="160px">
            <el-form-item label="\u5e94\u9000(\u5143)" required>
              <el-input-number v-model="detail.billReceivable" :precision="2" style="width:240px" @change="recalcRefund" />
            </el-form-item>
            <el-form-item label="\u6b20\u8d39(\u5143)" required>
              <el-input-number v-model="detail.billArrears" :precision="2" style="width:240px" @change="recalcRefund" />
            </el-form-item>
            <el-form-item label="\u4f59\u989d(\u5143)" required>
              <el-input-number v-model="detail.billBalance" :precision="2" style="width:240px" @change="recalcRefund" />
            </el-form-item>
            <el-form-item label="\u6700\u7ec8\u9000\u6b3e\u91d1\u989d(\u5143)">
              <el-input-number v-model="detail.refundAmount" :precision="2" style="width:240px" />
            </el-form-item>
            <p class="hint">\u9000\u6b3e = \u5e94\u9000 - \u6b20\u8d39 + \u4f59\u989d</p>
          </el-form>
        </div>

        <div v-if="step === 7" class="step-panel">
          <h4>\u8d39\u7528\u6e05\u7b97</h4>
          <p>\u5e94\u9000 {{ fmtMoney(detail.billReceivable) }}\u5143 \u00b7 \u6b20\u8d39 {{ fmtMoney(detail.billArrears) }}\u5143 \u00b7 \u4f59\u989d {{ fmtMoney(detail.billBalance) }}\u5143</p>
          <p>\u9000\u6b3e\u4fe1\u606f\uff1a\u5269\u4f59\u9884\u4ed8\u6b3e {{ fmtMoney(detail.refundAmount) }} \u5143</p>
          <p>\u9000\u6b3e\u65b9\u5f0f\uff1a\u73b0\u91d1 \u00b7 \u5171\u9000\u6b3e {{ fmtMoney(detail.refundAmount) }} \u5143\uff0c\u5df2\u7ed3\u6e05</p>
        </div>

        <div class="form-actions">
          <el-button @click="goBack">\u8fd4\u56de</el-button>
          <el-button type="primary" @click="submitCurrent">{{ step === 7 ? '\u5b8c\u6210\u6e05\u7b97' : '\u4fdd\u5b58' }}</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" action-prefix="\u9000\u4f4f" :data="detail" />
    </div>
  </PageCard>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'
import CheckoutSteps from '@/components/CheckoutSteps.vue'
import InfoSections from '@/components/checkout/InfoSections.vue'
import OperationTimeline from '@/components/checkout/OperationTimeline.vue'

const route = useRoute()
const router = useRouter()
const detail = reactive({})
const step = ref(2)
const approvalResult = ref('\u901a\u8fc7')
const approvalComment = ref('')

const stepIndex = computed(() => Math.max(0, (step.value || 2) - 1))
const readonlyView = computed(() => detail.flowStatus === '\u5df2\u5b8c\u6210' || detail.flowStatus === '\u5df2\u5173\u95ed')
const isApprovalStep = computed(() => [2, 5, 6].includes(step.value))
const approvalTitle = computed(() => {
  if (step.value === 2) return '\u7533\u8bf7\u5ba1\u6279'
  if (step.value === 5) return '\u8d26\u5355\u5ba1\u6279'
  if (step.value === 6) return '\u9000\u4f4f\u5ba1\u6279'
  return '\u5ba1\u6279'
})

onMounted(loadDetail)

function fmtMoney(v) {
  if (v == null || v === '') return '\u2014'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : String(v)
}

function recalcRefund() {
  const a = Number(detail.billReceivable) || 0
  const b = Number(detail.billArrears) || 0
  const c = Number(detail.billBalance) || 0
  detail.refundAmount = Number((a - b + c).toFixed(2))
}

function loadDetail() {
  const id = route.query.id
  if (!id) return
  axios.get('/checkout/detail', { params: { id } }).then(res => {
    if (res.data.code === 200) {
      Object.assign(detail, res.data.data || {})
      if (detail.billReceivable == null) detail.billReceivable = 0
      if (detail.billArrears == null) detail.billArrears = 0
      if (detail.billBalance == null) detail.billBalance = 0
      if (detail.refundAmount == null) recalcRefund()
      step.value = Number(route.query.step) || Number(detail.step) || 2
    } else {
      ElMessage.error(res.data.msg || '\u52a0\u8f7d\u8be6\u60c5\u5931\u8d25')
    }
  }).catch(() => ElMessage.error('\u52a0\u8f7d\u8be6\u60c5\u5931\u8d25'))
}

function goBack() {
  router.push('/CheckoutProcess')
}

function uploadTerminate(option) {
  const formData = new FormData()
  formData.append('mf', option.file)
  axios.post('/upload', formData).then(res => {
    if (res.data.code === 200) {
      detail.terminateFile = res.data.data
      ElMessage.success('\u4e0a\u4f20\u6210\u529f')
    } else {
      ElMessage.error(res.data.msg || '\u4e0a\u4f20\u5931\u8d25')
    }
  }).catch(() => ElMessage.error('\u4e0a\u4f20\u5931\u8d25'))
}

function validateCurrent() {
  if (isApprovalStep.value && !approvalResult.value) {
    ElMessage.warning('\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c')
    return false
  }
  if (step.value === 3) {
    if (!detail.terminateDate || !detail.terminateFile) {
      ElMessage.warning('\u8bf7\u5b8c\u5584\u89e3\u9664\u534f\u8bae\u4fe1\u606f')
      return false
    }
  }
  if (step.value === 4) {
    if (detail.billReceivable == null || detail.billArrears == null || detail.billBalance == null) {
      ElMessage.warning('\u8bf7\u586b\u5199\u8d26\u5355\u91d1\u989d')
      return false
    }
    recalcRefund()
  }
  return true
}

function submitCurrent() {
  if (!validateCurrent()) return
  const payload = {
    id: detail.id,
    approvalResult: approvalResult.value,
    approvalComment: approvalComment.value,
    terminateDate: detail.terminateDate,
    terminateFile: detail.terminateFile,
    billReceivable: detail.billReceivable,
    billArrears: detail.billArrears,
    billBalance: detail.billBalance,
    refundAmount: detail.refundAmount
  }
  if (!isApprovalStep.value) {
    payload.step = step.value === 7 ? 7 : step.value + 1
  }
  axios.post('/checkout/updateStep', payload).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(step.value === 7 ? '\u9000\u4f4f\u6d41\u7a0b\u5df2\u5b8c\u6210' : '\u4fdd\u5b58\u6210\u529f')
      if (step.value === 7 || approvalResult.value === '\u62d2\u7edd') {
        goBack()
      } else {
        const next = Number(res.data.data) || (step.value + 1)
        step.value = next
        router.replace({ path: '/CheckoutDetail', query: { id: detail.id, step: next } })
        loadDetail()
      }
    } else {
      ElMessage.error(res.data.msg || '\u64cd\u4f5c\u5931\u8d25')
    }
  }).catch(() => ElMessage.error('\u64cd\u4f5c\u5931\u8d25'))
}
</script>

<style scoped>
.detail-layout { display: flex; gap: 20px; align-items: flex-start; }
.detail-main { flex: 1; min-width: 0; }
.detail-side { width: 280px; flex-shrink: 0; }
.form-actions { margin-top: 24px; text-align: center; }
.step-panel { margin-top: 20px; }
.hint { color: #666; font-size: 13px; margin: 0; }
</style>
'''

LEAVE_DETAIL = r'''<template>
  <PageCard>
    <el-steps :active="stepsActive" align-center finish-status="success" style="margin-bottom: 24px">
      <el-step title="\u7533\u8bf7\u8bf7\u5047" />
      <el-step title="\u7533\u8bf7\u5ba1\u6279" />
      <el-step title="\u8bf7\u5047\u5b8c\u6210" />
    </el-steps>

    <el-row :gutter="20">
      <el-col :span="16">
        <h4>\u57fa\u672c\u4fe1\u606f</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="\u5355\u636e\u7f16\u53f7">{{ detail.docNo }}</el-descriptions-item>
          <el-descriptions-item label="\u8001\u4eba\u59d3\u540d">{{ detail.elderName }}</el-descriptions-item>
          <el-descriptions-item label="\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7">{{ detail.elderIdcard }}</el-descriptions-item>
          <el-descriptions-item label="\u8054\u7cfb\u65b9\u5f0f">{{ detail.elderPhone }}</el-descriptions-item>
          <el-descriptions-item label="\u62a4\u7406\u7b49\u7ea7">{{ detail.nursingLevel }}</el-descriptions-item>
          <el-descriptions-item label="\u5165\u4f4f\u5e8a\u4f4d">{{ detail.bedInfo }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h4>\u7533\u8bf7\u4fe1\u606f</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="\u966a\u540c\u4eba">{{ detail.escort }}</el-descriptions-item>
          <el-descriptions-item label="\u8bf7\u5047\u5468\u671f">{{ detail.startTime }} \u2014 {{ detail.expectReturnTime }}</el-descriptions-item>
          <el-descriptions-item label="\u8bf7\u5047\u5929\u6570">{{ detail.leaveDays }}\u5929</el-descriptions-item>
          <el-descriptions-item label="\u8bf7\u5047\u539f\u56e0">{{ detail.reason }}</el-descriptions-item>
          <el-descriptions-item label="\u7533\u8bf7\u4eba">{{ detail.applicant }}</el-descriptions-item>
          <el-descriptions-item label="\u7533\u8bf7\u65f6\u95f4">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="\u72b6\u6001">{{ detail.status }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detail.status === '\u5df2\u8fd4\u56de'">
          <el-divider />
          <h4>\u9500\u5047\u8bb0\u5f55</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="\u72b6\u6001">{{ detail.status }}</el-descriptions-item>
            <el-descriptions-item label="\u5b9e\u9645\u8fd4\u56de\u65f6\u95f4">{{ detail.actualReturnTime }}</el-descriptions-item>
            <el-descriptions-item label="\u5907\u6ce8">{{ detail.returnRemark }}</el-descriptions-item>
            <el-descriptions-item label="\u9500\u5047\u4eba">{{ detail.cancelUser }}</el-descriptions-item>
            <el-descriptions-item label="\u9500\u5047\u65f6\u95f4">{{ detail.cancelTime }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <div v-if="detail.status === '\u5f85\u5ba1\u6279'" class="step-panel">
          <h4>\u7533\u8bf7\u5ba1\u6279</h4>
          <el-radio-group v-model="approvalResult">
            <el-radio value="\u901a\u8fc7">\u901a\u8fc7</el-radio>
            <el-radio value="\u62d2\u7edd">\u62d2\u7edd</el-radio>
          </el-radio-group>
          <div class="form-actions" style="margin-top:16px">
            <el-button type="primary" @click="submitApprove">\u63d0\u4ea4\u5ba1\u6279</el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="timeline-box">
          <h4>\u64cd\u4f5c\u8bb0\u5f55</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(item, idx) in timelineItems"
              :key="idx"
              :timestamp="item.time"
              :type="item.type"
            >{{ item.text }}</el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-row>

    <div class="form-actions">
      <el-button type="primary" @click="$router.back()">\u8fd4\u56de</el-button>
    </div>
  </PageCard>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const route = useRoute()
const detail = ref({})
const approvalResult = ref('\u901a\u8fc7')

const stepsActive = computed(() => {
  const s = detail.value.status
  if (s === '\u5f85\u5ba1\u6279') return 1
  if (s === '\u8bf7\u5047\u4e2d' || s === '\u8d85\u65f6\u672a\u5f52') return 2
  if (s === '\u5df2\u8fd4\u56de' || s === '\u5df2\u62d2\u7edd' || s === '\u5df2\u5173\u95ed') return 3
  return 1
})

const timelineItems = computed(() => {
  const d = detail.value || {}
  const items = []
  if (d.applyTime) {
    items.push({
      time: d.applyTime,
      type: 'success',
      text: `\u53d1\u8d77\u7533\u8bf7-\u7533\u8bf7\u8bf7\u5047 \u00b7 ${d.applicant || d.creator || ''}`
    })
  }
  if (d.status && d.status !== '\u5f85\u5ba1\u6279') {
    const passed = d.status !== '\u5df2\u62d2\u7edd' && d.status !== '\u5df2\u5173\u95ed'
    items.push({
      time: d.applyTime || '',
      type: passed ? 'primary' : 'danger',
      text: passed
        ? `\u5ba1\u6279\u901a\u8fc7-\u7533\u8bf7\u5ba1\u6279 \u00b7 ${d.status}`
        : `\u5ba1\u6279\u62d2\u7edd-\u7533\u8bf7\u5ba1\u6279 \u00b7 ${d.status}`
    })
  }
  if (d.status === '\u5df2\u8fd4\u56de' && d.actualReturnTime) {
    items.push({
      time: d.actualReturnTime,
      type: 'success',
      text: `\u9500\u5047\u5b8c\u6210 \u00b7 ${d.cancelUser || ''}`
    })
  }
  return items
})

onMounted(loadDetail)

function loadDetail() {
  if (!route.query.id) return
  axios.get('/leave/detail', { params: { id: route.query.id } }).then(res => {
    if (res.data.code === 200) detail.value = res.data.data || {}
    else ElMessage.error(res.data.msg || '\u52a0\u8f7d\u8be6\u60c5\u5931\u8d25')
  }).catch(() => ElMessage.error('\u52a0\u8f7d\u8be6\u60c5\u5931\u8d25'))
}

function submitApprove() {
  if (!approvalResult.value) {
    ElMessage.warning('\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c')
    return
  }
  axios.post('/leave/approve', {
    id: detail.value.id,
    approvalResult: approvalResult.value
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('\u5ba1\u6279\u6210\u529f')
      loadDetail()
    } else {
      ElMessage.error(res.data.msg || '\u5ba1\u6279\u5931\u8d25')
    }
  }).catch(() => ElMessage.error('\u5ba1\u6279\u5931\u8d25'))
}
</script>

<style scoped>
.timeline-box { background: #fafafa; padding: 16px; border-radius: 4px; }
.form-actions { text-align: center; margin-top: 24px; }
.step-panel { margin-top: 20px; }
</style>
'''

LEAVE_MANAGE = r'''<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="\u5355\u636e\u7f16\u53f7"><el-input v-model="query.docNo" clearable /></el-form-item>
        <el-form-item label="\u8001\u4eba\u59d3\u540d"><el-input v-model="query.elderName" clearable /></el-form-item>
        <el-form-item label="\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7"><el-input v-model="query.elderIdcard" clearable /></el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">\u91cd\u7f6e</el-button>
          <el-button type="primary" @click="loadList(1)">\u641c\u7d22</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <el-radio-group v-model="query.status" @change="loadList(1)">
        <el-radio-button label="\u5168\u90e8" />
        <el-radio-button label="\u5f85\u5ba1\u6279" />
        <el-radio-button label="\u8bf7\u5047\u4e2d" />
        <el-radio-button label="\u8d85\u65f6\u672a\u5f52" />
        <el-radio-button label="\u5df2\u8fd4\u56de" />
      </el-radio-group>
      <el-button type="primary" @click="openDialog()">\u53d1\u8d77\u8bf7\u5047\u7533\u8bf7</el-button>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="\u5e8f\u53f7" width="60" />
      <el-table-column prop="docNo" label="\u5355\u636e\u7f16\u53f7" min-width="160" />
      <el-table-column prop="elderName" label="\u8001\u4eba\u59d3\u540d" width="100" />
      <el-table-column prop="elderIdcard" label="\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7" min-width="170" />
      <el-table-column prop="startTime" label="\u8bf7\u5047\u5f00\u59cb\u65f6\u95f4" min-width="160" />
      <el-table-column prop="expectReturnTime" label="\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4" min-width="160" />
      <el-table-column prop="actualReturnTime" label="\u5b9e\u9645\u8fd4\u56de\u65f6\u95f4" min-width="160">
        <template #default="{ row }">{{ row.actualReturnTime || '\u2014' }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="\u521b\u5efa\u4eba" width="90" />
      <el-table-column prop="createTime" label="\u521b\u5efa\u65f6\u95f4" min-width="160" />
      <el-table-column prop="status" label="\u8bf7\u5047\u72b6\u6001" width="100">
        <template #default="{ row }">
          <el-tag :type="leaveTag(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="\u64cd\u4f5c" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === '\u5f85\u5ba1\u6279'" link type="warning" @click="approve(row)">\u5ba1\u6279</el-button>
          <el-button v-if="canReturn(row.status)" link type="primary" @click="returnBack(row)">\u8fd4\u56de</el-button>
          <el-button link type="primary" @click="view(row)">\u67e5\u770b</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span>\u5171 {{ total }} \u6761\u6570\u636e</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>

    <el-dialog v-model="dialogVisible" title="\u53d1\u8d77\u8bf7\u5047\u7533\u8bf7" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="\u8001\u4eba\u59d3\u540d"><el-input v-model="form.elderName" /></el-form-item>
        <el-form-item label="\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7"><el-input v-model="form.elderIdcard" /></el-form-item>
        <el-form-item label="\u8bf7\u5047\u5f00\u59cb\u65f6\u95f4">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4">
          <el-date-picker v-model="form.expectReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="\u8bf7\u5047\u539f\u56e0"><el-input v-model="form.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">\u53d6\u6d88</el-button>
        <el-button type="primary" @click="save">\u63d0\u4ea4</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', elderName: '', elderIdcard: '', status: '\u5168\u90e8' })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ elderName: '', elderIdcard: '', startTime: '', expectReturnTime: '', reason: '', applicant: '', creator: '' })

onMounted(() => loadList(1))

function leaveTag(s) {
  if (s === '\u5df2\u8fd4\u56de') return 'success'
  if (s === '\u8bf7\u5047\u4e2d') return 'warning'
  if (s === '\u5f85\u5ba1\u6279') return 'info'
  if (s === '\u5df2\u62d2\u7edd' || s === '\u5df2\u5173\u95ed') return 'danger'
  return 'danger'
}

function canReturn(s) {
  return s === '\u8bf7\u5047\u4e2d' || s === '\u8d85\u65f6\u672a\u5f52'
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/leave/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  query.docNo = ''
  query.elderName = ''
  query.elderIdcard = ''
  loadList(1)
}

function openDialog() {
  Object.assign(form, { elderName: '', elderIdcard: '', startTime: '', expectReturnTime: '', reason: '', applicant: '', creator: '' })
  dialogVisible.value = true
}

function save() {
  axios.post('/leave/save', form).then(() => {
    ElMessage.success('\u63d0\u4ea4\u6210\u529f')
    dialogVisible.value = false
    loadList(1)
  }).catch(() => ElMessage.error('\u63d0\u4ea4\u5931\u8d25'))
}

function returnBack(row) {
  axios.post('/leave/return', { id: row.id, returnRemark: '\u8001\u4eba\u5df2\u8fd4\u56de' }).then(() => {
    ElMessage.success('\u9500\u5047\u6210\u529f')
    loadList(query.pageNum)
  })
}

function view(row) {
  router.push({ path: '/LeaveDetail', query: { id: row.id } })
}

function approve(row) {
  router.push({ path: '/LeaveDetail', query: { id: row.id } })
}
</script>
'''

MY_APPLY = r'''<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="\u5355\u636e\u7f16\u53f7"><el-input v-model="query.docNo" clearable placeholder="\u8bf7\u8f93\u5165" /></el-form-item>
        <el-form-item label="\u5355\u636e\u7c7b\u522b">
          <el-select v-model="query.type" clearable placeholder="\u8bf7\u9009\u62e9" style="width:120px">
            <el-option label="\u5165\u4f4f" value="\u5165\u4f4f" /><el-option label="\u9000\u4f4f" value="\u9000\u4f4f" /><el-option label="\u8bf7\u5047" value="\u8bf7\u5047" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">\u91cd\u7f6e</el-button>
          <el-button type="primary" @click="loadList(1)">\u641c\u7d22</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #toolbar>
      <el-radio-group v-model="query.status" @change="loadList(1)">
        <el-radio-button label="\u5168\u90e8" />
        <el-radio-button label="\u7533\u8bf7\u4e2d" />
        <el-radio-button label="\u5df2\u5b8c\u6210" />
        <el-radio-button label="\u5df2\u5173\u95ed" />
      </el-radio-group>
      <el-button type="primary" @click="showApplyMenu = true">\u53d1\u8d77\u7533\u8bf7</el-button>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="\u5e8f\u53f7" width="60" />
      <el-table-column prop="docNo" label="\u5355\u636e\u5355\u53f7" min-width="160" />
      <el-table-column prop="title" label="\u5355\u636e\u6807\u9898" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="\u5355\u636e\u7c7b\u522b" width="80" />
      <el-table-column prop="applicant" label="\u7533\u8bf7\u4eba" width="90" />
      <el-table-column prop="applyTime" label="\u7533\u8bf7\u65f6\u95f4" min-width="160" />
      <el-table-column prop="finishTime" label="\u5b8c\u6210\u65f6\u95f4" min-width="160" />
      <el-table-column prop="flowStatus" label="\u6d41\u7a0b\u72b6\u6001" width="100">
        <template #default="{ row }">
          <el-tag :type="flowTag(row.flowStatus)" size="small">{{ row.flowStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="\u64cd\u4f5c" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.flowStatus === '\u7533\u8bf7\u4e2d'" link type="danger" @click="cancel(row)">\u64a4\u9500</el-button>
          <el-button link type="primary" @click="view(row)">\u67e5\u770b</el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <span>\u5171 {{ total }} \u6761\u6570\u636e</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>

    <el-dialog v-model="showApplyMenu" title="\u53d1\u8d77\u7533\u8bf7" width="360px">
      <el-button type="primary" plain style="width:100%;margin-bottom:8px" @click="goApply('/CheckinApply')">\u5165\u4f4f\u7533\u8bf7</el-button>
      <el-button type="primary" plain style="width:100%;margin-bottom:8px" @click="goApply('/CheckoutApply')">\u9000\u4f4f\u7533\u8bf7</el-button>
      <el-button type="primary" plain style="width:100%" @click="goApply('/LeaveManage')">\u8bf7\u5047\u7533\u8bf7</el-button>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const showApplyMenu = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', type: '', status: '\u5168\u90e8' })
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function flowTag(s) {
  return s === '\u5df2\u5b8c\u6210' ? 'success' : s === '\u5df2\u5173\u95ed' ? 'danger' : 'warning'
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/collab/apply/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  query.docNo = ''
  query.type = ''
  loadList(1)
}

function view(row) {
  const path = row.bizType === 'checkout' ? '/CheckoutDetail' : row.bizType === 'leave' ? '/LeaveDetail' : '/CheckinDetail'
  const step = row.step || 1
  let mode = 'form'
  if (row.flowStatus === '\u7533\u8bf7\u4e2d') {
    if (row.bizType === 'checkin' && step === 3) mode = 'pending'
    if (row.bizType === 'checkout' && [2, 5, 6].includes(step)) mode = 'pending'
  }
  router.push({ path, query: { id: row.id, step, mode } })
}

function cancel(row) {
  const url = row.bizType === 'checkin' ? '/checkin/cancel'
    : row.bizType === 'checkout' ? '/checkout/cancel'
    : row.bizType === 'leave' ? '/leave/cancel' : null
  if (!url) { ElMessage.info('\u8be5\u7c7b\u578b\u6682\u4e0d\u652f\u6301\u64a4\u9500'); return }
  axios.get(url, { params: { id: row.id } }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('\u5df2\u64a4\u9500')
      loadList(1)
    } else {
      ElMessage.error(res.data.msg || '\u64a4\u9500\u5931\u8d25')
    }
  }).catch(() => ElMessage.error('\u64a4\u9500\u5931\u8d25'))
}

function goApply(path) {
  showApplyMenu.value = false
  router.push(path)
}
</script>
'''


def main():
    write("views/MainIndex.vue", MAIN_INDEX)
    write("views/UesrInfo.vue", UESR_INFO)
    write("components/checkout/InfoSections.vue", INFO_SECTIONS)
    write("views/CheckoutDetail.vue", CHECKOUT_DETAIL)
    write("views/LeaveDetail.vue", LEAVE_DETAIL)
    write("views/LeaveManage.vue", LEAVE_MANAGE)
    write("views/MyApply.vue", MY_APPLY)
    print("all done")


if __name__ == "__main__":
    main()
