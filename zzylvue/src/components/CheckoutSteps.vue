<template>
  <el-steps :active="active" align-center finish-status="success" class="checkout-steps">
    <el-step v-for="(s, i) in steps" :key="i" :title="s" />
  </el-steps>
</template>

<script setup>
defineProps({
  active: { type: Number, default: 0 },
  steps: {
    type: Array,
    default: () => ['申请退住', '申请审批', '解除合同', '调整账单', '账单审批', '退住审批', '费用清算']
  }
})
</script>

<style scoped>
.checkout-steps { margin-bottom: 24px; padding: 16px 0; background: #fff; }
</style>
