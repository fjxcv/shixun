<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="单据编号"><el-input v-model="query.docNo" clearable placeholder="单据编号" /></el-form-item>
        <el-form-item label="老人姓名"><el-input v-model="query.elderName" clearable placeholder="老人姓名" /></el-form-item>
        <el-form-item label="老人身份证号"><el-input v-model="query.elderIdcard" clearable placeholder="老人身份证号" /></el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="loadList(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <el-radio-group v-model="query.status" @change="loadList(1)">
        <el-radio-button label="全部" />
        <el-radio-button label="待审批" />
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
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === '待审批'" link type="warning" @click="openApprove(row)">审批</el-button>
          <el-button v-if="canReturn(row.status)" link type="primary" @click="returnBack(row)">销假</el-button>
          <el-button link type="primary" @click="view(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span>共 {{ total }} 条数据</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>

    <el-dialog v-model="dialogVisible" title="发起请假申请" width="600px" destroy-on-close>
      <el-form :model="form" label-width="120px">
        <el-form-item label="老人姓名" required>
          <el-select
            v-model="form.elderName"
            filterable
            clearable
            placeholder="请选择或搜索老人"
            style="width:100%"
            @change="onElderChange"
          >
            <el-option
              v-for="e in elderOptions"
              :key="e.elderIdcard || e.elderName"
              :label="e.elderName + (e.elderIdcard ? ' (' + e.elderIdcard + ')' : '')"
              :value="e.elderName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="老人身份证号">
          <el-input v-model="form.elderIdcard" readonly placeholder="自动填写" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.elderPhone" readonly />
        </el-form-item>
        <el-form-item label="入住床位">
          <el-input v-model="form.bedInfo" readonly />
        </el-form-item>
        <el-form-item label="护理等级">
          <el-input v-model="form.nursingLevel" readonly />
        </el-form-item>
        <el-form-item label="请假开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="预计返回时间" required>
          <el-date-picker v-model="form.expectReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="请假原因" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="请假审批" width="420px" destroy-on-close>
      <p style="margin:0 0 12px">{{ approveRow?.elderName }} · {{ approveRow?.docNo }}</p>
      <el-radio-group v-model="approvalResult">
        <el-radio value="通过">通过</el-radio>
        <el-radio value="拒绝">拒绝</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="approveVisible=false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
/**
 * 请假管理列表：分页查询、新建申请入口、按业务 status（待审批/请假中等）筛选。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  docNo: '',
  elderName: '',
  elderIdcard: '',
  status: '全部'
})
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approveVisible = ref(false)
const approveRow = ref(null)
const approvalResult = ref('通过')
const elderOptions = ref([])
const elderMap = reactive({})

const emptyForm = () => ({
  elderName: '',
  elderIdcard: '',
  elderPhone: '',
  bedInfo: '',
  nursingLevel: '',
  startTime: '',
  expectReturnTime: '',
  reason: '',
  escort: '无',
  applicant: '',
  creator: ''
})
const form = reactive(emptyForm())

onMounted(() => {
  loadElders()
  loadList(1)
})

function leaveTag(s) {
  if (s === '已返回') return 'success'
  if (s === '请假中') return 'warning'
  if (s === '待审批') return 'info'
  if (s === '已拒绝' || s === '已关闭') return 'danger'
  return 'danger'
}

function canReturn(s) {
  return s === '请假中' || s === '超时未归'
}

function loadElders() {
  axios.post('/checkin/page', { pageNum: 1, pageSize: 200 }).then(res => {
    if (res.data.code !== 200) return
    const list = (res.data.data || []).filter(c => c.flowStatus === '已完成')
    const map = {}
    list.forEach(c => {
      if (!c.elderName) return
      // prefer latest completed record per elder
      map[c.elderName] = {
        elderName: c.elderName,
        elderIdcard: c.elderIdcard || '',
        elderPhone: c.elderPhone || '',
        bedInfo: c.bedNo || '',
        nursingLevel: c.nursingLevel || ''
      }
    })
    elderOptions.value = Object.values(map)
    Object.assign(elderMap, map)
  }).catch(() => {})
}

function onElderChange(name) {
  const d = elderMap[name]
  if (!d) return
  form.elderIdcard = d.elderIdcard || ''
  form.elderPhone = d.elderPhone || ''
  form.bedInfo = d.bedInfo || ''
  form.nursingLevel = d.nursingLevel || ''
}

function loadList(page) {
  query.pageNum = page || query.pageNum
  axios.post('/leave/page', { ...query }).then(res => {
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  }).catch(() => ElMessage.error('加载失败'))
}

function resetQuery() {
  query.docNo = ''
  query.elderName = ''
  query.elderIdcard = ''
  query.status = '全部'
  loadList(1)
}

function openDialog() {
  Object.assign(form, emptyForm())
  if (!elderOptions.value.length) loadElders()
  dialogVisible.value = true
}

async function save() {
  if (!form.elderName || !form.elderIdcard) {
    ElMessage.warning('请选择老人')
    return
  }
  if (!form.startTime || !form.expectReturnTime) {
    ElMessage.warning('请填写请假时间')
    return
  }
  if (!form.reason) {
    ElMessage.warning('请填写请假原因')
    return
  }
  try {
    const info = await axios.get('/loadInfo')
    const name = info.data?.realname || info.data?.uname || ''
    if (name) {
      form.applicant = name
      form.creator = name
    }
  } catch (e) { /* ignore */ }
  axios.post('/leave/save', { ...form }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('提交成功，待审批')
      dialogVisible.value = false
      query.status = '待审批'
      loadList(1)
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  }).catch(err => {
    const msg = err.response?.data?.msg || err.message
    ElMessage.error(msg ? `提交失败：${msg}` : '提交失败')
  })
}

function returnBack(row) {
  axios.post('/leave/return', { id: row.id, returnRemark: '老人已返回' }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('销假成功')
      loadList(query.pageNum)
    } else ElMessage.error(res.data.msg || '销假失败')
  })
}

function view(row) {
  router.push({ path: '/LeaveDetail', query: { id: row.id } })
}

function openApprove(row) {
  approveRow.value = row
  approvalResult.value = '通过'
  approveVisible.value = true
}

function submitApprove() {
  if (!approveRow.value?.id) return
  axios.post('/leave/approve', {
    id: approveRow.value.id,
    approvalResult: approvalResult.value
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('审批成功')
      approveVisible.value = false
      loadList(query.pageNum)
    } else ElMessage.error(res.data.msg || '审批失败')
  }).catch(() => ElMessage.error('审批失败'))
}
</script>
