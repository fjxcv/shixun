# -*- coding: utf-8 -*-
from pathlib import Path

ROOTS = [
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src"),
    Path(r"D:/vsc-maven/zzylvue/src"),
]


def write(rel, content):
    for base in ROOTS:
        if not base.exists():
            continue
        p = base / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(content, encoding="utf-8", newline="\n")
        print("wrote", p)


# labels
L = {
    "docNo": "\u5355\u636e\u7f16\u53f7",
    "elderName": "\u8001\u4eba\u59d3\u540d",
    "idcard": "\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7",
    "reset": "\u91cd\u7f6e",
    "search": "\u641c\u7d22",
    "all": "\u5168\u90e8",
    "pending": "\u5f85\u5ba1\u6279",
    "leaving": "\u8bf7\u5047\u4e2d",
    "overdue": "\u8d85\u65f6\u672a\u5f52",
    "returned": "\u5df2\u8fd4\u56de",
    "applyBtn": "\u53d1\u8d77\u8bf7\u5047\u7533\u8bf7",
    "idx": "\u5e8f\u53f7",
    "start": "\u8bf7\u5047\u5f00\u59cb\u65f6\u95f4",
    "expect": "\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4",
    "actual": "\u5b9e\u9645\u8fd4\u56de\u65f6\u95f4",
    "creator": "\u521b\u5efa\u4eba",
    "createTime": "\u521b\u5efa\u65f6\u95f4",
    "status": "\u8bf7\u5047\u72b6\u6001",
    "op": "\u64cd\u4f5c",
    "approve": "\u5ba1\u6279",
    "returnBtn": "\u9500\u5047",
    "view": "\u67e5\u770b",
    "total": "\u5171",
    "items": "\u6761\u6570\u636e",
    "dialogTitle": "\u53d1\u8d77\u8bf7\u5047\u7533\u8bf7",
    "reason": "\u8bf7\u5047\u539f\u56e0",
    "cancel": "\u53d6\u6d88",
    "submit": "\u63d0\u4ea4",
    "pleaseSelect": "\u8bf7\u9009\u62e9\u6216\u641c\u7d22\u8001\u4eba",
    "phone": "\u8054\u7cfb\u65b9\u5f0f",
    "bed": "\u5165\u4f4f\u5e8a\u4f4d",
    "nursing": "\u62a4\u7406\u7b49\u7ea7",
    "approveTitle": "\u8bf7\u5047\u5ba1\u6279",
    "pass": "\u901a\u8fc7",
    "reject": "\u62d2\u7edd",
    "confirm": "\u786e\u5b9a",
}

