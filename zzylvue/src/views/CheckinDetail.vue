<template>
  <PageCard>
    <CheckinSteps :active="stepIndex" />
    <div v-if="readonlyView" class="detail-layout">
      <div class="detail-main">
        <InfoSectionsCheckin :data="detail" />
        <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
      </div>
      <OperationTimeline class="detail-side" :nodes="timelineNodes" />
    </div>
    <div v-else-if="isApprovalPending" class="status-panel">
      <el-result icon="info" title="审批中" sub-title="稍等，入住申请正在审批中，若该申请单长时间未处理，请联系审批角色！" />
      <OperationTimeline :nodes="timelineNodes" />
      <div class="form-actions"><el-button @click="goBack">返回</el-button></div>
    </div>
    <div v-else class="detail-layout">
      <div class="detail-main">
        <InfoSectionsCheckin v-if="step !== 2 && step !== 4 && step !== 5" :data="detail" />

        <div v-if="step === 2" class="assess-panel">
          <h4 class="assess-title">健康能力评估</h4>

          <el-tabs v-model="assessTab">
            <el-tab-pane label="健康评估" name="1">
              <el-divider content-position="left">疾病和用药情况</el-divider>
              <el-form label-width="140px" class="assess-form">
                <el-form-item label="疾病诊断" required>
                  <div class="checkbox-grid">
                    <el-checkbox v-for="d in diseaseOptions" :key="d" :label="d" :disabled="isNoneDisabled('diseases', d)"
                      :model-value="assess.diseases.includes(d)"
                      @change="(v) => onMultiWithNone('diseases', d, v)" />
                  </div>
                </el-form-item>
                <el-form-item label="用药情况">
                  <el-table :data="assess.medications" border class="med-table" table-layout="fixed">
                    <el-table-column label="药物名称">
                      <template #default="{ row }">
                        <el-input v-model="row.name" type="textarea" :rows="3" maxlength="20" show-word-limit placeholder="请输入" @input="(v) => row.name = stripEmoji(v)" />
                      </template>
                    </el-table-column>
                    <el-table-column label="服药方法">
                      <template #default="{ row }">
                        <el-input v-model="row.method" type="textarea" :rows="3" maxlength="20" show-word-limit placeholder="请输入" @input="(v) => row.method = stripEmoji(v)" />
                      </template>
                    </el-table-column>
                    <el-table-column label="用药剂量">
                      <template #default="{ row }">
                        <el-input v-model="row.dosage" type="textarea" :rows="3" maxlength="20" show-word-limit placeholder="请输入" @input="(v) => row.dosage = stripEmoji(v)" />
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="100">
                      <template #default="{ $index }">
                        <el-button link type="primary" @click="addMedication"><el-icon><Plus /></el-icon></el-button>
                        <el-button v-if="assess.medications.length > 1" link type="danger" @click="removeMedication($index)"><el-icon><Minus /></el-icon></el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-form-item>
              </el-form>

              <el-divider content-position="left">近三十天内风险事件</el-divider>
              <el-form label-width="180px" class="assess-form">
                <el-form-item v-for="r in riskItems" :key="r.key" :label="r.label" required>
                  <el-radio-group v-model="assess[r.key]">
                    <el-radio v-for="o in riskOptions" :key="o" :value="o">{{ o }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>

              <el-divider content-position="left">身体健康情况</el-divider>
              <el-form label-width="180px" class="assess-form">
                <el-form-item label="伤口情况" required>
                  <div class="checkbox-grid">
                    <el-checkbox v-for="w in woundOptions" :key="w" :label="w" :disabled="isNoneDisabled('wounds', w)"
                      :model-value="assess.wounds.includes(w)"
                      @change="(v) => onMultiWithNone('wounds', w, v)" />
                  </div>
                </el-form-item>
                <el-form-item label="特殊医疗照护情况" required>
                  <div class="checkbox-grid">
                    <el-checkbox v-for="s in specialCareOptions" :key="s" :label="s" :disabled="isNoneDisabled('specialCare', s)"
                      :model-value="assess.specialCare.includes(s)"
                      @change="(v) => onMultiWithNone('specialCare', s, v)" />
                  </div>
                </el-form-item>
                <el-form-item label="自理能力" required>
                  <el-radio-group v-model="assess.selfCare">
                    <el-radio v-for="o in selfCareOptions" :key="o" :value="o">{{ o }}</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="痴呆前兆" required>
                  <div class="checkbox-grid">
                    <el-checkbox v-for="d in dementiaOptions" :key="d" :label="d"
                      :model-value="assess.dementia.includes(d)"
                      @change="(v) => onDementiaCheck(d, v)" />
                  </div>
                </el-form-item>
                <el-form-item label="其他">
                  <el-input v-model="assess.otherNote" maxlength="20" show-word-limit placeholder="选填，20字符以内" @input="(v) => assess.otherNote = stripEmoji(v)" />
                </el-form-item>
              </el-form>

              <el-divider content-position="left">近期体检报告</el-divider>
              <el-form label-width="140px" class="assess-form">
                <el-form-item label="体检报告" required>
                  <el-upload action="#" :http-request="uploadReport" :show-file-list="false" :before-upload="beforeReport" accept=".pdf">
                    <el-button type="primary" plain>{{ assess.reportFile ? '重新上传' : '点击上传PDF文件' }}</el-button>
                  </el-upload>
                  <span v-if="assess.reportFile" class="file-ok">{{ assess.reportFile }}</span>
                  <div class="upload-hint">仅支持PDF文件，大小不超过60M</div>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="能力评估" name="2" :disabled="!assessTabsDone[1]">
              <div v-for="mod in abilityModules" :key="mod.name" class="ability-module">
                <h5 class="ability-mod-title">{{ mod.name }}</h5>
                <div v-for="q in mod.questions" :key="q.id" class="ability-card">
                  <table class="ability-table">
                    <tbody>
                    <tr>
                      <td class="at-title" colspan="2"><span class="q-required">*</span>{{ q.num }}.{{ q.title }}</td>
                      <td class="at-score">
                        得分：<span v-if="abilityScores[q.id] != null" class="score-val">{{ abilityScores[q.id] }}分</span><span v-else class="score-empty">-</span>
                      </td>
                    </tr>
                    <tr>
                      <td colspan="3" class="at-opt-cell">
                        <el-radio-group v-model="abilityScores[q.id]">
                          <el-radio v-for="o in q.options" :key="o.label" :value="o.score" class="at-radio">
                            <span class="opt-label">{{ o.label }}</span>
                            <span class="opt-desc">{{ o.desc }}</span>
                          </el-radio>
                        </el-radio-group>
                      </td>
                    </tr>
                                      </tbody>
                  </table>
                </div>
              </div>
              <div class="ability-footer">
                <span>总分：</span>
                <span v-if="abilityTotal != null" class="score-total">{{ abilityTotal }} / 50 分</span>
                <span v-else class="score-empty">--</span>
              </div>
            </el-tab-pane>

            <el-tab-pane label="评估报告" name="3" :disabled="!assessTabsDone[2]">

              <div class="report-section-head">
                <span class="report-section-title">能力评估结果</span>
                <el-tooltip placement="right" effect="light" raw-content>
                  <template #content>
                    <div style="max-width:260px;font-size:12px;line-height:1.6">
                      根据十道能力评估题目得分汇总：<br/>
                      自理能力（3题/15分）+ 精神状态（3题/15分）+ 感知觉与社会参与（4题/20分）= 评估总分（50分）<br/>
                      <b>能力初步等级划分：</b><br/>
                      10分 → 能力完好<br/>
                      11-20分 → 轻度失能<br/>
                      21-30分 → 中度失能<br/>
                      31-40分 → 中重度失能<br/>
                      41-50分 → 重度失能
                    </div>
                  </template>
                  <el-icon class="tip-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
              <table class="report-table">
                <thead>
                  <tr><th>指标</th><th>得分</th></tr>
                </thead>
                <tbody>
                  <tr><td>自理能力</td><td class="report-score">{{ selfCareScore }} / 15 分</td></tr>
                  <tr><td>精神状态</td><td class="report-score">{{ mentalScore }} / 15 分</td></tr>
                  <tr><td>感知觉与社会参与</td><td class="report-score">{{ socialScore }} / 20 分</td></tr>
                  <tr class="report-total-row"><td>评估总分</td><td class="report-score">{{ abilityTotal }} / 50 分</td></tr>
                  <tr><td>能力初步等级</td><td class="report-score">{{ abilityLevel }}</td></tr>
                </tbody>
              </table>

              <div class="report-section-head">
                <span class="report-section-title">护理等级结果</span>
                <el-tooltip placement="right" effect="light" raw-content>
                  <template #content>
                    <div style="max-width:300px;font-size:12px;line-height:1.6">
                      依据健康评估信息表中的疾病和用药情况、近30天风险事件、身体健康情况、近期体检报告，确定是否存在导致能力等级变更的项目。若有以下情况之一，在原有能力级别<b>提高一个级别</b>：<br/>
                      1. 确诊为认知障碍/痴呆<br/>
                      2. 精神科专科医生诊断的精神类疾病<br/>
                      3. 近30天内发生过2次及以上照护风险事件（如跌倒、自杀、走失等）
                    </div>
                  </template>
                  <el-icon class="tip-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
              <el-form label-width="180px" class="assess-form">
                <el-form-item label="能力等级变更依据" required>
                  <div class="checkbox-grid">
                    <el-checkbox v-for="r in upgradeReasons" :key="r" :label="r"
                      :model-value="assess.upgradeReasons.includes(r)"
                      @change="(v) => onUpgradeReasonChange(r, v)" />
                  </div>
                  <el-input v-if="assess.upgradeReasons.includes('其他')" v-model="assess.upgradeOtherNote"
                    type="textarea" :rows="3" maxlength="100" show-word-limit placeholder="请填写其他原因（必填，限100字符）"
                    @input="(v) => assess.upgradeOtherNote = stripEmoji(v)"
                    style="margin-top:8px" />
                </el-form-item>
                <el-form-item label="能力最终等级" required>
                  <el-radio-group v-model="assess.finalLevel">
                    <el-radio v-for="l in finalLevelOptions" :key="l" :value="l">{{ l }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>

              <div class="form-actions">
                <el-button @click="goBack">返回</el-button>
                <el-button type="primary" @click="confirmSubmit">保存</el-button>
              </div>
            </el-tab-pane>
          </el-tabs>

          <div v-if="assessTab !== '3'" class="form-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="saveAssessTab">保存</el-button>
          </div>
        </div>

        <div v-if="step === 3">
          <h4>审批结果</h4>
          <el-radio-group v-model="approvalResult">
            <el-radio value="审批通过">审批通过</el-radio>
            <el-radio value="审批拒绝">审批拒绝</el-radio>
            <el-radio value="驳回">驳回</el-radio>
          </el-radio-group>
          <el-input v-model="approvalComment" type="textarea" maxlength="200" show-word-limit placeholder="审批意见" style="margin-top:12px" />
        </div>
        <div v-if="step === 4" class="config-page">
          <!-- 入住设置 -->
          <div class="config-section">
            <h4 class="config-section-title">入住设置</h4>
            <el-form label-width="120px" class="config-form">
              <el-form-item label="入住期限" required>
                <el-date-picker v-model="periodRange" type="daterange" value-format="YYYY-MM-DD"
                  :disabled-date="disablePastDate" style="width:260px" />
              </el-form-item>
              <el-form-item label="护理等级" required>
                <el-select v-model="selectedNursingLevelName" placeholder="请选择护理等级" style="width:260px" @change="onNursingLevelChange">
                  <el-option v-for="nl in enabledNursingLevels" :key="nl.id" :label="nl.levelname" :value="nl.levelname" />
                </el-select>
              </el-form-item>
              <el-form-item label="入住床位">
                <template v-if="selectedBed">
                  <span class="bed-info">{{ selectedBed.floorName }} {{ selectedBed.roomNo }} - {{ selectedBed.bedNo }}</span>
                  <el-button link type="primary" style="margin-left:12px" @click="openBedDialog">重新选择</el-button>
                </template>
                <el-button v-else type="primary" plain @click="openBedDialog">选择入住床位</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 费用设置 -->
          <div class="config-section">
            <h4 class="config-section-title">费用设置</h4>
            <el-form label-width="140px" class="config-form">
              <el-form-item label="费用期限" required>
                <el-date-picker v-model="feePeriodRange" type="daterange" value-format="YYYY-MM-DD"
                  :disabled-date="disablePastDate" style="width:260px" />
              </el-form-item>
              <el-form-item label="押金（元）">
                <el-input-number v-model="detail.deposit" :min="0" :precision="2" style="width:200px" />
              </el-form-item>
              <el-form-item label="护理费用（元/月）" required>
                <el-input-number v-model="detail.nursingFee" :min="0" :precision="2" style="width:200px"
                  :disabled="!selectedNursingLevelName" @change="validateNursingFee" />
                <el-button link type="primary" class="plus-btn" @click="addFeeItem('nursing')"><el-icon><Plus /></el-icon></el-button>
              </el-form-item>
              <el-form-item label="床位费用（元/月）" required>
                <el-input-number v-model="detail.bedFee" :min="0" :precision="2" style="width:200px"
                  :disabled="!selectedBed" @change="validateBedFee" />
                <el-button link type="primary" class="plus-btn" @click="addFeeItem('bed')"><el-icon><Plus /></el-icon></el-button>
              </el-form-item>
              <el-form-item label="其他费用（元/月）">
                <el-input-number v-model="detail.otherFee" :min="0" :precision="2" style="width:200px" />
                <el-button link type="primary" class="plus-btn" @click="addFeeItem('other')"><el-icon><Plus /></el-icon></el-button>
                <div v-if="otherFeeItems.length" class="fee-extra-items">
                  <div v-for="(item, i) in otherFeeItems" :key="'other'+i" class="fee-extra-row">
                    <el-input v-model="item.desc" placeholder="费用说明" style="width:160px" />
                    <el-input-number v-model="item.amount" :min="0" :precision="2" style="width:140px;margin-left:8px" />
                    <el-button link type="danger" @click="otherFeeItems.splice(i,1)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="医保支付（元/月）">
                <el-input-number v-model="detail.insurancePay" :min="0" :precision="2" style="width:200px" />
                <el-button link type="primary" class="plus-btn" @click="addFeeItem('insurance')"><el-icon><Plus /></el-icon></el-button>
                <div v-if="insurancePayItems.length" class="fee-extra-items">
                  <div v-for="(item, i) in insurancePayItems" :key="'ins'+i" class="fee-extra-row">
                    <el-input v-model="item.desc" placeholder="费用说明" style="width:160px" />
                    <el-input-number v-model="item.amount" :min="0" :precision="2" style="width:140px;margin-left:8px" />
                    <el-button link type="danger" @click="insurancePayItems.splice(i,1)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="政府补贴（元/月）">
                <el-input-number v-model="detail.govSubsidy" :min="0" :precision="2" style="width:200px" />
                <el-button link type="primary" class="plus-btn" @click="addFeeItem('gov')"><el-icon><Plus /></el-icon></el-button>
                <div v-if="govSubsidyItems.length" class="fee-extra-items">
                  <div v-for="(item, i) in govSubsidyItems" :key="'gov'+i" class="fee-extra-row">
                    <el-input v-model="item.desc" placeholder="费用说明" style="width:160px" />
                    <el-input-number v-model="item.amount" :min="0" :precision="2" style="width:140px;margin-left:8px" />
                    <el-button link type="danger" @click="govSubsidyItems.splice(i,1)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </div>

          <!-- 账单预览按钮 -->
          <div class="bill-preview-bar">
            <el-button type="warning" @click="showBillPreview">账单预览</el-button>
          </div>

          <!-- 床位选择弹窗 -->
          <el-dialog v-model="bedDialogVisible" title="选择入住床位" width="900px" top="5vh">
            <div class="bed-select-dialog">
              <div class="bed-select-toolbar">
                <el-radio-group v-if="bedFloors.length" v-model="bedActiveFloorId" @change="loadBedRooms">
                  <el-radio-button v-for="f in bedFloors" :key="f.id" :value="f.id">{{ f.name }}</el-radio-button>
                </el-radio-group>
                <div class="bed-select-legend">
                  <span><i class="dot idle" />空闲</span>
                  <span><i class="dot occupied" />已入住</span>
                  <span><i class="dot leave" />请假中</span>
                </div>
              </div>
              <el-empty v-if="!bedFloors.length" description="暂无楼层数据" />
              <el-row v-else :gutter="16">
                <el-col v-for="item in bedRoomList" :key="item.room.id" :span="8" style="margin-bottom:12px">
                  <el-card class="bed-select-room-card" shadow="hover">
                    <div class="bed-select-room-header">
                      <span>{{ item.room.roomNo }}</span>
                      <span class="room-type-text">{{ item.room.roomTypeName }}</span>
                    </div>
                    <div v-if="item.beds.length" class="bed-grid">
                      <div v-for="bed in item.beds" :key="bed.id"
                        :class="['bed-item', bed.status === '空闲' ? 'idle' : (bed.status === '已入住' ? 'occupied' : 'leave'),
                                bed.id === tempSelectedBedId ? 'selected' : '',
                                bed.status !== '空闲' ? 'disabled' : '']"
                        @click="selectBed(bed, item.room)">
                        <div class="bed-no">床位号：{{ bed.bedNo }}</div>
                        <div class="bed-elder">{{ bed.status === '空闲' ? '空闲' : (bed.elderName || '--') }}</div>
                      </div>
                    </div>
                    <el-empty v-else description="暂无床位" :image-size="40" />
                  </el-card>
                </el-col>
              </el-row>
            </div>
            <template #footer>
              <el-button @click="bedDialogVisible = false">返回</el-button>
              <el-button type="primary" @click="confirmBedSelection">保存</el-button>
            </template>
          </el-dialog>

          <!-- 账单预览弹窗 -->
          <el-dialog v-model="billDialogVisible" title="账单预览" width="700px" top="5vh">
            <div v-if="billData" class="bill-preview">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="账单月份">{{ billData.month }}</el-descriptions-item>
                <el-descriptions-item label="老人姓名">{{ detail.elderName }}</el-descriptions-item>
                <el-descriptions-item label="账单周期" :span="2">{{ billData.period }}</el-descriptions-item>
                <el-descriptions-item label="共计天数" :span="2">{{ billData.totalDays }} 天</el-descriptions-item>
              </el-descriptions>

              <el-divider content-position="left">添加项</el-divider>
              <table class="bill-table">
                <thead><tr><th>项目</th><th>服务内容</th><th style="text-align:right">金额（元）</th></tr></thead>
                <tbody>
                  <tr v-if="detail.nursingFee">
                    <td>护理费用</td><td>{{ detail.nursingLevel || '--' }}</td>
                    <td class="bill-amount">{{ detail.nursingFee }}</td>
                  </tr>
                  <tr v-if="detail.bedFee">
                    <td>床位费用</td><td>{{ detail.bedNo || '--' }}</td>
                    <td class="bill-amount">{{ detail.bedFee }}</td>
                  </tr>
                  <template v-if="otherFeeBillRows > 0">
                    <tr v-if="detail.otherFee">
                      <td :rowspan="otherFeeBillRows">其他费用</td><td>--</td>
                      <td class="bill-amount">{{ detail.otherFee }}</td>
                    </tr>
                    <tr v-for="(item, i) in otherFeeItems" :key="'bill_other'+i">
                      <td v-if="!detail.otherFee && i === 0" :rowspan="otherFeeBillRows">其他费用</td>
                      <td>{{ item.desc || '--' }}</td>
                      <td class="bill-amount">{{ item.amount }}</td>
                    </tr>
                  </template>
                  <tr class="bill-subtotal"><td colspan="2">小计</td><td class="bill-amount">{{ billData.addSubtotal }}</td></tr>
                </tbody>
              </table>

              <el-divider content-position="left">扣减项</el-divider>
              <table class="bill-table">
                <thead><tr><th>项目</th><th style="text-align:right">金额（元）</th></tr></thead>
                <tbody>
                  <template v-if="insurancePayBillRows > 0">
                    <tr v-if="detail.insurancePay">
                      <td :rowspan="insurancePayBillRows">医保支付</td>
                      <td class="bill-amount">{{ detail.insurancePay }}</td>
                    </tr>
                    <tr v-for="(item, i) in insurancePayItems" :key="'bill_ins'+i">
                      <td v-if="!detail.insurancePay && i === 0" :rowspan="insurancePayBillRows">医保支付</td>
                      <td class="bill-amount">{{ item.amount }}</td>
                    </tr>
                  </template>
                  <template v-if="govSubsidyBillRows > 0">
                    <tr v-if="detail.govSubsidy">
                      <td :rowspan="govSubsidyBillRows">政府补贴</td>
                      <td class="bill-amount">{{ detail.govSubsidy }}</td>
                    </tr>
                    <tr v-for="(item, i) in govSubsidyItems" :key="'bill_gov'+i">
                      <td v-if="!detail.govSubsidy && i === 0" :rowspan="govSubsidyBillRows">政府补贴</td>
                      <td class="bill-amount">{{ item.amount }}</td>
                    </tr>
                  </template>
                  <tr class="bill-subtotal"><td>小计</td><td class="bill-amount">{{ billData.deductSubtotal }}</td></tr>
                </tbody>
              </table>

              <el-divider />
              <el-descriptions :column="1" border size="small" class="bill-summary">
                <el-descriptions-item label="每月应付">{{ billData.monthlyPayable }} 元</el-descriptions-item>
                <el-descriptions-item label="本期应付">{{ billData.monthlyPayable }} 元</el-descriptions-item>
                <el-descriptions-item label="押金">{{ detail.deposit || 0 }} 元</el-descriptions-item>
                <el-descriptions-item label="账单金额">{{ billData.totalBill }} 元</el-descriptions-item>
              </el-descriptions>
            </div>
            <template #footer>
              <el-button @click="billDialogVisible = false">关闭</el-button>
            </template>
          </el-dialog>
        </div>
        <div v-if="step === 5" class="config-page">
          <div class="config-section">
            <div class="config-section-head">
              <h4 class="config-section-title">签约信息</h4>
              <el-tooltip placement="right" effect="light">
                <template #content>
                  <div style="max-width:280px;font-size:12px;line-height:1.6">
                    完成合同签约后，系统将会生成首期账单，生成账单后入住配置和签约办理信息将无法修改，若修改信息需办理退住流程
                  </div>
                </template>
                <el-icon class="tip-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
            <el-form label-width="140px" class="config-form">
              <el-form-item label="合同编号">
                <el-input v-model="detail.contractNo" disabled />
              </el-form-item>
              <el-form-item label="合同名称" required :error="contractNameError">
                <el-input v-model="detail.contractName" maxlength="20" show-word-limit
                  placeholder="请输入合同名称" @input="onContractNameInput" />
              </el-form-item>
              <el-form-item label="签约日期" required :error="signDateError">
                <el-date-picker v-model="detail.signDate" type="date" value-format="YYYY-MM-DD"
                  placeholder="请选择签约日期" style="width:260px" />
              </el-form-item>
              <el-form-item label="合同期限">
                <span class="form-text">{{ periodRange[0] || '--' }} 至 {{ periodRange[1] || '--' }}</span>
              </el-form-item>
              <el-form-item label="丙方姓名">
                <el-input v-model="detail.thirdPartyName" maxlength="10" show-word-limit
                  placeholder="选填，10字符以内" @input="v => detail.thirdPartyName = stripEmoji(v)" />
              </el-form-item>
              <el-form-item label="丙方联系方式" :error="thirdPartyPhoneError">
                <el-input v-model="detail.thirdPartyPhone" maxlength="11" show-word-limit
                  placeholder="选填，11位手机号" @input="onThirdPartyPhoneInput" />
              </el-form-item>
              <el-form-item label="上传合同" required :error="contractFileError">
                <el-upload action="#" :http-request="uploadContract" :show-file-list="false"
                  :before-upload="beforeContractUpload" accept=".pdf">
                  <el-button type="primary" plain>{{ detail.contractFile ? '重新上传' : '点击上传PDF文件' }}</el-button>
                </el-upload>
                <span v-if="detail.contractFile" class="file-ok">{{ contractFileName }}</span>
                <div class="upload-hint">仅支持PDF文件，大小不超过60M</div>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div v-if="step !== 2" class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button v-if="canSubmit" type="primary" @click="submitCurrent()">{{ step === 5 ? '提交' : '保存' }}</el-button>
        </div>
      </div>
      <OperationTimeline class="detail-side" :nodes="timelineNodes" />
    </div>
  </PageCard>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Minus, QuestionFilled, Delete } from '@element-plus/icons-vue'
