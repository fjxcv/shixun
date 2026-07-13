# -*- coding: utf-8 -*-
from pathlib import Path
CTRL = Path(r"D:/vsc-maven/zzyl-project/zzyl/src/main/java/com/soft/controller")

def w(name, content):
    (CTRL / name).write_text(content, encoding="utf-8", newline="\n")
    print("wrote", name)

w("ReservationController.java", """package com.soft.controller;

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
 * \u9884\u7ea6\u767b\u8bb0\u63a5\u53e3\u3002
 * <p>
 * \u7ba1\u7406\u53c2\u89c2/\u63a2\u8bbf\u9884\u7ea6\u7684\u5206\u9875\u3001\u4fdd\u5b58\u3001\u5230\u8bbf\u786e\u8ba4\u4e0e\u53d6\u6d88\u3002
 * \u300c\u5df2\u5230\u8bbf\u300d\u4f1a\u5c06\u9884\u7ea6\u7f6e\u4e3a\u5df2\u5b8c\u6210\uff0c\u5e76\u81ea\u52a8\u5199\u5165\u4e00\u6761\u6765\u8bbf\u767b\u8bb0\u8bb0\u5f55\u3002
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private VisitMapper visitMapper;

    /**
     * \u9884\u7ea6\u5206\u9875\u5217\u8868\u3002
     * \u53ef\u6309\u7c7b\u578b\u3001\u8bbf\u5ba2\u59d3\u540d/\u624b\u673a\u3001\u72b6\u6001\u7b5b\u9009\uff1b\u7c7b\u578b\u4e3a\u300c\u5168\u90e8\u300d\u65f6\u4e0d\u8fc7\u6ee4\u7c7b\u578b\u3002
     *
     * @param q \u5206\u9875\u4e0e\u7b5b\u9009\u6761\u4ef6
     */
    @PostMapping("/page")
    public Result<List<Reservation>> page(@RequestBody PageQueryDto q) {
        Page<Reservation> page = reservationMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * \u65b0\u589e\u6216\u66f4\u65b0\u9884\u7ea6\u3002
     * \u7f3a\u7701\u72b6\u6001\u4e3a\u300c\u5f85\u4e0a\u95e8\u300d\uff0c\u521b\u5efa\u65f6\u95f4\u7f3a\u7701\u4e3a\u5f53\u524d\u65f6\u95f4\u3002
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Reservation r) {
        if (r.getStatus() == null) r.setStatus("\u5f85\u4e0a\u95e8");
        if (r.getCreateTime() == null) r.setCreateTime(LocalDateTime.now());
        if (r.getId() == null) reservationMapper.insert(r);
        else reservationMapper.updateById(r);
        return Result.ok("saved");
    }

    /**
     * \u786e\u8ba4\u5230\u8bbf\uff1a\u9884\u7ea6\u6539\u4e3a\u300c\u5df2\u5b8c\u6210\u300d\uff0c\u5e76\u540c\u6b65\u751f\u6210\u6765\u8bbf\u8bb0\u5f55\u3002
     * \u9700\u4f20\u5165\u9884\u7ea6 id \u4e0e visitTime\uff08yyyy-MM-dd HH:mm:ss\uff09\uff1b
     * \u6765\u8bbf\u7c7b\u578b\u7531\u9884\u7ea6\u7c7b\u578b\u6620\u5c04\uff08\u53c2\u89c2\u9884\u7ea6\u2192\u53c2\u89c2\u6765\u8bbf\uff0c\u63a2\u8bbf\u9884\u7ea6\u2192\u63a2\u8bbf\u6765\u8bbf\uff09\u3002
     *
     * @param dto \u5230\u8bbf\u786e\u8ba4\u53c2\u6570
     */
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

    /**
     * \u53d6\u6d88\u9884\u7ea6\u3002
     * \u5df2\u53d6\u6d88\u6216\u5df2\u5b8c\u6210\u7684\u9884\u7ea6\u4e0d\u53ef\u518d\u53d6\u6d88\u3002
     *
     * @param id \u9884\u7ea6 ID
     */
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

    /** \u9884\u7ea6\u7c7b\u578b \u2192 \u6765\u8bbf\u7c7b\u578b\u6620\u5c04\u3002 */
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
""")

