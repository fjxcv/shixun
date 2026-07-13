# -*- coding: utf-8 -*-
"""Rewrite LeaveDetail / MyTodo / MyApply as UTF-8 using only unicode escapes."""
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


# Chinese via unicode escapes only (avoid Windows editor re-encoding)
A = {
    "sqqj": "\u7533\u8bf7\u8bf7\u5047",
    "sqsp": "\u7533\u8bf7\u5ba1\u6279",
    "qjwc": "\u8bf7\u5047\u5b8c\u6210",
    "jbxx": "\u57fa\u672c\u4fe1\u606f",
    "djbh": "\u5355\u636e\u7f16\u53f7",
    "lrxm": "\u8001\u4eba\u59d3\u540d",
    "lrsfz": "\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7",
    "lxfs": "\u8054\u7cfb\u65b9\u5f0f",
    "hldj": "\u62a4\u7406\u7b49\u7ea7",
    "rzcw": "\u5165\u4f4f\u5e8a\u4f4d",
    "sqxx": "\u7533\u8bf7\u4fe1\u606f",
    "ptr": "\u966a\u540c\u4eba",
    "qjzq": "\u8bf7\u5047\u5468\u671f",
    "qjts": "\u8bf7\u5047\u5929\u6570",
    "tian": "\u5929",
    "qjyy": "\u8bf7\u5047\u539f\u56e0",
    "sqr": "\u7533\u8bf7\u4eba",
    "sqsj": "\u7533\u8bf7\u65f6\u95f4",
    "zt": "\u72b6\u6001",
    "yf": "\u5df2\u8fd4\u56de",
    "xjjl": "\u9500\u5047\u8bb0\u5f55",
    "sjsj": "\u5b9e\u9645\u8fd4\u56de\u65f6\u95f4",
    "bz": "\u5907\u6ce8",
    "xjr": "\u9500\u5047\u4eba",
    "xjsj": "\u9500\u5047\u65f6\u95f4",
    "dsp": "\u5f85\u5ba1\u6279",
    "tg": "\u901a\u8fc7",
    "jj": "\u62d2\u7edd",
    "tjsp": "\u63d0\u4ea4\u5ba1\u6279",
    "czjl": "\u64cd\u4f5c\u8bb0\u5f55",
    "fh": "\u8fd4\u56de",
    "qjz": "\u8bf7\u5047\u4e2d",
    "cswg": "\u8d85\u65f6\u672a\u5f52",
    "yjj": "\u5df2\u62d2\u7edd",
    "ygb": "\u5df2\u5173\u95ed",
    "jz": "\u5165\u4f4f",
    "tz": "\u9000\u4f4f",
    "qj": "\u8bf7\u5047",
    "sqz": "\u7533\u8bf7\u4e2d",
    "ywc": "\u5df2\u5b8c\u6210",
    "dcl": "\u5f85\u5904\u7406",
    "ycl": "\u5df2\u5904\u7406",
    "qb": "\u5168\u90e8",
    "op": "\u64cd\u4f5c",
    "ck": "\u67e5\u770b",
    "sp": "\u5ba1\u6279",
    "cl": "\u5904\u7406",
    "xh": "\u5e8f\u53f7",
    "djbt": "\u5355\u636e\u6807\u9898",
    "djl": "\u5355\u636e\u7c7b\u522b",
    "ddsc": "\u7b49\u5f85\u65f6\u957f",
    "wcsj": "\u5b8c\u6210\u65f6\u95f4",
    "lczt": "\u6d41\u7a0b\u72b6\u6001",
    "reset": "\u91cd\u7f6e",
    "search": "\u641c\u7d22",
    "qsr": "\u8bf7\u8f93\u5165",
    "qxz": "\u8bf7\u9009\u62e9",
    "gong": "\u5171",
    "tsj": "\u6761\u6570\u636e",
    "fqsq": "\u53d1\u8d77\u7533\u8bf7-\u7533\u8bf7\u8bf7\u5047",
    "sptg": "\u5ba1\u6279\u901a\u8fc7-\u7533\u8bf7\u5ba1\u6279",
    "spjj": "\u5ba1\u6279\u62d2\u7edd-\u7533\u8bf7\u5ba1\u6279",
    "xjwc": "\u9500\u5047\u5b8c\u6210",
    "jzxqsb": "\u52a0\u8f7d\u8be6\u60c5\u5931\u8d25",
    "qxspjg": "\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c",
    "spcg": "\u5ba1\u6279\u6210\u529f",
    "spsb": "\u5ba1\u6279\u5931\u8d25",
    "jzsjyc": "\u5355\u636e\u6570\u636e\u5f02\u5e38",
    "jzsbl": "\u52a0\u8f7d\u5931\u8d25",
}


