package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_contract")
public class Contract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    private String contractName;
    private String elderName;
    private String elderIdcard;
    private String checkinNo;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDate signDate;
    private String thirdPartyName;
    private String thirdPartyPhone;
    private String contractFile;
    private String terminateUser;
    private LocalDate terminateDate;
    private String terminateFile;
    private String creator;
    private LocalDateTime createTime;
}
