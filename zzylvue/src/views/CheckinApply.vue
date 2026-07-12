<template>
  <PageCard>
    <CheckinSteps :active="0" />
    <h3 class="section-title">补足申请资料</h3>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="form" label-width="120px" class="apply-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="老人姓名" required><el-input v-model="form.elderName" maxlength="10" show-word-limit placeholder="请输入" /></el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="出生日期"><el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
              <el-form-item label="联系方式" required><el-input v-model="form.elderPhone" maxlength="11" show-word-limit placeholder="请输入" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="老人身份证号" required><el-input v-model="form.elderIdcard" maxlength="18" show-word-limit placeholder="请输入" @blur="fillFromIdcard" /></el-form-item>
              <el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="150" style="width:100%" /></el-form-item>
              <el-form-item label="家庭住址" required><el-input v-model="form.address" type="textarea" maxlength="100" show-word-limit :rows="3" placeholder="请输入" /></el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="家属信息" name="family" :disabled="!basicDone">
        <el-table :data="familyList" border>
          <el-table-column label="家属姓名" min-width="140">
            <template #default="{ row }"><el-input v-model="row.name" maxlength="10" show-word-limit placeholder="请输入" /></template>
          </el-table-column>
          <el-table-column label="家属联系方式" min-width="160">
            <template #default="{ row }"><el-input v-model="row.phone" maxlength="11" show-word-limit placeholder="请输入" /></template>
          </el-table-column>
          <el-table-column label="与老人关系" min-width="140">
            <template #default="{ row }">
              <el-select v-model="row.relation" placeholder="请选择" style="width:100%">
                <el-option label="子女" value="子女" /><el-option label="配偶" value="配偶" />
                <el-option label="亲属" value="亲属" /><el-option label="朋友" value="朋友" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ $index }">
              <el-button link type="primary" @click="addFamily"><el-icon><Plus /></el-icon></el-button>
              <el-button v-if="familyList.length > 1" link type="danger" @click="removeFamily($index)"><el-icon><Minus /></el-icon></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="资料上传" name="upload" :disabled="!familyDone">
        <el-alert type="info" :closable="false" title="图片大小不超过2M，仅支持上传PNG JPG JPEG类型图片" style="margin-bottom:16px" />
        <el-form label-width="140px">
          <el-form-item label="一寸照片" required>
            <el-upload action="#" :http-request="(o) => uploadOne(o, 'photo')" :show-file-list="false" :before-upload="beforeImg">
              <el-button type="primary" plain>{{ uploads.photo ? '重新上传' : '点击上传图片' }}</el-button>
            </el-upload>
            <span v-if="uploads.photo" class="file-ok">已上传</span>
          </el-form-item>
          <el-form-item label="身份证人像面" required>
            <el-upload action="#" :http-request="(o) => uploadOne(o, 'idFront')" :show-file-list="false" :before-upload="beforeImg">
              <el-button type="primary" plain>{{ uploads.idFront ? '重新上传' : '点击上传图片' }}</el-button>
            </el-upload>
            <span v-if="uploads.idFront" class="file-ok">已上传</span>
          </el-form-item>
          <el-form-item label="身份证国徽面" required>
            <el-upload action="#" :http-request="(o) => uploadOne(o, 'idBack')" :show-file-list="false" :before-upload="beforeImg">
              <el-button type="primary" plain>{{ uploads.idBack ? '重新上传' : '点击上传图片' }}</el-button>
            </el-upload>
            <span v-if="uploads.idBack" class="file-ok">已上传</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    <div class="form-actions">
      <el-button @click="$router.back()">返回</el-button>
      <el-button v-if="activeTab !== 'upload'" type="primary" @click="saveTab">保存</el-button>
      <el-button v-else type="primary" @click="submit">提交</el-button>
    </div>
  </PageCard>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import PageCard from '@/components/PageCard.vue'
import CheckinSteps from '@/components/CheckinSteps.vue'

const router = useRouter()
const activeTab = ref('basic')
const basicDone = ref(false)
const familyDone = ref(false)
const familyList = ref([{ name: '', phone: '', relation: '' }])
const uploads = reactive({ photo: '', idFront: '', idBack: '' })
const form = reactive({
  elderName: '',
  elderIdcard: '',
  elderPhone: '',
  gender: '',
  birthDate: '',
  age: null,
  address: '',
  applicant: '',
  creator: '',
  checkinDate: ''
})

function addFamily() { familyList.value.push({ name: '', phone: '', relation: '' }) }
function removeFamily(i) { familyList.value.splice(i, 1) }

function fillFromIdcard() {
  const id = form.elderIdcard || ''
  if (!/^\d{17}[\dXx]$/.test(id)) return
  const birth = id.substring(6, 14)
  form.birthDate = `${birth.slice(0, 4)}-${birth.slice(4, 6)}-${birth.slice(6, 8)}`
  form.gender = Number(id.charAt(16)) % 2 === 1 ? '男' : '女'
  form.age = new Date().getFullYear() - Number(birth.slice(0, 4))
}

function beforeImg(file) {
  const ok = ['image/png', 'image/jpeg', 'image/jpg'].includes(file.type) && file.size / 1024 / 1024 < 2
  if (!ok) ElMessage.error('仅支持2M以内的PNG/JPG/JPEG')
  return ok
}

function uploadOne(option, key) {
  const fd = new FormData()
  fd.append('mf', option.file)
  axios.post('/upload', fd).then(res => {
    if (res.data.code === 200) {
      uploads[key] = res.data.data
      ElMessage.success('上传成功')
    } else ElMessage.error(res.data.msg || '上传失败')
  }).catch(() => ElMessage.error('上传失败'))
}

function saveTab() {
  if (activeTab.value === 'basic') {
    if (!form.elderName || !form.elderIdcard || !form.elderPhone || !form.address) {
      ElMessage.warning('请完善基本信息必填项')
      return
    }
    basicDone.value = true
    activeTab.value = 'family'
    ElMessage.success('保存成功')
  } else if (activeTab.value === 'family') {
    if (!familyList.value.some(f => f.name)) {
      ElMessage.warning('请至少填写一位家属')
      return
    }
    familyDone.value = true
    activeTab.value = 'upload'
    ElMessage.success('保存成功')
  }
}

function buildSubmitPayload() {
  const payload = {
    ...form,
    familyJson: JSON.stringify(familyList.value.filter(f => f.name)),
    extraJson: JSON.stringify({ uploads: { ...uploads }, assess: {} })
  }
  ;['birthDate', 'checkinDate', 'gender', 'applicant', 'creator'].forEach(key => {
    if (!payload[key]) delete payload[key]
  })
  if (payload.age == null) delete payload.age
  return payload
}

function submit() {
  if (!basicDone.value || !familyDone.value) {
    ElMessage.warning('请先完成前面的资料填写')
    return
  }
  if (!uploads.photo || !uploads.idFront || !uploads.idBack) {
    ElMessage.warning('请上传完整证件资料')
    return
  }
  axios.post('/checkin/save', buildSubmitPayload()).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('提交成功，请在入住办理中继续处理')
      router.push('/CheckinProcess')
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  }).catch(err => {
    const msg = err.response?.data?.msg || err.response?.data?.error || err.message
    ElMessage.error(msg ? `提交失败：${msg}` : '提交失败，请确认后端已启动')
  })
}
</script>

<style scoped>
.section-title { margin: 0 0 16px; font-size: 16px; }
.form-actions { margin-top: 24px; text-align: center; }
.file-ok { margin-left: 8px; color: #67c23a; font-size: 13px; }
</style>
