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

/**
 * 客户信息接口。
 * 对应前端「客户信息」页面，提供客户分页检索能力。
 * 客户一般由小程序/注册流程写入，本控制器侧重后台查询展示。
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired private CustomerMapper customerMapper;

    /**
     * 客户分页列表。
     * 支持按昵称、手机号模糊查询；按首次登录时间倒序。
     *
     * @param q 分页与筛选条件（pageNum/pageSize/nickname/phone）
     * @return 当前页客户列表与总数
     */
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
