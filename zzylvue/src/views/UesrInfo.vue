<template>
  <PageCard>
    <h3 class="page-title">个人中心</h3>
    <el-form :model="userInfo" label-width="100px" class="user-form">
      <el-row :gutter="40">
        <el-col :span="12">
          <el-form-item label="姓名" required>
            <el-input v-model="userInfo.realname" maxlength="10" show-word-limit />
          </el-form-item>
          <el-form-item label="邮箱">{{ userInfo.email || '—' }}</el-form-item>
          <el-form-item label="所属部门">{{ userInfo.department || '—' }}</el-form-item>
          <el-form-item label="所属职位">{{ userInfo.job || '—' }}</el-form-item>
          <el-form-item label="角色">{{ userInfo.role || '—' }}</el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" maxlength="11" show-word-limit />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="userInfo.sex">
              <el-radio value="男">男</el-radio>
              <el-radio value="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="头像" required>
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
                <p>点击左侧圆圈上传头像</p>
                <p>图片大小不超过2M</p>
                <p>仅支持 PNG/JPG/JPEG</p>
              </div>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-actions">
        <el-button type="primary" @click="saveUserInfo">保存</el-button>
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

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
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
      job: '', role: '', phone: '', sex: '男', image: DEFAULT_AVATAR
    })
    imageUrl.value = DEFAULT_AVATAR
  })
}

function beforeAvatarUpload(file) {
  const okType = ['image/png', 'image/jpeg', 'image/jpg'].includes(file.type)
  const okSize = file.size / 1024 / 1024 < 2
  if (!okType) {
    ElMessage.error('仅支持 PNG/JPG/JPEG')
    return false
  }
  if (!okSize) {
    ElMessage.error('图片大小不能超过2M')
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
      ElMessage.success('头像上传成功')
      option.onSuccess && option.onSuccess(res.data)
      notifyAvatar(imageUrl.value)
      if (userInfo.id) {
        axios.post('/updateUser', { id: userInfo.id, image: userInfo.image }).then(() => {
          notifyAvatar(imageUrl.value)
        }).catch(() => {})
      }
    } else {
      ElMessage.error(res.data?.msg || '头像上传失败')
      option.onError && option.onError(new Error(res.data?.msg || 'upload fail'))
    }
  }).catch(err => {
    ElMessage.error('头像上传失败')
    option.onError && option.onError(err)
  })
}

function saveUserInfo() {
  if (!userInfo.id) {
    ElMessage.error('用户信息未加载，请刷新后重试')
    return
  }
  if (!userInfo.realname) {
    ElMessage.warning('请填写姓名')
    return
  }
  axios.post('/updateUser', userInfo).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg || '保存成功')
      notifyAvatar(imageUrl.value || userInfo.image)
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  }).catch(() => ElMessage.error('保存失败'))
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