import PageCard from '@/components/PageCard.vue'
import CheckinSteps from '@/components/CheckinSteps.vue'
import OperationTimeline from '@/components/checkout/OperationTimeline.vue'
import InfoSectionsCheckin from '@/components/checkin/InfoSectionsCheckin.vue'

const route = useRoute()
const router = useRouter()
const detail = reactive({})
const assessTab = ref('1')
const assessTabsDone = reactive({ 1: false, 2: false, 3: false })
const abilityScores = reactive({
  q1: null, q2: null, q3: null, q4: null, q5: null,
  q6: null, q7: null, q8: null, q9: null, q10: null
})
const abilityTotal = computed(() => {
  const vals = Object.values(abilityScores)
  if (vals.some(v => v == null)) return null
  return vals.reduce((a, b) => a + b, 0)
})
const selfCareScore = computed(() => {
  if (abilityScores.q1 == null || abilityScores.q2 == null || abilityScores.q3 == null) return '--'
  return abilityScores.q1 + abilityScores.q2 + abilityScores.q3
})
const mentalScore = computed(() => {
  if (abilityScores.q4 == null || abilityScores.q5 == null || abilityScores.q6 == null) return '--'
  return abilityScores.q4 + abilityScores.q5 + abilityScores.q6
})
const socialScore = computed(() => {
  if (abilityScores.q7 == null || abilityScores.q8 == null || abilityScores.q9 == null || abilityScores.q10 == null) return '--'
  return abilityScores.q7 + abilityScores.q8 + abilityScores.q9 + abilityScores.q10
})
const abilityLevel = computed(() => {
  if (abilityTotal.value == null) return '--'
  const t = abilityTotal.value
  if (t === 10) return '能力完好'
  if (t <= 20) return '轻度失能'
  if (t <= 30) return '中度失能'
  if (t <= 40) return '中重度失能'
  return '重度失能'
})
const uploads = reactive({ photo: '', idFront: '', idBack: '' })
const assess = reactive({
  diseases: [],
  medications: [{ name: '', method: '', dosage: '' }],
  riskFalls: '', riskLost: '', riskChoking: '', riskSuicide: '', riskComa: '',
  wounds: [], specialCare: [], selfCare: '', dementia: [], otherNote: '',
  reportFile: '',
  upgradeReasons: [], upgradeOtherNote: '', finalLevel: ''
})
const diseaseOptions = ['无疾病','冠心病','糖尿病','肺炎','高血压','癫痫','艾滋病','慢性肾功能衰竭','脑出血','脑梗塞','尿路感染','帕金森综合症','消化道出血','肿瘤','截肢（六个月内）','骨折（三个月内）']
const riskItems = [
  { key: 'riskFalls', label: '跌倒' }, { key: 'riskLost', label: '走失' },
  { key: 'riskChoking', label: '噎食' }, { key: 'riskSuicide', label: '自杀' },
  { key: 'riskComa', label: '昏迷' }
]
const riskOptions = ['无','发生过一次','发生过两次','发生过三次以上']
const woundOptions = ['无','擦伤','烧烫伤','术后伤口','糖尿病足溃疡','血管性溃疡','其他伤口']
const specialCareOptions = ['无','胃管','尿管','气管切开','无创呼吸机','透析','胃肠膀胱造瘘','其他']
const selfCareOptions = ['不能自理','轻度依赖','中度依赖','可自理']
const dementiaOptions = ['行为异常','记忆障碍','日常生活能力减退','呆坐']
const upgradeReasons = ['已诊断疾病超过三项','风险事件超过三次','长期处于昏迷状态','认知障碍痴呆','精神类疾病','其他']
const finalLevelOptions = ['能力完好','轻度失能','中度失能','中重度失能','重度失能']

