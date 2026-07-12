<template>
  <div class="info-sections">
    <h4>基本信息</h4>
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="老人姓名">{{ data.elderName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="老人身份证号">{{ data.elderIdcard || '—' }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ data.gender || '—' }}</el-descriptions-item>
      <el-descriptions-item label="出生日期">{{ data.birthDate || '—' }}</el-descriptions-item>
      <el-descriptions-item label="详细地址" :span="2">{{ data.address || '—' }}</el-descriptions-item>
    </el-descriptions>

    <h4 style="margin-top:16px">健康状况</h4>
    <div v-if="hasHealthInfo" class="tag-row">
      <el-tag v-if="assess.disease" size="small">{{ assess.disease }}</el-tag>
      <el-tag v-if="assess.medicine" size="small" type="info">用药：{{ assess.medicine }}</el-tag>
      <el-tag v-if="assess.risk" size="small" type="warning">风险：{{ assess.risk }}</el-tag>
    </div>
    <el-empty v-else description="暂无健康评估信息" :image-size="48" />

    <h4 style="margin-top:16px">能力状况</h4>
    <el-descriptions v-if="hasAbilityInfo" :column="2" border size="small">
      <el-descriptions-item label="评估总分">{{ assess.score ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="能力初步等级">{{ assess.ability || '—' }}</el-descriptions-item>
      <el-descriptions-item label="能力最终等级">{{ data.nursingLevel || assess.ability || '—' }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="暂无能力评估信息" :image-size="48" />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ data: { type: Object, default: () => ({}) } })

const assess = computed(() => {
  const raw = props.data?.extraJson
  if (!raw) return {}
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    return obj?.assess || {}
  } catch {
    return {}
  }
})

const hasHealthInfo = computed(() =>
  !!(assess.value.disease || assess.value.medicine || assess.value.risk)
)

const hasAbilityInfo = computed(() =>
  assess.value.ability != null && assess.value.ability !== ''
    || assess.value.score != null
)
</script>

<style scoped>
.info-sections h4 { margin: 0 0 8px; font-size: 15px; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
