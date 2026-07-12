package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_leave")
public class Leave {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docNo;
    private String elderName;
    private String elderIdcard;
    private String elderPhone;
    private String nursingLevel;
    private String bedInfo;
    private String caregivers;
    private LocalDateTime startTime;
    private LocalDateTime expectReturnTime;
    private LocalDateTime actualReturnTime;
    private String status;
    private String escort;
    private Integer leaveDays;
    private String reason;
    private String applicant;
    private LocalDateTime applyTime;
    private String returnRemark;
    private String cancelUser;
    private LocalDateTime cancelTime;
    private String creator;
    private LocalDateTime createTime;
}