const abilityOptionsABC = [
  { label: 'A', score: 1, desc: '独立完成不需要协助' },
  { label: 'B', score: 2, desc: '在他人语言指导或照看下完成' },
  { label: 'C', score: 3, desc: '需要他人协助但以自身完成为主' },
  { label: 'D', score: 4, desc: '主要依靠协助且自身能予配合' },
  { label: 'E', score: 5, desc: '完全依赖他人协助且无法给予配合' }
]
const abilityModules = [
  {
    name: '自理能力',
    questions: [
      { id: 'q1', num: 1, title: '进食，即使用器具将食物送入嘴中并咽下', options: abilityOptionsABC },
      { id: 'q2', num: 2, title: '洗澡，即清洗和擦干身体', options: abilityOptionsABC },
      { id: 'q3', num: 3, title: '修饰，即洗脸、刷牙、梳头、刮脸、剪指趾甲等', options: abilityOptionsABC }
    ]
  },
  {
    name: '精神状态',
    questions: [
      { id: 'q4', num: 4, title: '时间与空间定向，即知道并确认时间和空间', options: [
        { label: 'A', score: 1, desc: '时间观念和空间清楚、可单独出远门并很快掌握新环境方位' },
        { label: 'B', score: 2, desc: '时间观念略有下降、年/月/日清楚但有时相差几天、可单独来往于近街且知道现住地名称和方位但不知回家路线' },
        { label: 'C', score: 3, desc: '时间观念较差、年/月/日不清楚但可知上半年或下半年、只能在家附近行动且对现住地只知名称不知方位' },
        { label: 'D', score: 4, desc: '时间观念很差、年/月/日不清楚但可知上午或下午、只能在左邻右舍间串门且对现住地不知名称和方位' },
        { label: 'E', score: 5, desc: '无时间观念且不能单独外出' }
      ]},
      { id: 'q5', num: 5, title: '人物定向，即知道并确认人物', options: [
        { label: 'A', score: 1, desc: '知道周围人们的关系及称谓意义并可分辨陌生人的大致年龄和身份并使用适当称呼' },
        { label: 'B', score: 2, desc: '只知家中亲密近亲的关系、不会分辨陌生人年龄且不能称呼陌生人' },
        { label: 'C', score: 3, desc: '只能称呼家中人或照样称呼但不知关系及不辨辈分' },
        { label: 'D', score: 4, desc: '只认识常同住的亲人、可称呼子女或孙子女并可辨熟人和生人' },
        { label: 'E', score: 5, desc: '只认识主要照顾者且不辨熟人和生人' }
      ]},
      { id: 'q6', num: 6, title: '记忆，即短时和长时记忆、瞬时、近期和远期记忆能力', options: [
        { label: 'A', score: 1, desc: '总是能够保持与社会及年龄相适应的长时和短时记忆并能完整回忆' },
        { label: 'B', score: 2, desc: '出现短时记忆紊乱或回忆不能，即三个词语经过五分钟后仅能回忆零至一个' },
        { label: 'C', score: 3, desc: '出现中度记忆紊乱或回忆不能，即不记得上一顿饭吃什么' },
        { label: 'D', score: 4, desc: '出现短时记忆紊乱或回忆不能，即不记得自己老朋友' },
        { label: 'E', score: 5, desc: '记忆完全紊乱或者完全不能对既往事务进行正确的回忆' }
      ]}
    ]
  },
  {
    name: '感知觉与社会参与',
    questions: [
      { id: 'q7', num: 7, title: '视力，即感受存在的光线并感受物体的大小和形状', options: [
        { label: 'A', score: 1, desc: '视力正常' },
        { label: 'B', score: 2, desc: '能看清楚大字体但看不清书报上的标准字体' },
        { label: 'C', score: 3, desc: '视力有限且看不清报纸大标题但能辨认物体' },
        { label: 'D', score: 4, desc: '只能看到光、颜色和形状' },
        { label: 'E', score: 5, desc: '完全失明' }
      ]},
      { id: 'q8', num: 8, title: '听力，即能够辨别声音的方位、音调、音量和音质的有关能力，可借助助听设备', options: [
        { label: 'A', score: 1, desc: '听力正常' },
        { label: 'B', score: 2, desc: '在轻声说话或说话距离超过两米时听不清' },
        { label: 'C', score: 3, desc: '正常交流有些困难且需在安静环境或大声说话才能听到' },
        { label: 'D', score: 4, desc: '讲话者大声说话或说话很慢才能部分听见' },
        { label: 'E', score: 5, desc: '完全失聪' }
      ]},
      { id: 'q9', num: 9, title: '执行日常事务，即计划、安排并完成日常事务，包括但不限于洗衣服、小金额购物、服药管理', options: [
        { label: 'A', score: 1, desc: '能够完全独立计划、安排和完成日常事务无需协助' },
        { label: 'B', score: 2, desc: '需要他人监护或指导' },
        { label: 'C', score: 3, desc: '需要小量协助' },
        { label: 'D', score: 4, desc: '需要大量协助' },
        { label: 'E', score: 5, desc: '完全依赖他人' }
      ]},
      { id: 'q10', num: 10, title: '使用交通工具外出', options: [
        { label: 'A', score: 1, desc: '能自己骑车或搭乘公共交通工具外出' },
        { label: 'B', score: 2, desc: '能自己搭乘出租车但不会搭乘公共交通工具外出' },
        { label: 'C', score: 3, desc: '当有人协助或陪伴时可搭乘公共交通工具外出' },
        { label: 'D', score: 4, desc: '只能在别人协助下搭乘出租车或私家车外出' },
        { label: 'E', score: 5, desc: '完全不能出门或者外出完全需要协助' }
      ]}
    ]
  }
]

