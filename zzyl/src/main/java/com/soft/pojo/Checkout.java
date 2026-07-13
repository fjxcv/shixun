package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 退住业务实体，对应表 t_checkout。
 * step / stepStatus / flowStatus 描述多步骤审批进度。
 */
@Data
@TableName("t_checkout")
public class Checkout {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 单据号，如 TZ+时间戳 */
    private String docNo;
    /** 老人姓名 */
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
    /** 计划退住日期 */
    private LocalDate checkoutDate;
    /** 退住原因 */
    private String reason;
    private String remark;
    /** 申请人（与登录 realname 对齐） */
    private String applicant;
    private LocalDateTime applyTime;
    /** 当前流程步骤号 */
    private Integer step;
    /** 步骤状态：进行中/已完成/已关闭 */
    private String stepStatus;
    /** 流程状态：申请中/已完成/已关闭 */
    private String flowStatus;
    /** 最近一次审批结果 */
    private String approvalResult;
    private String approvalComment;
    private LocalDate terminateDate;
    private String terminateFile;
    /** 应退/实退金额 */
    private BigDecimal refundAmount;
    /** 应退金额 */
    private BigDecimal billReceivable;
    /** 欠费金额 */
    private BigDecimal billArrears;
    /** 账户余额 */
    private BigDecimal billBalance;
    /** 创建人 */
    private String creator;
    private LocalDateTime createTime;
}
