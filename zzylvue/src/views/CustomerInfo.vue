<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="客户昵称">
          <el-input v-model="query.nickname" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="客户手机号">
          <el-input v-model="query.phone" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="nickname" label="客户昵称" width="100" />
      <el-table-column prop="phone" label="客户手机号" width="130" />
      <el-table-column prop="signed" label="是否签约" width="90">
        <template #default="{ row }">{{ row.signed ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="orderCount" label="服务下单次数(次)" width="140" />
      <el-table-column prop="elderNames" label="绑定老人姓名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="firstLoginTime" label="首次登录时间" min-width="160" />
    </el-table>

    <template #footer>
      <span>共 {{ total }} 项数据</span>
      <el-pagination
        background
        layout="sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        @change="loadList"
      />
    </template>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import PageCard from '@/components/PageCard.vue'

const query = reactive({ pageNum: 1, pageSize: 10, nickname: '', phone: '' })
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/customer/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  query.nickname = ''
  query.phone = ''
  loadList(1)
}
</script>
