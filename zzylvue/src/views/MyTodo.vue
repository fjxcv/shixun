<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号"><el-input v-model="query.docNo" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="申请人"><el-input v-model="query.applicant" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="单据类别">
          <el-select v-model="query.type" clearable placeholder="请选择" style="width:120px">
            <el-option label="入住" value="入住" />
            <el-option label="退住" value="退住" />
            <el-option label="请假" value="请假" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程状态">
          <el-select v-model="query.flowStatus" clearable placeholder="请选择" style="width:120px">
            <el-option label="申请中" value="申请中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #toolbar>
      <el-radio-group v-model="tabStatus" @change="loadList(1)">
        <el-radio-button label="待处理" />
        <el-radio-button label="已处理" />
      </el-radio-group>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="docNo" label="单据编号" min-width="160" />
      <el-table-column prop="title" label="单据标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="单据类别" width="80" />
      <el-table-column prop="applicant" label="申请人" width="90" />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" />
      <el-table-column v-if="tabStatus === '待处理'" prop="waitDuration" label="等待时长" width="120" />
      <el-table-column v-else prop="finishTime" label="完成时间" min-width="160" />
      <el-table-column prop="flowStatus" label="流程状态" width="100">
        <template #default="{ row }">
          <el-tag :type="flowTag(row.flowStatus)" size="small">{{ row.flowStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="process(row)">
            {{ tabStatus === '待处理' ? (row.bizType === 'leave' ? '审批' : '处理') : '查看' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <span>共 {{ total }} 条数据</span>
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
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const tabStatus = ref('待处理')
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', applicant: '', type: '', flowStatus: '', status: '待处理' })
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function flowTag(s) {
  return s === '已完成' ? 'success' : s === '已关闭' ? 'danger' : 'warning'
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  query.status = tabStatus.value === '已处理' ? '已处理' : '待处理'
  axios.post('/collab/todo/page', { ...query }).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  }).catch(() => ElMessage.error('加载失败'))
}

function resetQuery() {
  Object.assign(query, { docNo: '', applicant: '', type: '', flowStatus: '' })
  loadList(1)
}

function process(row) {
  if (!row.id) {
    ElMessage.warning('单据数据异常')
    return
  }
  const pathMap = {
    checkin: '/CheckinDetail',
    checkout: '/CheckoutDetail',
    leave: '/LeaveDetail'
  }
  const path = pathMap[row.bizType] || '/CheckinDetail'
  const q = { id: row.id, step: row.step || 1 }
  if (tabStatus.value === '待处理') {
    q.mode = 'form'
  }
  router.push({ path, query: q })
}
</script>
