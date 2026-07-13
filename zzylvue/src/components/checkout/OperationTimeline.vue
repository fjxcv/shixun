<template>
  <div class="timeline">
    <h4 v-if="showTitle">操作记录</h4>
    <el-timeline>
      <el-timeline-item
        v-for="(node, i) in displayNodes"
        :key="i"
        :timestamp="node.time"
        placement="top"
        :type="node.type"
      >
        <p>{{ node.name }}</p>
        <p class="sub">
          {{ node.operator }}
          <el-tag v-if="node.status === '审批中'" size="small" type="warning" class="status-tag">审批中</el-tag>
          <el-tag v-else-if="node.status === '已处理'" size="small" type="success" class="status-tag">已处理</el-tag>
          <el-tag v-else-if="node.status === '已发起'" size="small" type="info" class="status-tag">已发起</el-tag>
          <el-tag v-else-if="node.status === '已完成'" size="small" type="success" class="status-tag">已完成</el-tag>
        </p>
        <div v-if="node.comment" class="comment">{{ node.comment }}</div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  showTitle: { type: Boolean, default: true },
  actionPrefix: { type: String, default: '退住' },
  currentStep: { type: Number, default: 1 },
  nodes: { type: Array, default: null },
  data: { type: Object, default: () => ({}) }
})

const defaultNodes = computed(() => {
  const prefix = props.actionPrefix
  const d = props.data || {}
  const step = d.step || props.currentStep || 1
  const applicant = d.applicant || d.creator || '申请人'
  const applyTime = d.applyTime || d.createTime || '--'
  const comment = d.approvalComment || ''

  if (prefix === '退住') {
    return [
      {
        name: `发起申请-申请${prefix}`,
        time: applyTime,
        operator: `${applicant}（已发起）`,
        status: '已发起',
        type: 'success',
        comment: ''
      },
      {
        name: `护理组主管处理-${prefix}审批`,
        time: step >= 3 ? applyTime : '--',
        operator: step >= 3 ? '护理组主管（已处理）' : '待审批',
        status: step >= 3 ? '已处理' : '审批中',
        type: step >= 3 ? 'primary' : 'warning',
        comment: step >= 2 ? comment : ''
      }
    ]
  }

  return [
    {
      name: `发起申请-申请${prefix}`,
      time: applyTime,
      operator: applicant,
      status: '已发起',
      type: 'success',
      comment: ''
    },
    {
      name: `审批-${prefix}评估`,
      time: step >= 3 ? applyTime : '--',
      operator: step >= 3 ? '盛明兰' : '待审批',
      status: step >= 3 ? '已处理' : '审批中',
      type: step >= 3 ? 'primary' : 'warning',
      comment: ''
    },
    {
      name: `审批-${prefix}配置`,
      time: step >= 4 ? applyTime : '--',
      operator: step >= 4 ? '盛明兰' : '待处理',
      status: step >= 4 ? '已处理' : '待处理',
      type: step >= 4 ? 'primary' : 'info',
      comment: ''
    },
    {
      name: '完成签约办理',
      time: step >= 5 ? applyTime : '--',
      operator: step >= 5 ? applicant : '待处理',
      status: step >= 5 ? '已处理' : '待处理',
      type: step >= 5 ? 'success' : 'info',
      comment: ''
    }
  ]
})

const displayNodes = computed(() => props.nodes || defaultNodes.value)
</script>

<style scoped>
.timeline h4 { margin: 0 0 12px; }
.sub { color: #666; font-size: 13px; margin: 4px 0; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.status-tag { font-size: 11px; }
.comment { background: #f5f5f5; padding: 8px; border-radius: 4px; color: #999; font-size: 13px; margin-top: 8px; }
</style>
