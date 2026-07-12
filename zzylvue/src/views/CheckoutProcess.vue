<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号"><el-input v-model="query.docNo" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="老人姓名"><el-input v-model="query.elderName" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="老人身份证号"><el-input v-model="query.elderIdcard" clearable placeholder="请输入" /></el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #toolbar>
      <span />
      <el-button type="primary" @click="$router.push('/CheckoutApply')">发起退住申请</el-button>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="docNo" label="单据编号" min-width="160" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="elderIdcard" label="老人身份证号" min-width="170" />
      <el-table-column prop="checkoutDate" label="退住日期" width="120" />
      <el-table-column prop="flowStatus" label="流程状态" width="100" />
      <el-table-column prop="stepStatus" label="步骤状态" width="100" />
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', elderName: '', elderIdcard: '' })
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/checkout/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  Object.assign(query, { docNo: '', elderName: '', elderIdcard: '' })
  loadList(1)
}

function view(row) {
  const s = row.step || 2
  router.push({ path: '/CheckoutDetail', query: { id: row.id, step: s } })
}
</script>
