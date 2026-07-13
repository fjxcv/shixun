package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.mapper.BedMapper;
import com.soft.mapper.FloorMapper;
import com.soft.mapper.RoomMapper;
import com.soft.pojo.Bed;
import com.soft.pojo.Floor;
import com.soft.pojo.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 床位/楼层/房间管理接口。
 * 负责楼层、房间、床位的增删改查，以及智能床位看板所需的演示数据组装。
 * 前端「床位房型」「智能床位」页面主要调用本控制器。
 */
@RestController
@RequestMapping("/bed")
public class BedController {
    @Autowired private FloorMapper floorMapper;
    @Autowired private RoomMapper roomMapper;
    @Autowired private BedMapper bedMapper;

    /**
     * 查询楼层列表（按排序号、ID 升序）。
     * 同名楼层去重，仅保留第一条，避免历史脏数据导致前端出现重复楼层。
     *
     * @return 去重后的楼层列表
     */
    @GetMapping("/floors")
    public Result<List<Floor>> floors() {
        List<Floor> all = floorMapper.selectList(
                new LambdaQueryWrapper<Floor>().orderByAsc(Floor::getSortNum).orderByAsc(Floor::getId));
        Map<String, Floor> deduped = new LinkedHashMap<>();
        for (Floor floor : all) {
            String key = floor.getName() == null ? String.valueOf(floor.getId()) : floor.getName();
            deduped.putIfAbsent(key, floor);
        }
        return Result.ok(new ArrayList<>(deduped.values()));
    }

    /**
     * 新增或更新楼层。
     * 新增时名称不能为空，且不可与已有楼层重名。
     *
     * @param floor 楼层实体（有 id 则更新，无 id 则新增）
     */
    @PostMapping("/floor/save")
    public Result<String> saveFloor(@RequestBody Floor floor) {
        if (!StringUtils.hasText(floor.getName())) {
            return Result.fail("楼层名称不能为空");
        }
        long exists = floorMapper.selectCount(
                new LambdaQueryWrapper<Floor>().eq(Floor::getName, floor.getName()));
        if (exists > 0 && floor.getId() == null) {
            return Result.fail("楼层名称已存在");
        }
        if (floor.getId() == null) floorMapper.insert(floor);
        else floorMapper.updateById(floor);
        return Result.ok("saved");
    }

