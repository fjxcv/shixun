package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_floor")
public class Floor {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer sortNum;
}
