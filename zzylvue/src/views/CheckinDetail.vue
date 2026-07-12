<template>
  <PageCard>
    <CheckinSteps :active="stepIndex" />
    <div v-if="readonlyView" class="detail-layout">
      <div class="detail-main">
        <InfoSectionsCheckin :data="detail" />
        <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
      </div>
      <OperationTimeline class="detail-side" action-prefix="入住" />
    </div>
    <div v-else-if="isApprovalPending" class="status-panel">
      <el-result icon="info" title="审批中" sub-title="稍等，入住申请正在审批中，若该申请单长时间未处理，请联系审批角色！" />
      <OperationTimeline action-prefix="入住" />
      <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
    </div>
    <div v-else class="detail-layout">
      <div class="detail-main">
        <InfoSectionsCheckin :data="detail" />

        <div v-if="step === 2" class="assess-panel">
          <h4>入住评估</h4>
          <el-tabs v-model="assessTab" :before-leave="beforeAssessTabLeave">
            <el-tab-pane label="健康评估" name="health" :disabled="false">
              <el-form label-width="120px" class="assess-form">
                <el-form-item label="疾病情况" required>
                  <el-input v-model="assess.disease" type="textarea" :rows="3" placeholder="如：冠心病、糖尿病" maxlength="200" show-word-limit />
                </el-form-item>
                <el-form-item label="用药情况" required>
                  <el-input v-model="assess.medicine" type="textarea" :rows="3" placeholder="请填写用药情况" maxlength="200" show-word-limit />
                </el-form-item>
                <el-form-item label="风险事件" required>
                  <el-input v-model="assess.risk" placeholder="如：跌倒发生过1次" maxlength="100" show-word-limit />
                </el-form-item>
              </el-form>
              <div class="tab-actions">
                <el-button type="primary" @click="saveAssessTab('ability')">保存并进入能力评估</el-button>
              </div>
            </el-tab-pane>
            <el-tab-pane label="能力评估" name="ability" :disabled="!assessDone.health">
              <el-form label-width="120px" class="assess-form">
                <el-form-item label="自理能力" required>
                  <el-select v-model="assess.ability" placeholder="请选择" style="width: 240px">
                    <el-option label="能力完好" value="能力完好" />
                    <el-option label="轻度失能" value="轻度失能" />
                    <el-option label="中度失能" value="中度失能" />
                    <el-option label="重度失能" value="重度失能" />
                  </el-select>
                </el-form-item>
                <el-form-item label="评估总分" required>
                  <el-input-number v-model="assess.score" :min="0" :max="100" />
                </el-form-item>
              </el-form>
              <div class="tab-actions">
                <el-button @click="assessTab = 'health'">上一步</el-button>
                <el-button type="primary" @click="saveAssessTab('report')">保存并进入评估报告</el-button>
              </div>
            </el-tab-pane>
            <el-tab-pane label="评估报告" name="report" :disabled="!assessDone.ability">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="疾病情况">{{ assess.disease }}</el-descriptions-item>
                <el-descriptions-item label="用药情况">{{ assess.medicine }}</el-descriptions-item>
                <el-descriptions-item label="风险事件">{{ assess.risk }}</el-descriptions-item>
                <el-descriptions-item label="评估总分">{{ assess.score }}</el-descriptions-item>
                <el-descriptions-item label="能力初步等级">{{ assess.ability }}</el-descriptions-item>
              </el-descriptions>
              <div class="tab-actions">
                <el-button @click="assessTab = 'ability'">上一步</el-button>
                <el-button type="primary" @click="submitCurrent">提交评估（进入入住审批）</el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <div v-if="step === 3">
          <h4>审批结果</h4>
          <el-radio-group v-model="approvalResult">
            <el-radio value="审批通过">审批通过</el-radio>
            <el-radio value="审批拒绝">审批拒绝</el-radio>
            <el-radio value="驳回">驳回</el-radio>
          </el-radio-group>
          <el-input v-model="approvalComment" type="textarea" maxlength="200" show-word-limit placeholder="审批意见" style="margin-top:12px" />
        </div>
        <div v-if="step === 4">
          <h4>选择入住配置</h4>
          <el-form label-width="120px">
            <el-form-item label="入住期限" required><el-date-picker v-model="periodRange" type="daterange" value-format="YYYY-MM-DD" /></el-form-item>
            <el-form-item label="护理等级" required><el-input v-model="detail.nursingLevel" /></el-form-item>
            <el-form-item label="入住床位"><el-input v-model="detail.bedNo" /></el-form-item>
            <el-form-item label="押金(元)" required><el-input-number v-model="detail.deposit" :min="0" :precision="2" /></el-form-item>
            <el-form-item label="护理费用" required><el-input-number v-model="detail.nursingFee" :min="0" :precision="2" /></el-form-item>
            <el-form-item label="床位费用" required><el-input-number v-model="detail.bedFee" :min="0" :precision="2" /></el-form-item>
          </el-form>
        </div>
        <div v-if="step === 5">
          <h4>完成签约办理</h4>
          <el-form label-width="120px">
            <el-form-item label="合同编号">{{ detail.contractNo || ('HT' + Date.now()) }}</el-form-item>
            <el-form-item label="合同名称" required><el-input v-model="detail.contractName" /></el-form-item>
            <el-form-item label="签约日期" required><el-date-picker v-model="detail.signDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
            <el-form-item label="上传合同">
              <el-upload action="#" :http-request="uploadContract" :show-file-list="false">
                <el-button type="primary" plain>上传文件</el-button>
              </el-upload>
              <span v-if="detail.contractFile" style="margin-left:8px">{{ detail.contractFile }}</span>
            </el-form-item>
          </el-form>
        </div>
        <div v-if="step !== 2" class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button v-if="canSubmit" type="primary" @click="submitCurrent">{{ step === 5 ? '提交' : '保存' }}</el-button>
        </div>
        <div v-else class="form-actions">
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" action-prefix="入住" />
    </div>
  </PageCard>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'
