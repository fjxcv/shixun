<template>
  <PageCard>
    <CheckoutSteps :active="0" />
    <h3 class="section-title">填写申请信息</h3>
    <el-divider />
    <h4>基本信息</h4>
    <el-form :model="form" label-width="120px" class="apply-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="老人姓名" required>
            <el-select v-model="form.elderName" placeholder="请选择" style="width: 100%" @change="onElderChange">
              <el-option v-for="n in elderOptions" :key="n" :label="n" :value="n" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系方式"><el-input v-model="form.elderPhone" readonly /></el-form-item>
          <el-form-item label="护理等级"><el-input v-model="form.nursingLevel" readonly /></el-form-item>
          <el-form-item label="签约合同"><el-input v-model="form.contractName" readonly /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="老人身份证号"><el-input v-model="form.elderIdcard" readonly /></el-form-item>
          <el-form-item label="费用期限"><el-input :value="feePeriod" readonly /></el-form-item>
          <el-form-item label="入住床位"><el-input v-model="form.bedInfo" readonly /></el-form-item>
          <el-form-item label="养老顾问"><el-input v-model="form.consultant" readonly /></el-form-item>
        </el-col>
      </el-row>
      <el-divider />
      <h4>申请信息</h4>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="退住日期" required>
            <el-date-picker v-model="form.checkoutDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="退住原因" required>
            <el-select v-model="form.reason" placeholder="请选择" style="width: 100%">
              <el-option label="服务不周" value="服务不周" />
              <el-option label="身体原因" value="身体原因" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" maxlength="100" show-word-limit :rows="3" placeholder="选填" />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-actions">
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </div>
    </el-form>
  </PageCard>
</template>

<script setup>
/**
 * 退住申请页：填写必填信息后 POST /checkout/save，后端补全申请人并进入 step=2。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'
import CheckoutSteps from '@/components/CheckoutSteps.vue'

const router = useRouter()
const elderOptions = ref([])
const elderMap = reactive({})

const form = reactive({
  elderName: '',
  elderIdcard: '',
  elderPhone: '',
  feeStart: '',
  feeEnd: '',
  nursingLevel: '',
  bedInfo: '',
  contractName: '',
  consultant: '',
  checkoutDate: '',
  reason: '',
  remark: '',
  applicant: '',
  creator: ''
})

const feePeriod = computed(() => form.feeStart && form.feeEnd ? `${form.feeStart} ~ ${form.feeEnd}` : '')

onMounted(loadElders)

function loadElders() {
  axios.post('/checkin/page', { pageNum: 1, pageSize: 100 }).then(res => {
    if (res.data.code === 200) {
      const list = (res.data.data || []).filter(c => c.flowStatus === '已完成')
      elderOptions.value = [...new Set(list.map(c => c.elderName).filter(Boolean))]
      list.forEach(c => {
        elderMap[c.elderName] = {
          elderIdcard: c.elderIdcard || '',
          elderPhone: c.elderPhone || '',
          nursingLevel: c.nursingLevel || '',
          bedInfo: c.bedNo || '',
          contractName: c.contractName || '',
          consultant: c.applicant || c.creator || '',
          feeStart: c.periodStart || '',
          feeEnd: c.periodEnd || ''
        }
      })
      if (!elderOptions.value.length) {
        ElMessage.warning('暂无已完成入住的老人，请先完成入住流程')
      }
    }
  }).catch(() => ElMessage.error('加载老人列表失败'))
}

function onElderChange(name) {
  const d = elderMap[name]
  if (d) Object.assign(form, d)
}

function submit() {
  if (!form.elderName || !form.checkoutDate || !form.reason) {
    ElMessage.warning('请完善必填信息')
    return
  }
  axios.post('/checkout/save', form).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('提交成功，请在退住办理中继续处理')
      router.push('/CheckoutProcess')
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  }).catch(() => ElMessage.error('提交失败'))
}
</script>

<style scoped>
.section-title { margin: 0 0 8px; }
.apply-form { max-width: 1000px; }
.form-actions { text-align: center; margin-top: 24px; }
</style>
