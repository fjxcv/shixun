<template>
  <PageCard>
    <CheckoutSteps :active="stepIndex" />
    <div v-if="readonlyView" class="detail-layout">
      <div class="detail-main">
        <InfoSections :data="detail" show-terminate show-bill show-settlement />
        <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
      </div>
      <OperationTimeline class="detail-side" action-prefix="退住" :data="detail" />
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
            <el-radio value="通过">通过</el-radio>
            <el-radio value="拒绝">拒绝</el-radio>
            <el-radio value="退回">退回</el-radio>
          </el-radio-group>
          <el-input v-model="approvalComment" type="textarea" maxlength="200" show-word-limit placeholder="审批意见" style="margin-top:12px" />
        </div>

        <div v-if="step === 3" class="step-panel">
          <h4>解除合同</h4>
          <el-form label-width="120px">
            <el-form-item label="解除日期" required>
              <el-date-picker v-model="detail.terminateDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
            <el-form-item label="解除协议" required>
              <el-upload action="#" :http-request="uploadTerminate" :show-file-list="false">
                <el-button type="primary" plain>上传文件</el-button>
              </el-upload>
              <span v-if="detail.terminateFile" style="margin-left:8px">{{ detail.terminateFile }}</span>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="step === 4" class="step-panel">
          <h4>调整账单</h4>
          <el-form label-width="160px">
            <el-form-item label="应退(元)" required>
              <el-input-number v-model="detail.billReceivable" :precision="2" style="width:240px" @change="recalcRefund" />
            </el-form-item>
            <el-form-item label="欠费(元)" required>
              <el-input-number v-model="detail.billArrears" :precision="2" style="width:240px" @change="recalcRefund" />
            </el-form-item>
            <el-form-item label="余额(元)" required>
              <el-input-number v-model="detail.billBalance" :precision="2" style="width:240px" @change="recalcRefund" />
            </el-form-item>
            <el-form-item label="最终退款金额(元)">
              <el-input-number v-model="detail.refundAmount" :precision="2" style="width:240px" />
            </el-form-item>
            <p class="hint">退款 = 应退 - 欠费 + 余额</p>
          </el-form>
        </div>

        <div v-if="step === 7" class="step-panel">
          <h4>费用清算</h4>
          <p>应退 {{ fmtMoney(detail.billReceivable) }}元 · 欠费 {{ fmtMoney(detail.billArrears) }}元 · 余额 {{ fmtMoney(detail.billBalance) }}元</p>
          <p>退款信息：剩余预付款 {{ fmtMoney(detail.refundAmount) }} 元</p>
          <p>退款方式：现金 · 共退款 {{ fmtMoney(detail.refundAmount) }} 元，已结清</p>
        </div>

        <div class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submitCurrent">{{ step === 7 ? '完成清算' : '保存' }}</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" action-prefix="退住" :data="detail" />
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
const approvalResult = ref('通过')
const approvalComment = ref('')

const stepIndex = computed(() => Math.max(0, (step.value || 2) - 1))
const readonlyView = computed(() => detail.flowStatus === '已完成' || detail.flowStatus === '已关闭')
const isApprovalStep = computed(() => [2, 5, 6].includes(step.value))
const approvalTitle = computed(() => {
  if (step.value === 2) return '申请审批'
  if (step.value === 5) return '账单审批'
  if (step.value === 6) return '退住审批'
  return '审批'
})

onMounted(loadDetail)

function fmtMoney(v) {
  if (v == null || v === '') return '—'
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
      ElMessage.error(res.data.msg || '加载详情失败')
    }
  }).catch(() => ElMessage.error('加载详情失败'))
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
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.data.msg || '上传失败')
    }
  }).catch(() => ElMessage.error('上传失败'))
}

function validateCurrent() {
  if (isApprovalStep.value && !approvalResult.value) {
    ElMessage.warning('请选择审批结果')
    return false
  }
  if (step.value === 3) {
    if (!detail.terminateDate || !detail.terminateFile) {
      ElMessage.warning('请完善解除协议信息')
      return false
    }
  }
  if (step.value === 4) {
    if (detail.billReceivable == null || detail.billArrears == null || detail.billBalance == null) {
      ElMessage.warning('请填写账单金额')
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
      ElMessage.success(step.value === 7 ? '退住流程已完成' : '保存成功')
      if (step.value === 7 || approvalResult.value === '拒绝') {
        goBack()
      } else {
        const next = Number(res.data.data) || (step.value + 1)
        step.value = next
        router.replace({ path: '/CheckoutDetail', query: { id: detail.id, step: next } })
        loadDetail()
      }
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  }).catch(() => ElMessage.error('操作失败'))
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
