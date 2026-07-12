package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.config.DemoDataFill;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.CheckinMapper;
import com.soft.pojo.Checkin;
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
        w.orderByDesc(Checkin::getCreateTime);
        Page<Checkin> page = checkinMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Checkin> detail(@RequestParam("id") Long id) {
        Checkin c = checkinMapper.selectById(id);
        if (c == null) {
            c = checkinMapper.selectOne(new LambdaQueryWrapper<Checkin>().last("limit 1"));
        }
        if (c == null) {
            c = DemoDataFill.demoCheckin(id);
        }
        DemoDataFill.fillCheckin(c);
        return Result.ok(c);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Checkin c) {
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
            if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("\u987e\u5ef7\u70ec");
            if (!StringUtils.hasText(c.getCreator())) c.setCreator("\u987e\u5ef7\u70ec");
            checkinMapper.insert(c);
            return Result.ok(String.valueOf(c.getId()));
        }
        checkinMapper.updateById(c);
        return Result.ok(String.valueOf(c.getId()));
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
        String approval = body.getApprovalResult();

        if (current == 3) {
            if (!StringUtils.hasText(approval)) {
                return Result.fail("\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c");
            }
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
            db.setStep(5);
            db.setStepStatus("\u5df2\u5b8c\u6210");
            db.setFlowStatus("\u5df2\u5b8c\u6210");
            db.setFinishTime(LocalDateTime.now());
            checkinMapper.updateById(db);
            return Result.ok("5");
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
            if (body.getPeriodStart() != null) db.setPeriodStart(body.getPeriodStart());
            if (body.getPeriodEnd() != null) db.setPeriodEnd(body.getPeriodEnd());
            if (StringUtils.hasText(body.getBedNo())) db.setBedNo(body.getBedNo());
        } else if (current == 5) {
            if (StringUtils.hasText(body.getContractName())) db.setContractName(body.getContractName());
            if (body.getSignDate() != null) db.setSignDate(body.getSignDate());
            if (StringUtils.hasText(body.getContractFile())) db.setContractFile(body.getContractFile());
        }
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