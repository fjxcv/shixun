<template>
  <el-steps :active="active" align-center finish-status="success" class="checkin-steps">
    <el-step v-for="(s, i) in steps" :key="i" :title="s" />
  </el-steps>
</template>

<script setup>
defineProps({
  active: { type: Number, default: 0 },
  steps: {
    type: Array,
    default: () => ['申请入住', '入住评估', '入住审批', '入住配置', '签约办理']
  }
})
</script>

<style scoped>
.checkin-steps { margin-bottom: 24px; padding: 16px 0; background: #fff; }
</style>
