package com.soft.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String account;
    private String upwd;
    private String realname;
    private String phone;
    private String email;
    private String sex;
}
