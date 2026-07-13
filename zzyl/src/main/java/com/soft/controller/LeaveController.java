package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.LeaveMapper;
import com.soft.pojo.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired private LeaveMapper leaveMapper;

    @PostMapping("/page")
    public Result<List<Leave>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Leave> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Leave::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Leave::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getElderIdcard())) w.like(Leave::getElderIdcard, q.getElderIdcard());
        if (StringUtils.hasText(q.getStatus()) && !"\u5168\u90e8".equals(q.getStatus())) w.eq(Leave::getStatus, q.getStatus());
        w.orderByDesc(Leave::getCreateTime);
        Page<Leave> page = leaveMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Leave> detail(@RequestParam("id") Long id) {
        Leave l = leaveMapper.selectById(id);
        if (l == null) {
            return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        }
        return Result.ok(l);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Leave leave) {
        if (leave.getId() == null) {
            if (!StringUtils.hasText(leave.getElderName()) || !StringUtils.hasText(leave.getElderIdcard())) {
                return Result.fail("\u8bf7\u9009\u62e9\u8001\u4eba");
            }
            if (leave.getStartTime() == null || leave.getExpectReturnTime() == null) {
                return Result.fail("\u8bf7\u586b\u5199\u8bf7\u5047\u65f6\u95f4");
            }
            if (!StringUtils.hasText(leave.getReason())) {
                return Result.fail("\u8bf7\u586b\u5199\u8bf7\u5047\u539f\u56e0");
            }
            if (leave.getDocNo() == null) leave.setDocNo("QJ" + System.currentTimeMillis());
            leave.setStatus("\u5f85\u5ba1\u6279");
            leave.setApplyTime(LocalDateTime.now());
            if (leave.getCreateTime() == null) leave.setCreateTime(LocalDateTime.now());
            if (leave.getLeaveDays() == null
                    && leave.getStartTime() != null
                    && leave.getExpectReturnTime() != null) {
                long days = java.time.Duration.between(leave.getStartTime(), leave.getExpectReturnTime()).toDays();
                leave.setLeaveDays((int) Math.max(1, days));
            }
            if (!StringUtils.hasText(leave.getEscort())) leave.setEscort("\u65e0");
            leaveMapper.insert(leave);
        } else {
            leaveMapper.updateById(leave);
        }
        return Result.ok("saved");
    }

    @PostMapping("/approve")
    public Result<String> approve(@RequestBody Map<String, Object> body) {
        Number idNum = (Number) body.get("id");
        if (idNum == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        Leave db = leaveMapper.selectById(idNum.longValue());
        if (db == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        if (!"\u5f85\u5ba1\u6279".equals(db.getStatus())) {
            return Result.fail("\u5f53\u524d\u72b6\u6001\u4e0d\u53ef\u5ba1\u6279");
        }
        String approval = body.get("approvalResult") == null ? null : String.valueOf(body.get("approvalResult"));
        if ("\u901a\u8fc7".equals(approval) || "\u5ba1\u6279\u901a\u8fc7".equals(approval)) {
            db.setStatus("\u8bf7\u5047\u4e2d");
        } else if ("\u62d2\u7edd".equals(approval) || "\u5ba1\u6279\u62d2\u7edd".equals(approval)) {
            db.setStatus("\u5df2\u62d2\u7edd");
        } else {
            return Result.fail("\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c");
        }
        leaveMapper.updateById(db);
        return Result.ok(db.getStatus());
    }

    @PostMapping("/return")
    public Result<String> returnBack(@RequestBody Leave leave) {
        if (leave.getId() == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        Leave db = leaveMapper.selectById(leave.getId());
        if (db == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        if (!"\u8bf7\u5047\u4e2d".equals(db.getStatus()) && !"\u8d85\u65f6\u672a\u5f52".equals(db.getStatus())) {
            return Result.fail("\u4ec5\u8bf7\u5047\u4e2d\u53ef\u9500\u5047");
        }
        db.setStatus("\u5df2\u8fd4\u56de");
        db.setActualReturnTime(LocalDateTime.now());
        if (StringUtils.hasText(leave.getReturnRemark())) db.setReturnRemark(leave.getReturnRemark());
        if (StringUtils.hasText(leave.getCancelUser())) db.setCancelUser(leave.getCancelUser());
        else if (StringUtils.hasText(leave.getApplicant())) db.setCancelUser(leave.getApplicant());
        db.setCancelTime(LocalDateTime.now());
        leaveMapper.updateById(db);
        return Result.ok("ok");
    }

    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Leave db = leaveMapper.selectById(id);
        if (db == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        if (!"\u5f85\u5ba1\u6279".equals(db.getStatus())) {
            return Result.fail("\u4ec5\u5f85\u5ba1\u6279\u53ef\u64a4\u9500");
        }
        db.setStatus("\u5df2\u5173\u95ed");
        leaveMapper.updateById(db);
        return Result.ok("ok");
    }
}
