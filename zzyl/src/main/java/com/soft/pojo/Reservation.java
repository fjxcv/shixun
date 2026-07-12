package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String visitorName;
    private String visitorPhone;
    private String elderName;
    private LocalDateTime appointTime;
    private String status;
    private String creator;
    private LocalDateTime createTime;
}
