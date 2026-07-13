<template>
  <div class="info-sections">
    <!-- 基本信息 -->
    <h4 class="section-head">基本信息</h4>
    <el-form label-width="120px" class="readonly-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="老人姓名"><span class="form-text">{{ data.elderName }}</span></el-form-item>
          <el-form-item label="性别"><span class="form-text">{{ data.gender }}</span></el-form-item>
          <el-form-item label="出生日期"><span class="form-text">{{ data.birthDate }}</span></el-form-item>
          <el-form-item label="联系方式"><span class="form-text">{{ data.elderPhone }}</span></el-form-item>
          <el-form-item v-if="data.ethnicity" label="民族"><span class="form-text">{{ data.ethnicity }}</span></el-form-item>
          <el-form-item v-if="data.politicalStatus" label="政治面貌"><span class="form-text">{{ data.politicalStatus }}</span></el-form-item>
          <el-form-item v-if="data.religion" label="宗教信仰"><span class="form-text">{{ data.religion }}</span></el-form-item>
          <el-form-item v-if="data.hobbies" label="特长爱好"><span class="form-text">{{ data.hobbies || '暂无' }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="老人身份证号"><span class="form-text">{{ data.elderIdcard }}</span></el-form-item>
          <el-form-item label="年龄"><span class="form-text">{{ data.age }}</span></el-form-item>
          <el-form-item label="家庭住址"><span class="form-text">{{ data.address }}</span></el-form-item>
          <el-form-item v-if="data.maritalStatus" label="婚姻状况"><span class="form-text">{{ data.maritalStatus }}</span></el-form-item>
          <el-form-item v-if="data.educationLevel" label="文化程度"><span class="form-text">{{ data.educationLevel }}</span></el-form-item>
          <el-form-item v-if="data.incomeSource" label="经济来源"><span class="form-text">{{ data.incomeSource }}</span></el-form-item>
          <el-form-item v-if="data.medicalInsurance" label="医疗保障"><span class="form-text">{{ data.medicalInsurance }}</span></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20" v-if="data.medicalInsuranceNo">
        <el-col :span="12">
          <el-form-item label="医保卡号"><span class="form-text">{{ data.medicalInsuranceNo }}</span></el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 审批结果 -->
    <h4 class="section-head" style="margin-top:20px">审批结果</h4>
    <el-form label-width="120px" class="readonly-form">
      <el-form-item label="审批状态">
        <el-tag v-if="data.flowStatus === '已完成'" type="success" size="small">审批通过</el-tag>
        <el-tag v-else-if="data.flowStatus === '已关闭'" type="danger" size="small">已关闭</el-tag>
        <el-tag v-else type="warning" size="small">审批中</el-tag>
      </el-form-item>
      <el-form-item v-if="data.approvalResult" label="审批结果">
        <span :class="{ 'form-text': true, 'text-danger': data.approvalResult === '审批拒绝' }">{{ data.approvalResult }}</span>
      </el-form-item>
      <el-form-item v-if="data.approvalComment" label="审批意见">
        <span class="form-text">{{ data.approvalComment }}</span>
      </el-form-item>
    </el-form>

    <!-- 健康能力评估 -->
    <h4 class="section-head" style="margin-top:20px">健康能力评估</h4>

    <el-divider content-position="left">疾病和用药情况</el-divider>
    <el-form label-width="140px" class="readonly-form">
      <el-form-item label="疾病诊断">
        <template v-if="assess.diseases.length">
          <el-tag v-for="d in assess.diseases" :key="d" size="small" class="info-tag">{{ d }}</el-tag>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
      <el-form-item label="用药情况">
        <template v-if="assess.medications.length && assess.medications.some(m => m.name)">
          <el-table :data="assess.medications.filter(m => m.name)" border size="small" style="width:100%">
            <el-table-column prop="name" label="药物名称" />
            <el-table-column prop="method" label="服药方法" />
            <el-table-column prop="dosage" label="用药剂量" />
          </el-table>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">近三十天内风险事件</el-divider>
    <el-form label-width="180px" class="readonly-form">
      <el-form-item v-for="r in riskItems" :key="r.key" :label="r.label">
        <span class="form-text">{{ assess[r.key] || '暂无' }}</span>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">身体健康情况</el-divider>
    <el-form label-width="180px" class="readonly-form">
      <el-form-item label="伤口情况">
        <template v-if="assess.wounds.length">
          <el-tag v-for="w in assess.wounds" :key="w" size="small" class="info-tag">{{ w }}</el-tag>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
      <el-form-item label="特殊医疗照护情况">
        <template v-if="assess.specialCare.length">
          <el-tag v-for="s in assess.specialCare" :key="s" size="small" class="info-tag">{{ s }}</el-tag>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
      <el-form-item label="自理能力">
        <span class="form-text">{{ assess.selfCare || '暂无' }}</span>
      </el-form-item>
      <el-form-item label="痴呆前兆">
        <template v-if="assess.dementia.length">
          <el-tag v-for="d in assess.dementia" :key="d" size="small" class="info-tag">{{ d }}</el-tag>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
      <el-form-item v-if="assess.otherNote" label="其他">
        <span class="form-text">{{ assess.otherNote }}</span>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">近期体检报告</el-divider>
    <el-form label-width="140px" class="readonly-form">
      <el-form-item label="体检报告">
        <template v-if="assess.reportFile">
          <span class="form-text">{{ reportFileName }}</span>
          <el-button link type="primary" size="small" style="margin-left:8px" @click="viewReport">查看</el-button>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
    </el-form>

    <!-- 评估报告 -->
    <h4 class="section-head" style="margin-top:20px">评估报告</h4>

    <div class="report-section-head">
      <span class="report-section-title">能力评估结果</span>
      <el-tooltip placement="right" effect="light" raw-content>
        <template #content>
          <div style="max-width:260px;font-size:12px;line-height:1.6">
            根据十道能力评估题目得分汇总：<br/>
            自理能力（3题/15分）+ 精神状态（3题/15分）+ 感知觉与社会参与（4题/20分）= 评估总分（50分）<br/>
            <b>能力初步等级划分：</b><br/>
            10分 → 能力完好<br/>
            11-20分 → 轻度失能<br/>
            21-30分 → 中度失能<br/>
            31-40分 → 中重度失能<br/>
            41-50分 → 重度失能
          </div>
        </template>
        <el-icon class="tip-icon"><QuestionFilled /></el-icon>
      </el-tooltip>
    </div>
    <table class="report-table">
      <thead>
        <tr><th>指标</th><th>得分</th></tr>
      </thead>
      <tbody>
        <tr><td>自理能力</td><td class="report-score">{{ selfCareScore }} / 15 分</td></tr>
        <tr><td>精神状态</td><td class="report-score">{{ mentalScore }} / 15 分</td></tr>
        <tr><td>感知觉与社会参与</td><td class="report-score">{{ socialScore }} / 20 分</td></tr>
        <tr class="report-total-row"><td>评估总分</td><td class="report-score">{{ abilityTotal != null ? abilityTotal + ' / 50 分' : '--' }}</td></tr>
        <tr><td>能力初步等级</td><td class="report-score">{{ abilityLevel }}</td></tr>
      </tbody>
    </table>

    <div class="report-section-head">
      <span class="report-section-title">护理等级结果</span>
      <el-tooltip placement="right" effect="light" raw-content>
        <template #content>
          <div style="max-width:300px;font-size:12px;line-height:1.6">
            依据健康评估信息表中的疾病和用药情况、近30天风险事件、身体健康情况、近期体检报告，确定是否存在导致能力等级变更的项目。若有以下情况之一，在原有能力级别<b>提高一个级别</b>：<br/>
            1. 确诊为认知障碍/痴呆<br/>
            2. 精神科专科医生诊断的精神类疾病<br/>
            3. 近30天内发生过2次及以上照护风险事件（如跌倒、自杀、走失等）
          </div>
        </template>
        <el-icon class="tip-icon"><QuestionFilled /></el-icon>
      </el-tooltip>
    </div>
    <el-form label-width="180px" class="readonly-form">
      <el-form-item label="能力等级变更依据">
        <template v-if="assess.upgradeReasons && assess.upgradeReasons.length">
          <el-tag v-for="r in assess.upgradeReasons" :key="r" size="small" class="info-tag">{{ r }}</el-tag>
          <div v-if="assess.upgradeReasons.includes('其他') && assess.upgradeOtherNote" style="margin-top:8px">
            <span class="form-text">{{ assess.upgradeOtherNote }}</span>
          </div>
        </template>
        <span v-else class="text-muted">暂无</span>
      </el-form-item>
      <el-form-item label="能力最终等级">
        <span class="form-text" style="font-weight:600">{{ assess.finalLevel || '--' }}</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'

const props = defineProps({ data: { type: Object, default: () => ({}) } })

const riskItems = [
  { key: 'riskFalls', label: '跌倒' },
  { key: 'riskLost', label: '走失' },
  { key: 'riskChoking', label: '噎食' },
  { key: 'riskSuicide', label: '自杀' },
  { key: 'riskComa', label: '昏迷' }
]

const assess = reactive({
  diseases: [],
  medications: [],
  riskFalls: '', riskLost: '', riskChoking: '', riskSuicide: '', riskComa: '',
  wounds: [], specialCare: [], selfCare: '', dementia: [], otherNote: '',
  reportFile: '', finalLevel: '',
  upgradeReasons: [], upgradeOtherNote: ''
})

const abilityScores = reactive({})

const abilityTotal = computed(() => {
  const vals = Object.values(abilityScores)
  if (!vals.length || vals.some(v => v == null)) return null
  return vals.reduce((a, b) => a + b, 0)
})

const selfCareScore = computed(() => {
  if (abilityScores.q1 == null || abilityScores.q2 == null || abilityScores.q3 == null) return '--'
  return abilityScores.q1 + abilityScores.q2 + abilityScores.q3
})
const mentalScore = computed(() => {
  if (abilityScores.q4 == null || abilityScores.q5 == null || abilityScores.q6 == null) return '--'
  return abilityScores.q4 + abilityScores.q5 + abilityScores.q6
})
const socialScore = computed(() => {
  if (abilityScores.q7 == null || abilityScores.q8 == null || abilityScores.q9 == null || abilityScores.q10 == null) return '--'
  return abilityScores.q7 + abilityScores.q8 + abilityScores.q9 + abilityScores.q10
})

const abilityLevel = computed(() => {
  if (abilityTotal.value == null) return '--'
  const t = abilityTotal.value
  if (t === 10) return '能力完好'
  if (t <= 20) return '轻度失能'
  if (t <= 30) return '中度失能'
  if (t <= 40) return '中重度失能'
  return '重度失能'
})

function parseExtra(raw) {
  if (!raw) return
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!obj || typeof obj !== 'object') return
    if (obj.assess) {
      const a = obj.assess
      assess.diseases = a.diseases || []
      assess.medications = a.medications || []
      assess.riskFalls = a.riskFalls || ''
      assess.riskLost = a.riskLost || ''
      assess.riskChoking = a.riskChoking || ''
      assess.riskSuicide = a.riskSuicide || ''
      assess.riskComa = a.riskComa || ''
      assess.wounds = a.wounds || []
      assess.specialCare = a.specialCare || []
      assess.selfCare = a.selfCare || ''
      assess.dementia = a.dementia || []
      assess.otherNote = a.otherNote || a.otherConditions || ''
      assess.reportFile = a.reportFile || ''
      assess.finalLevel = a.finalLevel || ''
      assess.upgradeReasons = a.upgradeReasons || []
      assess.upgradeOtherNote = a.upgradeOtherNote || ''
    }
    if (obj.abilityScores) {
      Object.keys(obj.abilityScores).forEach(k => {
        if (obj.abilityScores[k] != null) abilityScores[k] = obj.abilityScores[k]
      })
    }
  } catch (e) { /* ignore */ }
}

