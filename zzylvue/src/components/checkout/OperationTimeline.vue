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
  nodes: { type: Array, default: null }
})

const defaultNodes = computed(() => {
  const prefix = props.actionPrefix
  return [
    {
      name: `发起申请-申请${prefix}`,
      time: '2048-10-10 15:00:00',
      operator: '顾廷烨',
      status: '已发起',
      type: 'success'
    },
    {
      name: `审批角色处理-${prefix}评估`,
      time: props.currentStep >= 3 ? '2048-10-10 16:00:00' : '--',
      operator: props.currentStep >= 3 ? '盛明兰' : '待审批',
      status: props.currentStep >= 3 ? '已处理' : '审批中',
      type: props.currentStep >= 3 ? 'primary' : 'warning'
    },
    {
      name: `审批角色处理-${prefix}配置`,
      time: props.currentStep >= 4 ? '2048-10-11 09:00:00' : '--',
      operator: props.currentStep >= 4 ? '盛明兰' : '待处理',
      status: props.currentStep >= 4 ? '已处理' : '待处理',
      type: props.currentStep >= 4 ? 'primary' : 'info'
    },
    {
      name: '完成签约办理',
      time: props.currentStep >= 5 ? '2048-10-12 10:00:00' : '--',
      operator: props.currentStep >= 5 ? '顾廷烨' : '待处理',
      status: props.currentStep >= 5 ? '已处理' : '待处理',
      type: props.currentStep >= 5 ? 'success' : 'info'
    }
  ]
})

const displayNodes = computed(() => {
  return props.nodes || defaultNodes.value
})
</script>

<style scoped>
.timeline h4 { margin: 0 0 12px; }
.sub { color: #666; font-size: 13px; margin: 4px 0; display: flex; align-items: center; gap: 8px; }
.status-tag { font-size: 11px; }
</style>