import CheckinSteps from '@/components/CheckinSteps.vue'
import OperationTimeline from '@/components/checkout/OperationTimeline.vue'
import InfoSectionsCheckin from '@/components/checkin/InfoSectionsCheckin.vue'

const route = useRoute()
const router = useRouter()
const detail = reactive({})
const uploads = reactive({ photo: '', idFront: '', idBack: '' })
const assessTab = ref('health')
const assess = reactive({ disease: '', medicine: '', risk: '', ability: '', score: 30 })
const assessDone = reactive({ health: false, ability: false, report: false })
const approvalResult = ref('审批通过')
const approvalComment = ref('')
const periodRange = ref([])
const mode = computed(() => route.query.mode || 'form')
const step = ref(1)
const stepIndex = computed(() => Math.max(0, step.value - 1))
const readonlyView = computed(() => detail.flowStatus === '已完成' || detail.flowStatus === '已关闭')
// 仅审批节点才显示「审批中」；评估/配置/签约必须可填
const isApprovalPending = computed(() => mode.value === 'pending' && step.value === 3)
const canSubmit = computed(() => !readonlyView.value && !isApprovalPending.value)

onMounted(loadDetail)

function parseExtra(raw) {
  if (!raw) return
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!obj || typeof obj !== 'object') return
    // 新结构 { uploads, assess }
    if (obj.assess || obj.uploads) {
      if (obj.uploads) Object.assign(uploads, obj.uploads)
      if (obj.assess) {
        Object.assign(assess, {
          disease: obj.assess.disease || '',
          medicine: obj.assess.medicine || '',
          risk: obj.assess.risk || '',
          ability: obj.assess.ability || '',
          score: obj.assess.score != null ? obj.assess.score : 30
        })
        if (assess.disease && assess.medicine && assess.risk) assessDone.health = true
        if (assess.ability) assessDone.ability = true
      }
      // 兼容旧上传结构直接挂在根上
      if (obj.photo || obj.idFront || obj.idBack) {
        uploads.photo = obj.photo || uploads.photo
        uploads.idFront = obj.idFront || uploads.idFront
        uploads.idBack = obj.idBack || uploads.idBack
      }
      return
    }
    // 旧结构：可能是纯上传，或纯评估字段
    if (obj.photo || obj.idFront || obj.idBack) {
      uploads.photo = obj.photo || ''
      uploads.idFront = obj.idFront || ''
      uploads.idBack = obj.idBack || ''
    }
    if (obj.disease || obj.medicine || obj.ability) {
      Object.assign(assess, {
        disease: obj.disease || '',
        medicine: obj.medicine || '',
        risk: obj.risk || '',
        ability: obj.ability || '',
        score: obj.score != null ? obj.score : 30
      })
      if (assess.disease && assess.medicine && assess.risk) assessDone.health = true
      if (assess.ability) assessDone.ability = true
    }
  } catch (e) { /* ignore */ }
}

function buildExtraJson() {
  return JSON.stringify({ uploads: { ...uploads }, assess: { ...assess } })
}

