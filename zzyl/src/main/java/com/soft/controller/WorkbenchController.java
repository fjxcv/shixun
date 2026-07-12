package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.*;
import com.soft.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/workbench")
public class WorkbenchController {

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private VisitMapper visitMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private LeaveMapper leaveMapper;
    @Autowired private CheckoutMapper checkoutMapper;

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
