package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_checkin")
public class Checkin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docNo;
    private String elderName;
    private String elderIdcard;
    private String elderPhone;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private String address;
    private String bedNo;
    private LocalDate checkinDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String nursingLevel;
    private BigDecimal deposit;
    private BigDecimal nursingFee;
    private BigDecimal bedFee;
    private BigDecimal otherFee;
    private BigDecimal insurancePay;
    private BigDecimal govSubsidy;
    private Integer step;
    private String stepStatus;
    private String flowStatus;
    private String approvalResult;
    private String approvalComment;
    private String contractNo;
    private String contractName;
    private String contractFile;
    private LocalDate signDate;
    private String thirdPartyName;
    private String thirdPartyPhone;
    private String familyJson;
    private String extraJson;
    private String applicant;
    private LocalDateTime applyTime;
    private LocalDateTime finishTime;
    private String creator;
    private LocalDateTime createTime;
}
