package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.VisitMapper;
import com.soft.pojo.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/visit")
public class VisitController {
    @Autowired private VisitMapper visitMapper;

    @PostMapping("/page")
    public Result<List<Visit>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Visit> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getType()) && !"\u5168\u90e8".equals(q.getType())) w.eq(Visit::getType, q.getType());
        if (StringUtils.hasText(q.getVisitorName())) w.like(Visit::getVisitorName, q.getVisitorName());
        if (StringUtils.hasText(q.getVisitorPhone())) w.like(Visit::getVisitorPhone, q.getVisitorPhone());
        w.orderByDesc(Visit::getCreateTime);
        Page<Visit> page = visitMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Visit v) {
        if (v.getCreateTime() == null) v.setCreateTime(LocalDateTime.now());
        if (v.getId() == null) visitMapper.insert(v);
        else visitMapper.updateById(v);
        return Result.ok("saved");
    }
}