content = f"""<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="{L['docNo']}"><el-input v-model="query.docNo" clearable placeholder="{L['docNo']}" /></el-form-item>
        <el-form-item label="{L['elderName']}"><el-input v-model="query.elderName" clearable placeholder="{L['elderName']}" /></el-form-item>
        <el-form-item label="{L['idcard']}"><el-input v-model="query.elderIdcard" clearable placeholder="{L['idcard']}" /></el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">{L['reset']}</el-button>
          <el-button type="primary" @click="loadList(1)">{L['search']}</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <el-radio-group v-model="query.status" @change="loadList(1)">
        <el-radio-button label="{L['all']}" />
        <el-radio-button label="{L['pending']}" />
        <el-radio-button label="{L['leaving']}" />
        <el-radio-button label="{L['overdue']}" />
        <el-radio-button label="{L['returned']}" />
      </el-radio-group>
      <el-button type="primary" @click="openDialog()">{L['applyBtn']}</el-button>
    </template>

    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="{L['idx']}" width="60" />
      <el-table-column prop="docNo" label="{L['docNo']}" min-width="160" />
      <el-table-column prop="elderName" label="{L['elderName']}" width="100" />
      <el-table-column prop="elderIdcard" label="{L['idcard']}" min-width="170" />
      <el-table-column prop="startTime" label="{L['start']}" min-width="160" />
      <el-table-column prop="expectReturnTime" label="{L['expect']}" min-width="160" />
      <el-table-column prop="actualReturnTime" label="{L['actual']}" min-width="160">
        <template #default="{{ row }}">{{{{ row.actualReturnTime || '\u2014' }}}}</template>
      </el-table-column>
      <el-table-column prop="creator" label="{L['creator']}" width="90" />
      <el-table-column prop="createTime" label="{L['createTime']}" min-width="160" />
      <el-table-column prop="status" label="{L['status']}" width="100">
        <template #default="{{ row }}">
          <el-tag :type="leaveTag(row.status)" size="small">{{{{ row.status }}}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="{L['op']}" width="180" fixed="right">
        <template #default="{{ row }}">
          <el-button v-if="row.status === '{L['pending']}'" link type="warning" @click="openApprove(row)">{L['approve']}</el-button>
          <el-button v-if="canReturn(row.status)" link type="primary" @click="returnBack(row)">{L['returnBtn']}</el-button>
          <el-button link type="primary" @click="view(row)">{L['view']}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span>{L['total']} {{{{ total }}}} {L['items']}</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>

    <el-dialog v-model="dialogVisible" title="{L['dialogTitle']}" width="600px" destroy-on-close>
      <el-form :model="form" label-width="120px">
        <el-form-item label="{L['elderName']}" required>
          <el-select
            v-model="form.elderName"
            filterable
            clearable
            placeholder="{L['pleaseSelect']}"
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
        <el-form-item label="{L['idcard']}">
          <el-input v-model="form.elderIdcard" readonly placeholder="\u81ea\u52a8\u586b\u5199" />
        </el-form-item>
        <el-form-item label="{L['phone']}">
          <el-input v-model="form.elderPhone" readonly />
        </el-form-item>
        <el-form-item label="{L['bed']}">
          <el-input v-model="form.bedInfo" readonly />
        </el-form-item>
        <el-form-item label="{L['nursing']}">
          <el-input v-model="form.nursingLevel" readonly />
        </el-form-item>
        <el-form-item label="{L['start']}" required>
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="{L['expect']}" required>
          <el-date-picker v-model="form.expectReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="{L['reason']}" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">{L['cancel']}</el-button>
        <el-button type="primary" @click="save">{L['submit']}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="{L['approveTitle']}" width="420px" destroy-on-close>
      <p style="margin:0 0 12px">{{{{ approveRow?.elderName }}}} \u00b7 {{{{ approveRow?.docNo }}}}</p>
      <el-radio-group v-model="approvalResult">
        <el-radio value="{L['pass']}">{L['pass']}</el-radio>
        <el-radio value="{L['reject']}">{L['reject']}</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="approveVisible=false">{L['cancel']}</el-button>
        <el-button type="primary" @click="submitApprove">{L['confirm']}</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import {{ onMounted, reactive, ref }} from 'vue'
import {{ useRouter }} from 'vue-router'
import axios from 'axios'
import {{ ElMessage }} from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({{
  pageNum: 1,
  pageSize: 10,
  docNo: '',
  elderName: '',
  elderIdcard: '',
  status: '{L['all']}'
}})
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approveVisible = ref(false)
const approveRow = ref(null)
const approvalResult = ref('{L['pass']}')
const elderOptions = ref([])
const elderMap = reactive({{}})

const emptyForm = () => ({{
  elderName: '',
  elderIdcard: '',
  elderPhone: '',
  bedInfo: '',
  nursingLevel: '',
  startTime: '',
  expectReturnTime: '',
  reason: '',
  escort: '\u65e0',
  applicant: '',
  creator: ''
}})
const form = reactive(emptyForm())

onMounted(() => {{
  loadElders()
  loadList(1)
}})

function leaveTag(s) {{
  if (s === '{L['returned']}') return 'success'
  if (s === '{L['leaving']}') return 'warning'
  if (s === '{L['pending']}') return 'info'
  if (s === '\u5df2\u62d2\u7edd' || s === '\u5df2\u5173\u95ed') return 'danger'
  return 'danger'
}}

function canReturn(s) {{
  return s === '{L['leaving']}' || s === '{L['overdue']}'
}}

function loadElders() {{
  axios.post('/checkin/page', {{ pageNum: 1, pageSize: 200 }}).then(res => {{
    if (res.data.code !== 200) return
    const list = (res.data.data || []).filter(c => c.flowStatus === '\u5df2\u5b8c\u6210')
    const map = {{}}
    list.forEach(c => {{
      if (!c.elderName) return
      // prefer latest completed record per elder
      map[c.elderName] = {{
        elderName: c.elderName,
        elderIdcard: c.elderIdcard || '',
        elderPhone: c.elderPhone || '',
        bedInfo: c.bedNo || '',
        nursingLevel: c.nursingLevel || ''
      }}
    }})
    elderOptions.value = Object.values(map)
    Object.assign(elderMap, map)
  }}).catch(() => {{}})
}}

function onElderChange(name) {{
  const d = elderMap[name]
  if (!d) return
  form.elderIdcard = d.elderIdcard || ''
  form.elderPhone = d.elderPhone || ''
  form.bedInfo = d.bedInfo || ''
  form.nursingLevel = d.nursingLevel || ''
}}

function loadList(page) {{
  query.pageNum = page || query.pageNum
  axios.post('/leave/page', {{ ...query }}).then(res => {{
    if (res.data.code === 200) {{
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }} else {{
      ElMessage.error(res.data.msg || '\u52a0\u8f7d\u5931\u8d25')
    }}
  }}).catch(() => ElMessage.error('\u52a0\u8f7d\u5931\u8d25'))
}}

function resetQuery() {{
  query.docNo = ''
  query.elderName = ''
  query.elderIdcard = ''
  query.status = '{L['all']}'
  loadList(1)
}}

function openDialog() {{
  Object.assign(form, emptyForm())
  if (!elderOptions.value.length) loadElders()
  dialogVisible.value = true
}}

function save() {{
  if (!form.elderName || !form.elderIdcard) {{
    ElMessage.warning('\u8bf7\u9009\u62e9\u8001\u4eba')
    return
  }}
  if (!form.startTime || !form.expectReturnTime) {{
    ElMessage.warning('\u8bf7\u586b\u5199\u8bf7\u5047\u65f6\u95f4')
    return
  }}
  if (!form.reason) {{
    ElMessage.warning('\u8bf7\u586b\u5199\u8bf7\u5047\u539f\u56e0')
    return
  }}
  axios.post('/leave/save', {{ ...form }}).then(res => {{
    if (res.data.code === 200) {{
      ElMessage.success('\u63d0\u4ea4\u6210\u529f\uff0c\u5f85\u5ba1\u6279')
      dialogVisible.value = false
      query.status = '{L['pending']}'
      loadList(1)
    }} else {{
      ElMessage.error(res.data.msg || '\u63d0\u4ea4\u5931\u8d25')
    }}
  }}).catch(err => {{
    const msg = err.response?.data?.msg || err.message
    ElMessage.error(msg ? `\u63d0\u4ea4\u5931\u8d25\uff1a${{msg}}` : '\u63d0\u4ea4\u5931\u8d25')
  }})
}}

function returnBack(row) {{
  axios.post('/leave/return', {{ id: row.id, returnRemark: '\u8001\u4eba\u5df2\u8fd4\u56de' }}).then(res => {{
    if (res.data.code === 200) {{
      ElMessage.success('\u9500\u5047\u6210\u529f')
      loadList(query.pageNum)
    }} else ElMessage.error(res.data.msg || '\u9500\u5047\u5931\u8d25')
  }})
}}

function view(row) {{
  router.push({{ path: '/LeaveDetail', query: {{ id: row.id }} }})
}}

function openApprove(row) {{
  approveRow.value = row
  approvalResult.value = '{L['pass']}'
  approveVisible.value = true
}}

function submitApprove() {{
  if (!approveRow.value?.id) return
  axios.post('/leave/approve', {{
    id: approveRow.value.id,
    approvalResult: approvalResult.value
  }}).then(res => {{
    if (res.data.code === 200) {{
      ElMessage.success('\u5ba1\u6279\u6210\u529f')
      approveVisible.value = false
      loadList(query.pageNum)
    }} else ElMessage.error(res.data.msg || '\u5ba1\u6279\u5931\u8d25')
  }}).catch(() => ElMessage.error('\u5ba1\u6279\u5931\u8d25'))
}}
</script>
"""

# Fix accidental double braces for Vue mustache - the f-string used {{{{ which becomes {{
# But we need {{{{ for format... wait we used f-string with L[] and {{{{ for vue.
# In f-string: {{{{ -> {{ which is correct for Vue template output as {{
# And {L['x']} is substituted.
# For script: {{ becomes { - good for JS objects.

write("views/LeaveManage.vue", content)
print("LeaveManage OK")
