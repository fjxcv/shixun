package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.dto.ReservationArriveDto;
import com.soft.mapper.ReservationMapper;
import com.soft.mapper.VisitMapper;
import com.soft.pojo.Reservation;
import com.soft.pojo.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约登记接口。
 * <p>
 * 管理参观/探访预约的分页、保存、到访确认与取消。
 * 「已到访」会将预约置为已完成，并自动写入一条来访登记记录。
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private VisitMapper visitMapper;

    /**
     * 预约分页列表。
     * 可按类型、访客姓名/手机、状态筛选；类型为「全部」时不过滤类型。
     *
     * @param q 分页与筛选条件
     */
    @PostMapping("/page")
    public Result<List<Reservation>> page(@RequestBody PageQueryDto q) {
        Page<Reservation> page = reservationMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 新增或更新预约。
     * 缺省状态为「待上门」，创建时间缺省为当前时间。
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Reservation r) {
        if (r.getStatus() == null) r.setStatus("待上门");
        if (r.getCreateTime() == null) r.setCreateTime(LocalDateTime.now());
        if (r.getId() == null) reservationMapper.insert(r);
        else reservationMapper.updateById(r);
        return Result.ok("saved");
    }

    /**
     * 确认到访：预约改为「已完成」，并同步生成来访记录。
     * 需传入预约 id 与 visitTime（yyyy-MM-dd HH:mm:ss）；
     * 来访类型由预约类型映射（参观预约→参观来访，探访预约→探访来访）。
     *
     * @param dto 到访确认参数
     */
    @PostMapping("/arrive")
    public Result<String> arrive(@RequestBody ReservationArriveDto dto) {
        if (dto.getId() == null) return Result.fail("预约不存在");
        if (!StringUtils.hasText(dto.getVisitTime())) return Result.fail("请选择来访时间");

        Reservation existing = reservationMapper.selectById(dto.getId());
        if (existing == null) return Result.fail("预约不存在");

        Reservation r = new Reservation();
        r.setId(dto.getId());
        r.setStatus("已完成");
        reservationMapper.updateById(r);

        Visit v = new Visit();
        v.setType(mapVisitType(existing.getType()));
        v.setVisitorName(existing.getVisitorName());
        v.setVisitorPhone(existing.getVisitorPhone());
        v.setElderName(existing.getElderName());
        v.setVisitTime(LocalDateTime.parse(dto.getVisitTime(), FMT));
        v.setCreator(existing.getCreator());
        v.setCreateTime(LocalDateTime.now());
        visitMapper.insert(v);

        return Result.ok("ok");
    }

    /**
     * 取消预约。
     * 已取消或已完成的预约不可再取消。
     *
     * @param id 预约 ID
     */
    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        if (id == null) return Result.fail("预约ID不能为空");
        Reservation existing = reservationMapper.selectById(id);
        if (existing == null) return Result.fail("预约不存在");
        String status = existing.getStatus() == null ? "" : existing.getStatus().trim();
        if ("已取消".equals(status) || status.contains("取消")) {
            return Result.fail("该预约已取消");
        }
        if ("已完成".equals(status) || status.contains("完成")) {
            return Result.fail("已完成的预约不可取消");
        }
        Reservation r = new Reservation();
        r.setId(id);
        r.setStatus("已取消");
        int rows = reservationMapper.updateById(r);
        if (rows <= 0) return Result.fail("取消失败");
        return Result.ok("ok");
    }

    /** 预约类型 → 来访类型映射。 */
    private String mapVisitType(String reservationType) {
        if ("参观预约".equals(reservationType)) return "参观来访";
        if ("探访预约".equals(reservationType)) return "探访来访";
        return reservationType;
    }

    private LambdaQueryWrapper<Reservation> buildWrapper(PageQueryDto q) {
        LambdaQueryWrapper<Reservation> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getType()) && !"全部".equals(q.getType())) w.eq(Reservation::getType, q.getType());
        if (StringUtils.hasText(q.getVisitorName())) w.like(Reservation::getVisitorName, q.getVisitorName());
        if (StringUtils.hasText(q.getVisitorPhone())) w.like(Reservation::getVisitorPhone, q.getVisitorPhone());
        if (StringUtils.hasText(q.getStatus())) w.eq(Reservation::getStatus, q.getStatus());
        w.orderByDesc(Reservation::getCreateTime);
        return w;
    }
}
