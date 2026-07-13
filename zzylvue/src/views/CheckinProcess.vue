<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="单据编号"><el-input v-model="query.docNo" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="老人姓名"><el-input v-model="query.elderName" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="老人身份证号"><el-input v-model="query.elderIdcard" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
            range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
            style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #toolbar>
      <span />
      <el-button type="primary" @click="$router.push('/CheckinApply')">发起入住申请</el-button>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="docNo" label="单据编号" min-width="160" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="elderIdcard" label="老人身份证号" min-width="170" />
      <el-table-column prop="bedNo" label="入住床位" width="100" />
      <el-table-column label="入住开始日期" width="130">
        <template #default="{ row }">{{ row.step >= 4 ? row.periodStart : '--' }}</template>
      </el-table-column>
      <el-table-column prop="checkinDate" label="入住日期" width="120" />
      <el-table-column prop="flowStatus" label="流程状态" width="100" />
      <el-table-column prop="creator" label="创建人" width="90" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="view(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <span>共 {{ total }} 项数据</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>
  </PageCard>
</template>

<script setup>
import { onMounted, onActivated, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', elderName: '', elderIdcard: '', startTime: '', endTime: '' })
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))
onActivated(() => loadList(1))

function loadList(page) {
  query.pageNum = page || query.pageNum
  query.startTime = dateRange.value?.[0] || ''
  query.endTime = dateRange.value?.[1] || ''
  axios.post('/checkin/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  Object.assign(query, { docNo: '', elderName: '', elderIdcard: '', startTime: '', endTime: '' })
  dateRange.value = []
  loadList(1)
}

function view(row) {
  router.push({ path: '/CheckinDetail', query: { id: row.id, step: row.step || 2, mode: 'form' } })
}
</script>

<style scoped>
.filter-form {
  text-align: left;
}
.filter-form :deep(.el-form-item) {
  margin-right: 16px;
}
</style>
