package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_room_type")
public class RoomType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private String intro;
    private Integer status;
    private String creator;
    private LocalDateTime createTime;
}
