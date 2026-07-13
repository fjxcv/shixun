package com.soft.controller;

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

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired private LeaveMapper leaveMapper;
    @Autowired private BedMapper bedMapper;

    @PostMapping("/page")
    public Result<List<Leave>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Leave> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getDocNo())) w.eq(Leave::getDocNo, q.getDocNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Leave::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getElderIdcard())) w.eq(Leave::getElderIdcard, q.getElderIdcard());
        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.hasText(q.getStatus()) && !"鍏ㄩ儴".equals(q.getStatus())) {
            switch (q.getStatus()) {
                case "寰呭鎵?: w.eq(Leave::getStatus, "寰呭鎵?); break;
                case "宸茶繑鍥?: w.eq(Leave::getStatus, "宸茶繑鍥?); break;
                case "宸叉嫆缁?:
                case "宸插叧闂?: w.eq(Leave::getStatus, q.getStatus()); break;
                case "璇峰亣涓?:
                    w.notIn(Leave::getStatus, "宸茶繑鍥?, "宸叉嫆缁?, "宸插叧闂?, "寰呭鎵?)
                     .gt(Leave::getExpectReturnTime, now);
                    break;
                case "瓒呮椂鏈綊":
                    w.notIn(Leave::getStatus, "宸茶繑鍥?, "宸叉嫆缁?, "宸插叧闂?, "寰呭鎵?)
                     .le(Leave::getExpectReturnTime, now);
                    break;
            }
        }
        w.orderByDesc(Leave::getCreateTime);
        Page<Leave> page = leaveMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        // 鍔ㄦ€佽绠楃姸鎬?        for (Leave l : page.getRecords()) {
            if ("宸茶繑鍥?.equals(l.getStatus()) || "宸叉嫆缁?.equals(l.getStatus()) || "宸插叧闂?.equals(l.getStatus())) continue;
            if ("寰呭鎵?.equals(l.getStatus())) continue;
            if (l.getExpectReturnTime() != null && !l.getExpectReturnTime().isAfter(now)) {
                l.setStatus("瓒呮椂鏈綊");
            } else {
                l.setStatus("璇峰亣涓?);
            }
        }
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/detail")
    public Result<Leave> detail(@RequestParam("id") Long id) {
        Leave l = leaveMapper.selectById(id);
        if (l == null) {
            return Result.fail("\u8bf7\u5047\u5355\u4e0d\u5b58\u5728");
        }
        return Result.ok(l);
    }

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

    /** \u4ece Session \u8865\u5168 applicant/creator\uff08\u4e0e\u767b\u5f55 realname \u4e00\u81f4\uff09 */
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

    @PostMapping("/return")
    public Result<String> returnBack(@RequestBody Leave leave) {
        if (leave.getId() == null) return Result.fail("璇峰亣鍗曚笉瀛樺湪");
        Leave db = leaveMapper.selectById(leave.getId());
        if (db == null) return Result.fail("璇峰亣鍗曚笉瀛樺湪");
        if (!"璇峰亣涓?.equals(db.getStatus()) && !"瓒呮椂鏈綊".equals(db.getStatus())) {
            return Result.fail("浠呰鍋囦腑鍙攢鍋?);
        }
        db.setStatus("宸茶繑鍥?);
        // 浣跨敤鍓嶇浼犲叆鐨勫疄闄呰繑鍥炴椂闂达紝鑻ユ湭浼犲垯鐢ㄥ綋鍓嶆椂闂?        if (leave.getActualReturnTime() != null) {
            db.setActualReturnTime(leave.getActualReturnTime());
        } else {
            db.setActualReturnTime(LocalDateTime.now());
        }
        // 鑷姩璁＄畻瀹為檯璇峰亣澶╂暟
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
        // 鍚屾鏇存柊搴婁綅鐘舵€侊紙浠?璇峰亣涓?鈫?宸插叆浣?锛?        try {
            List<Bed> beds = bedMapper.selectList(
                new LambdaQueryWrapper<Bed>().eq(Bed::getElderName, db.getElderName()));
            for (Bed bed : beds) {
                if ("璇峰亣涓?.equals(bed.getStatus())) {
                    bed.setStatus("宸插叆浣?);
                    bedMapper.updateById(bed);
                }
            }
        } catch (Exception ignored) {}
        return Result.ok("ok");
    }

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
