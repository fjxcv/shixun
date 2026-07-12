package com.soft.dto;

import lombok.Data;

@Data
public class PageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String status;
    private String type;
    private String visitorName;
    private String visitorPhone;
    private String elderName;
    private String nickname;
    private String phone;
    private String contractNo;
    private String docNo;
    private String elderIdcard;
    private String applicant;
    private String startTime;
    private String endTime;
}
