# -*- coding: utf-8 -*-
"""Resolve conflicts: pom + LeaveController with proper Chinese."""
from pathlib import Path
import re

ROOT = Path(r"D:/vsc-maven/zzyl-project")


def write(rel, text):
    p = ROOT / rel
    p.write_text(text, encoding="utf-8", newline="\n")
    print("wrote", p, "bytes", p.stat().st_size)


# ---- pom: keep hutool, drop AI ----
pom = (ROOT / "zzyl/pom.xml").read_text(encoding="utf-8")
pom = re.sub(
    r"<<<<<<< HEAD\n\s*<!-- hutool -->\n=======\n.*?>>>>>>> [^\n]+\n",
    "        <!-- hutool -->\n",
    pom,
    count=1,
    flags=re.S,
)
pom = re.sub(r"<<<<<<<[^\n]*\n|=======\n|>>>>>>>[^\n]*\n", "", pom)
pom = re.sub(
    r"\s*<dependency>\s*<groupId>com\.alibaba\.cloud\.ai</groupId>.*?</dependency>\s*",
    "\n",
    pom,
    flags=re.S,
)
write("zzyl/pom.xml", pom)
assert "spring-ai-alibaba" not in pom
assert "<<<<<<<" not in pom

# ---- LeaveController ----
# Non-raw string so \uXXXX become Chinese
leave = """package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.dto.UserLineDto;
import com.soft.mapper.BedMapper;
import com.soft.mapper.LeaveMapper;
import com.soft.pojo.Bed;
import com.soft.pojo.Leave;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * \u8bf7\u5047\u7ba1\u7406\uff1a\u5206\u9875\u3001\u8be6\u60c5\u3001\u7533\u8bf7\u3001\u5ba1\u6279\u3001\u9500\u5047\u3001\u64a4\u9500\u3002
 * \u4e1a\u52a1\u72b6\u6001 status\uff1a\u5f85\u5ba1\u6279 \u2192 \u8bf7\u5047\u4e2d/\u5df2\u62d2\u7edd\uff1b\u8bf7\u5047\u4e2d/\u8d85\u65f6\u672a\u5f52 \u2192 \u5df2\u8fd4\u56de\uff1b\u5f85\u5ba1\u6279\u53ef\u64a4\u9500\u4e3a\u5df2\u5173\u95ed\u3002
 * \u5217\u8868\u4e2d\u300c\u8bf7\u5047\u4e2d/\u8d85\u65f6\u672a\u5f52\u300d\u6309\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4\u52a8\u6001\u5224\u5b9a\uff1b\u534f\u540c\u6620\u5c04\u89c1 CollaborationController#mapLeaveFlow\u3002
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired private LeaveMapper leaveMapper;
    @Autowired private BedMapper bedMapper;

    /**
     * \u8bf7\u5047\u5355\u5206\u9875\u3002
     * status=\u5168\u90e8 \u65f6\u4e0d\u8fc7\u6ee4\uff1b\u8bf7\u5047\u4e2d/\u8d85\u65f6\u672a\u5f52\u6309\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4\u8fc7\u6ee4\uff0c\u8fd4\u56de\u524d\u52a8\u6001\u5237\u65b0\u5c55\u793a\u72b6\u6001\u3002
     */
    @PostMapping("/page")
    public Result<List<Leave>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Leave> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.eq(Leave::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Leave::getElderName, q.getElderName());
        // \u8eab\u4efd\u8bc1\u53f7\u7cbe\u786e\u5339\u914d
        if (StringUtils.hasText(q.getElderIdcard())) w.eq(Leave::getElderIdcard, q.getElderIdcard());
        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.hasText(q.getStatus()) && !"\u5168\u90e8".equals(q.getStatus())) {
            switch (q.getStatus()) {
                case "\u5f85\u5ba1\u6279":
                    w.eq(Leave::getStatus, "\u5f85\u5ba1\u6279");
                    break;
                case "\u5df2\u8fd4\u56de":
                    w.eq(Leave::getStatus, "\u5df2\u8fd4\u56de");
                    break;
                case "\u5df2\u62d2\u7edd":
                case "\u5df2\u5173\u95ed":
                    w.eq(Leave::getStatus, q.getStatus());
                    break;
                case "\u8bf7\u5047\u4e2d":
                    // \u672a\u7ec8\u6001\u4e14\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4\u5728\u5f53\u524d\u4e4b\u540e
                    w.notIn(Leave::getStatus, "\u5df2\u8fd4\u56de", "\u5df2\u62d2\u7edd", "\u5df2\u5173\u95ed", "\u5f85\u5ba1\u6279")
                     .gt(Leave::getExpectReturnTime, now);
                    break;
                case "\u8d85\u65f6\u672a\u5f52":
                    // \u672a\u7ec8\u6001\u4e14\u9884\u8ba1\u8fd4\u56de\u65f6\u95f4\u5df2\u5230/\u8d85\u65f6
                    w.notIn(Leave::getStatus, "\u5df2\u8fd4\u56de", "\u5df2\u62d2\u7edd", "\u5df2\u5173\u95ed", "\u5f85\u5ba1\u6279")
                     .le(Leave::getExpectReturnTime, now);
                    break;
                default:
                    w.eq(Leave::getStatus, q.getStatus());
                    break;
            }
        }
        w.orderByDesc(Leave::getCreateTime);
        Page<Leave> page = leaveMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        // \u52a8\u6001\u8ba1\u7b97\u5c55\u793a\u72b6\u6001\uff08\u4e0d\u56de\u5199\u5e93\uff0c\u4ec5\u5f71\u54cd\u672c\u6b21\u5217\u8868\uff09
        for (Leave l : page.getRecords()) {
            if ("\u5df2\u8fd4\u56de".equals(l.getStatus()) || "\u5df2\u62d2\u7edd".equals(l.getStatus()) || "\u5df2\u5173\u95ed".equals(l.getStatus())) {
                continue;
            }
            if ("\u5f85\u5ba1\u6279".equals(l.getStatus())) {
                continue;
            }
            if (l.getExpectReturnTime() != null && !l.getExpectReturnTime().isAfter(now)) {
                l.setStatus("\u8d85\u65f6\u672a\u5f52");
            } else {
                l.setStatus("\u8bf7\u5047\u4e2d");
            }
        }
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /** \u8bf7\u5047\u5355\u8be6\u60c5\u3002 */
    @GetMapping("/detail")
    public Result<Leave> detail(@RequestParam("id") Long id) {
        Leave l = leaveMapper.selectById(id);
        if (l == null) {
            return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        }
        return Result.ok(l);
    }

    /**
     * \u65b0\u5efa\u6216\u66f4\u65b0\u8bf7\u5047\u5355\u3002\u65b0\u5efa\u65f6\u9ed8\u8ba4 status=\u5f85\u5ba1\u6279\uff0c\u5e76\u4ece Session \u5199\u5165 applicant/creator\u3002
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Leave leave, HttpSession session) {
        if (leave.getId() == null) {
            if (!StringUtils.hasText(leave.getElderName()) || !StringUtils.hasText(leave.getElderIdcard())) {
                return Result.fail("\u8bf7\u9009\u62e9\u8001\u4eba");
            }
            if (leave.getStartTime() == null || leave.getExpectReturnTime() == null) {
                return Result.fail("\u8bf7\u586b\u5199\u8bf7\u5047\u65f6\u95f4");
            }
            if (!StringUtils.hasText(leave.getReason())) {
                return Result.fail("\u8bf7\u586b\u5199\u8bf7\u5047\u539f\u56e0");
            }
            if (leave.getDocNo() == null) leave.setDocNo("QJ" + System.currentTimeMillis());
            leave.setStatus("\u5f85\u5ba1\u6279");
            leave.setApplyTime(LocalDateTime.now());
            if (leave.getCreateTime() == null) leave.setCreateTime(LocalDateTime.now());
            if (leave.getLeaveDays() == null
                    && leave.getStartTime() != null
                    && leave.getExpectReturnTime() != null) {
                long days = java.time.Duration.between(leave.getStartTime(), leave.getExpectReturnTime()).toDays();
                leave.setLeaveDays((int) Math.max(1, days));
            }
            if (!StringUtils.hasText(leave.getEscort())) leave.setEscort("\u65e0");
            fillApplicantFromSession(leave, session);
            leaveMapper.insert(leave);
        } else {
            leaveMapper.updateById(leave);
        }
        return Result.ok("saved");
    }

    /** \u4ece Session \u8865\u5168 applicant/creator\uff08\u4e0e\u767b\u5f55 realname \u4e00\u81f4\uff09\u3002 */
    private void fillApplicantFromSession(Leave leave, HttpSession session) {
        String name = null;
        if (session != null) {
            Object online = session.getAttribute("online");
            if (online instanceof UserLineDto dto && StringUtils.hasText(dto.getRealname())) {
                name = dto.getRealname().trim();
            }
        }
        if (!StringUtils.hasText(name)) return;
        if (!StringUtils.hasText(leave.getApplicant())) leave.setApplicant(name);
        if (!StringUtils.hasText(leave.getCreator())) leave.setCreator(name);
    }

    /**
     * \u5ba1\u6279\uff1a\u4ec5\u300c\u5f85\u5ba1\u6279\u300d\u53ef\u64cd\u4f5c\u3002\u901a\u8fc7 \u2192 \u8bf7\u5047\u4e2d\uff1b\u62d2\u7edd \u2192 \u5df2\u62d2\u7edd\u3002
     */
    @PostMapping("/approve")
    public Result<String> approve(@RequestBody Map<String, Object> body) {
        Number idNum = (Number) body.get("id");
        if (idNum == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        Leave db = leaveMapper.selectById(idNum.longValue());
        if (db == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        if (!"\u5f85\u5ba1\u6279".equals(db.getStatus())) {
            return Result.fail("\u5f53\u524d\u72b6\u6001\u4e0d\u53ef\u5ba1\u6279");
        }
        String approval = body.get("approvalResult") == null ? null : String.valueOf(body.get("approvalResult"));
        if ("\u901a\u8fc7".equals(approval) || "\u5ba1\u6279\u901a\u8fc7".equals(approval)) {
            db.setStatus("\u8bf7\u5047\u4e2d");
        } else if ("\u62d2\u7edd".equals(approval) || "\u5ba1\u6279\u62d2\u7edd".equals(approval)) {
            db.setStatus("\u5df2\u62d2\u7edd");
        } else {
            return Result.fail("\u8bf7\u9009\u62e9\u5ba1\u6279\u7ed3\u679c");
        }
        leaveMapper.updateById(db);
        return Result.ok(db.getStatus());
    }

    /**
     * \u9500\u5047\uff1a\u4ec5\u8bf7\u5047\u4e2d\u6216\u8d85\u65f6\u672a\u5f52\u53ef\u6807\u8bb0\u5df2\u8fd4\u56de\u3002
     * \u4f18\u5148\u4f7f\u7528\u524d\u7aef\u4f20\u5165\u7684\u5b9e\u9645\u8fd4\u56de\u65f6\u95f4\uff0c\u5426\u5219\u53d6\u5f53\u524d\u65f6\u95f4\uff1b\u5e76\u6309\u5c0f\u65f6\u5dee\u6298\u7b97\u5b9e\u9645\u8bf7\u5047\u5929\u6570\u3002
     * \u9500\u5047\u540e\u5c1d\u8bd5\u5c06\u5bf9\u5e94\u5e8a\u4f4d\u4ece\u300c\u8bf7\u5047\u4e2d\u300d\u6062\u590d\u4e3a\u300c\u5df2\u5165\u4f4f\u300d\u3002
     */
    @PostMapping("/return")
    public Result<String> returnBack(@RequestBody Leave leave) {
        if (leave.getId() == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        Leave db = leaveMapper.selectById(leave.getId());
        if (db == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        if (!"\u8bf7\u5047\u4e2d".equals(db.getStatus()) && !"\u8d85\u65f6\u672a\u5f52".equals(db.getStatus())) {
            return Result.fail("\u4ec5\u8bf7\u5047\u4e2d\u53ef\u9500\u5047");
        }
        db.setStatus("\u5df2\u8fd4\u56de");
        // \u4f18\u5148\u4f7f\u7528\u524d\u7aef\u4f20\u5165\u7684\u5b9e\u9645\u8fd4\u56de\u65f6\u95f4
        if (leave.getActualReturnTime() != null) {
            db.setActualReturnTime(leave.getActualReturnTime());
        } else {
            db.setActualReturnTime(LocalDateTime.now());
        }
        // \u6309\u5c0f\u65f6\u5dee\u6298\u7b97\u5b9e\u9645\u8bf7\u5047\u5929\u6570\uff08\u6700\u5c11 0.5 \u5929\uff0c\u4e0d\u8db3 1 \u5929\u8bb0 1\uff09
        if (db.getStartTime() != null && db.getActualReturnTime() != null) {
            long diffHours = java.time.Duration.between(db.getStartTime(), db.getActualReturnTime()).toHours();
            double days = diffHours / 24.0;
            if (days < 0.5) days = 0.5;
            else if (days < 1) days = 1;
            else days = Math.round(days);
            db.setLeaveDays((int) days);
        }
        if (StringUtils.hasText(leave.getReturnRemark())) db.setReturnRemark(leave.getReturnRemark());
        if (StringUtils.hasText(leave.getCancelUser())) db.setCancelUser(leave.getCancelUser());
        else if (StringUtils.hasText(leave.getApplicant())) db.setCancelUser(leave.getApplicant());
        db.setCancelTime(LocalDateTime.now());
        leaveMapper.updateById(db);
        // \u540c\u6b65\u66f4\u65b0\u5e8a\u4f4d\uff1a\u8bf7\u5047\u4e2d \u2192 \u5df2\u5165\u4f4f
        try {
            List<Bed> beds = bedMapper.selectList(
                new LambdaQueryWrapper<Bed>().eq(Bed::getElderName, db.getElderName()));
            for (Bed bed : beds) {
                if ("\u8bf7\u5047\u4e2d".equals(bed.getStatus())) {
                    bed.setStatus("\u5df2\u5165\u4f4f");
                    bedMapper.updateById(bed);
                }
            }
        } catch (Exception ignored) {}
        return Result.ok("ok");
    }

    /** \u64a4\u9500\uff1a\u4ec5\u5f85\u5ba1\u6279\u53ef\u5173\u95ed\u3002 */
    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam("id") Long id) {
        Leave db = leaveMapper.selectById(id);
        if (db == null) return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        if (!"\u5f85\u5ba1\u6279".equals(db.getStatus())) {
            return Result.fail("\u4ec5\u5f85\u5ba1\u6279\u53ef\u64a4\u9500");
        }
        db.setStatus("\u5df2\u5173\u95ed");
        leaveMapper.updateById(db);
        return Result.ok("ok");
    }
}
"""
write("zzyl/src/main/java/com/soft/controller/LeaveController.java", leave)
assert "\u5f85\u5ba1\u6279" in leave
assert "<<<<<<<" not in leave
print("part1 OK")
