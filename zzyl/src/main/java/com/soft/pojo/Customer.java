package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String phone;
    private Integer signed;
    private Integer orderCount;
    private String elderNames;
    private LocalDateTime firstLoginTime;
}
