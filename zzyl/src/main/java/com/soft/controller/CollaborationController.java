package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.dto.UserLineDto;
import com.soft.dto.WorkflowItemDto;
import com.soft.mapper.CheckinMapper;
import com.soft.mapper.CheckoutMapper;
import com.soft.mapper.LeaveMapper;
import com.soft.pojo.Checkin;
import com.soft.pojo.Checkout;
import com.soft.pojo.Leave;
import jakarta.servlet.http.HttpSession;
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

/**
 * 协同工作接口：聚合入住/退住/请假三类单据，供「我的申请」「我的待办」使用。
 * <p>
 * 统一流程状态（flowStatus）：申请中、已完成、已关闭。
 * 请假业务有独立 status（待审批/请假中/已返回等），通过 mapLeaveFlow 映射后再过滤。
 */
@RestController
@RequestMapping("/collab")
public class CollaborationController {

    @Autowired private CheckinMapper checkinMapper;
    @Autowired private CheckoutMapper checkoutMapper;
    @Autowired private LeaveMapper leaveMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 我的申请：仅返回当前登录用户发起的单据（以 Session realname 为准）。
     * 前端传入的 applicant 仅作二次筛选，不能冒充身份。
     */
    @PostMapping("/apply/page")
    public Result<List<WorkflowItemDto>> applyPage(@RequestBody PageQueryDto q, HttpSession session) {
        String owner = resolveOnlineName(session);
        if (!StringUtils.hasText(owner)) {
            return pageSlice(new ArrayList<>(), q);
        }
        // 保留前端「申请人」搜索词，但不用它做身份范围
        String searchApplicant = q.getApplicant();
        q.setApplicant(null);
        List<WorkflowItemDto> all = loadAll(q, owner);
        if (StringUtils.hasText(searchApplicant)) {
            String kw = searchApplicant.trim();
            all = all.stream()
                    .filter(i -> i.getApplicant() != null && i.getApplicant().contains(kw))
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(q.getStatus()) && !"全部".equals(q.getStatus())) {
            all = all.stream().filter(i -> q.getStatus().equals(i.getFlowStatus())).collect(Collectors.toList());
        }
        return pageSlice(all, q);
    }

    /** 我的待办：全员共享，不按登录账号过滤 */
    @PostMapping("/todo/page")
    public Result<List<WorkflowItemDto>> todoPage(@RequestBody PageQueryDto q) {
        List<WorkflowItemDto> all = loadAll(q, null);
        boolean processed = "已处理".equals(q.getStatus());
        all = all.stream().filter(i -> {
            boolean pending;
            if ("leave".equals(i.getBizType())) {
                // 请假：仅「待审批」(step=1 且申请中) 进入待办
                pending = "申请中".equals(i.getFlowStatus()) && (i.getStep() == null || i.getStep() == 1);
            } else {
                pending = "申请中".equals(i.getFlowStatus());
            }
            return processed ? !pending : pending;
        }).collect(Collectors.toList());
        return pageSlice(all, q);
    }

    /** 从 Session.online 取登录用户真实姓名，用于「我的申请」归属过滤。 */
    private String resolveOnlineName(HttpSession session) {
        if (session == null) return null;
        Object online = session.getAttribute("online");
        if (online instanceof UserLineDto dto && StringUtils.hasText(dto.getRealname())) {
            return dto.getRealname().trim();
        }
        return null;
    }