leave_detail = f"""<template>
  <PageCard>
    <el-steps :active="stepsActive" align-center finish-status="success" style="margin-bottom: 24px">
      <el-step title="{A['sqqj']}" />
      <el-step title="{A['sqsp']}" />
      <el-step title="{A['qjwc']}" />
    </el-steps>

    <el-row :gutter="20">
      <el-col :span="16">
        <h4>{A['jbxx']}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="{A['djbh']}">{{{{ detail.docNo }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['lrxm']}">{{{{ detail.elderName }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['lrsfz']}">{{{{ detail.elderIdcard }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['lxfs']}">{{{{ detail.elderPhone }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['hldj']}">{{{{ detail.nursingLevel }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['rzcw']}">{{{{ detail.bedInfo }}}}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h4>{A['sqxx']}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="{A['ptr']}">{{{{ detail.escort }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['qjzq']}">{{{{ detail.startTime }}}} \u2014 {{{{ detail.expectReturnTime }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['qjts']}">{{{{ detail.leaveDays }}}}{A['tian']}</el-descriptions-item>
          <el-descriptions-item label="{A['qjyy']}">{{{{ detail.reason }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['sqr']}">{{{{ detail.applicant }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['sqsj']}">{{{{ detail.applyTime }}}}</el-descriptions-item>
          <el-descriptions-item label="{A['zt']}">{{{{ detail.status }}}}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detail.status === '{A['yf']}'">
          <el-divider />
          <h4>{A['xjjl']}</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="{A['zt']}">{{{{ detail.status }}}}</el-descriptions-item>
            <el-descriptions-item label="{A['sjsj']}">{{{{ detail.actualReturnTime }}}}</el-descriptions-item>
            <el-descriptions-item label="{A['bz']}">{{{{ detail.returnRemark }}}}</el-descriptions-item>
            <el-descriptions-item label="{A['xjr']}">{{{{ detail.cancelUser }}}}</el-descriptions-item>
            <el-descriptions-item label="{A['xjsj']}">{{{{ detail.cancelTime }}}}</el-descriptions-item>
          </el-descriptions>
        </template>

        <div v-if="detail.status === '{A['dsp']}'" class="step-panel">
          <h4>{A['sqsp']}</h4>
          <el-radio-group v-model="approvalResult">
            <el-radio value="{A['tg']}">{A['tg']}</el-radio>
            <el-radio value="{A['jj']}">{A['jj']}</el-radio>
          </el-radio-group>
          <div class="form-actions" style="margin-top:16px">
            <el-button type="primary" @click="submitApprove">{A['tjsp']}</el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="timeline-box">
          <h4>{A['czjl']}</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(item, idx) in timelineItems"
              :key="idx"
              :timestamp="item.time"
              :type="item.type"
            >{{{{ item.text }}}}</el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-row>

    <div class="form-actions">
      <el-button type="primary" @click="$router.back()">{A['fh']}</el-button>
    </div>
  </PageCard>
</template>

<script setup>
import {{ computed, onMounted, ref }} from 'vue'
import {{ useRoute }} from 'vue-router'
import axios from 'axios'
import {{ ElMessage }} from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const route = useRoute()
const detail = ref({{}})
const approvalResult = ref('{A['tg']}')

const stepsActive = computed(() => {{
  const s = detail.value.status
  if (s === '{A['dsp']}') return 1
  if (s === '{A['qjz']}' || s === '{A['cswg']}') return 2
  if (s === '{A['yf']}' || s === '{A['yjj']}' || s === '{A['ygb']}') return 3
  return 1
}})

const timelineItems = computed(() => {{
  const d = detail.value || {{}}
  const items = []
  if (d.applyTime) {{
    items.push({{
      time: d.applyTime,
      type: 'success',
      text: `{A['fqsq']} \\u00b7 ${{d.applicant || d.creator || ''}}`
    }})
  }}
  if (d.status && d.status !== '{A['dsp']}') {{
    const passed = d.status !== '{A['yjj']}' && d.status !== '{A['ygb']}'
    items.push({{
      time: d.applyTime || '',
      type: passed ? 'primary' : 'danger',
      text: passed
        ? `{A['sptg']} \\u00b7 ${{d.status}}`
        : `{A['spjj']} \\u00b7 ${{d.status}}`
    }})
  }}
  if (d.status === '{A['yf']}' && d.actualReturnTime) {{
    items.push({{
      time: d.actualReturnTime,
      type: 'success',
      text: `{A['xjwc']} \\u00b7 ${{d.cancelUser || ''}}`
    }})
  }}
  return items
}})

onMounted(loadDetail)

function loadDetail() {{
  if (!route.query.id) return
  axios.get('/leave/detail', {{ params: {{ id: route.query.id }} }}).then(res => {{
    if (res.data.code === 200) detail.value = res.data.data || {{}}
    else ElMessage.error(res.data.msg || '{A['jzxqsb']}')
  }}).catch(() => ElMessage.error('{A['jzxqsb']}'))
}}

function submitApprove() {{
  if (!approvalResult.value) {{
    ElMessage.warning('{A['qxspjg']}')
    return
  }}
  axios.post('/leave/approve', {{
    id: detail.value.id,
    approvalResult: approvalResult.value
  }}).then(res => {{
    if (res.data.code === 200) {{
      ElMessage.success('{A['spcg']}')
      loadDetail()
    }} else {{
      ElMessage.error(res.data.msg || '{A['spsb']}')
    }}
  }}).catch(() => ElMessage.error('{A['spsb']}'))
}}
</script>

<style scoped>
.timeline-box {{ background: #fafafa; padding: 16px; border-radius: 4px; }}
.form-actions {{ text-align: center; margin-top: 24px; }}
.step-panel {{ margin-top: 20px; }}
</style>
"""


