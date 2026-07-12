package com.soft.dto;

import lombok.Data;

/*
* 创建dto，封装前台提交的用户名和Miami
* */
@Data
public class UserDto {
    private String account;
    private String upwd;
}
