package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer floorId;
    private String roomNo;
    private Integer roomTypeId;
    private String roomTypeName;
}
