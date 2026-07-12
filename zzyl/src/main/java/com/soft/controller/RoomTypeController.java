package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.mapper.RoomTypeMapper;
import com.soft.pojo.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roomType")
public class RoomTypeController {
    @Autowired private RoomTypeMapper roomTypeMapper;

    @GetMapping("/list")
    public Result<List<RoomType>> list() {
        return Result.ok(roomTypeMapper.selectList(
                new LambdaQueryWrapper<RoomType>().orderByDesc(RoomType::getCreateTime)));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody RoomType rt) {
        if (rt.getStatus() == null) rt.setStatus(1);
        if (rt.getId() == null) roomTypeMapper.insert(rt);
        else roomTypeMapper.updateById(rt);
        return Result.ok("saved");
    }

    @GetMapping("/delete")
    public Result<String> delete(@RequestParam("id") Long id) {
        roomTypeMapper.deleteById(id);
        return Result.ok("deleted");
    }

    @GetMapping("/toggle")
    public Result<String> toggle(@RequestParam("id") Long id) {
        RoomType rt = roomTypeMapper.selectById(id);
        rt.setStatus(rt.getStatus() == 1 ? 0 : 1);
        roomTypeMapper.updateById(rt);
        return Result.ok("ok");
    }
}
