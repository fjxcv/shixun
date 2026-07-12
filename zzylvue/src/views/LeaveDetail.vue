<template>
  <PageCard>
    <el-steps :active="3" align-center finish-status="success" style="margin-bottom: 24px">
      <el-step title="申请请假" />
      <el-step title="申请审批" />
      <el-step title="请假审批" />
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
          <el-descriptions-item label="护理员">{{ detail.caregivers }}</el-descriptions-item>
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
      </el-col>
      <el-col :span="8">
        <div class="timeline-box">
          <h4>操作记录</h4>
          <el-timeline>
            <el-timeline-item timestamp="2048-10-15 09:00:00" type="success">发起申请-申请请假 · 顾廷烨</el-timeline-item>
            <el-timeline-item timestamp="2048-10-15 09:00:00" type="primary">护理组主管审批-申请审批 · 盛明兰（已通过）</el-timeline-item>
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
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const route = useRoute()
const detail = ref({})

onMounted(() => {
  if (!route.query.id) return
  axios.get('/leave/detail', { params: { id: route.query.id } }).then(res => {
    if (res.data.code === 200) detail.value = res.data.data || {}
  }).catch(() => ElMessage.error('加载详情失败'))
})
</script>

<style scoped>
.timeline-box { background: #fafafa; padding: 16px; border-radius: 4px; }
.form-actions { text-align: center; margin-top: 24px; }
</style>
