<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号"><el-input v-model="query.docNo" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="单据类别">
          <el-select v-model="query.type" clearable placeholder="请选择" style="width:120px">
            <el-option label="入住" value="入住" /><el-option label="退住" value="退住" /><el-option label="请假" value="请假" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #toolbar>
      <el-radio-group v-model="query.status" @change="loadList(1)">
        <el-radio-button label="全部" />
        <el-radio-button label="申请中" />
        <el-radio-button label="已完成" />
        <el-radio-button label="已关闭" />
      </el-radio-group>
      <el-button type="primary" @click="showApplyMenu = true">发起申请</el-button>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="docNo" label="单据单号" min-width="160" />
      <el-table-column prop="title" label="单据标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="单据类别" width="80" />
      <el-table-column prop="applicant" label="申请人" width="90" />
      <el-table-column prop="applyTime" label="申请时间" min-width="160" />
      <el-table-column prop="finishTime" label="完成时间" min-width="160" />
      <el-table-column prop="flowStatus" label="流程状态" width="100">
        <template #default="{ row }">
          <el-tag :type="flowTag(row.flowStatus)" size="small">{{ row.flowStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.flowStatus === '申请中'" link type="danger" @click="cancel(row)">撤销</el-button>
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

    <el-dialog v-model="showApplyMenu" title="发起申请" width="360px">
      <el-button type="primary" plain style="width:100%;margin-bottom:8px" @click="goApply('/CheckinApply')">入住申请</el-button>
      <el-button type="primary" plain style="width:100%;margin-bottom:8px" @click="goApply('/CheckoutApply')">退住申请</el-button>
      <el-button type="primary" plain style="width:100%" @click="goApply('/LeaveManage')">请假申请</el-button>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const showApplyMenu = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', type: '', status: '全部' })
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function flowTag(s) {
  return s === '已完成' ? 'success' : s === '已关闭' ? 'danger' : 'warning'
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/collab/apply/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  query.docNo = ''
  query.type = ''
  loadList(1)
}

function view(row) {
  const path = row.bizType === 'checkout' ? '/CheckoutDetail' : row.bizType === 'leave' ? '/LeaveDetail' : '/CheckinDetail'
  const step = row.step || 1
  // 仅审批节点用 pending；评估/配置等必须可编辑
  let mode = 'form'
  if (row.flowStatus === '申请中') {
    if (row.bizType === 'checkin' && step === 3) mode = 'pending'
    if (row.bizType === 'checkout' && [2, 5, 6].includes(step)) mode = 'pending'
  }
  router.push({ path, query: { id: row.id, step, mode } })
}

function cancel(row) {
  const url = row.bizType === 'checkin' ? '/checkin/cancel'
    : row.bizType === 'checkout' ? '/checkout/cancel' : null
  if (!url) { ElMessage.info('该类型暂不支持撤销'); return }
  axios.get(url, { params: { id: row.id } }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('已撤销')
      loadList(1)
    } else {
      ElMessage.error(res.data.msg || '撤销失败')
    }
  }).catch(() => ElMessage.error('撤销失败'))
}

function goApply(path) {
  showApplyMenu.value = false
  router.push(path)
}
</script>
