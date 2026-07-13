package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.dto.UserLineDto;
import com.soft.mapper.LeaveMapper;
import com.soft.pojo.Leave;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 请假管理：分页、详情、申请、审批、销假、撤销。
 * 业务状态 status：待审批 → 请假中 / 已拒绝；请假中/超时未归 → 已返回；待审批可撤销为已关闭。
 * 与协同模块映射关系见 CollaborationController#mapLeaveFlow。
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired private LeaveMapper leaveMapper;

    /** 请假单分页；status=全部 时不过滤状态。 */
    @PostMapping("/page")
    public Result<List<Leave>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Leave> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Leave::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Leave::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getElderIdcard())) w.like(Leave::getElderIdcard, q.getElderIdcard());
        if (StringUtils.hasText(q.getStatus()) && !"全部".equals(q.getStatus())) w.eq(Leave::getStatus, q.getStatus());
        w.orderByDesc(Leave::getCreateTime);
        Page<Leave> page = leaveMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Leave> detail(@RequestParam("id") Long id) {
        Leave l = leaveMapper.selectById(id);
        if (l == null) {
            return Result.fail("请假单不存在");
        }
        return Result.ok(l);
    }

    /**
     * 新建或更新请假单。新建时默认 status=待审批，并从 Session 写入 applicant/creator。
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Leave leave, HttpSession session) {
        if (leave.getId() == null) {
            if (!StringUtils.hasText(leave.getElderName()) || !StringUtils.hasText(leave.getElderIdcard())) {
                return Result.fail("请选择老人");
            }
            if (leave.getStartTime() == null || leave.getExpectReturnTime() == null) {
                return Result.fail("请填写请假时间");
            }
            if (!StringUtils.hasText(leave.getReason())) {
                return Result.fail("请填写请假原因");
            }
            if (leave.getDocNo() == null) leave.setDocNo("QJ" + System.currentTimeMillis());
            leave.setStatus("待审批");
            leave.setApplyTime(LocalDateTime.now());
            if (leave.getCreateTime() == null) leave.setCreateTime(LocalDateTime.now());
            if (leave.getLeaveDays() == null
                    && leave.getStartTime() != null
                    && leave.getExpectReturnTime() != null) {
                long days = java.time.Duration.between(leave.getStartTime(), leave.getExpectReturnTime()).toDays();
                leave.setLeaveDays((int) Math.max(1, days));
            }
            if (!StringUtils.hasText(leave.getEscort())) leave.setEscort("无");
            fillApplicantFromSession(leave, session);
            leaveMapper.insert(leave);
        } else {
            leaveMapper.updateById(leave);
        }
        return Result.ok("saved");
    }

    /** 从 Session 补全 applicant/creator（与登录 realname 一致） */
    private void fillApplicantFromSession(Leave leave, HttpSession session) {
        String name = null;
        if (session != null) {
            Object online = session.getAttribute("online");
            if (online instanceof UserLineDto dto && StringUtils.hasText(dto.getRealname())) {
                name = dto.getRealname().trim();
            }
        }
        if (!StringUtils.hasText(name)) return;
        if (!StringUtils.hasText(leave.getApplicant())) leave.setApplicant(name);
        if (!StringUtils.hasText(leave.getCreator())) leave.setCreator(name);
    }

    /** 审批：仅「待审批」可操作。通过 → 请假中；拒绝 → 已拒绝。 */
    @PostMapping("/approve")
    public Result<String> approve(@RequestBody Map<String, Object> body) {
        Number idNum = (Number) body.get("id");
        if (idNum == null) return Result.fail("请假单不存在");
        Leave db = leaveMapper.selectById(idNum.longValue());
        if (db == null) return Result.fail("请假单不存在");
        if (!"待审批".equals(db.getStatus())) {
            return Result.fail("当前状态不可审批");
        }
        String approval = body.get("approvalResult") == null ? null : String.valueOf(body.get("approvalResult"));
        if ("通过".equals(approval) || "审批通过".equals(approval)) {
            db.setStatus("请假中");
        } else if ("拒绝".equals(approval) || "审批拒绝".equals(approval)) {
            db.setStatus("已拒绝");
        } else {
            return Result.fail("请选择审批结果");
        }
        leaveMapper.updateById(db);
        return Result.ok(db.getStatus());
    }

    /** 销假：仅请假中或超时未归可标记已返回。 */
    @PostMapping("/return")
    public Result<String> returnBack(@RequestBody Leave leave) {
        if (leave.getId() == null) return Result.fail("请假单不存在");
        Leave db = leaveMapper.selectById(leave.getId());
        if (db == null) return Result.fail("请假单不存在");
        if (!"请假中".equals(db.getStatus()) && !"超时未归".equals(db.getStatus())) {
            return Result.fail("仅请假中可销假");
        }
        db.setStatus("已返回");
        db.setActualReturnTime(LocalDateTime.now());
        if (StringUtils.hasText(leave.getReturnRemark())) db.setReturnRemark(leave.getReturnRemark());
        if (StringUtils.hasText(leave.getCancelUser())) db.setCancelUser(leave.getCancelUser());
        else if (StringUtils.hasText(leave.getApplicant())) db.setCancelUser(leave.getApplicant());
        db.setCancelTime(LocalDateTime.now());
        leaveMapper.updateById(db);
        return Result.ok("ok");
    }

    /** 撤销：仅待审批可关闭。 */
    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Leave db = leaveMapper.selectById(id);
        if (db == null) return Result.fail("请假单不存在");
        if (!"待审批".equals(db.getStatus())) {
            return Result.fail("仅待审批可撤销");
        }
        db.setStatus("已关闭");
        leaveMapper.updateById(db);
        return Result.ok("ok");
    }
}
