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

/**
 * 来访登记接口。
 * <p>
 * 管理实际到访记录的分页查询与手工登记。
 * 预约确认到访时也会由 ReservationController 写入来访表。
 */
@RestController
@RequestMapping("/visit")
public class VisitController {
    @Autowired private VisitMapper visitMapper;

    /**
     * 来访分页列表。
     * 可按来访类型、访客姓名/手机筛选；类型为「全部」时不过滤。
     *
     * @param q 分页与筛选条件
     */
    @PostMapping("/page")
    public Result<List<Visit>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Visit> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getType()) && !"全部".equals(q.getType())) w.eq(Visit::getType, q.getType());
        if (StringUtils.hasText(q.getVisitorName())) w.like(Visit::getVisitorName, q.getVisitorName());
        if (StringUtils.hasText(q.getVisitorPhone())) w.like(Visit::getVisitorPhone, q.getVisitorPhone());
        w.orderByDesc(Visit::getCreateTime);
        Page<Visit> page = visitMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 新增或更新来访记录。
     * 创建时间缺省为当前时间。
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Visit v) {
        if (v.getCreateTime() == null) v.setCreateTime(LocalDateTime.now());
        if (v.getId() == null) visitMapper.insert(v);
        else visitMapper.updateById(v);
        return Result.ok("saved");
    }
}
