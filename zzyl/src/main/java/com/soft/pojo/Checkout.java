package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_checkout")
public class Checkout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docNo;
    private String elderName;
    private String elderIdcard;
    private String elderPhone;
    private LocalDate feeStart;
    private LocalDate feeEnd;
    private String nursingLevel;
    private String bedInfo;
    private String contractName;
    private String contractFile;
    private String consultant;
    private String caregivers;
    private LocalDate checkoutDate;
    private String reason;
    private String remark;
    private String applicant;
    private LocalDateTime applyTime;
    private Integer step;
    private String stepStatus;
    private String flowStatus;
    private String approvalResult;
    private String approvalComment;
    private LocalDate terminateDate;
    private String terminateFile;
    private BigDecimal refundAmount;
    private String creator;
    private LocalDateTime createTime;
}
