<template>
  <PageCard>
    <el-steps :active="stepsActive" align-center finish-status="success" style="margin-bottom: 24px">
      <el-step title="申请请假" />
      <el-step title="申请审批" />
      <el-step title="请假完成" />
    </el-steps>

    <el-row :gutter="20">
      <el-col :span="16">
        <h4>基本信息</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="单据编号">{{ detail.docNo }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ detail.elderName }}</el-descriptions-item>
          <el-descriptions-item label="老人身份证号">{{ detail.elderIdcard }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detail.elderPhone }}</el-descriptions-item>
          <el-descriptions-item label="护理等级">{{ detail.nursingLevel }}</el-descriptions-item>
          <el-descriptions-item label="入住床位">{{ detail.bedInfo }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h4>申请信息</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="陪同人">{{ detail.escort }}</el-descriptions-item>
          <el-descriptions-item label="请假周期">{{ detail.startTime }} — {{ detail.expectReturnTime }}</el-descriptions-item>
          <el-descriptions-item label="请假天数">{{ detail.leaveDays }}天</el-descriptions-item>
          <el-descriptions-item label="请假原因">{{ detail.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicant }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detail.status === '已返回'">
          <el-divider />
          <h4>销假记录</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
            <el-descriptions-item label="实际返回时间">{{ detail.actualReturnTime }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.returnRemark }}</el-descriptions-item>
            <el-descriptions-item label="销假人">{{ detail.cancelUser }}</el-descriptions-item>
            <el-descriptions-item label="销假时间">{{ detail.cancelTime }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <div v-if="detail.status === '待审批'" class="step-panel">
          <h4>申请审批</h4>
          <el-radio-group v-model="approvalResult">
            <el-radio value="通过">通过</el-radio>
            <el-radio value="拒绝">拒绝</el-radio>
          </el-radio-group>
          <div class="form-actions" style="margin-top:16px">
            <el-button type="primary" @click="submitApprove">提交审批</el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="timeline-box">
          <h4>操作记录</h4>
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
      <el-button type="primary" @click="$router.back()">返回</el-button>
    </div>
  </PageCard>
</template>

<script setup>
/**
 * 请假详情：展示单据信息，并按状态提供审批、销假、撤销等操作。
 * 状态含义与后端 LeaveController 一致。
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const route = useRoute()
const detail = ref({})
const approvalResult = ref('通过')

const stepsActive = computed(() => {
  const s = detail.value.status
  if (s === '待审批') return 1
  if (s === '请假中' || s === '超时未归') return 2
  if (s === '已返回' || s === '已拒绝' || s === '已关闭') return 3
  return 1
})

const timelineItems = computed(() => {
  const d = detail.value || {}
  const items = []
  if (d.applyTime) {
    items.push({
      time: d.applyTime,
      type: 'success',
      text: `发起申请-申请请假 · ${d.applicant || d.creator || ''}`
    })
  }
  if (d.status && d.status !== '待审批') {
    const passed = d.status !== '已拒绝' && d.status !== '已关闭'
    items.push({
      time: d.applyTime || '',
      type: passed ? 'primary' : 'danger',
      text: passed
        ? `审批通过-申请审批 · ${d.status}`
        : `审批拒绝-申请审批 · ${d.status}`
    })
  }
  if (d.status === '已返回' && d.actualReturnTime) {
    items.push({
      time: d.actualReturnTime,
      type: 'success',
      text: `销假完成 · ${d.cancelUser || ''}`
    })
  }
  return items
})

onMounted(loadDetail)

function loadDetail() {
  if (!route.query.id) return
  axios.get('/leave/detail', { params: { id: route.query.id } }).then(res => {
    if (res.data.code === 200) detail.value = res.data.data || {}
    else ElMessage.error(res.data.msg || '加载详情失败')
  }).catch(() => ElMessage.error('加载详情失败'))
}

function submitApprove() {
  if (!approvalResult.value) {
    ElMessage.warning('请选择审批结果')
    return
  }
  axios.post('/leave/approve', {
    id: detail.value.id,
    approvalResult: approvalResult.value
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('审批成功')
      loadDetail()
    } else {
      ElMessage.error(res.data.msg || '审批失败')
    }
  }).catch(() => ElMessage.error('审批失败'))
}
</script>

<style scoped>
.timeline-box { background: #fafafa; padding: 16px; border-radius: 4px; }
.form-actions { text-align: center; margin-top: 24px; }
.step-panel { margin-top: 20px; }
</style>
