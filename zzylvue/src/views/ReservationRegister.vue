<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="预约人姓名">
          <el-input v-model="query.visitorName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="预约人手机号">
          <el-input v-model="query.visitorPhone" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="预约状态">
          <el-select v-model="query.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待上门" value="待上门" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
            <el-option label="已过期" value="已过期" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <el-radio-group v-model="query.type" @change="loadList(1)">
        <el-radio-button label="全部" />
        <el-radio-button label="参观预约" />
        <el-radio-button label="探访预约" />
      </el-radio-group>
      <el-button type="primary" @click="openDialog()">新增预约</el-button>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="type" label="预约类型" width="100" />
      <el-table-column prop="visitorName" label="预约人姓名" width="100" />
      <el-table-column prop="visitorPhone" label="预约人手机号" width="130" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="appointTime" label="预约时间" min-width="160" />
      <el-table-column prop="creator" label="创建人" width="90" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column prop="status" label="预约状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="!canArrive(row.status)"
            @click="openArriveDialog(row)"
          >到院</el-button>
          <el-button v-if="row.status === '待上门'" link type="danger" @click="cancel(row.id)">取消</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增预约" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="预约类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="参观预约" value="参观预约" />
            <el-option label="探访预约" value="探访预约" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约人姓名" required>
          <el-input v-model="form.visitorName" />
        </el-form-item>
        <el-form-item label="预约人手机号" required>
          <el-input v-model="form.visitorPhone" />
        </el-form-item>
        <el-form-item label="老人姓名">
          <el-input v-model="form.elderName" />
        </el-form-item>
        <el-form-item label="预约时间" required>
          <el-date-picker v-model="form.appointTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="arriveVisible" title="确认到院时间" width="480px">
      <el-form :model="arriveForm" label-width="90px">
        <el-form-item label="来访时间" required>
          <el-date-picker
            v-model="arriveForm.visitTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择来访时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arriveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmArrive">确定</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const query = reactive({ pageNum: 1, pageSize: 10, visitorName: '', visitorPhone: '', status: '', type: '全部' })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const arriveVisible = ref(false)
const form = reactive({ type: '参观预约', visitorName: '', visitorPhone: '', elderName: '', appointTime: '', creator: '顾廷烨' })
const arriveForm = reactive({ id: null, visitTime: '' })

onMounted(() => loadList(1))

function statusType(s) {
  if (s === '已完成') return 'success'
  if (s === '待上门') return 'warning'
  if (s === '已过期') return 'info'
  return 'danger'
}

function canArrive(status) {
  return status === '待上门' || status === '已过期'
}

function loadList(page) {
  query.pageNum = typeof page === 'number' ? page : query.pageNum
  axios.post('/reservation/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  }).catch(() => ElMessage.error('加载列表失败'))
}

function resetQuery() {
  query.visitorName = ''
  query.visitorPhone = ''
  query.status = ''
  loadList(1)
}

function openDialog() {
  Object.assign(form, { type: '参观预约', visitorName: '', visitorPhone: '', elderName: '', appointTime: '', creator: '顾廷烨' })
  dialogVisible.value = true
}

function save() {
  if (!form.visitorName || !form.visitorPhone || !form.appointTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  axios.post('/reservation/save', form).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadList(1)
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  }).catch(() => ElMessage.error('保存失败，请检查后端服务'))
}

function openArriveDialog(row) {
  if (!canArrive(row.status)) return
  arriveForm.id = row.id
  arriveForm.visitTime = ''
  arriveVisible.value = true
}

function confirmArrive() {
  if (!arriveForm.visitTime) {
    ElMessage.warning('请选择来访时间')
    return
  }
  axios.post('/reservation/arrive', { id: arriveForm.id, visitTime: arriveForm.visitTime }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('到院登记成功')
      arriveVisible.value = false
      loadList(query.pageNum)
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  }).catch(() => ElMessage.error('到院登记失败'))
}

function cancel(id) {
  if (id == null || id === '') {
    ElMessage.error('预约ID无效')
    return
  }
  ElMessageBox.confirm('确认取消该预约吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    return axios.get('/reservation/cancel', { params: { id } })
  }).then(res => {
    // 用户点了对话框「取消」时不会走到这里
    if (res.data && Number(res.data.code) === 200) {
      ElMessage.success('取消成功')
      loadList(query.pageNum)
    } else {
      ElMessage.error((res.data && res.data.msg) || '取消失败')
    }
  }).catch(err => {
    // Element Plus 关闭/取消确认框会 reject，不能当成接口失败
    const action = err && (err === 'cancel' || err === 'close' || err.action === 'cancel' || err.action === 'close')
    if (action || err === 'cancel' || err === 'close') return
    const msg = err?.response?.data?.msg || err?.message
    ElMessage.error(msg || '取消失败')
  })
}
</script>
