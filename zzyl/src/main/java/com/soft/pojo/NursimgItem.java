package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName t_nursimg_item
 */
@TableName(value ="t_nursimg_item")
@Data
public class NursimgItem implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String itemname;

    /**
     * 
     */
    private BigDecimal price;

    /**
     * 
     */
    private String unit;

    /**
     * 
     */
    private String sort;

    /**
     * 
     */
    private String islock;

    /**
     * 
     */
    private String image;

    /**
     * 
     */
    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}