my_todo = f"""<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="{A['djbh']}"><el-input v-model="query.docNo" clearable placeholder="{A['qsr']}" /></el-form-item>
        <el-form-item label="{A['sqr']}"><el-input v-model="query.applicant" clearable placeholder="{A['qsr']}" /></el-form-item>
        <el-form-item label="{A['djl']}">
          <el-select v-model="query.type" clearable placeholder="{A['qxz']}" style="width:120px">
            <el-option label="{A['jz']}" value="{A['jz']}" />
            <el-option label="{A['tz']}" value="{A['tz']}" />
            <el-option label="{A['qj']}" value="{A['qj']}" />
          </el-select>
        </el-form-item>
        <el-form-item label="{A['lczt']}">
          <el-select v-model="query.flowStatus" clearable placeholder="{A['qxz']}" style="width:120px">
            <el-option label="{A['sqz']}" value="{A['sqz']}" />
            <el-option label="{A['ywc']}" value="{A['ywc']}" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">{A['reset']}</el-button>
          <el-button type="primary" @click="loadList(1)">{A['search']}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #toolbar>
      <el-radio-group v-model="tabStatus" @change="loadList(1)">
        <el-radio-button label="{A['dcl']}" />
        <el-radio-button label="{A['ycl']}" />
      </el-radio-group>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="{A['xh']}" width="60" />
      <el-table-column prop="docNo" label="{A['djbh']}" min-width="160" />
      <el-table-column prop="title" label="{A['djbt']}" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="{A['djl']}" width="80" />
      <el-table-column prop="applicant" label="{A['sqr']}" width="90" />
      <el-table-column prop="applyTime" label="{A['sqsj']}" min-width="160" />
      <el-table-column v-if="tabStatus === '{A['dcl']}'" prop="waitDuration" label="{A['ddsc']}" width="120" />
      <el-table-column v-else prop="finishTime" label="{A['wcsj']}" min-width="160" />
      <el-table-column prop="flowStatus" label="{A['lczt']}" width="100">
        <template #default="{{ row }}">
          <el-tag :type="flowTag(row.flowStatus)" size="small">{{{{ row.flowStatus }}}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="{A['op']}" width="100" fixed="right">
        <template #default="{{ row }}">
          <el-button link type="primary" @click="process(row)">
            {{{{ tabStatus === '{A['dcl']}' ? (row.bizType === 'leave' ? '{A['sp']}' : '{A['cl']}') : '{A['ck']}' }}}}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <span>{A['gong']} {{{{ total }}}} {A['tsj']}</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>
  </PageCard>
</template>

<script setup>
import {{ onMounted, reactive, ref }} from 'vue'
import {{ useRouter }} from 'vue-router'
import axios from 'axios'
import {{ ElMessage }} from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const tabStatus = ref('{A['dcl']}')
const query = reactive({{ pageNum: 1, pageSize: 10, docNo: '', applicant: '', type: '', flowStatus: '', status: '{A['dcl']}' }})
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function flowTag(s) {{
  return s === '{A['ywc']}' ? 'success' : s === '{A['ygb']}' ? 'danger' : 'warning'
}}

function loadList(page) {{
  query.pageNum = page || query.pageNum
  query.status = tabStatus.value === '{A['ycl']}' ? '{A['ycl']}' : '{A['dcl']}'
  axios.post('/collab/todo/page', {{ ...query }}).then(res => {{
    if (res.data.code === 200) {{
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }}
  }}).catch(() => ElMessage.error('{A['jzsbl']}'))
}}

function resetQuery() {{
  Object.assign(query, {{ docNo: '', applicant: '', type: '', flowStatus: '' }})
  loadList(1)
}}

function process(row) {{
  if (!row.id) {{
    ElMessage.warning('{A['jzsjyc']}')
    return
  }}
  const pathMap = {{
    checkin: '/CheckinDetail',
    checkout: '/CheckoutDetail',
    leave: '/LeaveDetail'
  }}
  const path = pathMap[row.bizType] || '/CheckinDetail'
  const q = {{ id: row.id, step: row.step || 1 }}
  if (tabStatus.value === '{A['dcl']}') {{
    q.mode = 'form'
  }}
  router.push({{ path, query: q }})
}}
</script>
"""


