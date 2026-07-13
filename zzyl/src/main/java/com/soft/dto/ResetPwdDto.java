package com.soft.dto;

import lombok.Data;

@Data
public class ResetPwdDto {
    private String account;
    private String phone;
    private String newpwd;
}
