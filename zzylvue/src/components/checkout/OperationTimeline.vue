<template>
  <div class="timeline">
    <h4 v-if="showTitle">操作记录</h4>
    <el-timeline>
      <el-timeline-item timestamp="2048-10-15 09:00:00" placement="top" type="success">
        <p>发起申请-申请{{ actionPrefix }}</p>
        <p class="sub">顾廷烨（已发起）</p>
      </el-timeline-item>
      <el-timeline-item timestamp="2048-10-15 09:00:00" placement="top" type="primary">
        <p>（角色）处理-{{ actionPrefix }}评估</p>
        <p class="sub">盛明兰（已处理）</p>
        <div class="comment">这里是审批意见。</div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
defineProps({
  showTitle: { type: Boolean, default: true },
  actionPrefix: { type: String, default: '退住' }
})
</script>

<style scoped>
.timeline h4 { margin: 0 0 12px; }
.sub { color: #666; font-size: 13px; margin: 4px 0; }
.comment { background: #f5f5f5; padding: 8px; border-radius: 4px; color: #999; font-size: 13px; margin-top: 8px; }
</style>