const approvalResult = ref('审批通过')
const approvalComment = ref('')
const periodRange = ref([])
const feePeriodRange = ref([])

// 护理等级
const nursingLevels = ref([])
const selectedNursingLevelName = ref('')
const enabledNursingLevels = computed(() => {
  const enabled = nursingLevels.value.filter(n => n.islock === '启用')
  return enabled.length ? enabled : nursingLevels.value
})

// 床位选择
const bedDialogVisible = ref(false)
const bedFloors = ref([])
const bedActiveFloorId = ref(null)
const bedRoomList = ref([])
const tempSelectedBed = ref(null)
const tempSelectedRoom = ref(null)
const selectedBedInfo = ref(null)

// 费用附加项
const otherFeeItems = ref([])
const insurancePayItems = ref([])
const govSubsidyItems = ref([])

// 费用原始值（用于±10%校验）
const originalNursingFee = ref(null)
const originalBedFee = ref(null)

// 签约办理
const contractNameError = ref('')
const signDateError = ref('')
const contractFileError = ref('')
const thirdPartyPhoneError = ref('')

function onContractNameInput(v) {
  detail.contractName = stripEmoji(v)
  contractNameError.value = ''
}
function onThirdPartyPhoneInput(v) {
  detail.thirdPartyPhone = v.replace(/\D/g, '').slice(0, 11)
  thirdPartyPhoneError.value = ''
}
function beforeContractUpload(file) {
  const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')
  const ok = isPdf && file.size / 1024 / 1024 <= 60
  if (!isPdf) ElMessage.error('仅支持PDF文件')
  else if (!ok) ElMessage.error('文件大小不能超过60M')
  contractFileError.value = ''
  return ok
}
const contractFileName = computed(() => {
  const url = detail.contractFile
  if (!url) return ''
  const idx = url.lastIndexOf('/')
  return idx >= 0 ? url.substring(idx + 1) : url
})

