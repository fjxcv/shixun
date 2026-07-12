package com.soft.controller;

import com.soft.dto.NursingItemDto;
import com.soft.pojo.NursimgItem;
import com.soft.service.NursimgItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class NursingItemController {

    //注入service
    @Autowired
    private NursimgItemService nursimgItemService;

    /*定义添加护理计划接口*/
    @RequestMapping("/saveNursingItme")
    public Map<String,Object> saveNursingItem(
            @RequestBody NursimgItem nursimgItem){
        Map<String,Object> result=new HashMap<>();
        result.put("code",400);
        result.put("msg","添加护理项目失败......");

        nursimgItemService.save(nursimgItem);

        result.put("code",200);
        result.put("msg","添加护理项目成功......");

        return result;
    }
    //定义护理项目分页查询接口
    @RequestMapping("/nursingItemPage")
    public Map<String,Object> nursingItemPageList(
            @RequestBody NursingItemDto dto){
        return nursimgItemService.queryNursingItemList(dto);
    }
    /*定义添加护理更新接口*/
    @RequestMapping("/updateNursingItme")
    public Map<String,Object> updateNursingItme(
            @RequestBody NursimgItem nursimgItem){
        Map<String,Object> result=new HashMap<>();
        result.put("code",400);
        result.put("msg","更新护理项目失败......");

        nursimgItemService.updateById(nursimgItem);

        result.put("code",200);
        result.put("msg","更新护理项目成功......");

        return result;
    }

    /*定义添加护理更新接口*/
    @RequestMapping("/deleteNursingItme")
    public Map<String,Object> deleteNursingItme(@RequestParam(name="id") Integer id){
        Map<String,Object> result=new HashMap<>();
        result.put("code",400);
        result.put("msg","删除护理项目失败......");

        nursimgItemService.removeById(id);

        result.put("code",200);
        result.put("msg","删除护理项目成功......");

        return result;
    }

}
