package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.config.DemoDataFill;
import com.soft.mapper.LeaveMapper;
import com.soft.pojo.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
            l = leaveMapper.selectOne(new LambdaQueryWrapper<Leave>().last("limit 1"));
        }
        if (l == null) {
            l = DemoDataFill.demoLeave(id);
        }
        DemoDataFill.fillLeave(l);
        return Result.ok(l);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Leave leave) {
        if (leave.getId() == null) {
            if (leave.getDocNo() == null) leave.setDocNo("QJ" + System.currentTimeMillis());
            leave.setStatus("\u8bf7\u5047\u4e2d");
            leave.setApplyTime(LocalDateTime.now());
            leaveMapper.insert(leave);
        } else {
            leaveMapper.updateById(leave);
        }
        return Result.ok("saved");
    }

    @PostMapping("/return")
    public Result<String> returnBack(@RequestBody Leave leave) {
        leave.setStatus("\u5df2\u8fd4\u56de");
        leave.setActualReturnTime(LocalDateTime.now());
        leaveMapper.updateById(leave);
        return Result.ok("ok");
    }
}
