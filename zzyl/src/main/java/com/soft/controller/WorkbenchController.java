package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.mapper.*;
import com.soft.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作台统计接口。
 * <p>
 * 为首页工作台提供汇总指标：预约数、来访数、客户数、请假中人数、退住申请数。
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
     * 工作台汇总统计。
     * leaveCount 仅统计状态为「请假中」的请假单；其余为全表计数。
     *
     * @return 含 reservationCount/visitCount/customerCount/leaveCount/checkoutCount 的 Map
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("reservationCount", reservationMapper.selectCount(null));
        data.put("visitCount", visitMapper.selectCount(null));
        data.put("customerCount", customerMapper.selectCount(null));
        data.put("leaveCount", leaveMapper.selectCount(
                new LambdaQueryWrapper<Leave>().eq(Leave::getStatus, "请假中")));
        data.put("checkoutCount", checkoutMapper.selectCount(null));
        return Result.ok(data);
    }
}
