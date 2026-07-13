package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.config.DemoDataFill;
import com.soft.dto.PageQueryDto;
import com.soft.dto.UserLineDto;
import com.soft.mapper.CheckinMapper;
import com.soft.pojo.Checkin;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入住管理：分页、详情、申请、多步骤推进、床位冲突检查、撤销。
 * <p>
 * 新建后 step 从 2 开始；step=3 为审批节点；step=5 签合同并办结。
 * 新建时从 Session 补全 applicant/creator，与协同「我的申请」归属一致。
 */
@RestController
@RequestMapping("/checkin")
public class CheckinController {
    @Autowired private CheckinMapper checkinMapper;

    /** 入住单分页列表。 */
    @PostMapping("/page")
    public Result<List<Checkin>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Checkin> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Checkin::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Checkin::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getElderIdcard())) w.like(Checkin::getElderIdcard, q.getElderIdcard());
        if (StringUtils.hasText(q.getStartTime())) w.ge(Checkin::getCheckinDate, q.getStartTime());
        if (StringUtils.hasText(q.getEndTime())) w.le(Checkin::getCheckinDate, q.getEndTime());
        w.orderByDesc(Checkin::getCreateTime);
        Page<Checkin> page = checkinMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Checkin> detail(@RequestParam("id") Long id) {
        Checkin c = checkinMapper.selectById(id);
        if (c == null) {
            c = DemoDataFill.demoCheckin(id);
            DemoDataFill.fillCheckin(c);
        }
        return Result.ok(c);
    }

    /** 新建或更新入住单；新建时校验必填与重复入住，并从 Session 补全申请人。 */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Checkin c, HttpSession session) {
        if (!StringUtils.hasText(c.getElderName()) || !StringUtils.hasText(c.getElderIdcard())
                || !StringUtils.hasText(c.getElderPhone()) || !StringUtils.hasText(c.getAddress())) {
            return Result.fail("请完善申请必填信息");
        }
        Long dup = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getElderIdcard, c.getElderIdcard())
                .eq(Checkin::getFlowStatus, "已完成"));
        if (dup != null && dup > 0) {
            return Result.fail("该老人已入住，请重新输入");
        }
        if (c.getId() == null) {
            if (c.getDocNo() == null) c.setDocNo("RZ" + System.currentTimeMillis());
            c.setStep(2);
            c.setStepStatus("进行中");
            c.setFlowStatus("申请中");
            c.setApplyTime(LocalDateTime.now());
            c.setCreateTime(LocalDateTime.now());
            if (c.getCheckinDate() == null) c.setCheckinDate(java.time.LocalDate.now());
            fillApplicantFromSession(c, session);
            checkinMapper.insert(c);
            return Result.ok(String.valueOf(c.getId()));
        }
        checkinMapper.updateById(c);
        return Result.ok(String.valueOf(c.getId()));
    }

    /** 从 Session 补全 applicant/creator（与登录 realname 一致） */
    private void fillApplicantFromSession(Checkin c, HttpSession session) {
        String name = null;
        if (session != null) {
            Object online = session.getAttribute("online");
            if (online instanceof UserLineDto dto && StringUtils.hasText(dto.getRealname())) {
                name = dto.getRealname().trim();
            }
        }
        if (StringUtils.hasText(name)) {
            if (!StringUtils.hasText(c.getApplicant())) c.setApplicant(name);
            if (!StringUtils.hasText(c.getCreator())) c.setCreator(name);
        } else {
            if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("顾廷烬");
            if (!StringUtils.hasText(c.getCreator())) c.setCreator("顾廷烬");
        }
    }

    /**
     * 推进入住步骤。currentStep 与库不一致时拒绝。
     * step=3 走审批；step=5 可办结；其余仅允许 current 或 current+1。
     */
    @PostMapping("/updateStep")
    public Result<String> updateStep(@RequestBody Checkin body) {
        if (body.getId() == null) return Result.fail("入住单不存在");
        Checkin db = checkinMapper.selectById(body.getId());
        if (db == null) return Result.fail("入住单不存在");
        if ("已完成".equals(db.getFlowStatus()) || "已关闭".equals(db.getFlowStatus())) {
            return Result.fail("当前流程已结束");
        }
        int current = db.getStep() == null ? 1 : db.getStep();
        Integer reqStep = body.getStep();

        // 校验前端当前步骤与数据库一致
        Integer clientStep = body.getCurrentStep();
        if (clientStep != null && clientStep != current) {
            return Result.fail("页面状态已过期，请刷新后重试");
        }

        String approval = body.getApprovalResult();
        boolean isApproval = StringUtils.hasText(approval);

        if (current == 3 && isApproval) {
            db.setApprovalResult(approval);
            db.setApprovalComment(body.getApprovalComment());
            if ("审批通过".equals(approval) || "通过".equals(approval)) {
                db.setStep(4);
                db.setStepStatus("进行中");
                db.setFlowStatus("申请中");
            } else if ("审批拒绝".equals(approval) || "拒绝".equals(approval)) {
                db.setStep(3);
                db.setStepStatus("已关闭");
                db.setFlowStatus("已关闭");
                db.setFinishTime(LocalDateTime.now());
            } else if ("驳回".equals(approval)) {
                db.setStep(2);
                db.setStepStatus("进行中");
                db.setFlowStatus("申请中");
            } else {
                return Result.fail("无效的审批结果");
            }
            checkinMapper.updateById(db);
            return Result.ok(String.valueOf(db.getStep()));
        }

        if (reqStep == null) return Result.fail("缺少目标步骤");
        if (current == 5 && (reqStep == 5 || "已完成".equals(body.getFlowStatus()) || "已完成".equals(body.getStepStatus()))) {
            if (StringUtils.hasText(body.getContractName())) db.setContractName(body.getContractName());
            if (body.getSignDate() != null) db.setSignDate(body.getSignDate());
            if (StringUtils.hasText(body.getContractFile())) db.setContractFile(body.getContractFile());
            if (StringUtils.hasText(body.getContractNo())) db.setContractNo(body.getContractNo());
            if (StringUtils.hasText(body.getThirdPartyName())) db.setThirdPartyName(body.getThirdPartyName());
            if (StringUtils.hasText(body.getThirdPartyPhone())) db.setThirdPartyPhone(body.getThirdPartyPhone());
            db.setStep(5);
            db.setStepStatus("已完成");
            db.setFlowStatus("已完成");
            db.setFinishTime(LocalDateTime.now());
            checkinMapper.updateById(db);
            return Result.ok("5");
        }
        if (reqStep == current) {
            mergeStepFields(db, body, current);
            checkinMapper.updateById(db);
            return Result.ok(String.valueOf(db.getStep()));
        }
        if (reqStep != current + 1) {
            return Result.fail("请先完成当前步骤，不可跳步");
        }
        mergeStepFields(db, body, current);
        db.setStep(reqStep);
        db.setStepStatus("进行中");
        db.setFlowStatus("申请中");
        checkinMapper.updateById(db);
        return Result.ok(String.valueOf(db.getStep()));
    }

    /** 按当前步骤合并前端提交的业务字段。 */
    private void mergeStepFields(Checkin db, Checkin body, int current) {
        if (current == 2) {
            if (StringUtils.hasText(body.getExtraJson())) db.setExtraJson(body.getExtraJson());
            if (StringUtils.hasText(body.getNursingLevel())) db.setNursingLevel(body.getNursingLevel());
        } else if (current == 4) {
            if (StringUtils.hasText(body.getNursingLevel())) db.setNursingLevel(body.getNursingLevel());
            if (body.getDeposit() != null) db.setDeposit(body.getDeposit());
            if (body.getNursingFee() != null) db.setNursingFee(body.getNursingFee());
            if (body.getBedFee() != null) db.setBedFee(body.getBedFee());
            if (body.getOtherFee() != null) db.setOtherFee(body.getOtherFee());
            if (body.getInsurancePay() != null) db.setInsurancePay(body.getInsurancePay());
            if (body.getGovSubsidy() != null) db.setGovSubsidy(body.getGovSubsidy());
            if (body.getPeriodStart() != null) { db.setPeriodStart(body.getPeriodStart()); db.setCheckinDate(body.getPeriodStart()); }
            if (body.getPeriodEnd() != null) db.setPeriodEnd(body.getPeriodEnd());
            if (StringUtils.hasText(body.getBedNo())) db.setBedNo(body.getBedNo());
            // 生成唯一合同编号（基于记录ID保证唯一性）
            if (!StringUtils.hasText(db.getContractNo())) {
                String contractNo = "HT" + db.getId() + String.format("%04d",
                        java.util.concurrent.ThreadLocalRandom.current().nextInt(10000));
                db.setContractNo(contractNo);
            }
        } else if (current == 5) {
            if (StringUtils.hasText(body.getContractName())) db.setContractName(body.getContractName());
            if (body.getSignDate() != null) db.setSignDate(body.getSignDate());
            if (StringUtils.hasText(body.getContractFile())) db.setContractFile(body.getContractFile());
            if (StringUtils.hasText(body.getThirdPartyName())) db.setThirdPartyName(body.getThirdPartyName());
            if (StringUtils.hasText(body.getThirdPartyPhone())) db.setThirdPartyPhone(body.getThirdPartyPhone());
        }
    }

    /** 检查床位在时间段内是否与申请中/已完成单据冲突。 */
    @PostMapping("/bed-conflicts")
    public Result<List<String>> bedConflicts(@RequestBody java.util.Map<String, Object> body) {
        String start = (String) body.get("start");
        String end = (String) body.get("end");
        @SuppressWarnings("unchecked")
        java.util.List<String> bedNos = (java.util.List<String>) body.get("bedNos");
        Number excludeId = (Number) body.get("excludeId");
        if (start == null || end == null || bedNos == null || bedNos.isEmpty()) {
            return Result.ok(java.util.Collections.emptyList());
        }
        java.time.LocalDate startDate = java.time.LocalDate.parse(start);
        java.time.LocalDate endDate = java.time.LocalDate.parse(end);
        java.util.Set<String> conflicts = new java.util.HashSet<>();
        for (String bedNo : bedNos) {
            LambdaQueryWrapper<Checkin> w = new LambdaQueryWrapper<Checkin>()
                    .eq(Checkin::getBedNo, bedNo)
                    .in(Checkin::getFlowStatus, "申请中", "已完成")
                    .le(Checkin::getPeriodStart, endDate)
                    .ge(Checkin::getPeriodEnd, startDate);
            if (excludeId != null && excludeId.longValue() > 0) {
                w.ne(Checkin::getId, excludeId.longValue());
            }
            Long count = checkinMapper.selectCount(w);
            if (count != null && count > 0) {
                conflicts.add(bedNo);
            }
        }
        return Result.ok(new java.util.ArrayList<>(conflicts));
    }

    /** 撤销：仅申请中可关闭。 */
    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Checkin db = checkinMapper.selectById(id);
        if (db == null) return Result.fail("入住单不存在");
        if (!"申请中".equals(db.getFlowStatus())) {
            return Result.fail("仅申请中可撤销");
        }
        db.setFlowStatus("已关闭");
        db.setStepStatus("已关闭");
        checkinMapper.updateById(db);
        return Result.ok("ok");
    }
}