<template>
  <div>
    <h4>基本信息</h4>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="单据编号">{{ data.docNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="老人姓名">{{ data.elderName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="老人身份证号">{{ data.elderIdcard || '—' }}</el-descriptions-item>
      <el-descriptions-item label="联系方式">{{ data.elderPhone || '—' }}</el-descriptions-item>
      <el-descriptions-item label="费用期限">{{ feePeriod }}</el-descriptions-item>
      <el-descriptions-item label="护理等级">{{ data.nursingLevel || '—' }}</el-descriptions-item>
      <el-descriptions-item label="入住床位">{{ data.bedInfo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="签订合同">{{ data.contractName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="养老顾问">{{ data.consultant || '—' }}</el-descriptions-item>
    </el-descriptions>

    <el-divider />
    <h4>申请信息</h4>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="退住日期">{{ data.checkoutDate || '—' }}</el-descriptions-item>
      <el-descriptions-item label="退住原因">{{ data.reason || '—' }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ data.remark || '—' }}</el-descriptions-item>
      <el-descriptions-item label="申请人">{{ data.applicant || '—' }}</el-descriptions-item>
      <el-descriptions-item label="申请时间">{{ data.applyTime || '—' }}</el-descriptions-item>
    </el-descriptions>

    <template v-if="showTerminate">
      <el-divider />
      <h4>解除记录</h4>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交人">{{ data.consultant || data.applicant || '—' }}</el-descriptions-item>
        <el-descriptions-item label="解除日期">{{ data.terminateDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="解除协议">{{ data.terminateFile || '—' }}</el-descriptions-item>
      </el-descriptions>
    </template>

    <template v-if="showBill">
      <el-divider />
      <h4>账单明细</h4>
      <p>应退 {{ billReceivable }}元 · 欠费 {{ billArrears }}元 · 余额 {{ billBalance }}元</p>
      <p>最终退款金额 = 应退 - 欠费 + 余额 = {{ refundDisplay }}元</p>
    </template>

    <template v-if="showSettlement">
      <el-divider />
      <h4>完成账单清算</h4>
      <p>应退 {{ billReceivable }}元 · 欠费 {{ billArrears }}元 · 余额 {{ billBalance }}元</p>
      <p>退款信息：剩余预付款 {{ refundDisplay }} 元</p>
      <p>退款方式：现金 · 共退款 {{ refundDisplay }} 元，已结清</p>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Object, default: () => ({}) },
  showTerminate: Boolean,
  showBill: Boolean,
  showSettlement: Boolean
})

const feePeriod = computed(() => {
  const { feeStart, feeEnd } = props.data
  return feeStart && feeEnd ? `${feeStart} — ${feeEnd}` : '—'
})

function num(v) {
  if (v == null || v === '') return '—'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : String(v)
}

const billReceivable = computed(() => num(props.data.billReceivable))
const billArrears = computed(() => num(props.data.billArrears))
const billBalance = computed(() => num(props.data.billBalance))

const refundDisplay = computed(() => {
  if (props.data.refundAmount != null && props.data.refundAmount !== '') {
    return num(props.data.refundAmount)
  }
  const a = Number(props.data.billReceivable)
  const b = Number(props.data.billArrears)
  const c = Number(props.data.billBalance)
  if ([a, b, c].every(x => Number.isFinite(x))) {
    return (a - b + c).toFixed(2)
  }
  return '—'
})
</script>