    /**
     * 按楼层查询房间及其床位。
     * 会把同名楼层的所有 ID 一并纳入查询（兼容同名楼层拆分存储的情况）。
     *
     * @param floorId 楼层 ID；为空时返回空列表
     * @return 每项含 room 与 beds
     */
    @GetMapping("/rooms")
    public Result<List<Map<String, Object>>> rooms(@RequestParam(required = false) Integer floorId) {
        if (floorId == null) {
            return Result.ok(Collections.emptyList());
        }
        List<Integer> floorIds = resolveFloorIds(floorId);
        List<Room> rooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>().in(Room::getFloorId, floorIds));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room room : rooms) {
            Map<String, Object> m = new HashMap<>();
            m.put("room", room);
            List<Bed> beds = bedMapper.selectList(
                    new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, room.getId()));
            m.put("beds", beds);
            result.add(m);
        }
        return Result.ok(result);
    }

    /** 解析同名楼层的全部 ID，保证查房间时不漏数据。 */
    private List<Integer> resolveFloorIds(Integer floorId) {
        List<Integer> floorIds = new ArrayList<>();
        floorIds.add(floorId);
        Floor floor = floorMapper.selectById(floorId);
        if (floor != null && StringUtils.hasText(floor.getName())) {
            List<Floor> sameName = floorMapper.selectList(
                    new LambdaQueryWrapper<Floor>().eq(Floor::getName, floor.getName()));
            for (Floor f : sameName) {
                if (!floorIds.contains(f.getId())) {
                    floorIds.add(f.getId());
                }
            }
        }
        return floorIds;
    }

    /**
     * 新增或更新房间。
     * 房间号、楼层必填；房型名缺省为「四人间」；同楼层房间号不可重复。
     */
    @PostMapping("/room/save")
    public Result<String> saveRoom(@RequestBody Room room) {
        if (!StringUtils.hasText(room.getRoomNo())) {
            return Result.fail("房间号不能为空");
        }
        if (room.getFloorId() == null) {
            return Result.fail("请选择楼层");
        }
        if (!StringUtils.hasText(room.getRoomTypeName())) {
            room.setRoomTypeName("四人间");
        }
        long dup = roomMapper.selectCount(new LambdaQueryWrapper<Room>()
                .eq(Room::getFloorId, room.getFloorId())
                .eq(Room::getRoomNo, room.getRoomNo())
                .ne(room.getId() != null, Room::getId, room.getId()));
        if (dup > 0) {
            return Result.fail("同楼层房间号已存在");
        }
        if (room.getId() == null) roomMapper.insert(room);
        else roomMapper.updateById(room);
        return Result.ok("saved");
    }

    /**
     * 删除房间，并级联删除该房间下全部床位。
     *
     * @param id 房间 ID
     */
    @GetMapping("/room/delete")
    public Result<String> deleteRoom(@RequestParam("id") Integer id) {
        bedMapper.delete(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, id));
        roomMapper.deleteById(id);
        return Result.ok("deleted");
    }

    /**
     * 新增或更新床位。
     * 状态缺省「空闲」；空闲时清空老人姓名；已入住/请假中必须填写老人姓名。
     * 同房间床位号不可重复。
     */
    @PostMapping("/bed/save")
    public Result<String> saveBed(@RequestBody Bed bed) {
        if (bed.getRoomId() == null) {
            return Result.fail("缺少房间ID");
        }
        if (!StringUtils.hasText(bed.getBedNo())) {
            return Result.fail("床位号不能为空");
        }
        if (!StringUtils.hasText(bed.getStatus())) {
            bed.setStatus("空闲");
        }
        if ("空闲".equals(bed.getStatus())) {
            bed.setElderName("");
        } else if (!StringUtils.hasText(bed.getElderName())) {
            return Result.fail("已入住/请假中必须填写老人姓名");
        }
        long dup = bedMapper.selectCount(new LambdaQueryWrapper<Bed>()
                .eq(Bed::getRoomId, bed.getRoomId())
                .eq(Bed::getBedNo, bed.getBedNo())
                .ne(bed.getId() != null, Bed::getId, bed.getId()));
        if (dup > 0) {
            return Result.fail("同房间床位号已存在");
        }
        if (bed.getId() == null) bedMapper.insert(bed);
        else bedMapper.updateById(bed);
        return Result.ok("saved");
    }

    /**
     * 按 ID 删除床位（不影响房间）。
     */
    @GetMapping("/bed/delete")
    public Result<String> deleteBed(@RequestParam("id") Integer id) {
        bedMapper.deleteById(id);
        return Result.ok("deleted");
    }

    /**
     * 智能床位看板数据。
     * 基于真实房间/床位状态，补充门磁、温湿度、心率等演示字段，供前端可视化展示。
     *
     * @param floorId 楼层 ID；为空返回空列表
     */
    @GetMapping("/smart")
    public Result<List<Map<String, Object>>> smartBeds(@RequestParam(required = false) Integer floorId) {
        if (floorId == null) {
            return Result.ok(Collections.emptyList());
        }
        List<Map<String, Object>> roomData = rooms(floorId).getData();
        if (roomData == null) {
            return Result.ok(Collections.emptyList());
        }
        List<Map<String, Object>> smart = new ArrayList<>();
        for (Map<String, Object> rm : roomData) {
            Room room = (Room) rm.get("room");
            if (room == null) continue;
            @SuppressWarnings("unchecked")
            List<Bed> beds = (List<Bed>) rm.getOrDefault("beds", Collections.emptyList());
            Map<String, Object> item = new HashMap<>();
            item.put("roomNo", room.getRoomNo());
            item.put("doorStatus", "开启");
            item.put("temperature", "26℃");
            item.put("humidity", "40%");
            item.put("alarmStatus", "正常");
            item.put("beds", beds.stream().map(b -> {
                Map<String, Object> bm = new HashMap<>();
                bm.put("bedNo", b.getBedNo());
                bm.put("elderName", StringUtils.hasText(b.getElderName()) ? b.getElderName() : "--");
                bm.put("status", b.getStatus());
                boolean occupied = "已入住".equals(b.getStatus());
                boolean onLeave = "请假中".equals(b.getStatus());
                bm.put("alert", onLeave || (occupied && b.getBedNo() != null && b.getBedNo().endsWith("1")));
                if (occupied) {
                    bm.put("heartRate", onLeave ? 0 : 80);
                    bm.put("breathRate", onLeave ? 0 : 20);
                    String bedNo = b.getBedNo() == null ? "" : b.getBedNo();
                    bm.put("stateLabel", onLeave ? "清醒中" : (bedNo.endsWith("2") ? "睡眠中" : "清醒中"));
                    bm.put("leaveCount", 0);
                    bm.put("leaveTime", "--");
                } else if ("空闲".equals(b.getStatus())) {
                    bm.put("heartRate", 0);
                    bm.put("breathRate", 0);
                    bm.put("stateLabel", "--");
                    bm.put("leaveCount", 0);
                    bm.put("leaveTime", "--");
                } else {
                    bm.put("heartRate", 0);
                    bm.put("breathRate", 0);
                    bm.put("stateLabel", "已离床");
                    bm.put("leaveCount", 6);
                    bm.put("leaveTime", "20:00:00");
                }
                return bm;
            }).collect(Collectors.toList()));
            smart.add(item);
        }
        return Result.ok(smart);
    }
}
