<template>
  <PageCard>
    <template #toolbar>
      <span />
      <el-button type="primary" @click="openDialog()">+ 新增房型</el-button>
    </template>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column label="房间图片" width="90">
        <template #default>
          <div class="room-thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="房间类型" width="120" />
      <el-table-column prop="price" label="床位费用(元/月)" width="130">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" width="90" />
      <el-table-column prop="intro" label="房型介绍" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'danger' : 'primary'" @click="toggle(row.id)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span>共 {{ tableData.length }} 项数据</span>
    </template>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房型' : '新增房型'" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="房间类型" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="床位费用" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="房型介绍"><el-input v-model="form.intro" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
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

const tableData = ref([])
const dialogVisible = ref(false)
const loading = ref(false)
const defaultForm = () => ({ id: null, name: '', price: 800, intro: '房间内设有24小时cctv监控', creator: '顾廷烬', status: 1 })
const form = reactive(defaultForm())

onMounted(() => loadList())

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function loadList() {
  loading.value = true
  axios.get('/roomType/list')
    .then(res => {
      tableData.value = Array.isArray(res.data?.data) ? res.data.data : []
    })
    .catch(() => {
      tableData.value = []
      ElMessage.error('加载房型列表失败')
    })
    .finally(() => { loading.value = false })
}

function openDialog(row) {
  Object.assign(form, defaultForm())
  if (row) {
    Object.assign(form, row)
    form.price = Number(row.price) || 0
  }
  dialogVisible.value = true
}

function save() {
  if (!form.name) {
    ElMessage.warning('请输入房间类型')
    return
  }
  axios.post('/roomType/save', form).then(() => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  })
}

function remove(id) {
  axios.get('/roomType/delete', { params: { id } }).then(() => { ElMessage.success('删除成功'); loadList() })
}

function toggle(id) {
  axios.get('/roomType/toggle', { params: { id } }).then(() => { ElMessage.success('操作成功'); loadList() })
}
</script>

<style scoped>
.room-thumb { width: 48px; height: 48px; background: #eee; border-radius: 4px; }
</style>
