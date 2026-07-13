package com.soft.dto;

import lombok.Data;

/**
 * 协同工作列表项：入住/退住/请假聚合后的统一展示模型。
 * flowStatus 为统一状态（申请中/已完成/已关闭）；bizType 区分业务跳转。
 */
@Data
public class WorkflowItemDto {
    private Long id;
    private String docNo;
    private String title;
    /** 分类展示：入住 / 退住 / 请假 */
    private String category;
    private String applicant;
    private String applyTime;
    private String finishTime;
    /** 统一流程状态：申请中、已完成、已关闭 */
    private String flowStatus;
    private String waitDuration;
    /** 当前步骤；请假待审批为 1，用于待办过滤 */
    private Integer step;
    /** checkin / checkout / leave */
    private String bizType;
}
