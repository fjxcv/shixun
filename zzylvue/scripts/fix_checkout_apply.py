# -*- coding: utf-8 -*-
from pathlib import Path

ROOTS = [Path(r"D:/vsc-maven/zzyl-project/zzylvue/src"), Path(r"D:/vsc-maven/zzylvue/src")]


def write(rel, content):
    for base in ROOTS:
        if not base.exists():
            continue
        p = base / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(content, encoding="utf-8", newline="\n")
        print("wrote", p)


content = """<template>
  <PageCard>
    <CheckoutSteps :active="0" />
    <h3 class="section-title">{title}</h3>
    <el-divider />
    <h4>{basic}</h4>
    <el-form :model="form" label-width="120px" class="apply-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="{elderName}" required>
            <el-select v-model="form.elderName" placeholder="{pleaseSelect}" style="width: 100%" @change="onElderChange">
              <el-option v-for="n in elderOptions" :key="n" :label="n" :value="n" />
            </el-select>
          </el-form-item>
          <el-form-item label="{phone}"><el-input v-model="form.elderPhone" readonly /></el-form-item>
          <el-form-item label="{nursing}"><el-input v-model="form.nursingLevel" readonly /></el-form-item>
          <el-form-item label="{contract}"><el-input v-model="form.contractName" readonly /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="{idcard}"><el-input v-model="form.elderIdcard" readonly /></el-form-item>
          <el-form-item label="{feePeriod}"><el-input :value="feePeriod" readonly /></el-form-item>
          <el-form-item label="{bed}"><el-input v-model="form.bedInfo" readonly /></el-form-item>
          <el-form-item label="{consultant}"><el-input v-model="form.consultant" readonly /></el-form-item>
        </el-col>
      </el-row>
      <el-divider />
      <h4>{applyInfo}</h4>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="{checkoutDate}" required>
            <el-date-picker v-model="form.checkoutDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="{reason}" required>
            <el-select v-model="form.reason" placeholder="{pleaseSelect}" style="width: 100%">
              <el-option label="{r1}" value="{r1}" />
              <el-option label="{r2}" value="{r2}" />
              <el-option label="{r3}" value="{r3}" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="{remark}">
            <el-input v-model="form.remark" type="textarea" maxlength="100" show-word-limit :rows="3" placeholder="{optional}" />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-actions">
        <el-button @click="$router.back()">{back}</el-button>
        <el-button type="primary" @click="submit">{submit}</el-button>
      </div>
    </el-form>
  </PageCard>
</template>

<script setup>
import {{ computed, onMounted, reactive, ref }} from 'vue'
import {{ useRouter }} from 'vue-router'
import axios from 'axios'
import {{ ElMessage }} from 'element-plus'
import PageCard from '@/components/PageCard.vue'
import CheckoutSteps from '@/components/CheckoutSteps.vue'

const router = useRouter()
const elderOptions = ref([])
const elderMap = reactive({{}})

const form = reactive({{
  elderName: '',
  elderIdcard: '',
  elderPhone: '',
  feeStart: '',
  feeEnd: '',
  nursingLevel: '',
  bedInfo: '',
  contractName: '',
  consultant: '',
  checkoutDate: '',
  reason: '',
  remark: '',
  applicant: '',
  creator: ''
}})

const feePeriod = computed(() => form.feeStart && form.feeEnd ? `${{form.feeStart}} ~ ${{form.feeEnd}}` : '')

onMounted(loadElders)

function loadElders() {{
  axios.post('/checkin/page', {{ pageNum: 1, pageSize: 100 }}).then(res => {{
    if (res.data.code === 200) {{
      const list = (res.data.data || []).filter(c => c.flowStatus === '{done}')
      elderOptions.value = [...new Set(list.map(c => c.elderName).filter(Boolean))]
      list.forEach(c => {{
        elderMap[c.elderName] = {{
          elderIdcard: c.elderIdcard || '',
          elderPhone: c.elderPhone || '',
          nursingLevel: c.nursingLevel || '',
          bedInfo: c.bedNo || '',
          contractName: c.contractName || '',
          consultant: c.applicant || c.creator || '',
          feeStart: c.periodStart || '',
          feeEnd: c.periodEnd || ''
        }}
      }})
      if (!elderOptions.value.length) {{
        ElMessage.warning('{noElder}')
      }}
    }}
  }}).catch(() => ElMessage.error('{loadFail}'))
}}

function onElderChange(name) {{
  const d = elderMap[name]
  if (d) Object.assign(form, d)
}}

function submit() {{
  if (!form.elderName || !form.checkoutDate || !form.reason) {{
    ElMessage.warning('{needFill}')
    return
  }}
  axios.post('/checkout/save', form).then(res => {{
    if (res.data.code === 200) {{
      ElMessage.success('{okMsg}')
      router.push('/CheckoutProcess')
    }} else {{
      ElMessage.error(res.data.msg || '{fail}')
    }}
  }}).catch(() => ElMessage.error('{fail}'))
}}
</script>

<style scoped>
.section-title {{ margin: 0 0 8px; }}
.apply-form {{ max-width: 1000px; }}
.form-actions {{ text-align: center; margin-top: 24px; }}
</style>
""".format(
    title="\u586b\u5199\u7533\u8bf7\u4fe1\u606f",
    basic="\u57fa\u672c\u4fe1\u606f",
    elderName="\u8001\u4eba\u59d3\u540d",
    pleaseSelect="\u8bf7\u9009\u62e9",
    phone="\u8054\u7cfb\u65b9\u5f0f",
    nursing="\u62a4\u7406\u7b49\u7ea7",
    contract="\u7b7e\u7ea6\u5408\u540c",
    idcard="\u8001\u4eba\u8eab\u4efd\u8bc1\u53f7",
    feePeriod="\u8d39\u7528\u671f\u9650",
    bed="\u5165\u4f4f\u5e8a\u4f4d",
    consultant="\u517b\u8001\u987e\u95ee",
    applyInfo="\u7533\u8bf7\u4fe1\u606f",
    checkoutDate="\u9000\u4f4f\u65e5\u671f",
    reason="\u9000\u4f4f\u539f\u56e0",
    r1="\u670d\u52a1\u4e0d\u5468",
    r2="\u8eab\u4f53\u539f\u56e0",
    r3="\u5176\u4ed6",
    remark="\u5907\u6ce8",
    optional="\u9009\u586b",
    back="\u8fd4\u56de",
    submit="\u63d0\u4ea4",
    done="\u5df2\u5b8c\u6210",
    noElder="\u6682\u65e0\u5df2\u5b8c\u6210\u5165\u4f4f\u7684\u8001\u4eba\uff0c\u8bf7\u5148\u5b8c\u6210\u5165\u4f4f\u6d41\u7a0b",
    loadFail="\u52a0\u8f7d\u8001\u4eba\u5217\u8868\u5931\u8d25",
    needFill="\u8bf7\u5b8c\u5584\u5fc5\u586b\u4fe1\u606f",
    okMsg="\u63d0\u4ea4\u6210\u529f\uff0c\u8bf7\u5728\u9000\u4f4f\u529e\u7406\u4e2d\u7ee7\u7eed\u5904\u7406",
    fail="\u63d0\u4ea4\u5931\u8d25",
)

write("views/CheckoutApply.vue", content)
print("ok")