function loadDetail() {
  const id = route.query.id
  if (!id) return
  axios.get('/checkin/detail', { params: { id } }).then(res => {
    if (res.data.code === 200) {
      Object.assign(detail, res.data.data || {})
      step.value = Number(detail.step) || 1
      if (detail.periodStart && detail.periodEnd) {
        periodRange.value = [detail.periodStart, detail.periodEnd]
      }
      parseExtra(detail.extraJson)
    } else {
      ElMessage.error(res.data.msg || '加载详情失败')
    }
  }).catch(() => ElMessage.error('加载详情失败'))
}

function goBack() {
  router.push('/CheckinProcess')
}

function uploadContract(option) {
  const formData = new FormData()
  formData.append('mf', option.file)
  axios.post('/upload', formData).then(res => {
    if (res.data.code === 200) {
      detail.contractFile = res.data.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.data.msg || '上传失败')
    }
  }).catch(() => ElMessage.error('上传失败'))
}

function validateHealth() {
  if (!assess.disease || !assess.medicine || !assess.risk) {
    ElMessage.warning('请完整填写健康评估')
    return false
  }
  return true
}

function validateAbility() {
  if (!assess.ability || assess.score == null) {
    ElMessage.warning('请完整填写能力评估')
    return false
  }
  return true
}

function beforeAssessTabLeave(activeName, oldActiveName) {
  if (oldActiveName === 'health' && activeName !== 'health' && !assessDone.health) {
    ElMessage.warning('请先保存健康评估')
    return false
  }
  if (oldActiveName === 'ability' && activeName === 'report' && !assessDone.ability) {
    ElMessage.warning('请先保存能力评估')
    return false
  }
  return true
}

function saveAssessTab(next) {
  if (next === 'ability') {
    if (!validateHealth()) return
    assessDone.health = true
    assessTab.value = 'ability'
    ElMessage.success('健康评估已保存')
    return
  }
  if (next === 'report') {
    if (!assessDone.health && !validateHealth()) return
    if (!validateAbility()) return
    assessDone.health = true
    assessDone.ability = true
    assessTab.value = 'report'
    ElMessage.success('能力评估已保存')
  }
}

function validateCurrent() {
  if (step.value === 2) {
    if (!validateHealth() || !validateAbility()) return false
    assessDone.health = true
    assessDone.ability = true
  } else if (step.value === 3) {
    if (!approvalResult.value) {
      ElMessage.warning('请选择审批结果')
      return false
    }
  } else if (step.value === 4) {
    if (!periodRange.value || periodRange.value.length !== 2 || !detail.nursingLevel) {
      ElMessage.warning('请完善入住配置')
      return false
    }
  } else if (step.value === 5) {
    if (!detail.contractName || !detail.signDate) {
      ElMessage.warning('请完善签约信息')
      return false
    }
  }
  return true
}

function submitCurrent() {
  if (!validateCurrent()) return
  const payload = {
    id: detail.id,
    step: step.value === 5 ? 5 : step.value + 1,
    approvalResult: approvalResult.value,
    approvalComment: approvalComment.value,
    nursingLevel: detail.nursingLevel || assess.ability,
    deposit: detail.deposit,
    nursingFee: detail.nursingFee,
    bedFee: detail.bedFee,
    bedNo: detail.bedNo,
    contractName: detail.contractName,
    contractFile: detail.contractFile,
    contractNo: detail.contractNo || ('HT' + Date.now()),
    signDate: detail.signDate,
    flowStatus: step.value === 5 ? '已完成' : '申请中',
    stepStatus: step.value === 5 ? '已完成' : '进行中',
    extraJson: buildExtraJson()
  }
  if (periodRange.value && periodRange.value.length === 2) {
    payload.periodStart = periodRange.value[0]
    payload.periodEnd = periodRange.value[1]
  }
  axios.post('/checkin/updateStep', payload).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(step.value === 5 ? '提交成功' : '保存成功')
      if (step.value === 5 || approvalResult.value === '审批拒绝') {
        goBack()
      } else {
        const next = Number(res.data.data) || (step.value + 1)
        step.value = next
        router.replace({ path: '/CheckinDetail', query: { id: detail.id, step: next, mode: 'form' } })
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
.status-panel { max-width: 720px; margin: 0 auto; }
.assess-panel { margin-top: 16px; }
.assess-form { max-width: 640px; margin-top: 12px; }
.tab-actions { margin-top: 16px; display: flex; gap: 12px; }
</style>
