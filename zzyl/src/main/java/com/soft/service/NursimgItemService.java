package com.soft.service;

import com.soft.dto.NursingItemDto;
import com.soft.pojo.NursimgItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author Teacher
* @description 针对表【t_nursimg_item】的数据库操作Service
* @createDate 2026-07-07 14:25:45
*/
public interface NursimgItemService extends IService<NursimgItem> {

    /*添加方法，处理护理项分页查询*/
    public Map<String,Object> queryNursingItemList(
            NursingItemDto nursingItemDto);

}
