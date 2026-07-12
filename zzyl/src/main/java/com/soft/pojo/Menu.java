package com.soft.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 
 * @TableName t_menu
 */
@TableName(value ="t_menu")
@Data
public class Menu implements Serializable {

    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 
     */
    private Integer pid;
    /**
     * 
     */
    private String mname;
    /**
     * 
     */
    private String path;
    /**
     * 
     */
    private Integer sort;
    /**
     * 
     */
    private Integer visible;

    @TableField(exist = false)
    //扩展属性子集合菜单属性
    private List<Menu> subItems;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}