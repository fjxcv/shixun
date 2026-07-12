package com.soft.dto;

import lombok.Data;

@Data
public class NursingItemDto {

    private String itemname;
    private String islock;
    private Integer pageNum=1;
    private Integer pageSize=10;
}
