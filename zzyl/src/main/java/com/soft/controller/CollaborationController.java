package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.dto.WorkflowItemDto;
import com.soft.mapper.CheckinMapper;
import com.soft.mapper.CheckoutMapper;
import com.soft.mapper.LeaveMapper;
import com.soft.pojo.Checkin;
import com.soft.pojo.Checkout;
import com.soft.pojo.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collab")
public class CollaborationController {

    @Autowired private CheckinMapper checkinMapper;
    @Autowired private CheckoutMapper checkoutMapper;
    @Autowired private LeaveMapper leaveMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/apply/page")
    public Result<List<WorkflowItemDto>> applyPage(@RequestBody PageQueryDto q) {
        List<WorkflowItemDto> all = loadAll(q);
        if (StringUtils.hasText(q.getStatus()) && !"\u5168\u90e8".equals(q.getStatus())) {
            all = all.stream().filter(i -> q.getStatus().equals(i.getFlowStatus())).collect(Collectors.toList());
        }
        return pageSlice(all, q);
    }

    @PostMapping("/todo/page")
    public Result<List<WorkflowItemDto>> todoPage(@RequestBody PageQueryDto q) {
        List<WorkflowItemDto> all = loadAll(q);
        boolean processed = "\u5df2\u5904\u7406".equals(q.getStatus());
        all = all.stream().filter(i -> {
            boolean pending = "\u7533\u8bf7\u4e2d".equals(i.getFlowStatus());
            return processed ? !pending : pending;
        }).collect(Collectors.toList());
        return pageSlice(all, q);
    }

    private List<WorkflowItemDto> loadAll(PageQueryDto q) {
        List<WorkflowItemDto> list = new ArrayList<>();
        String type = q.getType();
        if (!StringUtils.hasText(type) || "\u5165\u4f4f".equals(type)) list.addAll(fromCheckin(q));
        if (!StringUtils.hasText(type) || "\u9000\u4f4f".equals(type)) list.addAll(fromCheckout(q));
        if (!StringUtils.hasText(type) || "\u8bf7\u5047".equals(type)) list.addAll(fromLeave(q));
        list.sort(Comparator.comparing(WorkflowItemDto::getApplyTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private List<WorkflowItemDto> fromCheckin(PageQueryDto q) {
        LambdaQueryWrapper<Checkin> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Checkin::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getApplicant())) w.like(Checkin::getApplicant, q.getApplicant());
        return checkinMapper.selectList(w).stream().map(c -> {
            WorkflowItemDto d = new WorkflowItemDto();
            d.setId(c.getId());
            d.setDocNo(c.getDocNo());
            d.setTitle(c.getElderName() + "\u7684\u5165\u4f4f\u7533\u8bf7");
            d.setCategory("\u5165\u4f4f");
            d.setApplicant(c.getApplicant());
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime(fmt(c.getFinishTime()));
            d.setFlowStatus(c.getFlowStatus());
            d.setWaitDuration(wait(c.getApplyTime()));
            d.setStep(c.getStep());
            d.setBizType("checkin");
            return d;
        }).collect(Collectors.toList());
    }

    private List<WorkflowItemDto> fromCheckout(PageQueryDto q) {
        LambdaQueryWrapper<Checkout> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Checkout::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getApplicant())) w.like(Checkout::getApplicant, q.getApplicant());
        return checkoutMapper.selectList(w).stream().map(c -> {
            WorkflowItemDto d = new WorkflowItemDto();
            d.setId(c.getId());
            d.setDocNo(c.getDocNo());
            d.setTitle(c.getElderName() + "\u7684\u9000\u4f4f\u7533\u8bf7");
            d.setCategory("\u9000\u4f4f");
            d.setApplicant(c.getApplicant());
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime("\u5df2\u5b8c\u6210".equals(c.getFlowStatus()) || "\u5df2\u5b8c\u6210".equals(c.getStepStatus())
                    ? fmt(c.getCreateTime()) : "\u2014");
            d.setFlowStatus(mapCheckoutFlow(c));
            d.setWaitDuration(wait(c.getApplyTime()));
            d.setStep(c.getStep());
            d.setBizType("checkout");
            return d;
        }).collect(Collectors.toList());
    }

    private List<WorkflowItemDto> fromLeave(PageQueryDto q) {
        LambdaQueryWrapper<Leave> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Leave::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getApplicant())) w.like(Leave::getApplicant, q.getApplicant());
        return leaveMapper.selectList(w).stream().map(c -> {
            WorkflowItemDto d = new WorkflowItemDto();
            d.setId(c.getId());
            d.setDocNo(c.getDocNo());
            d.setTitle(c.getElderName() + "\u7684\u8bf7\u5047\u7533\u8bf7");
            d.setCategory("\u8bf7\u5047");
            d.setApplicant(c.getApplicant());
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime("\u5df2\u8fd4\u56de".equals(c.getStatus()) ? fmt(c.getActualReturnTime()) : "\u2014");
            d.setFlowStatus(mapLeaveFlow(c.getStatus()));
            d.setWaitDuration(wait(c.getApplyTime()));
            d.setStep(1);
            d.setBizType("leave");
            return d;
        }).collect(Collectors.toList());
    }

    private String mapCheckoutFlow(Checkout c) {
        if ("\u5df2\u5b8c\u6210".equals(c.getFlowStatus()) || "\u5df2\u5b8c\u6210".equals(c.getStepStatus())) return "\u5df2\u5b8c\u6210";
        if ("\u5df2\u5173\u95ed".equals(c.getFlowStatus()) || "\u5df2\u5173\u95ed".equals(c.getStepStatus())) return "\u5df2\u5173\u95ed";
        return "\u7533\u8bf7\u4e2d";
    }

    private String mapLeaveFlow(String s) {
        if ("\u5df2\u8fd4\u56de".equals(s)) return "\u5df2\u5b8c\u6210";
        if ("\u8d85\u65f6\u672a\u5f52".equals(s)) return "\u5df2\u5173\u95ed";
        return "\u7533\u8bf7\u4e2d";
    }

    private String fmt(LocalDateTime t) {
        return t == null ? "\u2014" : t.format(FMT);
    }

    private String wait(LocalDateTime t) {
        if (t == null) return "\u2014";
        Duration d = Duration.between(t, LocalDateTime.now());
        long secs = Math.max(0, d.getSeconds());
        long mins = secs / 60;
        if (mins < 1) return secs + "\u79d2";
        if (mins < 60) return mins + "\u5206\u949f";
        return (mins / 60) + "\u5c0f\u65f6" + (mins % 60) + "\u5206\u949f";
    }

    private Result<List<WorkflowItemDto>> pageSlice(List<WorkflowItemDto> all, PageQueryDto q) {
        int pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        List<WorkflowItemDto> slice = from >= all.size() ? new ArrayList<>() : all.subList(from, to);
        return Result.ok(slice, (long) all.size());
    }
}