// 账单预览
const billDialogVisible = ref(false)
const billData = ref(null)

const otherFeeBillRows = computed(() => (detail.otherFee ? 1 : 0) + otherFeeItems.value.length)
const insurancePayBillRows = computed(() => (detail.insurancePay ? 1 : 0) + insurancePayItems.value.length)
const govSubsidyBillRows = computed(() => (detail.govSubsidy ? 1 : 0) + govSubsidyItems.value.length)

const selectedBed = computed(() => selectedBedInfo.value)
const selectedBedId = computed(() => selectedBedInfo.value?.bedId || null)
const tempSelectedBedId = computed(() => tempSelectedBed.value?.bedId || null)

const mode = computed(() => route.query.mode || 'form')
const step = ref(1)
const stepIndex = computed(() => Math.max(0, step.value - 1))
const readonlyView = computed(() => detail.flowStatus === '已完成' || detail.flowStatus === '已关闭')
// 仅审批节点才显示「审批中」；评估/配置/签约必须可填
const isApprovalPending = computed(() => mode.value === 'pending' && step.value === 3)
const canSubmit = computed(() => !readonlyView.value && !isApprovalPending.value)

const timelineNodes = computed(() => {
  const t = detail || {}
  const fmtTime = (val) => {
    if (!val || val === '--') return '--'
    if (typeof val === 'string') return val
    return val
  }
  const s = Number(t.step) || 1
  const creator = t.creator || t.applicant || '--'
  const approver = '盛明兰'
  return [
    {
      name: '发起申请-申请入住',
      time: fmtTime(t.createTime) || '--',
      operator: creator,
      status: '已发起',
      type: 'success'
    },
    {
      name: '审批-申请审批',
      time: s > 3 ? fmtTime(t.createTime) || '--' : '--',
      operator: approver,
      status: s > 3 ? '已处理' : (s === 3 ? '审批中' : '待处理'),
      type: s > 3 ? 'primary' : (s === 3 ? 'warning' : 'info')
    },
    {
      name: '审批-入住配置',
      time: s > 4 ? fmtTime(t.createTime) || '--' : '--',
      operator: approver,
      status: s > 4 ? '已处理' : (s === 4 ? '进行中' : '待处理'),
      type: s > 4 ? 'primary' : (s === 4 ? 'primary' : 'info')
    },
    {
      name: '完成签约办理',
      time: s >= 5 ? fmtTime(t.finishTime || t.createTime) || '--' : '--',
      operator: s >= 5 ? creator : '待处理',
      status: s >= 5 ? '已完成' : '待处理',
      type: s >= 5 ? 'success' : 'info'
    }
  ]
})

onMounted(() => { loadDetail(); initNursingLevels() })
watch(() => route.query, loadDetail, { deep: true })

function parseExtra(raw) {
  if (!raw) return
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!obj || typeof obj !== 'object') return
    if (obj.uploads) Object.assign(uploads, obj.uploads)
    if (obj.assess) {
      const a = obj.assess
      assess.diseases = a.diseases || []
      if (a.medications && a.medications.length) {
        assess.medications = a.medications
      } else if (a.medicationName || a.medicationMethod || a.medicationDosage) {
        assess.medications = [{ name: a.medicationName || '', method: a.medicationMethod || '', dosage: a.medicationDosage || '' }]
      }
      assess.riskFalls = a.riskFalls || ''
      assess.riskLost = a.riskLost || ''
      assess.riskChoking = a.riskChoking || ''
      assess.riskSuicide = a.riskSuicide || ''
      assess.riskComa = a.riskComa || ''
      assess.wounds = a.wounds || []
      assess.specialCare = a.specialCare || []
      assess.selfCare = a.selfCare || ''
      assess.otherNote = a.otherNote || a.otherConditions || ''
      assess.dementia = a.dementia || []
      assess.reportFile = a.reportFile || ''
      assess.upgradeReasons = a.upgradeReasons || []
      assess.upgradeOtherNote = a.upgradeOtherNote || ''
      assess.finalLevel = a.finalLevel || ''
    }
    if (obj.abilityScores) {
      Object.keys(abilityScores).forEach(k => {
        if (obj.abilityScores[k] != null) abilityScores[k] = obj.abilityScores[k]
      })
    }
    // 恢复 tab 完成状态
    if (assess.diseases.length && riskItems.every(r => assess[r.key])) {
      assessTabsDone['1'] = true
    }
    if (Object.values(abilityScores).every(v => v != null)) {
      assessTabsDone['2'] = true
    }
    if (obj.photo || obj.idFront || obj.idBack) {
      uploads.photo = obj.photo || uploads.photo
      uploads.idFront = obj.idFront || uploads.idFront
      uploads.idBack = obj.idBack || uploads.idBack
    }
    if (obj.config) {
      const cfg = obj.config
      if (cfg.feePeriodStart && cfg.feePeriodEnd) {
        feePeriodRange.value = [cfg.feePeriodStart, cfg.feePeriodEnd]
      }
      otherFeeItems.value = cfg.otherFeeItems || []
      insurancePayItems.value = cfg.insurancePayItems || []
      govSubsidyItems.value = cfg.govSubsidyItems || []
      if (cfg.bedNo) {
        selectedBedInfo.value = {
          bedNo: cfg.bedNo, bedId: cfg.bedId || null,
          roomNo: cfg.bedRoomNo || '', roomTypeName: cfg.bedRoomType || '',
          floorName: cfg.bedFloorName || ''
        }
        originalBedFee.value = cfg.originalBedFee != null ? cfg.originalBedFee : detail.bedFee
      }
    }
  } catch (e) { /* ignore */ }
}

