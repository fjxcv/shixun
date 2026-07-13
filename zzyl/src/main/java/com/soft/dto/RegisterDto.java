package com.soft.dto;

import lombok.Data;

/**
 * 注册请求体：账号、密码、真实姓名、手机号为必填；邮箱与性别可选。
 */
@Data
public class RegisterDto {
    /** 登录账号（唯一） */
    private String account;
    /** 登录密码 */
    private String upwd;
    /** 真实姓名（写入协同申请归属） */
    private String realname;
    private String phone;
    private String email;
    private String sex;
}
