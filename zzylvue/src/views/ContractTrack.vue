<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="合同编号">
          <el-input v-model="query.contractNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="老人姓名">
          <el-input v-model="query.elderName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="query.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="未生效" value="未生效" />
            <el-option label="生效中" value="生效中" />
            <el-option label="已失效" value="已失效" />
            <el-option label="已过期" value="已过期" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="contractNo" label="合同编号" min-width="160" />
      <el-table-column prop="contractName" label="合同名称" min-width="120" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="elderIdcard" label="老人身份证号" min-width="170" />
      <el-table-column label="合同期限" min-width="200">
        <template #default="{ row }">{{ row.startDate }} — {{ row.endDate }}</template>
      </el-table-column>
      <el-table-column prop="status" label="合同状态" width="100">
        <template #default="{ row }">
          <el-tag :type="contractTag(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
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
      <el-pagination
        background
        layout="sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        @current-change="loadList"
        @size-change="loadList(1)"
      />
    </template>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({ pageNum: 1, pageSize: 10, contractNo: '', elderName: '', status: '' })
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function contractTag(s) {
  const map = { '未生效': 'info', '生效中': 'success', '已失效': 'warning', '已过期': 'danger' }
  return map[s] || 'info'
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/contract/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  query.contractNo = ''
  query.elderName = ''
  query.status = ''
  loadList(1)
}

function view(row) {
  router.push({ path: '/ContractDetail', query: { id: row.id } })
}
</script>
