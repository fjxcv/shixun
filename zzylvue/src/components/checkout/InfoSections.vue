<template>
  <div>
    <h4>基本信息</h4>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="单据编号">{{ data.docNo }}</el-descriptions-item>
      <el-descriptions-item label="老人姓名">{{ data.elderName }}</el-descriptions-item>
      <el-descriptions-item label="老人身份证号">{{ data.elderIdcard }}</el-descriptions-item>
      <el-descriptions-item label="联系方式">{{ data.elderPhone }}</el-descriptions-item>
      <el-descriptions-item label="费用期限">{{ data.feeStart }} — {{ data.feeEnd }}</el-descriptions-item>
      <el-descriptions-item label="护理等级">{{ data.nursingLevel }}</el-descriptions-item>
      <el-descriptions-item label="入住床位">{{ data.bedInfo }}</el-descriptions-item>
      <el-descriptions-item label="签约合同">
        {{ data.contractName }} <el-button link type="primary">查看</el-button>
      </el-descriptions-item>
      <el-descriptions-item label="养老顾问">{{ data.consultant }}</el-descriptions-item>
      <el-descriptions-item label="护理员">{{ data.caregivers }}</el-descriptions-item>
    </el-descriptions>

    <el-divider />
    <h4>申请信息</h4>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="退住日期">{{ data.checkoutDate }}</el-descriptions-item>
      <el-descriptions-item label="退住原因">{{ data.reason }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ data.remark }}</el-descriptions-item>
      <el-descriptions-item label="申请人">{{ data.applicant }}</el-descriptions-item>
      <el-descriptions-item label="申请时间">{{ data.applyTime }}</el-descriptions-item>
    </el-descriptions>

    <template v-if="showTerminate">
      <el-divider />
      <h4>解除记录</h4>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交人">{{ data.consultant }}</el-descriptions-item>
        <el-descriptions-item label="解除日期">{{ data.terminateDate || '2024-05-05' }}</el-descriptions-item>
        <el-descriptions-item label="解除协议">
          高启强解除协议.pdf <el-button link type="primary">查看</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </template>

    <template v-if="showBill">
      <el-divider />
      <h4>账单明细</h4>
      <p>应退 3820.00元 · 欠费 6000.00元 · 余额 4000.00元</p>
      <p>最终退款金额 = 应退 - 欠费 + 余额 = -380.00元</p>
    </template>

    <template v-if="showSettlement">
      <el-divider />
      <h4>完成账单清算</h4>
      <p>退款信息：剩余预付款 2000.00元</p>
      <p>退款方式：现金 · 共退款5520.00元，已结清</p>
    </template>
  </div>
</template>

<script setup>
defineProps({
  data: { type: Object, default: () => ({}) },
  showTerminate: Boolean,
  showBill: Boolean,
  showSettlement: Boolean
})
</script>
