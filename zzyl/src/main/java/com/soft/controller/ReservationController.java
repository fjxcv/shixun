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

@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private VisitMapper visitMapper;

    @PostMapping("/page")
    public Result<List<Reservation>> page(@RequestBody PageQueryDto q) {
        Page<Reservation> page = reservationMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Reservation r) {
        if (r.getStatus() == null) r.setStatus("\u5f85\u4e0a\u95e8");
        if (r.getCreateTime() == null) r.setCreateTime(LocalDateTime.now());
        if (r.getId() == null) reservationMapper.insert(r);
        else reservationMapper.updateById(r);
        return Result.ok("saved");
    }

    @PostMapping("/arrive")
    public Result<String> arrive(@RequestBody ReservationArriveDto dto) {
        if (dto.getId() == null) return Result.fail("\u9884\u7ea6\u4e0d\u5b58\u5728");
        if (!StringUtils.hasText(dto.getVisitTime())) return Result.fail("\u8bf7\u9009\u62e9\u6765\u8bbf\u65f6\u95f4");

        Reservation existing = reservationMapper.selectById(dto.getId());
        if (existing == null) return Result.fail("\u9884\u7ea6\u4e0d\u5b58\u5728");

        Reservation r = new Reservation();
        r.setId(dto.getId());
        r.setStatus("\u5df2\u5b8c\u6210");
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

    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        if (id == null) return Result.fail("\u9884\u7ea6ID\u4e0d\u80fd\u4e3a\u7a7a");
        Reservation existing = reservationMapper.selectById(id);
        if (existing == null) return Result.fail("\u9884\u7ea6\u4e0d\u5b58\u5728");
        String status = existing.getStatus() == null ? "" : existing.getStatus().trim();
        if ("\u5df2\u53d6\u6d88".equals(status) || status.contains("\u53d6\u6d88")) {
            return Result.fail("\u8be5\u9884\u7ea6\u5df2\u53d6\u6d88");
        }
        if ("\u5df2\u5b8c\u6210".equals(status) || status.contains("\u5b8c\u6210")) {
            return Result.fail("\u5df2\u5b8c\u6210\u7684\u9884\u7ea6\u4e0d\u53ef\u53d6\u6d88");
        }
        Reservation r = new Reservation();
        r.setId(id);
        r.setStatus("\u5df2\u53d6\u6d88");
        int rows = reservationMapper.updateById(r);
        if (rows <= 0) return Result.fail("\u53d6\u6d88\u5931\u8d25");
        return Result.ok("ok");
    }

    private String mapVisitType(String reservationType) {
        if ("\u53c2\u89c2\u9884\u7ea6".equals(reservationType)) return "\u53c2\u89c2\u6765\u8bbf";
        if ("\u63a2\u8bbf\u9884\u7ea6".equals(reservationType)) return "\u63a2\u8bbf\u6765\u8bbf";
        return reservationType;
    }

    private LambdaQueryWrapper<Reservation> buildWrapper(PageQueryDto q) {
        LambdaQueryWrapper<Reservation> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getType()) && !"\u5168\u90e8".equals(q.getType())) w.eq(Reservation::getType, q.getType());
        if (StringUtils.hasText(q.getVisitorName())) w.like(Reservation::getVisitorName, q.getVisitorName());
        if (StringUtils.hasText(q.getVisitorPhone())) w.like(Reservation::getVisitorPhone, q.getVisitorPhone());
        if (StringUtils.hasText(q.getStatus())) w.eq(Reservation::getStatus, q.getStatus());
        w.orderByDesc(Reservation::getCreateTime);
        return w;
    }
}

