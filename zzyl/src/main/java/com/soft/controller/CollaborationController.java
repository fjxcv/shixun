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

@RestController
@RequestMapping("/collab")
public class CollaborationController {

    @Autowired private CheckinMapper checkinMapper;
    @Autowired private CheckoutMapper checkoutMapper;
    @Autowired private LeaveMapper leaveMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * \u6211\u7684\u7533\u8bf7\uff1a\u4ec5\u8fd4\u56de\u5f53\u524d\u767b\u5f55\u7528\u6237\u53d1\u8d77\u7684\u5355\u636e\uff08\u4ee5 Session realname \u4e3a\u51c6\uff09\u3002
     * \u524d\u7aef\u4f20\u5165\u7684 applicant \u4ec5\u4f5c\u4e8c\u6b21\u7b5b\u9009\uff0c\u4e0d\u80fd\u5192\u5145\u8eab\u4efd\u3002
     */
    @PostMapping("/apply/page")
    public Result<List<WorkflowItemDto>> applyPage(@RequestBody PageQueryDto q, HttpSession session) {
        String owner = resolveOnlineName(session);
        if (!StringUtils.hasText(owner)) {
            return pageSlice(new ArrayList<>(), q);
        }
        // \u4fdd\u7559\u524d\u7aef\u300c\u7533\u8bf7\u4eba\u300d\u641c\u7d22\u8bcd\uff0c\u4f46\u4e0d\u7528\u5b83\u505a\u8eab\u4efd\u8303\u56f4
        String searchApplicant = q.getApplicant();
        q.setApplicant(null);
        List<WorkflowItemDto> all = loadAll(q, owner);
        if (StringUtils.hasText(searchApplicant)) {
            String kw = searchApplicant.trim();
            all = all.stream()
                    .filter(i -> i.getApplicant() != null && i.getApplicant().contains(kw))
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(q.getStatus()) && !"\u5168\u90e8".equals(q.getStatus())) {
            all = all.stream().filter(i -> q.getStatus().equals(i.getFlowStatus())).collect(Collectors.toList());
        }
        return pageSlice(all, q);
    }

    /** \u6211\u7684\u5f85\u529e\uff1a\u5168\u5458\u5171\u4eab\uff0c\u4e0d\u6309\u767b\u5f55\u8d26\u53f7\u8fc7\u6ee4 */
    @PostMapping("/todo/page")
    public Result<List<WorkflowItemDto>> todoPage(@RequestBody PageQueryDto q) {
        List<WorkflowItemDto> all = loadAll(q, null);
        boolean processed = "\u5df2\u5904\u7406".equals(q.getStatus());
        all = all.stream().filter(i -> {
            boolean pending;
            if ("leave".equals(i.getBizType())) {
                // 请假：仅「待审批」(step=1 且申请中) 进入待办
                pending = "\u7533\u8bf7\u4e2d".equals(i.getFlowStatus()) && (i.getStep() == null || i.getStep() == 1);
            } else {
                pending = "\u7533\u8bf7\u4e2d".equals(i.getFlowStatus());
            }
            return processed ? !pending : pending;
        }).collect(Collectors.toList());
        return pageSlice(all, q);
    }

    private String resolveOnlineName(HttpSession session) {
        if (session == null) return null;
        Object online = session.getAttribute("online");
        if (online instanceof UserLineDto dto && StringUtils.hasText(dto.getRealname())) {
            return dto.getRealname().trim();
        }
        return null;
    }

    /**
     * @param owner \u975e\u7a7a\u65f6\u4ec5\u52a0\u8f7d\u8be5\u7528\u6237\u53d1\u8d77\u7684\u5355\u636e\uff08applicant / creator\uff09\uff1b
     *              \u4e3a null \u65f6\u4e0d\u6309\u7528\u6237\u8fc7\u6ee4\uff08\u5f85\u529e\u5171\u4eab\uff09\u3002
     */
    private List<WorkflowItemDto> loadAll(PageQueryDto q, String owner) {
        List<WorkflowItemDto> list = new ArrayList<>();
        String type = q.getType();
        if (!StringUtils.hasText(type) || "\u5165\u4f4f".equals(type)) list.addAll(fromCheckin(q, owner));
        if (!StringUtils.hasText(type) || "\u9000\u4f4f".equals(type)) list.addAll(fromCheckout(q, owner));
        if (!StringUtils.hasText(type) || "\u8bf7\u5047".equals(type)) list.addAll(fromLeave(q, owner));
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
            d.setTitle(c.getElderName() + "\u7684\u5165\u4f4f\u7533\u8bf7");
            d.setCategory("\u5165\u4f4f");
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
            d.setTitle(c.getElderName() + "\u7684\u9000\u4f4f\u7533\u8bf7");
            d.setCategory("\u9000\u4f4f");
            d.setApplicant(displayApplicant(c.getApplicant(), c.getCreator()));
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
            d.setTitle(c.getElderName() + "\u7684\u8bf7\u5047\u7533\u8bf7");
            d.setCategory("\u8bf7\u5047");
            d.setApplicant(displayApplicant(c.getApplicant(), c.getCreator()));
            d.setApplyTime(fmt(c.getApplyTime()));
            d.setFinishTime("\u5df2\u8fd4\u56de".equals(c.getStatus()) ? fmt(c.getActualReturnTime()) : "\u2014");
            d.setFlowStatus(mapLeaveFlow(c.getStatus()));
            d.setWaitDuration(wait(c.getApplyTime()));
            d.setStep("\u5f85\u5ba1\u6279".equals(c.getStatus()) ? 1 : 2);
            d.setBizType("leave");
            return d;
        }).collect(Collectors.toList());
    }

    private String displayApplicant(String applicant, String creator) {
        if (StringUtils.hasText(applicant)) return applicant;
        return StringUtils.hasText(creator) ? creator : null;
    }

    private String mapCheckoutFlow(Checkout c) {
        if ("\u5df2\u5b8c\u6210".equals(c.getFlowStatus()) || "\u5df2\u5b8c\u6210".equals(c.getStepStatus())) return "\u5df2\u5b8c\u6210";
        if ("\u5df2\u5173\u95ed".equals(c.getFlowStatus()) || "\u5df2\u5173\u95ed".equals(c.getStepStatus())) return "\u5df2\u5173\u95ed";
        return "\u7533\u8bf7\u4e2d";
    }

    /**
     * 请假状态映射到协同统一流程状态：
     * 待审批 -> 申请中（进入待办）；请假中 -> 申请中（我的申请仍可见）；
     * 已返回 -> 已完成；超时未归/已拒绝/已关闭 -> 已关闭。
     * 待办列表仅把「待审批」视为待处理（见 todoPage）。
     */
    private String mapLeaveFlow(String s) {
        if ("\u5df2\u8fd4\u56de".equals(s)) return "\u5df2\u5b8c\u6210";
        if ("\u8d85\u65f6\u672a\u5f52".equals(s) || "\u5df2\u62d2\u7edd".equals(s) || "\u5df2\u5173\u95ed".equals(s)) {
            return "\u5df2\u5173\u95ed";
        }
        // 待审批、请假中
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
