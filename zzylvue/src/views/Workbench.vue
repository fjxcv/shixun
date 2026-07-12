<template>
  <PageCard>
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card header="快捷入口">
          <el-space wrap>
            <el-button type="primary" plain @click="$router.push('/ReservationRegister')">预约登记</el-button>
            <el-button type="primary" plain @click="$router.push('/VisitRegister')">来访登记</el-button>
            <el-button type="primary" plain @click="$router.push('/CustomerInfo')">客户信息</el-button>
            <el-button type="primary" plain @click="$router.push('/LeaveManage')">请假管理</el-button>
          </el-space>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="系统概览">
          <p>欢迎使用中州养老服务平台工作台。</p>
          <p>今日可处理预约、来访、退住及在住老人相关业务。</p>
        </el-card>
      </el-col>
    </el-row>
  </PageCard>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import PageCard from '@/components/PageCard.vue'

const cards = ref([
  { label: '预约总数', value: 0 },
  { label: '来访总数', value: 0 },
  { label: '客户总数', value: 0 },
  { label: '请假中', value: 0 }
])

onMounted(() => {
  axios.get('/workbench/stats').then(res => {
    if (res.data.code === 200 && res.data.data) {
      const d = res.data.data
      cards.value = [
        { label: '预约总数', value: d.reservationCount || 0 },
        { label: '来访总数', value: d.visitCount || 0 },
        { label: '客户总数', value: d.customerCount || 0 },
        { label: '请假中', value: d.leaveCount || 0 }
      ]
    }
  }).catch(() => {})
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: 600; color: #409eff; }
.stat-label { color: #666; margin-top: 8px; }
</style>
