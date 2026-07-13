package com.soft.dto;

import lombok.Data;

/**
 * 通用分页与条件查询参数（入住/退住/请假/协同等列表共用）。
 */
@Data
public class PageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    /** 业务或流程状态；协同里「全部」「已处理」有特殊含义 */
    private String status;
    /** 协同类型筛选：入住/退住/请假 */
    private String type;
    private String visitorName;
    private String visitorPhone;
    private String elderName;
    private String nickname;
    private String phone;
    private String contractNo;
    private String docNo;
    private String elderIdcard;
    /** 申请人关键词；「我的申请」中仅作二次筛选，不作身份范围 */
    private String applicant;
    private String startTime;
    private String endTime;
}