w("RoomTypeController.java", """package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.mapper.RoomTypeMapper;
import com.soft.pojo.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * \u623f\u578b\u8bbe\u7f6e\u63a5\u53e3\u3002
 * <p>
 * \u7ef4\u62a4\u517b\u8001\u9662\u623f\u578b\uff08\u540d\u79f0\u3001\u4ef7\u683c\u3001\u5e8a\u4f4d\u6570\u3001\u542f\u7528\u72b6\u6001\u7b49\uff09\uff0c\u4f9b\u300c\u623f\u578b\u8bbe\u7f6e\u300d\u53ca\u5e8a\u4f4d\u623f\u578b\u5173\u8054\u4f7f\u7528\u3002
 */
@RestController
@RequestMapping("/roomType")
public class RoomTypeController {
    @Autowired private RoomTypeMapper roomTypeMapper;

    /**
     * \u67e5\u8be2\u5168\u90e8\u623f\u578b\uff0c\u6309\u521b\u5efa\u65f6\u95f4\u5012\u5e8f\u3002
     */
    @GetMapping("/list")
    public Result<List<RoomType>> list() {
        return Result.ok(roomTypeMapper.selectList(
                new LambdaQueryWrapper<RoomType>().orderByDesc(RoomType::getCreateTime)));
    }

    /**
     * \u65b0\u589e\u6216\u66f4\u65b0\u623f\u578b\u3002
     * \u7f3a\u7701 status=1\uff08\u542f\u7528\uff09\uff1b\u6709 id \u5219\u66f4\u65b0\uff0c\u65e0 id \u5219\u65b0\u589e\u3002
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody RoomType rt) {
        if (rt.getStatus() == null) rt.setStatus(1);
        if (rt.getId() == null) roomTypeMapper.insert(rt);
        else roomTypeMapper.updateById(rt);
        return Result.ok("saved");
    }

    /**
     * \u6309 ID \u5220\u9664\u623f\u578b\u3002
     * \u6ce8\u610f\uff1a\u4e0d\u6821\u9a8c\u662f\u5426\u5df2\u88ab\u623f\u95f4\u5f15\u7528\uff0c\u8c03\u7528\u65b9\u9700\u81ea\u884c\u4fdd\u8bc1\u6570\u636e\u4e00\u81f4\u6027\u3002
     */
    @GetMapping("/delete")
    public Result<String> delete(@RequestParam("id") Long id) {
        roomTypeMapper.deleteById(id);
        return Result.ok("deleted");
    }

    /**
     * \u5207\u6362\u623f\u578b\u542f\u7528/\u505c\u7528\u72b6\u6001\uff081 \u2194 0\uff09\u3002
     *
     * @param id \u623f\u578b ID
     */
    @GetMapping("/toggle")
    public Result<String> toggle(@RequestParam("id") Long id) {
        RoomType rt = roomTypeMapper.selectById(id);
        rt.setStatus(rt.getStatus() == 1 ? 0 : 1);
        roomTypeMapper.updateById(rt);
        return Result.ok("ok");
    }
}
""")

w("VisitController.java", """package com.soft.controller;

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
 * \u6765\u8bbf\u767b\u8bb0\u63a5\u53e3\u3002
 * <p>
 * \u7ba1\u7406\u5b9e\u9645\u5230\u8bbf\u8bb0\u5f55\u7684\u5206\u9875\u67e5\u8be2\u4e0e\u624b\u5de5\u767b\u8bb0\u3002
 * \u9884\u7ea6\u786e\u8ba4\u5230\u8bbf\u65f6\u4e5f\u4f1a\u7531 ReservationController \u5199\u5165\u6765\u8bbf\u8868\u3002
 */
@RestController
@RequestMapping("/visit")
public class VisitController {
    @Autowired private VisitMapper visitMapper;

    /**
     * \u6765\u8bbf\u5206\u9875\u5217\u8868\u3002
     * \u53ef\u6309\u6765\u8bbf\u7c7b\u578b\u3001\u8bbf\u5ba2\u59d3\u540d/\u624b\u673a\u7b5b\u9009\uff1b\u7c7b\u578b\u4e3a\u300c\u5168\u90e8\u300d\u65f6\u4e0d\u8fc7\u6ee4\u3002
     *
     * @param q \u5206\u9875\u4e0e\u7b5b\u9009\u6761\u4ef6
     */
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

    /**
     * \u65b0\u589e\u6216\u66f4\u65b0\u6765\u8bbf\u8bb0\u5f55\u3002
     * \u521b\u5efa\u65f6\u95f4\u7f3a\u7701\u4e3a\u5f53\u524d\u65f6\u95f4\u3002
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Visit v) {
        if (v.getCreateTime() == null) v.setCreateTime(LocalDateTime.now());
        if (v.getId() == null) visitMapper.insert(v);
        else visitMapper.updateById(v);
        return Result.ok("saved");
    }
}
""")

w("WorkbenchController.java", """package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.mapper.*;
import com.soft.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * \u5de5\u4f5c\u53f0\u7edf\u8ba1\u63a5\u53e3\u3002
 * <p>
 * \u4e3a\u9996\u9875\u5de5\u4f5c\u53f0\u63d0\u4f9b\u6c47\u603b\u6307\u6807\uff1a\u9884\u7ea6\u6570\u3001\u6765\u8bbf\u6570\u3001\u5ba2\u6237\u6570\u3001\u8bf7\u5047\u4e2d\u4eba\u6570\u3001\u9000\u4f4f\u7533\u8bf7\u6570\u3002
 */
@RestController
@RequestMapping("/workbench")
public class WorkbenchController {

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private VisitMapper visitMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private LeaveMapper leaveMapper;
    @Autowired private CheckoutMapper checkoutMapper;

    /**
     * \u5de5\u4f5c\u53f0\u6c47\u603b\u7edf\u8ba1\u3002
     * leaveCount \u4ec5\u7edf\u8ba1\u72b6\u6001\u4e3a\u300c\u8bf7\u5047\u4e2d\u300d\u7684\u8bf7\u5047\u5355\uff1b\u5176\u4f59\u4e3a\u5168\u8868\u8ba1\u6570\u3002
     *
     * @return \u542b reservationCount/visitCount/customerCount/leaveCount/checkoutCount \u7684 Map
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("reservationCount", reservationMapper.selectCount(null));
        data.put("visitCount", visitMapper.selectCount(null));
        data.put("customerCount", customerMapper.selectCount(null));
        data.put("leaveCount", leaveMapper.selectCount(
                new LambdaQueryWrapper<Leave>().eq(Leave::getStatus, "\u8bf7\u5047\u4e2d")));
        data.put("checkoutCount", checkoutMapper.selectCount(null));
        return Result.ok(data);
    }
}
""")

for n in ["ReservationController.java","RoomTypeController.java","VisitController.java","WorkbenchController.java","FileController.java","CustomerController.java","ContractController.java","BedController.java"]:
    t = (CTRL / n).read_text(encoding="utf-8")
    assert "\ufffd" not in t
    print(n, "UTF8 check OK, has chinese:", any("\u4e00" <= ch <= "\u9fff" for ch in t))