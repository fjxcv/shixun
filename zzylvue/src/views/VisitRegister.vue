<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="来访人姓名">
          <el-input v-model="query.visitorName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="来访人手机号">
          <el-input v-model="query.visitorPhone" placeholder="请输入" clearable />
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
        <el-radio-button label="参观来访" />
        <el-radio-button label="探访来访" />
      </el-radio-group>
      <el-button type="primary" @click="openDialog()">来访登记</el-button>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="type" label="来访类型" width="100" />
      <el-table-column prop="visitorName" label="来访人姓名" width="100" />
      <el-table-column prop="visitorPhone" label="来访人手机号" width="130" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="visitTime" label="来访时间" min-width="160" />
      <el-table-column prop="creator" label="创建人" width="90" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
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

    <el-dialog v-model="dialogVisible" title="来访登记" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="来访类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="参观来访" value="参观来访" />
            <el-option label="探访来访" value="探访来访" />
          </el-select>
        </el-form-item>
        <el-form-item label="来访人姓名" required>
          <el-input v-model="form.visitorName" />
        </el-form-item>
        <el-form-item label="来访人手机号" required>
          <el-input v-model="form.visitorPhone" />
        </el-form-item>
        <el-form-item label="老人姓名">
          <el-input v-model="form.elderName" />
        </el-form-item>
        <el-form-item label="来访时间" required>
          <el-date-picker v-model="form.visitTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const query = reactive({ pageNum: 1, pageSize: 10, visitorName: '', visitorPhone: '', type: '全部' })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ type: '参观来访', visitorName: '', visitorPhone: '', elderName: '', visitTime: '', creator: '顾廷烨' })

onMounted(() => loadList(1))

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/visit/page', query).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }
  }).catch(() => ElMessage.error('加载失败'))
}

function resetQuery() {
  query.visitorName = ''
  query.visitorPhone = ''
  loadList(1)
}

function openDialog() {
  Object.assign(form, { type: '参观来访', visitorName: '', visitorPhone: '', elderName: '', visitTime: '', creator: '顾廷烨' })
  dialogVisible.value = true
}

function save() {
  if (!form.visitorName || !form.visitorPhone || !form.visitTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  axios.post('/visit/save', form).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadList(1)
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  }).catch(() => ElMessage.error('保存失败，请检查后端服务'))
}
</script>
