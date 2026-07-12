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
          <el-form-item label="护理员"><el-input v-model="form.caregivers" readonly /></el-form-item>
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
            <el-input v-model="form.remark" type="textarea" maxlength="100" show-word-limit :rows="3" />
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
import { computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'
import CheckoutSteps from '@/components/CheckoutSteps.vue'

const router = useRouter()
const elderOptions = ['高启强', '安欣', '陈书婷']
const elderMap = {
  '高启强': { elderIdcard: '230203197702221029', elderPhone: '13898988888', nursingLevel: '特级护理等级', bedInfo: '101床位', contractName: '高启强长住合同.pdf', consultant: '顾廷烨', caregivers: '盛长柏、盛明兰', feeStart: '2048-10-10', feeEnd: '2049-10-10' },
  '安欣': { elderIdcard: '230203199701221029', elderPhone: '13898987777', nursingLevel: '重度失能等级', bedInfo: '102床位', contractName: '安欣试住合同.pdf', consultant: '顾廷烨', caregivers: '盛明兰', feeStart: '2048-10-10', feeEnd: '2049-10-10' },
  '陈书婷': { elderIdcard: '230203197811221029', elderPhone: '13678987777', nursingLevel: '中度失能等级', bedInfo: '103床位', contractName: '陈书婷合同.pdf', consultant: '顾廷烨', caregivers: '盛如兰', feeStart: '2048-10-10', feeEnd: '2049-10-10' }
}

const form = reactive({
  elderName: '', elderIdcard: '', elderPhone: '', feeStart: '', feeEnd: '',
  nursingLevel: '', bedInfo: '', contractName: '', consultant: '', caregivers: '',
  checkoutDate: '', reason: '', remark: '', applicant: '顾廷烨', creator: '顾廷烨'
})

const feePeriod = computed(() => form.feeStart && form.feeEnd ? `${form.feeStart} —— ${form.feeEnd}` : '')

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
      ElMessage.success('提交成功，已进入申请审批')
      router.push({ path: '/CheckoutDetail', query: { id: res.data.data, step: 2, mode: 'form' } })
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
