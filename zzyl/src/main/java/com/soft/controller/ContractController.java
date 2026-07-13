package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.config.DemoDataFill;
import com.soft.mapper.CheckinMapper;
import com.soft.mapper.CheckoutMapper;
import com.soft.mapper.ContractMapper;
import com.soft.pojo.Checkin;
import com.soft.pojo.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired private CheckinMapper checkinMapper;
    @Autowired private CheckoutMapper checkoutMapper;

    /**
     * 合同分页列表。
     * 支持按合同号、老人姓名模糊查询，以及按状态精确筛选；按创建时间倒序。
     *
     * @param q 分页与筛选条件（pageNum/pageSize/contractNo/elderName/status）
     * @return 当前页合同列表与总数
     */
    @PostMapping("/page")
    public Result<List<Contract>> page(@RequestBody PageQueryDto q) {
        // 只展示入住完成（flowStatus=已完成）的合同
        List<String> validCheckinNos = checkinMapper.selectList(
            new LambdaQueryWrapper<Checkin>().eq(Checkin::getFlowStatus, "已完成")
        ).stream().map(Checkin::getDocNo).collect(Collectors.toList());

        if (validCheckinNos.isEmpty()) {
            return Result.ok(List.of(), 0L);
        }

        LambdaQueryWrapper<Contract> w = new LambdaQueryWrapper<>();
        w.in(Contract::getCheckinNo, validCheckinNos);
        if (StringUtils.hasText(q.getContractNo())) w.eq(Contract::getContractNo, q.getContractNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Contract::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getStatus())) {
            LocalDate now = LocalDate.now();
            switch (q.getStatus()) {
                case "未生效": w.gt(Contract::getStartDate, now); break;
                case "生效中": w.le(Contract::getStartDate, now).gt(Contract::getEndDate, now).isNull(Contract::getTerminateUser); break;
                case "已过期": w.le(Contract::getEndDate, now).isNull(Contract::getTerminateUser); break;
                case "已失效": w.isNotNull(Contract::getTerminateUser); break;
            }
        }
        w.orderByDesc(Contract::getCreateTime);
        Page<Contract> page = contractMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);

        // 动态计算合同状态
        LocalDate today = LocalDate.now();
        for (Contract c : page.getRecords()) {
            if (c.getTerminateUser() != null) {
                c.setStatus("已失效");
            } else if (!today.isBefore(c.getEndDate())) {
                c.setStatus("已过期");
            } else if (!today.isBefore(c.getStartDate())) {
                c.setStatus("生效中");
            } else {
                c.setStatus("未生效");
            }
        }
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
        if (c == null) return Result.ok(null);

        // 从退住记录中补充解除信息（覆盖合同表中的种子数据）
        List<com.soft.pojo.Checkout> checkouts = checkoutMapper.selectList(
            new LambdaQueryWrapper<com.soft.pojo.Checkout>()
                .eq(com.soft.pojo.Checkout::getElderIdcard, c.getElderIdcard())
                .eq(com.soft.pojo.Checkout::getFlowStatus, "已完成")
                .last("LIMIT 1"));
        if (!checkouts.isEmpty()) {
            com.soft.pojo.Checkout co = checkouts.get(0);
            c.setTerminateUser(co.getApplicant());
            c.setTerminateDate(co.getTerminateDate());
            c.setTerminateFile(co.getTerminateFile());
        }

        // 动态计算合同状态
        LocalDate today = LocalDate.now();
        if (c.getTerminateUser() != null) {
            c.setStatus("已失效");
        } else if (!today.isBefore(c.getEndDate())) {
            c.setStatus("已过期");
        } else if (!today.isBefore(c.getStartDate())) {
            c.setStatus("生效中");
        } else {
            c.setStatus("未生效");
        }
        return Result.ok(c);
    }
}
