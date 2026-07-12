package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_bed")
public class Bed {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer roomId;
    private String bedNo;
    private String elderName;
    private String status;
}
