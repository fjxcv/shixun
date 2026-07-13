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

@RestController
@RequestMapping("/checkin")
public class CheckinController {
    @Autowired private CheckinMapper checkinMapper;

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

    @PostMapping("/save")
    public Result<String> save(@RequestBody Checkin c, HttpSession session) {
        if (!StringUtils.hasText(c.getElderName()) || !StringUtils.hasText(c.getElderIdcard())
                || !StringUtils.hasText(c.getElderPhone()) || !StringUtils.hasText(c.getAddress())) {
            return Result.fail("\u8bf7\u5b8c\u5584\u7533\u8bf7\u5fc5\u586b\u4fe1\u606f");
        }
        Long dup = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getElderIdcard, c.getElderIdcard())
                .eq(Checkin::getFlowStatus, "\u5df2\u5b8c\u6210"));
        if (dup != null && dup > 0) {
            return Result.fail("\u8be5\u8001\u4eba\u5df2\u5165\u4f4f\uff0c\u8bf7\u91cd\u65b0\u8f93\u5165");
        }
        if (c.getId() == null) {
            if (c.getDocNo() == null) c.setDocNo("RZ" + System.currentTimeMillis());
            c.setStep(2);
            c.setStepStatus("\u8fdb\u884c\u4e2d");
            c.setFlowStatus("\u7533\u8bf7\u4e2d");
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

    /** \u4ece Session \u8865\u5168 applicant/creator\uff08\u4e0e\u767b\u5f55 realname \u4e00\u81f4\uff09 */
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
            if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("\u987e\u5ef7\u70ec");
            if (!StringUtils.hasText(c.getCreator())) c.setCreator("\u987e\u5ef7\u70ec");
        }
    }

    @PostMapping("/updateStep")
    public Result<String> updateStep(@RequestBody Checkin body) {
        if (body.getId() == null) return Result.fail("\u5165\u4f4f\u5355\u4e0d\u5b58\u5728");
        Checkin db = checkinMapper.selectById(body.getId());
        if (db == null) return Result.fail("\u5165\u4f4f\u5355\u4e0d\u5b58\u5728");
        if ("\u5df2\u5b8c\u6210".equals(db.getFlowStatus()) || "\u5df2\u5173\u95ed".equals(db.getFlowStatus())) {
            return Result.fail("\u5f53\u524d\u6d41\u7a0b\u5df2\u7ed3\u675f");
        }
        int current = db.getStep() == null ? 1 : db.getStep();
        Integer reqStep = body.getStep();

        // \u6821\u9a8c\u524d\u7aef\u5f53\u524d\u6b65\u9aa4\u4e0e\u6570\u636e\u5e93\u4e00\u81f4
        Integer clientStep = body.getCurrentStep();
        if (clientStep != null && clientStep != current) {
            return Result.fail("\u9875\u9762\u72b6\u6001\u5df2\u8fc7\u671f\uff0c\u8bf7\u5237\u65b0\u540e\u91cd\u8bd5");
        }

        String approval = body.getApprovalResult();
        boolean isApproval = StringUtils.hasText(approval);

        if (current == 3 && isApproval) {
            db.setApprovalResult(approval);
            db.setApprovalComment(body.getApprovalComment());
            if ("\u5ba1\u6279\u901a\u8fc7".equals(approval) || "\u901a\u8fc7".equals(approval)) {
                db.setStep(4);
                db.setStepStatus("\u8fdb\u884c\u4e2d");
                db.setFlowStatus("\u7533\u8bf7\u4e2d");
            } else if ("\u5ba1\u6279\u62d2\u7edd".equals(approval) || "\u62d2\u7edd".equals(approval)) {
                db.setStep(3);
                db.setStepStatus("\u5df2\u5173\u95ed");
                db.setFlowStatus("\u5df2\u5173\u95ed");
                db.setFinishTime(LocalDateTime.now());
            } else if ("\u9a73\u56de".equals(approval)) {
                db.setStep(2);
                db.setStepStatus("\u8fdb\u884c\u4e2d");
                db.setFlowStatus("\u7533\u8bf7\u4e2d");
            } else {
                return Result.fail("\u65e0\u6548\u7684\u5ba1\u6279\u7ed3\u679c");
            }
            checkinMapper.updateById(db);
            return Result.ok(String.valueOf(db.getStep()));
        }

        if (reqStep == null) return Result.fail("\u7f3a\u5c11\u76ee\u6807\u6b65\u9aa4");
        if (current == 5 && (reqStep == 5 || "\u5df2\u5b8c\u6210".equals(body.getFlowStatus()) || "\u5df2\u5b8c\u6210".equals(body.getStepStatus()))) {
            if (StringUtils.hasText(body.getContractName())) db.setContractName(body.getContractName());
            if (body.getSignDate() != null) db.setSignDate(body.getSignDate());
            if (StringUtils.hasText(body.getContractFile())) db.setContractFile(body.getContractFile());
            if (StringUtils.hasText(body.getContractNo())) db.setContractNo(body.getContractNo());
            if (StringUtils.hasText(body.getThirdPartyName())) db.setThirdPartyName(body.getThirdPartyName());
            if (StringUtils.hasText(body.getThirdPartyPhone())) db.setThirdPartyPhone(body.getThirdPartyPhone());
            db.setStep(5);
            db.setStepStatus("\u5df2\u5b8c\u6210");
            db.setFlowStatus("\u5df2\u5b8c\u6210");
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
            return Result.fail("\u8bf7\u5148\u5b8c\u6210\u5f53\u524d\u6b65\u9aa4\uff0c\u4e0d\u53ef\u8df3\u6b65");
        }
        mergeStepFields(db, body, current);
        db.setStep(reqStep);
        db.setStepStatus("\u8fdb\u884c\u4e2d");
        db.setFlowStatus("\u7533\u8bf7\u4e2d");
        checkinMapper.updateById(db);
        return Result.ok(String.valueOf(db.getStep()));
    }

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
                    .in(Checkin::getFlowStatus, "\u7533\u8bf7\u4e2d", "\u5df2\u5b8c\u6210")
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

    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Checkin db = checkinMapper.selectById(id);
        if (db == null) return Result.fail("\u5165\u4f4f\u5355\u4e0d\u5b58\u5728");
        if (!"\u7533\u8bf7\u4e2d".equals(db.getFlowStatus())) {
            return Result.fail("\u4ec5\u7533\u8bf7\u4e2d\u53ef\u64a4\u9500");
        }
        db.setFlowStatus("\u5df2\u5173\u95ed");
        db.setStepStatus("\u5df2\u5173\u95ed");
        checkinMapper.updateById(db);
        return Result.ok("ok");
    }
}