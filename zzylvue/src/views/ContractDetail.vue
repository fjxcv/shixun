<template>
  <PageCard>
    <h3 class="page-title">合同详情</h3>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="合同编号">{{ detail.contractNo }}</el-descriptions-item>
      <el-descriptions-item label="合同名称">{{ detail.contractName }}</el-descriptions-item>
      <el-descriptions-item label="老人姓名">{{ detail.elderName }}</el-descriptions-item>
      <el-descriptions-item label="老人身份证号">{{ detail.elderIdcard }}</el-descriptions-item>
      <el-descriptions-item label="入住单号">{{ detail.checkinNo }}</el-descriptions-item>
      <el-descriptions-item label="合同期限">{{ detail.startDate }} — {{ detail.endDate }}</el-descriptions-item>
      <el-descriptions-item label="合同状态">
        <el-tag :type="detail.status === '生效中' ? 'success' : 'warning'">{{ detail.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="签约日期">{{ detail.signDate }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.thirdPartyName" label="丙方姓名">{{ detail.thirdPartyName }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.thirdPartyPhone" label="丙方联系方式">{{ detail.thirdPartyPhone }}</el-descriptions-item>
      <el-descriptions-item label="合同文件">
        {{ detail.contractFile }} <el-button link type="primary">查看</el-button>
      </el-descriptions-item>
    </el-descriptions>

    <template v-if="detail.status === '已失效'">
      <el-divider />
      <h4>解除记录</h4>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交人">{{ detail.terminateUser }}</el-descriptions-item>
        <el-descriptions-item label="解除日期">{{ detail.terminateDate }}</el-descriptions-item>
        <el-descriptions-item label="解除协议">
          {{ detail.terminateFile }} <el-button link type="primary">查看</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </template>

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
  axios.get('/contract/detail', { params: { id: route.query.id } }).then(res => {
    if (res.data.code === 200) detail.value = res.data.data || {}
  }).catch(() => ElMessage.error('加载详情失败'))
})
</script>

<style scoped>
.page-title { margin: 0 0 20px; }
.form-actions { text-align: center; margin-top: 24px; }
</style>
