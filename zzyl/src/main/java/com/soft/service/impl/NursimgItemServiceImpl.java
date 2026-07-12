package com.soft.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft.dto.NursingItemDto;
import com.soft.pojo.NursimgItem;
import com.soft.service.NursimgItemService;
import com.soft.mapper.NursimgItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Teacher
* @description 针对表【t_nursimg_item】的数据库操作Service实现
* @createDate 2026-07-07 14:25:45
*/
@Service
public class NursimgItemServiceImpl extends
        ServiceImpl<NursimgItemMapper, NursimgItem>
    implements NursimgItemService{


    @Autowired
    private NursimgItemMapper nursimgItemMapper;
    @Override
    public Map<String, Object> queryNursingItemList(
            NursingItemDto nursingItemDto) {
        //创建Page对象封装分页参数
        Page<NursimgItem> page=new Page<>(
                nursingItemDto.getPageNum()
                ,nursingItemDto.getPageSize());
        //创建Wrapper封装where条件
        QueryWrapper<NursimgItem> params=new QueryWrapper<>();

        String itemname = nursingItemDto.getItemname();
        String islock = nursingItemDto.getIslock();
        params.eq(!StringUtils.isEmpty(itemname),"itemname",itemname);
        params.eq(!StringUtils.isEmpty(islock),"islock",islock);

        List<NursimgItem> nursimgItems
                = nursimgItemMapper.selectList(page, params);


        Map<String, Object> result=new HashMap<>();
        result.put("total",page.getTotal());
        result.put("nursimgItems",nursimgItems);
        return result;
    }
}




