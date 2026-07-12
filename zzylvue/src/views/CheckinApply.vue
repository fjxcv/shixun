<template>
  <PageCard>
    <CheckinSteps :active="0" />
    <h3 class="section-title">补全申请资料</h3>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="form" label-width="120px" class="apply-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="老人姓名" required><el-input v-model="form.elderName" maxlength="10" show-word-limit /></el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio value="男">男</el-radio>
                  <el-radio value="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="出生日期"><el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled="idcardExtracted" /></el-form-item>
              <el-form-item label="联系方式" required><el-input v-model="form.elderPhone" maxlength="11" show-word-limit /></el-form-item>
              <el-form-item label="民族">
                <el-select v-model="form.ethnicity" placeholder="请选择民族" clearable filterable style="width:100%">
                  <el-option v-for="e in ethnicityOptions" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
              <el-form-item label="政治面貌">
                <el-select v-model="form.politicalStatus" placeholder="请选择政治面貌" clearable style="width:100%">
                  <el-option v-for="p in politicalStatusOptions" :key="p" :label="p" :value="p" />
                </el-select>
              </el-form-item>
              <el-form-item label="宗教信仰">
                <el-select v-model="form.religion" placeholder="请选择宗教信仰" clearable style="width:100%">
                  <el-option v-for="r in religionOptions" :key="r" :label="r" :value="r" />
                </el-select>
              </el-form-item>
              <el-form-item label="特长爱好"><el-input v-model="form.hobbies" maxlength="20" show-word-limit placeholder="选填，20字以内" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="老人身份证号" required><el-input v-model="form.elderIdcard" maxlength="18" show-word-limit @input="onIdcardInput" /></el-form-item>
              <el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="150" style="width:100%" :disabled="idcardExtracted" /></el-form-item>
              <el-form-item label="家庭住址" required><el-input v-model="form.address" type="textarea" maxlength="100" show-word-limit :rows="3" /></el-form-item>
              <el-form-item label="婚姻状况">
                <el-select v-model="form.maritalStatus" placeholder="请选择婚姻状况" clearable style="width:100%">
                  <el-option v-for="m in maritalStatusOptions" :key="m" :label="m" :value="m" />
                </el-select>
              </el-form-item>
              <el-form-item label="文化程度">
                <el-select v-model="form.educationLevel" placeholder="请选择文化程度" clearable style="width:100%">
                  <el-option v-for="e in educationLevelOptions" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
              <el-form-item label="经济来源">
                <el-select v-model="form.incomeSource" placeholder="请选择经济来源" clearable style="width:100%">
                  <el-option v-for="i in incomeSourceOptions" :key="i" :label="i" :value="i" />
                </el-select>
              </el-form-item>
              <el-form-item label="医疗保障">
                <el-select v-model="form.medicalInsurance" placeholder="请选择医疗保障" clearable style="width:100%">
                  <el-option v-for="m in medicalInsuranceOptions" :key="m" :label="m" :value="m" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="医保卡号"><el-input v-model="form.medicalInsuranceNo" maxlength="19" show-word-limit placeholder="选填，19位数字" /></el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="家属信息" name="family" :disabled="!basicDone">
        <el-table :data="familyList" border>
          <el-table-column label="家属姓名" min-width="140">
            <template #default="{ row }"><el-input v-model="row.name" maxlength="10" show-word-limit placeholder="请输入" @input="(v) => row.name = stripEmoji(v)" /></template>
          </el-table-column>
          <el-table-column label="家属联系方式" min-width="160">
            <template #default="{ row }"><el-input v-model="row.phone" maxlength="11" show-word-limit placeholder="请输入" /></template>
          </el-table-column>
          <el-table-column label="与老人关系" min-width="140">
            <template #default="{ row }">
              <el-select v-model="row.relation" placeholder="请选择" style="width:100%">
                <el-option label="子女" value="子女" /><el-option label="配偶" value="配偶" />
                <el-option label="亲属" value="亲属" /><el-option label="朋友" value="朋友" />
                <el-option label="社工" value="社工" /><el-option label="其他" value="其他" />
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
import { computed, reactive, ref } from 'vue'
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
const idcardExtracted = ref(false)
const familyList = ref([
  { name: '', phone: '', relation: '' }
])
const uploads = reactive({ photo: '', idFront: '', idBack: '' })
const form = reactive({
  elderName: '高启强', elderIdcard: '230203197702221029', elderPhone: '13898988888',
  gender: '男', birthDate: '1977-02-22', age: 71, address: '24号楼3单元401室',
  ethnicity: '', politicalStatus: '', religion: '', maritalStatus: '',
  educationLevel: '', incomeSource: '', medicalInsurance: '',
  hobbies: '', medicalInsuranceNo: '',
  applicant: '顾廷烨', creator: '顾廷烨', checkinDate: '2048-10-10'
})