    /**
     * @param owner 非空时仅加载该用户发起的单据（applicant / creator）；
     *              为 null 时不按用户过滤（待办共享）。
     */
    private List<WorkflowItemDto> loadAll(PageQueryDto q, String owner) {
        List<WorkflowItemDto> list = new ArrayList<>();
        String type = q.getType();
        if (!StringUtils.hasText(type) || "入住".equals(type)) list.addAll(fromCheckin(q, owner));
        if (!StringUtils.hasText(type) || "退住".equals(type)) list.addAll(fromCheckout(q, owner));
        if (!StringUtils.hasText(type) || "请假".equals(type)) list.addAll(fromLeave(q, owner));
        list.sort(Comparator.comparing(WorkflowItemDto::getApplyTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private List<WorkflowItemDto> fromCheckin(PageQueryDto q, String owner) {
        LambdaQueryWrapper<Checkin> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Checkin::getDocNo, q.getDocNo());
        if (StringUtils.hasText(owner)) {
            w.and(x -> x.eq(Checkin::getApplicant, owner).or().eq(Checkin::getCreator, owner));
        } else if (StringUtils.hasText(q.getApplicant())) {
            w.like(Checkin::getApplicant, q.getApplicant());
        }
        return checkinMapper.selectList(w).stream().map(c -> {
            WorkflowItemDto d = new WorkflowItemDto();
            d.setId(c.getId());
            d.setDocNo(c.getDocNo());
            d.setTitle(c.getElderName() + "的入住申请");
            d.setCategory("入住");
            d.setApplicant(displayApplicant(c.getApplicant(), c.getCreator()));
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime(fmt(c.getFinishTime()));
            d.setFlowStatus(c.getFlowStatus());
            d.setWaitDuration(wait(c.getApplyTime()));
            d.setStep(c.getStep());
            d.setBizType("checkin");
            return d;
        }).collect(Collectors.toList());
    }

    private List<WorkflowItemDto> fromCheckout(PageQueryDto q, String owner) {
        LambdaQueryWrapper<Checkout> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Checkout::getDocNo, q.getDocNo());
        if (StringUtils.hasText(owner)) {
            w.and(x -> x.eq(Checkout::getApplicant, owner).or().eq(Checkout::getCreator, owner));
        } else if (StringUtils.hasText(q.getApplicant())) {
            w.like(Checkout::getApplicant, q.getApplicant());
        }
        return checkoutMapper.selectList(w).stream().map(c -> {
            WorkflowItemDto d = new WorkflowItemDto();
            d.setId(c.getId());
            d.setDocNo(c.getDocNo());
            d.setTitle(c.getElderName() + "的退住申请");
            d.setCategory("退住");
            d.setApplicant(displayApplicant(c.getApplicant(), c.getCreator()));
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime("已完成".equals(c.getFlowStatus()) || "已完成".equals(c.getStepStatus())
                    ? fmt(c.getCreateTime()) : "—");
            d.setFlowStatus(mapCheckoutFlow(c));
            d.setWaitDuration(wait(c.getApplyTime()));
            d.setStep(c.getStep());
            d.setBizType("checkout");
            return d;
        }).collect(Collectors.toList());
    }

    private List<WorkflowItemDto> fromLeave(PageQueryDto q, String owner) {
        LambdaQueryWrapper<Leave> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.like(Leave::getDocNo, q.getDocNo());
        if (StringUtils.hasText(owner)) {
            w.and(x -> x.eq(Leave::getApplicant, owner).or().eq(Leave::getCreator, owner));
        } else if (StringUtils.hasText(q.getApplicant())) {
            w.like(Leave::getApplicant, q.getApplicant());
        }
        return leaveMapper.selectList(w).stream().map(c -> {
            WorkflowItemDto d = new WorkflowItemDto();
            d.setId(c.getId());
            d.setDocNo(c.getDocNo());
            d.setTitle(c.getElderName() + "的请假申请");
            d.setCategory("请假");
            d.setApplicant(displayApplicant(c.getApplicant(), c.getCreator()));
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime("已返回".equals(c.getStatus()) ? fmt(c.getActualReturnTime()) : "—");
            d.setFlowStatus(mapLeaveFlow(c.getStatus()));
            d.setWaitDuration(wait(c.getApplyTime()));
            // step=1 表示待审批阶段，供 todoPage 区分「待审批」与「请假中」
            d.setStep("待审批".equals(c.getStatus()) ? 1 : 2);
            d.setBizType("leave");
            return d;
        }).collect(Collectors.toList());
    }

    /** 展示用申请人：优先 applicant，否则 creator。 */
    private String displayApplicant(String applicant, String creator) {
        if (StringUtils.hasText(applicant)) return applicant;
        return StringUtils.hasText(creator) ? creator : null;
    }

    /** 退住：flowStatus/stepStatus 任一终态则映射为已完成或已关闭，否则申请中。 */
    private String mapCheckoutFlow(Checkout c) {
        if ("已完成".equals(c.getFlowStatus()) || "已完成".equals(c.getStepStatus())) return "已完成";
        if ("已关闭".equals(c.getFlowStatus()) || "已关闭".equals(c.getStepStatus())) return "已关闭";
        return "申请中";
    }

    /**
     * 请假状态映射到协同统一流程状态：
     * 待审批 -> 申请中（进入待办）；请假中 -> 申请中（我的申请仍可见）；
     * 已返回 -> 已完成；超时未归/已拒绝/已关闭 -> 已关闭。
     * 待办列表仅把「待审批」视为待处理（见 todoPage）。
     */
    /**
     * 请假业务状态映射到协同统一流程状态：
     * 待审批 -> 申请中（进入待办）；请假中 -> 申请中（我的申请仍可见）；
     * 已返回 -> 已完成；超时未归/已拒绝/已关闭 -> 已关闭。
     * 待办列表只把「待审批」视为待处理（见 todoPage）。
     */
    private String mapLeaveFlow(String s) {
        if ("已返回".equals(s)) return "已完成";
        if ("超时未归".equals(s) || "已拒绝".equals(s) || "已关闭".equals(s)) {
            return "已关闭";
        }
        // 待审批、请假中
        return "申请中";
    }

    private String fmt(LocalDateTime t) {
        return t == null ? "—" : t.format(FMT);
    }

    /** 自申请时间起算的等待时长文案。 */
    private String wait(LocalDateTime t) {
        if (t == null) return "—";
        Duration d = Duration.between(t, LocalDateTime.now());
        long secs = Math.max(0, d.getSeconds());
        long mins = secs / 60;
        if (mins < 1) return secs + "秒";
        if (mins < 60) return mins + "分钟";
        return (mins / 60) + "小时" + (mins % 60) + "分钟";
    }

    /** 内存分页：返回当前页切片与总条数。 */
    private Result<List<WorkflowItemDto>> pageSlice(List<WorkflowItemDto> all, PageQueryDto q) {
        int pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        List<WorkflowItemDto> slice = from >= all.size() ? new ArrayList<>() : all.subList(from, to);
        return Result.ok(slice, (long) all.size());
    }
}