function buildExtraJson() {
  return JSON.stringify({
    uploads: { ...uploads },
    assess: { ...assess },
    abilityScores: { ...abilityScores },
    config: {
      feePeriodStart: feePeriodRange.value?.[0] || null,
      feePeriodEnd: feePeriodRange.value?.[1] || null,
      otherFeeItems: otherFeeItems.value,
      insurancePayItems: insurancePayItems.value,
      govSubsidyItems: govSubsidyItems.value,
      bedNo: selectedBedInfo.value?.bedNo || '',
      bedId: selectedBedInfo.value?.bedId || null,
      bedRoomNo: selectedBedInfo.value?.roomNo || '',
      bedRoomType: selectedBedInfo.value?.roomTypeName || '',
      bedFloorName: selectedBedInfo.value?.floorName || '',
      originalNursingFee: originalNursingFee.value,
      originalBedFee: originalBedFee.value
    }
  })
}

function disablePastDate(time) {
  return time.getTime() < Date.now() - 8.64e7
}

// ========== 护理等级 ==========
async function initNursingLevels() {
  try {
    const res = await axios.post('/levelList', { pageNum: 1, pageSize: 999 })
    const data = res.data
    nursingLevels.value = data?.nursingLevels || data?.data?.nursingLevels || []
  } catch (e) { console.error('加载护理等级失败', e); nursingLevels.value = [] }
  if (!nursingLevels.value.length) {
    nursingLevels.value = [
      { id: 1, levelname: '特级护理等级', islock: '启用', money: 5000 },
      { id: 2, levelname: '一级护理等级', islock: '启用', money: 3000 },
      { id: 3, levelname: '二级护理等级', islock: '启用', money: 2000 },
      { id: 4, levelname: '三级护理等级', islock: '启用', money: 1000 }
    ]
  }
}

function onNursingLevelChange(name) {
  const nl = nursingLevels.value.find(n => n.levelname === name)
  if (nl) {
    detail.nursingLevel = nl.levelname
    detail.nursingFee = nl.money || 0
    originalNursingFee.value = detail.nursingFee
  }
}

function validateNursingFee() {
  if (originalNursingFee.value == null || originalNursingFee.value === 0) return
  const orig = Number(originalNursingFee.value)
  const curr = Number(detail.nursingFee) || 0
  if (curr > orig * 1.1 || curr < orig * 0.9) {
    ElMessage.warning('该费用已超出或低于原费用百分之十，请重新输入')
  }
}

// ========== 床位选择 ==========
async function openBedDialog() {
  tempSelectedBed.value = selectedBedInfo.value ? { ...selectedBedInfo.value } : null
  bedDialogVisible.value = true
  if (bedFloors.value.length === 0) await initBedFloors()
}
async function initBedFloors() {
  try {
    const res = await axios.get('/bed/floors')
    if (res.data?.code === 200) {
      bedFloors.value = res.data.data || []
      if (bedFloors.value.length) {
        bedActiveFloorId.value = Number(bedFloors.value[0].id)
        loadBedRooms()
      }
    }
  } catch { bedFloors.value = [] }
}
async function loadBedRooms() {
  const fid = Number(bedActiveFloorId.value)
  if (!fid) return
  try {
    const res = await axios.get('/bed/rooms', { params: { floorId: fid } })
    if (res.data?.code !== 200) { bedRoomList.value = []; return }
    let rooms = (res.data.data || [])
      .filter(item => item && item.room)
      .map(item => ({ room: item.room, beds: item.beds || [] }))

    // 检查床位冲突：根据入住期限查询已被占用的床位
    if (periodRange.value && periodRange.value.length === 2) {
      const allBedNos = []
      rooms.forEach(r => r.beds.forEach(b => allBedNos.push(b.bedNo)))
      if (allBedNos.length) {
        try {
          const cr = await axios.post('/checkin/bed-conflicts', {
            start: periodRange.value[0],
            end: periodRange.value[1],
            bedNos: allBedNos,
            excludeId: detail.id
          })
          if (cr.data?.code === 200 && cr.data.data?.length) {
            const conflictSet = new Set(cr.data.data)
            rooms = rooms.map(r => ({
              room: r.room,
              beds: r.beds.map(b => conflictSet.has(b.bedNo)
                ? { ...b, status: '已入住', elderName: '已预约' }
                : b)
            }))
          }
        } catch { /* ignore conflict check errors */ }
      }
    }

    bedRoomList.value = rooms
    if (selectedBedInfo.value) {
      tempSelectedBed.value = selectedBedInfo.value
      tempSelectedRoom.value = { roomNo: selectedBedInfo.value.roomNo }
    }
  } catch { bedRoomList.value = [] }
}
function selectBed(bed, room) {
  if (bed.status !== '空闲') return
  const floor = bedFloors.value.find(f => f.id === bedActiveFloorId.value)
  tempSelectedBed.value = { bedId: bed.id, bedNo: bed.bedNo, roomNo: room.roomNo, roomId: room.id, roomTypeName: room.roomTypeName, floorName: floor?.name || '' }
  tempSelectedRoom.value = room
}
function confirmBedSelection() {
  if (!tempSelectedBed.value) {
    ElMessage.warning('请选择一个空闲床位')
    return
  }
  selectedBedInfo.value = { ...tempSelectedBed.value }
  detail.bedNo = tempSelectedBed.value.bedNo
  // 自动填充床位费用（从房型价格获取）
  autoFillBedFee(tempSelectedRoom.value)
  bedDialogVisible.value = false
  ElMessage.success('床位选择成功')
}
async function autoFillBedFee(room) {
  try {
    const res = await axios.get('/roomType/list')
    if (res.data?.code === 200) {
      const types = res.data.data || []
      const t = types.find(t => t.name === room.roomTypeName)
      if (t && t.price != null) {
        detail.bedFee = t.price
        originalBedFee.value = t.price
      }
    }
  } catch { /* fallback */ }
}

function validateBedFee() {
  if (originalBedFee.value == null || originalBedFee.value === 0) return
  const orig = Number(originalBedFee.value)
  const curr = Number(detail.bedFee) || 0
  if (curr > orig * 1.1 || curr < orig * 0.9) {
    ElMessage.warning('该费用已超出或低于原费用百分之十，请重新输入')
  }
}

// ========== 费用附加 ==========
function addFeeItem(type) {
  const item = { desc: '', amount: 0 }
  if (type === 'other') otherFeeItems.value.push(item)
  else if (type === 'insurance') insurancePayItems.value.push(item)
  else if (type === 'gov') govSubsidyItems.value.push(item)
}

// ========== 账单预览 ==========
function showBillPreview() {
  if (!feePeriodRange.value || feePeriodRange.value.length !== 2) {
    ElMessage.warning('请先选择费用期限')
    return
  }
  const start = feePeriodRange.value[0]
  const end = feePeriodRange.value[1]
  const days = Math.floor((new Date(end) - new Date(start)) / 86400000) + 1
  const month = start.substring(0, 7).replace('-', '年') + '月'

  const addItems = []
  if (detail.nursingFee) addItems.push(Number(detail.nursingFee))
  if (detail.bedFee) addItems.push(Number(detail.bedFee))
  if (detail.otherFee) addItems.push(Number(detail.otherFee))
  otherFeeItems.value.forEach(f => addItems.push(Number(f.amount) || 0))
  const addSubtotal = addItems.reduce((a, b) => a + b, 0)

  const deductItems = []
  if (detail.insurancePay) deductItems.push(Number(detail.insurancePay))
  if (detail.govSubsidy) deductItems.push(Number(detail.govSubsidy))
  insurancePayItems.value.forEach(f => deductItems.push(Number(f.amount) || 0))
  govSubsidyItems.value.forEach(f => deductItems.push(Number(f.amount) || 0))
  const deductSubtotal = deductItems.reduce((a, b) => a + b, 0)

  const monthlyPayable = addSubtotal > deductSubtotal ? (addSubtotal - deductSubtotal).toFixed(2) : '0.00'
  const totalBill = (Number(monthlyPayable) + Number(detail.deposit || 0)).toFixed(2)

  billData.value = {
    month,
    period: start + ' 至 ' + end,
    totalDays: days,
    addSubtotal: addSubtotal.toFixed(2),
    deductSubtotal: deductSubtotal.toFixed(2),
    monthlyPayable,
    totalBill
  }
  billDialogVisible.value = true
}

