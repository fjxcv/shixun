package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.CustomerMapper;
import com.soft.pojo.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired private CustomerMapper customerMapper;

    @PostMapping("/page")
    public Result<List<Customer>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getNickname())) w.like(Customer::getNickname, q.getNickname());
        if (StringUtils.hasText(q.getPhone())) w.like(Customer::getPhone, q.getPhone());
        w.orderByDesc(Customer::getFirstLoginTime);
        Page<Customer> page = customerMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }
}
