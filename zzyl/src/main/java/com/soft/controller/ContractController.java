package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.config.DemoDataFill;
import com.soft.mapper.ContractMapper;
import com.soft.pojo.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract")
public class ContractController {
    @Autowired private ContractMapper contractMapper;

    @PostMapping("/page")
    public Result<List<Contract>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Contract> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getContractNo())) w.like(Contract::getContractNo, q.getContractNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Contract::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getStatus())) w.eq(Contract::getStatus, q.getStatus());
        w.orderByDesc(Contract::getCreateTime);
        Page<Contract> page = contractMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Contract> detail(@RequestParam("id") Long id) {
        Contract c = contractMapper.selectById(id);
        if (c == null) {
            c = contractMapper.selectOne(new LambdaQueryWrapper<Contract>().last("limit 1"));
        }
        if (c == null) {
            c = DemoDataFill.demoContract(id);
        }
        DemoDataFill.fillContract(c);
        return Result.ok(c);
    }
}
