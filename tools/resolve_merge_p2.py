# -*- coding: utf-8 -*-
"""Write RoomTypeSetting.vue (remote upload feature + Chinese comments)."""
from pathlib import Path

ROOT = Path(r"D:/vsc-maven/zzyl-project")

vue = """<!-- \u623f\u578b\u8bbe\u7f6e\uff1a\u5217\u8868\u3001\u65b0\u589e/\u7f16\u8f91\uff08\u542b\u56fe\u7247\u4e0a\u4f20\uff09\u3001\u542f\u7528/\u7981\u7528\u3001\u5220\u9664 -->
<template>
  <PageCard>
    <template #toolbar>
      <span />
      <el-button type="primary" @click="openDialog()">+ \u65b0\u589e\u623f\u578b</el-button>
    </template>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column type="index" label="\u5e8f\u53f7" width="60" />
      <el-table-column label="\u623f\u95f4\u56fe\u7247" width="90">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" style="width:48px;height:48px;border-radius:4px" fit="cover" />
          <div v-else class="room-thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="\u623f\u95f4\u7c7b\u578b" width="120" />
      <el-table-column prop="price" label="\u5e8a\u4f4d\u8d39\u7528(\u5143/\u6708)" width="130">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="\u521b\u5efa\u4eba" width="90" />
      <el-table-column prop="intro" label="\u623f\u578b\u4ecb\u7ecd" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="\u521b\u5efa\u65f6\u95f4" min-width="160" />
      <el-table-column prop="status" label="\u72b6\u6001" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '\u542f\u7528' : '\u7981\u7528' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="\u64cd\u4f5c" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.id)">\u5220\u9664</el-button>
          <el-button link type="primary" @click="openDialog(row)">\u7f16\u8f91</el-button>
          <el-button link :type="row.status === 1 ? 'danger' : 'primary'" @click="toggle(row.id)">
            {{ row.status === 1 ? '\u7981\u7528' : '\u542f\u7528' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span>\u5171 {{ tableData.length }} \u6761\u6570\u636e</span>
    </template>

    <el-dialog v-model="dialogVisible" :title="form.id ? '\u7f16\u8f91\u623f\u578b' : '\u65b0\u589e\u623f\u578b'" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="\u623f\u95f4\u7c7b\u578b" required><el-input v-model="form.name" maxlength="10" show-word-limit /></el-form-item>
        <el-form-item label="\u5e8a\u4f4d\u8d39\u7528" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="\u623f\u578b\u56fe\u7247">
          <el-upload
            :action="uploadUrl"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :limit="1"
            list-type="picture-card"
            :file-list="fileList"
            name="mf"
            accept=".png,.jpg,.jpeg"
          >
            <el-icon><Plus /></el-icon>
            <template #tip><div style="font-size:12px;color:#999">\u56fe\u7247\u4e0d\u8d85\u8fc7 2M\uff0c\u4ec5\u652f\u6301 PNG/JPG</div></template>
          </el-upload>
        </el-form-item>
        <el-form-item label="\u72b6\u6001">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">\u542f\u7528</el-radio>
            <el-radio :value="0">\u7981\u7528</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="\u623f\u578b\u4ecb\u7ecd"><el-input v-model="form.intro" type="textarea" maxlength="50" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">\u53d6\u6d88</el-button>
        <el-button type="primary" @click="save">\u786e\u5b9a</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
/**
 * \u623f\u578b\u8bbe\u7f6e\u9875\uff1a\u8c03\u7528 /roomType/* \u63a5\u53e3\uff1b\u56fe\u7247\u901a\u8fc7 /upload \u4e0a\u4f20\u540e\u5199\u5165 form.image\u3002
 */
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageCard from '@/components/PageCard.vue'

const tableData = ref([])
const dialogVisible = ref(false)
const loading = ref(false)
/** \u4e0a\u4f20\u7ec4\u4ef6\u5c55\u793a\u7528\u6587\u4ef6\u5217\u8868 */
const fileList = ref([])
/** \u76f4\u8fde\u540e\u7aef\u4e0a\u4f20\u5730\u5740\uff08\u4e0e\u5f53\u524d\u9875\u9762\u4e3b\u673a\u540c\u57df\uff0c\u7aef\u53e3 8080\uff09 */
const uploadUrl = `${location.protocol}//${location.hostname}:8080/upload`
const defaultForm = () => ({
  id: null,
  name: '',
  price: 800,
  intro: '\u623f\u95f4\u5185\u8bbe\u670924\u5c0f\u65f6cctv\u76d1\u63a7',
  image: '',
  creator: '\u987e\u5ef7\u70ec',
  status: 1
})
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
      ElMessage.error('\u52a0\u8f7d\u623f\u578b\u5217\u8868\u5931\u8d25')
    })
    .finally(() => { loading.value = false })
}

/** \u6253\u5f00\u65b0\u589e/\u7f16\u8f91\u5f39\u7a97\uff1b\u7f16\u8f91\u65f6\u56de\u586b\u5df2\u6709\u56fe\u7247 */
function openDialog(row) {
  Object.assign(form, defaultForm())
  fileList.value = []
  if (row) {
    Object.assign(form, row)
    form.price = Number(row.price) || 0
    if (row.image) fileList.value = [{ name: row.image, url: row.image }]
  }
  dialogVisible.value = true
}

/** \u4e0a\u4f20\u6210\u529f\uff1a\u540e\u7aef\u8fd4\u56de\u56fe\u7247 URL \u5199\u5165\u8868\u5355 */
function handleUploadSuccess(res, file) {
  if (res?.code === 200 && res.data) {
    form.image = res.data
    file.url = res.data
  }
}

function handleUploadRemove() {
  form.image = ''
}

function save() {
  if (!form.name) {
    ElMessage.warning('\u8bf7\u8f93\u5165\u623f\u95f4\u7c7b\u578b')
    return
  }
  axios.post('/roomType/save', form).then(() => {
    ElMessage.success('\u4fdd\u5b58\u6210\u529f')
    dialogVisible.value = false
    loadList()
  })
}

function remove(id) {
  axios.get('/roomType/delete', { params: { id } }).then(() => {
    ElMessage.success('\u5220\u9664\u6210\u529f')
    loadList()
  })
}

function toggle(id) {
  axios.get('/roomType/toggle', { params: { id } }).then(() => {
    ElMessage.success('\u64cd\u4f5c\u6210\u529f')
    loadList()
  })
}
</script>

<style scoped>
.room-thumb { width: 48px; height: 48px; background: #eee; border-radius: 4px; }
</style>
"""

p = ROOT / "zzylvue/src/views/RoomTypeSetting.vue"
p.write_text(vue, encoding="utf-8", newline="\n")
print("wrote", p)
# sync copy
p2 = Path(r"D:/vsc-maven/zzylvue/src/views/RoomTypeSetting.vue")
if p2.parent.exists():
    p2.write_text(vue, encoding="utf-8", newline="\n")
    print("synced", p2)
assert "fileList" in vue and "uploadUrl" in vue and "<<<<<<<" not in vue
print("RoomType OK")
