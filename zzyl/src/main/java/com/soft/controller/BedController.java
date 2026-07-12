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

@RestController
@RequestMapping("/bed")
public class BedController {
    @Autowired private FloorMapper floorMapper;
    @Autowired private RoomMapper roomMapper;
    @Autowired private BedMapper bedMapper;

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

    @PostMapping("/floor/save")
    public Result<String> saveFloor(@RequestBody Floor floor) {
        if (!StringUtils.hasText(floor.getName())) {
            return Result.fail("\u697c\u5c42\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a");
        }
        long exists = floorMapper.selectCount(
                new LambdaQueryWrapper<Floor>().eq(Floor::getName, floor.getName()));
        if (exists > 0 && floor.getId() == null) {
            return Result.fail("\u697c\u5c42\u540d\u79f0\u5df2\u5b58\u5728");
        }
        if (floor.getId() == null) floorMapper.insert(floor);
        else floorMapper.updateById(floor);
        return Result.ok("saved");
    }

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

    @PostMapping("/room/save")
    public Result<String> saveRoom(@RequestBody Room room) {
        if (!StringUtils.hasText(room.getRoomNo())) {
            return Result.fail("\u623f\u95f4\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (room.getFloorId() == null) {
            return Result.fail("\u8bf7\u9009\u62e9\u697c\u5c42");
        }
        if (!StringUtils.hasText(room.getRoomTypeName())) {
            room.setRoomTypeName("\u56db\u4eba\u95f4");
        }
        long dup = roomMapper.selectCount(new LambdaQueryWrapper<Room>()
                .eq(Room::getFloorId, room.getFloorId())
                .eq(Room::getRoomNo, room.getRoomNo())
                .ne(room.getId() != null, Room::getId, room.getId()));
        if (dup > 0) {
            return Result.fail("\u540c\u697c\u5c42\u623f\u95f4\u53f7\u5df2\u5b58\u5728");
        }
        if (room.getId() == null) roomMapper.insert(room);
        else roomMapper.updateById(room);
        return Result.ok("saved");
    }

    @GetMapping("/room/delete")
    public Result<String> deleteRoom(@RequestParam("id") Integer id) {
        bedMapper.delete(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, id));
        roomMapper.deleteById(id);
        return Result.ok("deleted");
    }

    @PostMapping("/bed/save")
    public Result<String> saveBed(@RequestBody Bed bed) {
        if (bed.getRoomId() == null) {
            return Result.fail("\u7f3a\u5c11\u623f\u95f4ID");
        }
        if (!StringUtils.hasText(bed.getBedNo())) {
            return Result.fail("\u5e8a\u4f4d\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (!StringUtils.hasText(bed.getStatus())) {
            bed.setStatus("\u7a7a\u95f2");
        }
        if ("\u7a7a\u95f2".equals(bed.getStatus())) {
            bed.setElderName("");
        } else if (!StringUtils.hasText(bed.getElderName())) {
            return Result.fail("\u5df2\u5165\u4f4f/\u8bf7\u5047\u4e2d\u5fc5\u987b\u586b\u5199\u8001\u4eba\u59d3\u540d");
        }
        long dup = bedMapper.selectCount(new LambdaQueryWrapper<Bed>()
                .eq(Bed::getRoomId, bed.getRoomId())
                .eq(Bed::getBedNo, bed.getBedNo())
                .ne(bed.getId() != null, Bed::getId, bed.getId()));
        if (dup > 0) {
            return Result.fail("\u540c\u623f\u95f4\u5e8a\u4f4d\u53f7\u5df2\u5b58\u5728");
        }
        if (bed.getId() == null) bedMapper.insert(bed);
        else bedMapper.updateById(bed);
        return Result.ok("saved");
    }

    @GetMapping("/bed/delete")
    public Result<String> deleteBed(@RequestParam("id") Integer id) {
        bedMapper.deleteById(id);
        return Result.ok("deleted");
    }

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
            item.put("doorStatus", "\u5f00\u542f");
            item.put("temperature", "26\u2103");
            item.put("humidity", "40%");
            item.put("alarmStatus", "\u6b63\u5e38");
            item.put("beds", beds.stream().map(b -> {
                Map<String, Object> bm = new HashMap<>();
                bm.put("bedNo", b.getBedNo());
                bm.put("elderName", StringUtils.hasText(b.getElderName()) ? b.getElderName() : "--");
                bm.put("status", b.getStatus());
                boolean occupied = "\u5df2\u5165\u4f4f".equals(b.getStatus());
                boolean onLeave = "\u8bf7\u5047\u4e2d".equals(b.getStatus());
                bm.put("alert", onLeave || (occupied && b.getBedNo() != null && b.getBedNo().endsWith("1")));
                if (occupied) {
                    bm.put("heartRate", onLeave ? 0 : 80);
                    bm.put("breathRate", onLeave ? 0 : 20);
                    String bedNo = b.getBedNo() == null ? "" : b.getBedNo();
                    bm.put("stateLabel", onLeave ? "\u6e05\u9192\u4e2d" : (bedNo.endsWith("2") ? "\u7761\u7720\u4e2d" : "\u6e05\u9192\u4e2d"));
                    bm.put("leaveCount", 0);
                    bm.put("leaveTime", "--");
                } else if ("\u7a7a\u95f2".equals(b.getStatus())) {
                    bm.put("heartRate", 0);
                    bm.put("breathRate", 0);
                    bm.put("stateLabel", "--");
                    bm.put("leaveCount", 0);
                    bm.put("leaveTime", "--");
                } else {
                    bm.put("heartRate", 0);
                    bm.put("breathRate", 0);
                    bm.put("stateLabel", "\u5df2\u79bb\u5e8a");
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
