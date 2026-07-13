package com.soft.dto;

import lombok.Data;

/**
 * 忘记密码请求体：账号 + 预留手机号校验通过后设置新密码。
 */
@Data
public class ResetPwdDto {
    private String account;
    /** 须与库中该账号预留手机号一致 */
    private String phone;
    private String newpwd;
}
