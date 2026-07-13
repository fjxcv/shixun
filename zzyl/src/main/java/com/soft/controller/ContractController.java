package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.config.DemoDataFill;
import com.soft.mapper.ContractMapper;
import com.soft.pojo.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同管理接口。
 * <p>
 * 提供合同分页查询与详情查看，供「合同跟踪」「合同详情」页面使用。
 * 详情在数据缺失时会回落演示数据并补齐展示字段，便于演示环境联调。
 */
@RestController
@RequestMapping("/contract")
public class ContractController {
    @Autowired private ContractMapper contractMapper;

    /**
     * 合同分页列表。
     * 支持按合同号、老人姓名模糊查询，以及按状态精确筛选；按创建时间倒序。
     *
     * @param q 分页与筛选条件（pageNum/pageSize/contractNo/elderName/status）
     * @return 当前页合同列表与总数
     */
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

    /**
     * 合同详情。
     * 优先按 id 查询；查不到则取库中任意一条；仍无数据则生成演示合同。
     * 返回前统一调用 DemoDataFill 补齐展示字段。
     *
     * @param id 合同 ID
     */
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