function stripEmoji(val) {
  return (val || '').replace(/[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{FE00}-\u{FE0F}\u{1F900}-\u{1F9FF}\u{1FA00}-\u{1FA6F}\u{1FA70}-\u{1FAFF}\u{200D}\u{20E3}\u{FE0F}]/gu, '')
}

function addMedication() { assess.medications.push({ name: '', method: '', dosage: '' }) }
function removeMedication(i) { assess.medications.splice(i, 1) }

function saveAssessTab() {
  if (assessTab.value === '1') {
    if (!assess.diseases.length) { ElMessage.warning('请选择疾病诊断'); return }
    for (const r of riskItems) {
      if (!assess[r.key]) { ElMessage.warning('请完整填写所有风险事件'); return }
    }
    assessTabsDone['1'] = true
    assessTab.value = '2'
    saveAssessDraft()
    ElMessage.success('保存成功')
  } else if (assessTab.value === '2') {
    const vals = Object.values(abilityScores)
    if (vals.some(v => v == null)) {
      ElMessage.warning('选项为空，请选择选项')
      return
    }
    assessTabsDone['2'] = true
    assessTab.value = '3'
    saveAssessDraft()
    ElMessage.success('保存成功')
  }
}

function saveAssessDraft() {
  axios.post('/checkin/updateStep', {
    id: detail.id,
    step: detail.step,
    currentStep: detail.step,
    extraJson: buildExtraJson()
  }).catch(() => {})
}

function isNoneDisabled(field, item) {
  if (item === '无' || item === '无疾病') return assess[field].length > 0 && !assess[field].includes(item)
  return assess[field].includes('无') || assess[field].includes('无疾病')
}
function onMultiWithNone(field, item, checked) {
  if (item === '无' || item === '无疾病') {
    assess[field] = checked ? [item] : []
  } else {
    if (checked) {
      assess[field] = assess[field].filter(v => v !== '无' && v !== '无疾病')
      assess[field].push(item)
    } else {
      assess[field] = assess[field].filter(v => v !== item)
    }
  }
}
function onUpgradeReasonChange(item, checked) {
  if (checked) {
    assess.upgradeReasons.push(item)
  } else {
    assess.upgradeReasons = assess.upgradeReasons.filter(v => v !== item)
    if (item === '其他') assess.upgradeOtherNote = ''
  }
}
function onDementiaCheck(item, checked) {
  if (checked) {
    assess.dementia.push(item)
  } else {
    assess.dementia = assess.dementia.filter(v => v !== item)
  }
}

function confirmSubmit() {
  if (!assess.upgradeReasons.length) { ElMessage.warning('请选择能力等级变更依据'); return }
  if (assess.upgradeReasons.includes('其他') && !assess.upgradeOtherNote.trim()) {
    ElMessage.warning('请填写其他原因'); return
  }
  if (!assess.finalLevel) { ElMessage.warning('请选择能力最终等级'); return }
  if (!assess.diseases.length) { ElMessage.warning('请选择疾病诊断'); return }
  for (const r of riskItems) {
    if (!assess[r.key]) { ElMessage.warning('请完整填写所有风险事件'); return }
  }
  if (!assess.reportFile) { ElMessage.warning('请上传近期体检报告'); return }
  ElMessageBox.confirm('此操作将发起入住审批，是否继续？', '确认申请', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    const payload = {
      id: detail.id, step: 3, currentStep: detail.step,
      flowStatus: '申请中', stepStatus: '进行中',
      extraJson: buildExtraJson()
    }
    axios.post('/checkin/updateStep', payload).then(res => {
      if (res.data.code === 200) {
        router.push({ path: '/CheckinDetail', query: { id: detail.id, mode: 'pending' } })
        ElMessage.success('提交成功，请在入住办理中继续处理')
      } else {
        ElMessage.error(res.data.msg || '操作失败')
      }
    }).catch(() => ElMessage.error('操作失败'))
  }).catch(() => {})
}

function beforeReport(file) {
  const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')
  const ok = isPdf && file.size / 1024 / 1024 <= 60
  if (!isPdf) ElMessage.error('仅支持PDF文件')
  else if (!ok) ElMessage.error('文件大小不能超过60M')
  return ok
}

function uploadReport(option) {
  const fd = new FormData()
  fd.append('mf', option.file)
  axios.post('/upload/report', fd).then(res => {
    if (res.data.code === 200) {
      assess.reportFile = res.data.data
      ElMessage.success('上传成功')
    } else ElMessage.error(res.data.msg || '上传失败')
  }).catch(() => ElMessage.error('上传失败'))
}

function loadDetail() {
  const id = route.query.id
  if (!id) return
  axios.get('/checkin/detail', { params: { id } }).then(res => {
    if (res.data.code === 200) {
      Object.assign(detail, res.data.data || {})
      step.value = Number(detail.step) || 1
      if (detail.periodStart && detail.periodEnd) {
        periodRange.value = [detail.periodStart, detail.periodEnd]
      }
      // 恢复护理等级选择
      if (detail.nursingLevel) {
        selectedNursingLevelName.value = detail.nursingLevel
        originalNursingFee.value = detail.nursingFee
      }
      // 押金默认3000
      if (!detail.deposit) detail.deposit = 3000
      // 签约信息默认值
      if (!detail.contractName) detail.contractName = (detail.elderName || '老人') + '的入住合同'
      if (!detail.signDate) {
        const today = new Date()
        detail.signDate = today.getFullYear() + '-' +
          String(today.getMonth() + 1).padStart(2, '0') + '-' +
          String(today.getDate()).padStart(2, '0')
      }
      parseExtra(detail.extraJson)
    } else {
      ElMessage.error(res.data.msg || '加载详情失败')
    }
  }).catch(() => ElMessage.error('加载详情失败'))
}

function goBack() {
  router.push('/CheckinProcess')
}

function uploadContract(option) {
  const formData = new FormData()
  formData.append('mf', option.file)
  axios.post('/upload', formData).then(res => {
    if (res.data.code === 200) {
      detail.contractFile = res.data.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.data.msg || '上传失败')
    }
  }).catch(() => ElMessage.error('上传失败'))
}

function validateCurrent() {
  if (step.value === 2) {
    if (!assess.diseases.length) {
      ElMessage.warning('请选择疾病诊断')
      return false
    }
    for (const r of riskItems) {
      if (!assess[r.key]) {
        ElMessage.warning('请完整填写所有风险事件')
        return false
      }
    }
    if (!assess.reportFile) {
      ElMessage.warning('请上传近期体检报告')
      return false
    }
  } else if (step.value === 3) {
    if (!approvalResult.value) {
      ElMessage.warning('请选择审批结果')
      return false
    }
  } else if (step.value === 4) {
    if (!periodRange.value || periodRange.value.length !== 2) {
      ElMessage.warning('请选择入住期限'); return false
    }
    if (!selectedNursingLevelName.value) {
      ElMessage.warning('请选择护理等级'); return false
    }
    if (!selectedBedInfo.value) {
      ElMessage.warning('请选择入住床位'); return false
    }
    if (!feePeriodRange.value || feePeriodRange.value.length !== 2) {
      ElMessage.warning('请选择费用期限'); return false
    }
    if (!detail.nursingFee && detail.nursingFee !== 0) {
      ElMessage.warning('请输入护理费用'); return false
    }
    if (!detail.bedFee && detail.bedFee !== 0) {
      ElMessage.warning('请输入床位费用'); return false
    }
  } else if (step.value === 5) {
    let valid = true
    if (!detail.contractName || !detail.contractName.trim()) {
      contractNameError.value = '合同名称为空，请输入合同名称'
      valid = false
    } else { contractNameError.value = '' }
    if (!detail.signDate) {
      signDateError.value = '签约日期为空，请选择签约日期'
      valid = false
    } else { signDateError.value = '' }
    if (!detail.contractFile) {
      contractFileError.value = '上传合同为空，请上传合同'
      valid = false
    } else { contractFileError.value = '' }
    if (detail.thirdPartyPhone && !/^1[3-9]\d{9}$/.test(detail.thirdPartyPhone)) {
      thirdPartyPhoneError.value = '丙方联系方式格式错误，请重新输入'
      valid = false
    } else { thirdPartyPhoneError.value = '' }
    if (!valid) return false
  }
  return true
}

