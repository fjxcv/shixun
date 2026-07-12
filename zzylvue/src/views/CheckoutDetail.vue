<template>
  <PageCard>
    <CheckoutSteps :active="Math.max(0, currentStep - 1)" />

    <div v-if="finished" class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" show-terminate show-bill show-settlement />
        <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
      </div>
      <OperationTimeline class="detail-side" />
    </div>

    <div v-else-if="isPendingApprove" class="status-panel">
      <el-result icon="info" title="审批中" sub-title="稍等，退住申请正在审批中，若该申请单长时间未处理，请联系审批角色！" />
      <OperationTimeline />
      <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
    </div>

    <div v-else-if="currentStep === 2 || currentStep === 5 || currentStep === 6" class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" :show-terminate="currentStep >= 3" :show-bill="currentStep >= 4" />
        <div class="form-actions" style="justify-content: flex-end">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submitApproval">提交</el-button>
        </div>
      </div>
      <div class="detail-side approve-side">
        <h4>审批结果</h4>
        <el-radio-group v-model="approveResult">
          <el-radio value="通过">通过</el-radio>
          <el-radio value="拒绝">拒绝</el-radio>
          <el-radio value="退回">退回</el-radio>
        </el-radio-group>
        <el-input v-model="approveComment" type="textarea" placeholder="审批意见" maxlength="200" show-word-limit :rows="4" style="margin-top: 12px" />
        <OperationTimeline style="margin-top: 20px" />
      </div>
    </div>

    <div v-else-if="currentStep === 3" class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" />
        <h4>上传解除协议</h4>
        <el-form label-width="120px">
          <el-form-item label="合同名称">{{ detail.contractName }}</el-form-item>
          <el-form-item label="解除日期" required>
            <el-date-picker v-model="terminateDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="解除协议" required>
            <el-upload action="#" :http-request="uploadTerminate" :show-file-list="false">
              <el-button type="primary" plain>上传文件</el-button>
            </el-upload>
            <span class="upload-tip">{{ terminateFile || '请上传pdf文件，60MB以内' }}</span>
          </el-form-item>
        </el-form>
        <div class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submitAuto(4)">保存</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" />
    </div>

    <div v-else-if="currentStep === 4" class="detail-layout">
      <div class="detail-main bill-panel">
        <InfoSections :data="detail" show-terminate />
        <h4>调整费用账单</h4>
        <el-alert type="info" :closable="false" title="账单明细。提交账单审批前，请处理完入账列表中的未缴账单，否则无法提交。" />
        <el-card class="bill-card" header="应退 · 2项 · 小计：3820.00元">
          <div class="bill-item">月度账单 2048-10 应退3800.00元</div>
          <div class="bill-item">费用账单 口腔清洁 20.00元</div>
        </el-card>
        <el-card class="bill-card" header="欠费 · 3项 · 小计：6000.00元">
          <div class="bill-item">月度账单 2048-10 欠费2000.00元</div>
        </el-card>
        <el-card class="bill-card" header="余额 · 小计：4000.00元">
          <div class="bill-item">可退押金 2000.00元 · 预付款 2000.00元</div>
        </el-card>
        <p class="bill-summary">最终退款金额 = 应退 - 欠费 + 余额 = {{ refundAmount }}元</p>
        <div class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submitAuto(5)">保存</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" />
    </div>

    <div v-else-if="currentStep === 7" class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" show-terminate show-bill show-settlement />
        <div class="form-actions" style="justify-content: flex-end">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submitFinish">提交</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" />
    </div>

    <div v-else class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" />
        <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
      </div>
      <OperationTimeline class="detail-side" />
    </div>
  </PageCard>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'
import CheckoutSteps from '@/components/CheckoutSteps.vue'
import InfoSections from '@/components/checkout/InfoSections.vue'
import OperationTimeline from '@/components/checkout/OperationTimeline.vue'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const currentStep = ref(1)
const mode = ref('form')
const terminateDate = ref('')
const terminateFile = ref('')
const approveResult = ref('通过')
const approveComment = ref('')
const refundAmount = ref(20)

const finished = computed(() => detail.value.flowStatus === '已完成' || detail.value.stepStatus === '已完成' || detail.value.flowStatus === '已关闭')
const isPendingApprove = computed(() => mode.value === 'pending' && (currentStep.value === 2 || currentStep.value === 5 || currentStep.value === 6))

onMounted(() => {
  mode.value = route.query.mode || 'form'
  loadDetail()
})

function loadDetail() {
  const id = route.query.id
  if (!id) return
  axios.get('/checkout/detail', { params: { id } }).then(res => {
    if (res.data.code === 200) {
      detail.value = res.data.data || {}
      // 强制后端当前步骤，禁止跳步
      currentStep.value = Number(detail.value.step) || 1
      terminateDate.value = detail.value.terminateDate || ''
      terminateFile.value = detail.value.terminateFile || ''
      if (detail.value.refundAmount != null) refundAmount.value = detail.value.refundAmount
    } else {
      ElMessage.error(res.data.msg || '加载详情失败')
    }
  }).catch(() => ElMessage.error('加载详情失败'))
}

function uploadTerminate(option) {
  const formData = new FormData()
  formData.append('mf', option.file)
  axios.post('/upload', formData).then(res => {
    if (res.data.code === 200) {
      terminateFile.value = res.data.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.data.msg || '上传失败')
    }
  }).catch(() => ElMessage.error('上传失败'))
}

function submitApproval() {
  if (!approveResult.value) {
    ElMessage.warning('请选择审批结果')
    return
  }
  axios.post('/checkout/updateStep', {
    id: detail.value.id,
    step: currentStep.value,
    approvalResult: approveResult.value,
    approvalComment: approveComment.value
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('提交成功')
      if (approveResult.value === '拒绝') goBack()
      else {
        currentStep.value = Number(res.data.data) || currentStep.value
        loadDetail()
      }
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  }).catch(() => ElMessage.error('提交失败'))
}

function submitAuto(nextStep) {
  if (currentStep.value === 3) {
    if (!terminateDate.value || !terminateFile.value) {
      ElMessage.warning('请完善解除协议信息')
      return
    }
  }
  axios.post('/checkout/updateStep', {
    id: detail.value.id,
    step: nextStep,
    terminateDate: terminateDate.value,
    terminateFile: terminateFile.value,
    refundAmount: refundAmount.value
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      currentStep.value = Number(res.data.data) || nextStep
      loadDetail()
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  }).catch(() => ElMessage.error('保存失败'))
}

function submitFinish() {
  axios.post('/checkout/updateStep', {
    id: detail.value.id,
    step: 7,
    refundAmount: refundAmount.value
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('提交成功')
      goBack()
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  }).catch(() => ElMessage.error('提交失败'))
}

function goBack() {
  router.push('/CheckoutProcess')
}
</script>

<style scoped>
.detail-layout { display: flex; gap: 20px; align-items: flex-start; }
.detail-main { flex: 1; min-width: 0; }
.detail-side { width: 280px; flex-shrink: 0; background: #fafafa; padding: 16px; border-radius: 4px; }
.status-panel { text-align: center; padding: 40px 0; }
.form-actions { text-align: center; margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
.bill-card { margin-top: 12px; }
.bill-item { padding: 8px 0; color: #666; border-bottom: 1px dashed #eee; }
.bill-summary { margin-top: 16px; font-weight: 600; }
.upload-tip { margin-left: 12px; color: #999; font-size: 12px; }
.approve-side h4 { margin: 0 0 12px; }
</style>
