<template>
  <div class="info-sections">
    <h4>基本信息</h4>
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="老人姓名">{{ data.elderName }}</el-descriptions-item>
      <el-descriptions-item label="老人身份证号">{{ data.elderIdcard }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ data.gender }}</el-descriptions-item>
      <el-descriptions-item label="出生日期">{{ data.birthDate }}</el-descriptions-item>
      <el-descriptions-item label="详细地址" :span="2">{{ data.address }}</el-descriptions-item>
    </el-descriptions>
    <h4 style="margin-top:16px">健康状况</h4>
    <div class="tag-row">
      <el-tag v-for="t in healthTags" :key="t" size="small">{{ t }}</el-tag>
    </div>
    <h4 style="margin-top:16px">能力状况</h4>
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="评估总分">30</el-descriptions-item>
      <el-descriptions-item label="能力初步等级">轻度失能</el-descriptions-item>
      <el-descriptions-item label="能力最终等级">中度失能</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
defineProps({ data: { type: Object, default: () => ({}) } })
const healthTags = ['冠心病', '糖尿病', '跌倒：发生过1次', '不能自理', '行为异常']
</script>

<style scoped>
.info-sections h4 { margin: 0 0 8px; font-size: 15px; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
