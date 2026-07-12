package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.config.DemoDataFill;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.CheckoutMapper;
import com.soft.pojo.Checkout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    @Autowired private CheckoutMapper checkoutMapper;

    @PostMapping("/page")
    public Result<List<Checkout>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Checkout> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Checkout::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Checkout::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getElderIdcard())) w.like(Checkout::getElderIdcard, q.getElderIdcard());
        w.orderByDesc(Checkout::getCreateTime);
        Page<Checkout> page = checkoutMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Checkout> detail(@RequestParam("id") Long id) {
        Checkout c = checkoutMapper.selectById(id);
        if (c == null) {
            c = checkoutMapper.selectOne(new LambdaQueryWrapper<Checkout>().last("limit 1"));
        }
        if (c == null) {
            c = DemoDataFill.demoCheckout(id);
        }
        DemoDataFill.fillCheckout(c);
        return Result.ok(c);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Checkout c) {
        if (!StringUtils.hasText(c.getElderName()) || c.getCheckoutDate() == null || !StringUtils.hasText(c.getReason())) {
            return Result.fail("\u8bf7\u5b8c\u5584\u9000\u4f4f\u7533\u8bf7\u5fc5\u586b\u4fe1\u606f");
        }
        if (c.getId() == null) {
            if (c.getDocNo() == null) c.setDocNo("TZ" + System.currentTimeMillis());
            c.setStep(2);
            c.setStepStatus("\u8fdb\u884c\u4e2d");
            c.setFlowStatus("\u7533\u8bf7\u4e2d");
            c.setApplyTime(LocalDateTime.now());
            c.setCreateTime(LocalDateTime.now());
            if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("\u987e\u5ef7\u70ec");
            if (!StringUtils.hasText(c.getCreator())) c.setCreator("\u987e\u5ef7\u70ec");
            checkoutMapper.insert(c);
            return Result.ok(String.valueOf(c.getId()));
        }
        checkoutMapper.updateById(c);
        return Result.ok(String.valueOf(c.getId()));
    }

    @PostMapping("/updateStep")
    public Result<String> updateStep(@RequestBody Checkout body) {
        if (body.getId() == null) return Result.fail("\u9000\u4f4f\u5355\u4e0d\u5b58\u5728");
        Checkout db = checkoutMapper.selectById(body.getId());
        if (db == null) return Result.fail("\u9000\u4f4f\u5355\u4e0d\u5b58\u5728");
        if ("\u5df2\u5b8c\u6210".equals(db.getFlowStatus()) || "\u5df2\u5173\u95ed".equals(db.getFlowStatus())
                || "\u5df2\u5b8c\u6210".equals(db.getStepStatus()) || "\u5df2\u5173\u95ed".equals(db.getStepStatus())) {
            return Result.fail("\u5f53\u524d\u6d41\u7a0b\u5df2\u7ed3\u675f");
        }
        int current = db.getStep() == null ? 1 : db.getStep();
        Integer reqStep = body.getStep();
        String approval = body.getApprovalResult();

        if (current == 2 || current == 5 || current == 6) {
            if (!StringUtils.hasText(approval)) {
                return Result.fail("\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c");
            }
            db.setApprovalResult(approval);
            db.setApprovalComment(body.getApprovalComment());
            if ("\u901a\u8fc7".equals(approval) || "\u5ba1\u6279\u901a\u8fc7".equals(approval)) {
                db.setStep(current + 1);
                db.setStepStatus("\u8fdb\u884c\u4e2d");
                db.setFlowStatus("\u7533\u8bf7\u4e2d");
            } else if ("\u62d2\u7edd".equals(approval) || "\u5ba1\u6279\u62d2\u7edd".equals(approval)) {
                db.setStepStatus("\u5df2\u5173\u95ed");
                db.setFlowStatus("\u5df2\u5173\u95ed");
            } else if ("\u9000\u56de".equals(approval) || "\u9a73\u56de".equals(approval)) {
                db.setStep(Math.max(1, current - 1));
                db.setStepStatus("\u8fdb\u884c\u4e2d");
                db.setFlowStatus("\u7533\u8bf7\u4e2d");
            } else {
                return Result.fail("\u65e0\u6548\u7684\u5ba1\u6279\u7ed3\u679c");
            }
            checkoutMapper.updateById(db);
            return Result.ok(String.valueOf(db.getStep()));
        }

        if (reqStep == null) return Result.fail("\u7f3a\u5c11\u76ee\u6807\u6b65\u9aa4");
        if (current == 7 && reqStep == 7) {
            if (body.getRefundAmount() != null) db.setRefundAmount(body.getRefundAmount());
            db.setStep(7);
            db.setStepStatus("\u5df2\u5b8c\u6210");
            db.setFlowStatus("\u5df2\u5b8c\u6210");
            checkoutMapper.updateById(db);
            return Result.ok("7");
        }
        if (reqStep != current + 1) {
            return Result.fail("\u8bf7\u5148\u5b8c\u6210\u5f53\u524d\u6b65\u9aa4\uff0c\u4e0d\u53ef\u8df3\u6b65");
        }
        if (current == 3) {
            if (body.getTerminateDate() == null || !StringUtils.hasText(body.getTerminateFile())) {
                return Result.fail("\u8bf7\u5b8c\u5584\u89e3\u9664\u534f\u8bae\u4fe1\u606f");
            }
            db.setTerminateDate(body.getTerminateDate());
            db.setTerminateFile(body.getTerminateFile());
        } else if (current == 4) {
            if (body.getRefundAmount() != null) db.setRefundAmount(body.getRefundAmount());
            else if (db.getRefundAmount() == null) db.setRefundAmount(new BigDecimal("20.00"));
        }
        db.setStep(reqStep);
        db.setStepStatus("\u8fdb\u884c\u4e2d");
        db.setFlowStatus("\u7533\u8bf7\u4e2d");
        checkoutMapper.updateById(db);
        return Result.ok(String.valueOf(db.getStep()));
    }

    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Checkout db = checkoutMapper.selectById(id);
        if (db == null) return Result.fail("\u9000\u4f4f\u5355\u4e0d\u5b58\u5728");
        if (!"\u7533\u8bf7\u4e2d".equals(db.getFlowStatus()) && !"\u8fdb\u884c\u4e2d".equals(db.getStepStatus())) {
            return Result.fail("\u4ec5\u8fdb\u884c\u4e2d\u53ef\u64a4\u9500");
        }
        db.setFlowStatus("\u5df2\u5173\u95ed");
        db.setStepStatus("\u5df2\u5173\u95ed");
        checkoutMapper.updateById(db);
        return Result.ok("ok");
    }
}