const ethnicityOptions = [
  '汉族','蒙古族','回族','藏族','维吾尔族','苗族','彝族','壮族','布依族',
  '朝鲜族','满族','侗族','瑶族','白族','土家族','哈尼族','哈萨克族','傣族','黎族',
  '傈僳族','佤族','高山族','拉祜族','水族','东乡族','纳西族','景颇族','柯尔克孜族',
  '土族','达斡尔族','么佬族','羌族','布朗族','撒拉族','毛南族','仡佬族','锡伯族',
  '阿昌族','普米族','塔吉克族','怒族','乌孜别克族','俄罗斯族','鄂温克族','德昂族',
  '保安族','裕固族','京族','塔塔尔族','独龙族','鄂伦春族','赫哲族','门巴族','珞巴族','基诺族'
]
const politicalStatusOptions = ['群众','中共党员','中共预备党员','其他民主党派','无党派人士']
const religionOptions = ['佛教','道教','基督教','天主教','伊斯兰教','其他']
const maritalStatusOptions = ['已婚','丧偶','未婚','离婚','再婚']
const educationLevelOptions = ['文盲','小学','初中','高中','中专','大专','本科','硕士','博士']
const incomeSourceOptions = ['退休金','子女补贴','亲友资助','政府补助','其他']
const medicalInsuranceOptions = ['城镇职工基本医疗保险','城乡居民基本医疗保险']

function stripEmoji(val) {
  return (val || '').replace(/[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{FE00}-\u{FE0F}\u{1F900}-\u{1F9FF}\u{1FA00}-\u{1FA6F}\u{1FA70}-\u{1FAFF}\u{200D}\u{20E3}\u{FE0F}]/gu, '')
}
function addFamily() { familyList.value.push({ name: '', phone: '', relation: '' }) }
function removeFamily(i) { familyList.value.splice(i, 1) }

function onIdcardInput(val) {
  form.elderIdcard = (val || '').replace(/[^\dXx]/g, '')
  if (form.elderIdcard.length === 18) {
    fillFromIdcard()
  } else {
    idcardExtracted.value = false
  }
}

function fillFromIdcard() {
  const id = form.elderIdcard || ''
  if (!/^\d{17}[\dXx]$/.test(id)) return
  const birth = id.substring(6, 14)
  form.birthDate = `${birth.slice(0, 4)}-${birth.slice(4, 6)}-${birth.slice(6, 8)}`
  form.gender = Number(id.charAt(16)) % 2 === 1 ? '男' : '女'
  form.age = new Date().getFullYear() - Number(birth.slice(0, 4))
  idcardExtracted.value = true
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
    if (!familyList.value.some(f => f.name && f.phone && f.relation)) {
      ElMessage.warning('家属信息不完整，请输入家属信息')
      return
    }
    const phoneReg = /^1[3-9]\d{9}$/
    for (const f of familyList.value) {
      if (f.name && !phoneReg.test(f.phone)) {
        ElMessage.warning('手机号格式错误，请重新输入')
        return
      }
    }
    familyDone.value = true
    activeTab.value = 'upload'
    ElMessage.success('保存成功')
  }
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
  const payload = {
    ...form,
    familyJson: JSON.stringify(familyList.value.filter(f => f.name && f.phone && f.relation)),
    extraJson: JSON.stringify({ uploads: { ...uploads }, assess: {} })
  }
  axios.post('/checkin/save', payload).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('提交成功，已进入入住评估')
      router.push({ path: '/CheckinDetail', query: { id: res.data.data, step: 2 } })
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  }).catch(() => ElMessage.error('提交失败'))
}
</script>

<style scoped>
.section-title { margin: 0 0 16px; font-size: 16px; }
.form-actions { margin-top: 24px; text-align: center; }
.file-ok { margin-left: 8px; color: #67c23a; font-size: 13px; }
</style>
