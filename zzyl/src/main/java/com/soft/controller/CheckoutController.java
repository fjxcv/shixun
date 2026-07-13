package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.dto.UserLineDto;
import com.soft.mapper.CheckoutMapper;
import com.soft.pojo.Checkout;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退住管理：分页查询、详情、新建申请、多步骤审批推进、撤销。
 * <p>
 * 步骤约定（step）：新建后从 2 开始；2/5/6 为审批节点；3 解除协议；4 账单核算；7 退款完成。
 * 流程状态 flowStatus：申请中 / 已完成 / 已关闭。
 */
@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    @Autowired private CheckoutMapper checkoutMapper;

    /** 退住单分页列表，支持单据号、老人姓名、身份证模糊查询。 */
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
            return Result.fail("退住单不存在");
        }
        return Result.ok(c);
    }

    /**
     * 新建或更新退住单。新建时生成单据号 TZ*，初始化 step=2、申请中，
     * 并从 Session 补全 applicant/creator。
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Checkout c, HttpSession session) {
        if (!StringUtils.hasText(c.getElderName()) || c.getCheckoutDate() == null || !StringUtils.hasText(c.getReason())) {
            return Result.fail("请完善退住申请必填信息");
        }
        if (c.getId() == null) {
            if (c.getDocNo() == null) c.setDocNo("TZ" + System.currentTimeMillis());
            c.setStep(2);
            c.setStepStatus("进行中");
            c.setFlowStatus("申请中");
            c.setApplyTime(LocalDateTime.now());
            c.setCreateTime(LocalDateTime.now());
            fillApplicantFromSession(c, session);
            checkoutMapper.insert(c);
            return Result.ok(String.valueOf(c.getId()));
        }
        checkoutMapper.updateById(c);
        return Result.ok(String.valueOf(c.getId()));
    }

    /**
     * 从 Session 补全 applicant/creator（与登录 realname 一致）。
     * 无登录信息时回退为演示账号「顾廷烬」，便于本地联调。
     */
    private void fillApplicantFromSession(Checkout c, HttpSession session) {
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
     * 推进退住流程步骤。step 2/5/6 为审批；3 解除协议；4 账单；7 办结。
     * 禁止跳步；终态单据不可再改。
     */
    @PostMapping("/updateStep")
    public Result<String> updateStep(@RequestBody Checkout body) {
        if (body.getId() == null) return Result.fail("退住单不存在");
        Checkout db = checkoutMapper.selectById(body.getId());
        if (db == null) return Result.fail("退住单不存在");
        if ("已完成".equals(db.getFlowStatus()) || "已关闭".equals(db.getFlowStatus())
                || "已完成".equals(db.getStepStatus()) || "已关闭".equals(db.getStepStatus())) {
            return Result.fail("当前流程已结束");
        }
        int current = db.getStep() == null ? 1 : db.getStep();
        Integer reqStep = body.getStep();
        String approval = body.getApprovalResult();

        if (current == 2 || current == 5 || current == 6) {
            if (!StringUtils.hasText(approval)) {
                return Result.fail("请选择审批结果");
            }
            db.setApprovalResult(approval);
            db.setApprovalComment(body.getApprovalComment());
            if ("通过".equals(approval) || "审批通过".equals(approval)) {
                db.setStep(current + 1);
                db.setStepStatus("进行中");
                db.setFlowStatus("申请中");
            } else if ("拒绝".equals(approval) || "审批拒绝".equals(approval)) {
                db.setStepStatus("已关闭");
                db.setFlowStatus("已关闭");
            } else if ("退回".equals(approval) || "驳回".equals(approval)) {
                db.setStep(Math.max(1, current - 1));
                db.setStepStatus("进行中");
                db.setFlowStatus("申请中");
            } else {
                return Result.fail("无效的审批结果");
            }
            checkoutMapper.updateById(db);
            return Result.ok(String.valueOf(db.getStep()));
        }

        if (reqStep == null) return Result.fail("缺少目标步骤");
        if (current == 7 && reqStep == 7) {
            if (body.getRefundAmount() != null) db.setRefundAmount(body.getRefundAmount());
            db.setStep(7);
            db.setStepStatus("已完成");
            db.setFlowStatus("已完成");
            checkoutMapper.updateById(db);
            return Result.ok("7");
        }
        if (reqStep != current + 1) {
            return Result.fail("请先完成当前步骤，不可跳步");
        }
        if (current == 3) {
            if (body.getTerminateDate() == null || !StringUtils.hasText(body.getTerminateFile())) {
                return Result.fail("请完善解除协议信息");
            }
            db.setTerminateDate(body.getTerminateDate());
            db.setTerminateFile(body.getTerminateFile());
        } else if (current == 4) {
            if (body.getBillReceivable() != null) db.setBillReceivable(body.getBillReceivable());
            if (body.getBillArrears() != null) db.setBillArrears(body.getBillArrears());
            if (body.getBillBalance() != null) db.setBillBalance(body.getBillBalance());
            BigDecimal receivable = db.getBillReceivable() == null ? BigDecimal.ZERO : db.getBillReceivable();
            BigDecimal arrears = db.getBillArrears() == null ? BigDecimal.ZERO : db.getBillArrears();
            BigDecimal balance = db.getBillBalance() == null ? BigDecimal.ZERO : db.getBillBalance();
            BigDecimal refund = body.getRefundAmount() != null
                    ? body.getRefundAmount()
                    : receivable.subtract(arrears).add(balance);
            db.setRefundAmount(refund);
        }
        db.setStep(reqStep);
        db.setStepStatus("进行中");
        db.setFlowStatus("申请中");
        checkoutMapper.updateById(db);
        return Result.ok(String.valueOf(db.getStep()));
    }

    /** 撤销：仅申请中/进行中可关闭。 */
    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Checkout db = checkoutMapper.selectById(id);
        if (db == null) return Result.fail("退住单不存在");
        if (!"申请中".equals(db.getFlowStatus()) && !"进行中".equals(db.getStepStatus())) {
            return Result.fail("仅进行中可撤销");
        }
        db.setFlowStatus("已关闭");
        db.setStepStatus("已关闭");
        checkoutMapper.updateById(db);
        return Result.ok("ok");
    }
}