my_apply = f"""<template>
  <PageCard>
    <template #filter>
      <el-form :inline="true" :model="query">
        <el-form-item label="{A['djbh']}"><el-input v-model="query.docNo" clearable placeholder="{A['qsr']}" /></el-form-item>
        <el-form-item label="{A['djl']}">
          <el-select v-model="query.type" clearable placeholder="{A['qxz']}" style="width:120px">
            <el-option label="{A['jz']}" value="{A['jz']}" />
            <el-option label="{A['tz']}" value="{A['tz']}" />
            <el-option label="{A['qj']}" value="{A['qj']}" />
          </el-select>
        </el-form-item>
        <el-form-item label="{A['lczt']}">
          <el-select v-model="query.status" clearable placeholder="{A['qxz']}" style="width:120px">
            <el-option label="{A['qb']}" value="{A['qb']}" />
            <el-option label="{A['sqz']}" value="{A['sqz']}" />
            <el-option label="{A['ywc']}" value="{A['ywc']}" />
            <el-option label="{A['ygb']}" value="{A['ygb']}" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">{A['reset']}</el-button>
          <el-button type="primary" @click="loadList(1)">{A['search']}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column type="index" label="{A['xh']}" width="60" />
      <el-table-column prop="docNo" label="{A['djbh']}" min-width="160" />
      <el-table-column prop="title" label="{A['djbt']}" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="{A['djl']}" width="80" />
      <el-table-column prop="applicant" label="{A['sqr']}" width="90" />
      <el-table-column prop="applyTime" label="{A['sqsj']}" min-width="160" />
      <el-table-column prop="finishTime" label="{A['wcsj']}" min-width="160" />
      <el-table-column prop="flowStatus" label="{A['lczt']}" width="100">
        <template #default="{{ row }}">
          <el-tag :type="flowTag(row.flowStatus)" size="small">{{{{ row.flowStatus }}}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="{A['op']}" width="80" fixed="right">
        <template #default="{{ row }}">
          <el-button link type="primary" @click="view(row)">{A['ck']}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <span>{A['gong']} {{{{ total }}}} {A['tsj']}</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @current-change="loadList" @size-change="loadList(1)" />
    </template>
  </PageCard>
</template>

<script setup>
import {{ onMounted, reactive, ref }} from 'vue'
import {{ useRouter }} from 'vue-router'
import axios from 'axios'
import {{ ElMessage }} from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const router = useRouter()
const query = reactive({{ pageNum: 1, pageSize: 10, docNo: '', type: '', status: '{A['qb']}' }})
const tableData = ref([])
const total = ref(0)

onMounted(() => loadList(1))

function flowTag(s) {{
  return s === '{A['ywc']}' ? 'success' : s === '{A['ygb']}' ? 'danger' : 'warning'
}}

function loadList(page) {{
  query.pageNum = page || query.pageNum
  axios.post('/collab/apply/page', {{ ...query }}).then(res => {{
    if (res.data.code === 200) {{
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    }}
  }}).catch(() => ElMessage.error('{A['jzsbl']}'))
}}

function resetQuery() {{
  Object.assign(query, {{ docNo: '', type: '', status: '{A['qb']}' }})
  loadList(1)
}}

function view(row) {{
  if (!row.id) {{
    ElMessage.warning('{A['jzsjyc']}')
    return
  }}
  const pathMap = {{
    checkin: '/CheckinDetail',
    checkout: '/CheckoutDetail',
    leave: '/LeaveDetail'
  }}
  router.push({{ path: pathMap[row.bizType] || '/CheckinDetail', query: {{ id: row.id }} }})
}}
</script>
"""

write("views/LeaveDetail.vue", leave_detail)
write("views/MyTodo.vue", my_todo)
write("views/MyApply.vue", my_apply)
print("OK")
