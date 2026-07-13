package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.mapper.RoomTypeMapper;
import com.soft.pojo.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房型设置接口。
 * <p>
 * 维护养老院房型（名称、价格、床位数、启用状态等），供「房型设置」及床位房型关联使用。
 */
@RestController
@RequestMapping("/roomType")
public class RoomTypeController {
    @Autowired private RoomTypeMapper roomTypeMapper;

    /**
     * 查询全部房型，按创建时间倒序。
     */
    @GetMapping("/list")
    public Result<List<RoomType>> list() {
        return Result.ok(roomTypeMapper.selectList(
                new LambdaQueryWrapper<RoomType>().orderByDesc(RoomType::getCreateTime)));
    }

    /**
     * 新增或更新房型。
     * 缺省 status=1（启用）；有 id 则更新，无 id 则新增。
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody RoomType rt) {
        if (rt.getStatus() == null) rt.setStatus(1);
        if (rt.getId() == null) roomTypeMapper.insert(rt);
        else roomTypeMapper.updateById(rt);
        return Result.ok("saved");
    }

    /**
     * 按 ID 删除房型。
     * 注意：不校验是否已被房间引用，调用方需自行保证数据一致性。
     */
    @GetMapping("/delete")
    public Result<String> delete(@RequestParam("id") Long id) {
        roomTypeMapper.deleteById(id);
        return Result.ok("deleted");
    }

    /**
     * 切换房型启用/停用状态（1 ↔ 0）。
     *
     * @param id 房型 ID
     */
    @GetMapping("/toggle")
    public Result<String> toggle(@RequestParam("id") Long id) {
        RoomType rt = roomTypeMapper.selectById(id);
        rt.setStatus(rt.getStatus() == 1 ? 0 : 1);
        roomTypeMapper.updateById(rt);
        return Result.ok("ok");
    }
}
