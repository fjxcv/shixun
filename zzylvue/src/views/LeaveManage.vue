<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号"><el-input v-model="query.docNo" clearable /></el-form-item>
        <el-form-item label="老人姓名"><el-input v-model="query.elderName" clearable /></el-form-item>
        <el-form-item label="老人身份证号"><el-input v-model="query.elderIdcard" clearable /></el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <el-radio-group v-model="query.status" @change="loadList(1)">
        <el-radio-button label="全部" />
        <el-radio-button label="请假中" />
        <el-radio-button label="超时未归" />
        <el-radio-button label="已返回" />
      </el-radio-group>
      <el-button type="primary" @click="openDialog()">发起请假申请</el-button>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="docNo" label="单据编号" min-width="160" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="elderIdcard" label="老人身份证号" min-width="170" />
      <el-table-column prop="startTime" label="请假开始时间" min-width="160" />
      <el-table-column prop="expectReturnTime" label="预计返回时间" min-width="160" />
      <el-table-column prop="actualReturnTime" label="实际返回时间" min-width="160">
        <template #default="{ row }">{{ row.actualReturnTime || '—' }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" width="90" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column prop="status" label="请假状态" width="100">
        <template #default="{ row }">
          <el-tag :type="leaveTag(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== '已返回'" link type="primary" @click="returnBack(row)">返回</el-button>
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

    <el-dialog v-model="dialogVisible" title="发起请假申请" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="老人姓名"><el-input v-model="form.elderName" /></el-form-item>
        <el-form-item label="老人身份证号"><el-input v-model="form.elderIdcard" /></el-form-item>
        <el-form-item label="请假开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计返回时间">
          <el-date-picker v-model="form.expectReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假原因"><el-input v-model="form.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">提交</el-button>
      </template>
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
const query = reactive({ pageNum: 1, pageSize: 10, docNo: '', elderName: '', elderIdcard: '', status: '全部' })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ elderName: '', elderIdcard: '', startTime: '', expectReturnTime: '', reason: '', applicant: '顾廷烨', creator: '顾廷烨' })

onMounted(() => loadList(1))

function leaveTag(s) {
  return s === '已返回' ? 'success' : s === '请假中' ? 'warning' : 'danger'
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/leave/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  })
}

function resetQuery() {
  query.docNo = ''
  query.elderName = ''
  query.elderIdcard = ''
  loadList(1)
}

function openDialog() {
  Object.assign(form, { elderName: '', elderIdcard: '', startTime: '', expectReturnTime: '', reason: '', applicant: '顾廷烨', creator: '顾廷烨' })
  dialogVisible.value = true
}

function save() {
  axios.post('/leave/save', form).then(() => {
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadList(1)
  }).catch(() => ElMessage.error('提交失败'))
}

function returnBack(row) {
  axios.post('/leave/return', { id: row.id, returnRemark: '老人已返回' }).then(() => {
    ElMessage.success('销假成功')
    loadList(query.pageNum)
  })
}

function view(row) {
  router.push({ path: '/LeaveDetail', query: { id: row.id } })
}
</script>
