<template>
  <PageCard>
    <template #toolbar>
      <span />
      <el-button type="primary" @click="openDialog()">+ 鏂板鎴垮瀷</el-button>
    </template>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column type="index" label="搴忓彿" width="60" />
      <el-table-column label="鎴块棿鍥剧墖" width="90">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" style="width:48px;height:48px;border-radius:4px" fit="cover" />
          <div v-else class="room-thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="鎴块棿绫诲瀷" width="120" />
      <el-table-column prop="price" label="搴婁綅璐圭敤(鍏?鏈?" width="130">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="鍒涘缓浜? width="90" />
      <el-table-column prop="intro" label="鎴垮瀷浠嬬粛" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="鍒涘缓鏃堕棿" min-width="160" />
      <el-table-column prop="status" label="鐘舵€? width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '鍚敤' : '绂佺敤' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="鎿嶄綔" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.id)">鍒犻櫎</el-button>
          <el-button link type="primary" @click="openDialog(row)">缂栬緫</el-button>
          <el-button link :type="row.status === 1 ? 'danger' : 'primary'" @click="toggle(row.id)">
            {{ row.status === 1 ? '绂佺敤' : '鍚敤' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span>鍏?{{ tableData.length }} 椤规暟鎹?/span>
    </template>

    <el-dialog v-model="dialogVisible" :title="form.id ? '缂栬緫鎴垮瀷' : '鏂板鎴垮瀷'" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="鎴块棿绫诲瀷" required><el-input v-model="form.name" maxlength="10" show-word-limit /></el-form-item>
        <el-form-item label="搴婁綅璐圭敤" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="鎴垮瀷鍥剧墖">
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
            <template #tip><div style="font-size:12px;color:#999">鍥剧墖涓嶈秴杩?2M锛屼粎鏀寔 PNG/JPG</div></template>
          </el-upload>
        </el-form-item>
        <el-form-item label="鐘舵€?>
          <el-radio-group v-model="form.status">
            <el-radio :value="1">鍚敤</el-radio>
            <el-radio :value="0">绂佺敤</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="鎴垮瀷浠嬬粛"><el-input v-model="form.intro" type="textarea" maxlength="50" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">鍙栨秷</el-button>
        <el-button type="primary" @click="save">纭畾</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageCard from '@/components/PageCard.vue'

const tableData = ref([])
const dialogVisible = ref(false)
const loading = ref(false)
const fileList = ref([])
const uploadUrl = `${location.protocol}//${location.hostname}:8080/upload`
const defaultForm = () => ({ id: null, name: '', price: 800, intro: '\u623f\u95f4\u5185\u8bbe\u670924\u5c0f\u65f6cctv\u76d1\u63a7', image: '', creator: '\u987e\u5ef7\u70ec', status: 1 })
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
      ElMessage.error('鍔犺浇鎴垮瀷鍒楄〃澶辫触')
    })
    .finally(() => { loading.value = false })
}

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
    ElMessage.warning('璇疯緭鍏ユ埧闂寸被鍨?)
    return
  }
  axios.post('/roomType/save', form).then(() => {
    ElMessage.success('淇濆瓨鎴愬姛')
    dialogVisible.value = false
    loadList()
  })
}

function remove(id) {
  axios.get('/roomType/delete', { params: { id } }).then(() => { ElMessage.success('鍒犻櫎鎴愬姛'); loadList() })
}

function toggle(id) {
  axios.get('/roomType/toggle', { params: { id } }).then(() => { ElMessage.success('鎿嶄綔鎴愬姛'); loadList() })
}
</script>

<style scoped>
.room-thumb { width: 48px; height: 48px; background: #eee; border-radius: 4px; }
</style>