function submitCurrent(skipRedirect) {
  if (!validateCurrent()) return Promise.resolve(false)

  // 同步 step 4 数据到 detail
  if (step.value === 4) {
    detail.nursingLevel = selectedNursingLevelName.value
    if (selectedBedInfo.value) detail.bedNo = selectedBedInfo.value.bedNo
    detail.otherFee = detail.otherFee || 0
    detail.insurancePay = detail.insurancePay || 0
    detail.govSubsidy = detail.govSubsidy || 0
  }

  const payload = {
    id: detail.id,
    step: step.value === 5 ? 5 : step.value + 1,
    currentStep: step.value,
    approvalResult: approvalResult.value,
    approvalComment: approvalComment.value,
    nursingLevel: detail.nursingLevel,
    deposit: detail.deposit,
    nursingFee: detail.nursingFee,
    bedFee: detail.bedFee,
    bedNo: detail.bedNo,
    otherFee: detail.otherFee,
    insurancePay: detail.insurancePay,
    govSubsidy: detail.govSubsidy,
    contractName: detail.contractName,
    contractFile: detail.contractFile,
    contractNo: detail.contractNo,
    signDate: detail.signDate,
    thirdPartyName: detail.thirdPartyName,
    thirdPartyPhone: detail.thirdPartyPhone,
    flowStatus: step.value === 5 ? '已完成' : '申请中',
    stepStatus: step.value === 5 ? '已完成' : '进行中',
    extraJson: buildExtraJson()
  }
  if (periodRange.value && periodRange.value.length === 2) {
    payload.periodStart = periodRange.value[0]
    payload.periodEnd = periodRange.value[1]
  }
  const fromStep = step.value
  return axios.post('/checkin/updateStep', payload).then(res => {
    if (res.data.code === 200) {
      if (!skipRedirect && fromStep !== 5 && approvalResult.value !== '审批拒绝') {
        const next = Number(res.data.data) || (fromStep + 1)
        step.value = next
        router.replace({ path: '/CheckinDetail', query: { id: detail.id, step: next, mode: 'form' } })
        loadDetail()
      }
      if (fromStep === 5) {
        ElMessage.success('提交成功')
        goBack()
      }
      return true
    } else {
      ElMessage.error(res.data.msg || '操作失败')
      return false
    }
  }).catch(() => { ElMessage.error('操作失败'); return false })
}
</script>

<style scoped>
.detail-layout { display: flex; gap: 20px; align-items: flex-start; }
.detail-main { flex: 1; min-width: 0; }
.detail-side { width: 280px; flex-shrink: 0; }
.form-actions { margin-top: 24px; text-align: center; }
.status-panel { max-width: 720px; margin: 0 auto; }
.assess-panel { margin-top: 16px; }
.assess-title { font-size: 18px; font-weight: 600; margin: 0 0 16px; color: #303133; text-align: left; }
.assess-form { margin-top: 12px; }
.checkbox-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px 12px; }
.med-table :deep(.el-textarea__inner) { resize: vertical; }
.step-placeholder { padding: 60px 0; }
.ability-module { margin-bottom: 16px; }
.ability-mod-title { font-size: 15px; font-weight: 600; margin: 0 0 12px; padding-bottom: 8px; border-bottom: 2px solid #409eff; color: #303133; }
.ability-card { border: 1px solid #e4e7ed; border-radius: 6px; margin-bottom: 12px; overflow: hidden; }
.ability-table { width: 100%; border-collapse: collapse; }
.ability-table td { padding: 8px 16px; text-align: left; vertical-align: middle; }
.ability-table td:first-child { padding-left: 16px; }
.at-title { font-size: 14px; color: #303133; }
.at-score { width: 140px; font-size: 14px; color: #303133; text-align: left; white-space: nowrap; }
.at-opt-cell { border-top: 1px solid #f0f0f0; padding-left: 16px !important; }
.at-opt-cell :deep(.el-radio-group) { display: flex; flex-direction: column; gap: 6px; padding: 0 !important; margin: 0 !important; }
.at-radio { margin: 0 !important; padding: 0 !important; display: flex !important; }
.at-radio :deep(.el-radio__input) { margin: 0 !important; padding: 0 !important; }
.at-radio :deep(.el-radio__label) { padding-left: 6px !important; }
.q-required { color: #f56c6c; margin-right: 2px; font-size: 14px; }
.opt-label { font-weight: 400; color: #303133; margin-right: 6px; }
.opt-desc { font-size: 13px; color: #303133; }
.score-val { font-size: 14px; font-weight: 600; color: #303133; }
.score-empty { font-size: 14px; color: #303133; }
.ability-footer { margin-top: 16px; text-align: left; font-size: 15px; }
.score-total { font-size: 18px; font-weight: 700; color: #303133; }
.report-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.report-table th, .report-table td { padding: 10px 16px; text-align: left; border: 1px solid #e4e7ed; font-size: 14px; color: #303133; }
.report-table th { background: #f5f7fa; font-weight: 600; }
.report-score { font-weight: 600; }
.report-total-row td { background: #f0f5ff; font-weight: 700; }
.report-section-head { display: flex; align-items: center; margin: 16px 0 8px; }
.report-section-title { font-size: 15px; font-weight: 600; color: #303133; }
.tip-icon { margin-left: 8px; color: #909399; font-size: 16px; cursor: help; }
.tip-icon:hover { color: #409eff; }
.file-ok { margin-left: 8px; color: #67c23a; font-size: 13px; }
.upload-hint { margin-top: 4px; color: #909399; font-size: 12px; }

/* 入住配置页 */
.config-page { max-width: 800px; }
.config-section { margin-bottom: 24px; }
.config-section-head { display: flex; align-items: center; gap: 8px; }
.config-section-title { margin: 0 0 14px; font-size: 16px; font-weight: 600; color: #303133; padding-bottom: 10px; border-bottom: 2px solid #409eff; }
.config-form { margin-top: 8px; }
.config-radio { margin-right: 24px; margin-bottom: 8px; }
.bed-info { color: #303133; font-size: 14px; }
.plus-btn { margin-left: 4px; font-size: 18px; }
.fee-extra-items { margin-top: 8px; }
.fee-extra-row { display: flex; align-items: center; margin-bottom: 6px; }
.bill-preview-bar { margin-top: 20px; text-align: center; }
.bill-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.bill-table th, .bill-table td { padding: 8px 12px; text-align: left; border: 1px solid #e4e7ed; font-size: 14px; }
.bill-table th { background: #f5f7fa; font-weight: 600; }
.bill-table tbody td { color: #303133; }
.bill-amount { text-align: right; }
.bill-subtotal td { font-weight: 700; background: #f0f5ff; }
.bill-summary { margin-top: 8px; }
.bill-summary :deep(.el-descriptions-item__label) { font-weight: 600; }
.bill-summary :deep(.el-descriptions-item__content) { font-size: 18px; font-weight: 700; color: #e6a23c; }

/* 床位选择弹窗 */
.bed-select-toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 16px; margin-bottom: 16px; }
.bed-select-legend { display: flex; gap: 16px; color: #666; font-size: 13px; margin-left: auto; }
.bed-select-room-card { border: 1px solid #ebeef5; }
.bed-select-room-header { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.bed-select-room-header .room-type-text { color: #909399; font-size: 13px; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; }
.dot.idle { background: #ccc; }
.dot.occupied { background: #67c23a; }
.dot.leave { background: #e6a23c; }
.bed-item { border: 1px solid #ebeef5; border-radius: 4px; padding: 8px; font-size: 13px; cursor: pointer; }
.bed-item.idle { border-color: #dcdfe6; }
.bed-item.idle:hover, .bed-item.idle.selected { border-color: #409eff; background: #ecf5ff; }
.bed-item.occupied { border-color: #b3e19d; background: #f0f9eb; }
.bed-item.leave { border-color: #f3d19e; background: #fdf6ec; }
.bed-item.disabled { cursor: not-allowed; opacity: 0.7; }
.bed-elder { color: #666; margin: 4px 0; }
</style>