watch(() => props.data, (val) => {
  if (val && val.extraJson) parseExtra(val.extraJson)
}, { immediate: true, deep: true })

const reportFileName = computed(() => {
  const url = assess.reportFile
  if (!url) return ''
  const idx = url.lastIndexOf('/')
  return idx >= 0 ? url.substring(idx + 1) : url
})

function viewReport() {
  if (assess.reportFile) {
    const url = assess.reportFile.startsWith('http') ? assess.reportFile : '/uploads/' + assess.reportFile
    window.open(url, '_blank')
  }
}
</script>

<style scoped>
.section-head { margin: 0 0 12px; font-size: 16px; font-weight: 600; color: #303133; }
.readonly-form { margin-top: 4px; }
.readonly-form :deep(.el-form-item) { margin-bottom: 12px; }
.readonly-form :deep(.el-form-item__label) { color: #606266; }

.form-text { color: #303133; font-size: 14px; word-break: break-all; }
.text-muted { color: #c0c4cc; font-size: 14px; }
.text-danger { color: #f56c6c; }
.info-tag { margin-right: 6px; margin-bottom: 4px; }

/* 报告区域（与评估表单样式一致） */
.report-section-head { display: flex; align-items: center; margin: 8px 0; }
.report-section-title { font-size: 15px; font-weight: 600; color: #303133; }
.tip-icon { margin-left: 8px; color: #909399; font-size: 16px; cursor: help; }
.tip-icon:hover { color: #409eff; }

.report-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.report-table th, .report-table td { padding: 10px 16px; text-align: left; border: 1px solid #e4e7ed; font-size: 14px; color: #303133; }
.report-table th { background: #f5f7fa; font-weight: 600; }
.report-score { font-weight: 600; }
.report-total-row td { background: #f0f5ff; font-weight: 700; }
</style>
