package com.soft.dto;

import lombok.Data;

@Data
public class WorkflowItemDto {
    private Long id;
    private String docNo;
    private String title;
    private String category;
    private String applicant;
    private String applyTime;
    private String finishTime;
    private String flowStatus;
    private String waitDuration;
    private Integer step;
    private String bizType;
}
