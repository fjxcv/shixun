<template>
  <div class="timeline">
    <h4 v-if="showTitle">操作记录</h4>
    <el-timeline>
      <el-timeline-item :timestamp="applyTime" placement="top" type="success">
        <p>发起申请-申请{{ actionPrefix }}</p>
        <p class="sub">{{ applicant }}（已发起）</p>
      </el-timeline-item>
      <el-timeline-item v-if="step >= 2" :timestamp="applyTime" placement="top" type="primary">
        <p>（角色）处理-{{ actionPrefix }}评估/审批</p>
        <p class="sub">护理组主管（已处理）</p>
        <div v-if="comment" class="comment">{{ comment }}</div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  showTitle: { type: Boolean, default: true },
  actionPrefix: { type: String, default: '退住' },
  data: { type: Object, default: () => ({}) }
})

const step = computed(() => props.data?.step || 1)
const applicant = computed(() => props.data?.applicant || props.data?.creator || '申请人')
const applyTime = computed(() => props.data?.applyTime || props.data?.createTime || '')
const comment = computed(() => props.data?.approvalComment || '')
</script>

<style scoped>
.timeline h4 { margin: 0 0 12px; }
.sub { color: #666; font-size: 13px; margin: 4px 0; }
.comment { background: #f5f5f5; padding: 8px; border-radius: 4px; color: #999; font-size: 13px; margin-top: 8px; }
</style>
