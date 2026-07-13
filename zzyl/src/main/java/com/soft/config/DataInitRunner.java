package com.soft.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.mapper.*;
import com.soft.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitRunner implements CommandLineRunner {

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private VisitMapper visitMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private ContractMapper contractMapper;
    @Autowired private RoomTypeMapper roomTypeMapper;
    @Autowired private FloorMapper floorMapper;
    @Autowired private RoomMapper roomMapper;
    @Autowired private BedMapper bedMapper;
    @Autowired private LeaveMapper leaveMapper;
    @Autowired private CheckoutMapper checkoutMapper;
    @Autowired private CheckinMapper checkinMapper;
    @Autowired private com.soft.mapper.UserMapper userMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    // 15 位老人的统一数据（姓名、身份证号、性别、电话）
    private static final String[][] ELDERS = {
        {"赵德福", "230203194801011001", "男", "13800138001"},
        {"钱秀英", "230203195202021002", "女", "13800138002"},
        {"孙志强", "230203195103031003", "男", "13800138003"},
        {"李桂花", "230203194904041004", "女", "13800138004"},
        {"周明远", "230203195005051005", "男", "13800138005"},
        {"吴淑珍", "230203195106061006", "女", "13800138006"},
        {"郑永昌", "230203194707071007", "男", "13800138007"},
        {"王玉兰", "230203195208081008", "女", "13800138008"},
        {"冯国华", "230203194909091009", "男", "13800138009"},
        {"陈素芬", "230203195110101010", "女", "13800138010"},
        {"蒋孝文", "230203194611111011", "男", "13800138011"},
        {"沈美琴", "230203195312121012", "女", "13800138012"},
        {"韩德胜", "230203195001011013", "男", "13800138013"},
        {"曹桂芳", "230203195102021014", "女", "13800138014"},
        {"许长山", "230203194903031015", "男", "13800138015"}
    };

    // 15人合同状态分布：前8人生效中，后7人未生效/已过期/已失效
    private static final String[] CONTRACT_STATUS = {
        "生效中","生效中","生效中","生效中","未生效",
        "生效中","生效中","已过期","已失效","已失效",
        "生效中","未生效","已过期","已过期","已过期"
    };
    private static final String[] LEAVE_STATUS = {"请假中", "超时未归", "已返回"};
    private static final String[] ROOM_TYPES = {"四人间", "双人间", "双人间"};
    private static final String NURSING_LEVEL = "中度失能等级";
    private static final String CREATOR = "顾廷烨";

    @Override
    public void run(String... args) {
        ensureSchema();
        seedUserIfNeeded();
        clearWorkflowDemoData();
        if (reservationMapper.selectCount(null) == 0) {
            seedAll();
            return;
        }
        if (floorMapper.selectCount(null) == 0) {
            seedFloorsAndRooms();
        }
        // 每次启动重新生成请假和退住演示数据（被 clearWorkflowDemoData 清空后）
        if (leaveMapper.selectCount(null) == 0) {
            seedLeaves();
        }
        if (checkinMapper.selectCount(null) == 0) {
            seedCheckins();
        }
        if (checkoutMapper.selectCount(null) == 0) {
            seedCheckouts();
        }
    }

    private void clearWorkflowDemoData() {
        try {
            jdbcTemplate.update("DELETE FROM t_leave");
            jdbcTemplate.update("DELETE FROM t_checkout");
            jdbcTemplate.update("DELETE FROM t_checkin");
        } catch (Exception ignored) {}
    }

    private void ensureSchema() {
        String[][] cols = {
            {"t_checkout", "flow_status", "VARCHAR(20)"},
            {"t_checkout", "approval_result", "VARCHAR(20)"},
            {"t_checkout", "approval_comment", "VARCHAR(500)"},
            {"t_checkout", "bill_receivable", "DECIMAL(12,2)"},
            {"t_checkout", "bill_arrears", "DECIMAL(12,2)"},
            {"t_checkout", "bill_balance", "DECIMAL(12,2)"},
            {"t_checkin", "ethnicity", "VARCHAR(20)"},
            {"t_checkin", "political_status", "VARCHAR(20)"},
            {"t_checkin", "religion", "VARCHAR(20)"},
            {"t_checkin", "marital_status", "VARCHAR(20)"},
            {"t_checkin", "education_level", "VARCHAR(10)"},
            {"t_checkin", "income_source", "VARCHAR(20)"},
            {"t_checkin", "medical_insurance", "VARCHAR(30)"},
            {"t_checkin", "hobbies", "VARCHAR(20)"},
            {"t_checkin", "medical_insurance_no", "VARCHAR(19)"}
        };
        for (String[] col : cols) {
            tryAddColumn(col[0], col[1], col[2]);
        }
    }

    private void tryAddColumn(String table, String column, String definition) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?",
                Integer.class, table, column);
            if (cnt != null && cnt == 0) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        } catch (Exception ignored) {}
    }

    private void seedUserIfNeeded() {
        if (userMapper.selectCount(null) > 0) return;
        User u = new User();
        u.setAccount("admin");
        u.setUpwd("123456");
        u.setRealname(CREATOR);
        u.setEmail("gutingye@zzyl.com");
        u.setDepartment("入住部");
        u.setJob("部门主管");
        u.setRole("护理组主管");
        u.setPhone("13898988888");
        u.setSex("男");
        u.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        u.setIslock(0);
        userMapper.insert(u);
    }

    private void seedAll() {
        seedReservationAndVisit();
        seedCustomers();
        seedRoomTypes();
        seedFloorsAndRooms();
        seedLeaves();
        seedCheckins();
        seedCheckouts();
    }

    private void seedReservationAndVisit() {
        String[] types = {"参观预约", "探访预约"};
        String[] visitTypes = {"参观来访", "探访来访"};
        for (int i = 0; i < 10; i++) {
            String[] rStatus = {"已完成", "待上门", "已取消", "已过期"};
            Reservation r = new Reservation();
            r.setType(types[i % 2]);
            r.setVisitorName("访客" + (i + 1));
            r.setVisitorPhone("1387556889" + i);
            r.setElderName(ELDERS[(i + 2) % 15][0]);
            r.setAppointTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            r.setStatus(rStatus[i % 4]);
            r.setCreator(CREATOR);
            r.setCreateTime(LocalDateTime.now().minusDays(i));
            reservationMapper.insert(r);

            Visit v = new Visit();
            v.setType(visitTypes[i % 2]);
            v.setVisitorName("访客" + (i + 1));
            v.setVisitorPhone("1387556889" + i);
            v.setElderName(ELDERS[(i + 1) % 15][0]);
            v.setVisitTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            v.setCreator(CREATOR);
            v.setCreateTime(LocalDateTime.now().minusDays(i));
            visitMapper.insert(v);
        }
    }

    private void seedCustomers() {
        String[] nicknames = {"范闲", "范思哲", "范若若", "林婉儿", "海棠朵朵"};
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer();
            c.setNickname(nicknames[i % nicknames.length]);
            c.setPhone("1380013800" + i);
            c.setSigned(i % 2);
            c.setOrderCount(i % 6);
            c.setElderNames("赵德福、钱秀英");
            c.setFirstLoginTime(LocalDateTime.now().minusDays(i * 2));
            customerMapper.insert(c);
        }
    }

    private void seedRoomTypes() {
        String[] names = {"四人间", "三人间", "双人间", "普通单人间", "豪华单人间"};
        BigDecimal[] prices = {new BigDecimal("800"), new BigDecimal("1200"), new BigDecimal("1500"),
                new BigDecimal("2000"), new BigDecimal("2700")};
        for (int i = 0; i < 5; i++) {
            RoomType rt = new RoomType();
            rt.setName(names[i]);
            rt.setPrice(prices[i]);
            rt.setIntro("房间内设有24小时cctv监控");
            rt.setStatus(i == 4 ? 0 : 1);
            rt.setCreator(CREATOR);
            rt.setCreateTime(LocalDateTime.now());
            roomTypeMapper.insert(rt);
        }
    }

    private void seedFloorsAndRooms() {
        // 每层 3 个房间，房号规则：楼层*100 + j
        // 房间1=双人间(2床)，房间2=双人间(2床)，房间3=四人间(4床)
        int[] bedsPerRoom = {2, 2, 4};
        String[] roomTypeLabels = {"双人间", "双人间", "四人间"};
        LocalDate today = LocalDate.now();
        // 先确定哪些人合同生效中
        boolean[] active = new boolean[15];
        for (int i = 0; i < 15; i++) {
            active[i] = "生效中".equals(CONTRACT_STATUS[i]);
        }

        for (int floor = 1; floor <= 3; floor++) {
            Floor f = new Floor();
            f.setName(floor + "楼");
            f.setSortNum(floor);
            floorMapper.insert(f);

            for (int j = 1; j <= 3; j++) {
                Room room = new Room();
                room.setFloorId(f.getId());
                room.setRoomNo(String.valueOf(floor * 100 + j));
                room.setRoomTypeId(j);
                room.setRoomTypeName(roomTypeLabels[j - 1]);
                roomMapper.insert(room);

                for (int k = 1; k <= bedsPerRoom[j - 1]; k++) {
                    Bed bed = new Bed();
                    bed.setRoomId(room.getId());
                    bed.setBedNo(room.getRoomNo() + String.valueOf((char)('A' + k - 1)));
                    bed.setStatus("空闲");
                    bedMapper.insert(bed);
                }
            }
        }

        // 为 15 位老人创建合同（按 CONTRACT_STATUS 分布计算日期）
        // 提取生效中的人员并分配到床位（男/女分房间）
        int activeIdx = 0;
        int[] activePeople = new int[15];
        for (int i = 0; i < 15; i++) {
            if (active[i]) activePeople[activeIdx++] = i;
        }
        // 按男女分组
        int[] maleActive = Arrays.stream(activePeople, 0, activeIdx).filter(i -> "男".equals(ELDERS[i][2])).toArray();
        int[] femaleActive = Arrays.stream(activePeople, 0, activeIdx).filter(i -> "女".equals(ELDERS[i][2])).toArray();
        // 获取所有空床位，按房间分组
        List<Bed> allBeds = bedMapper.selectList(null);
        Map<Integer, List<Bed>> bedsByRoom = allBeds.stream().collect(Collectors.groupingBy(Bed::getRoomId));
        // 安排男性到1楼
        int bedAssignIdx = 0;
        List<Integer> roomIds = bedsByRoom.keySet().stream().sorted().collect(Collectors.toList());
        for (Integer roomId : roomIds) {
            Room r = roomMapper.selectById(roomId);
            if (r == null || Integer.parseInt(r.getRoomNo()) / 100 != 1) continue;
            for (Bed b : bedsByRoom.get(roomId)) {
                if (bedAssignIdx < maleActive.length) {
                    b.setElderName(ELDERS[maleActive[bedAssignIdx]][0]);
                    b.setStatus(maleActive[bedAssignIdx] == 0 ? "请假中" : "已入住");
                    bedMapper.updateById(b);
                    bedAssignIdx++;
                }
            }
        }
        // 安排女性到2楼
        bedAssignIdx = 0;
        for (Integer roomId : roomIds) {
            Room r = roomMapper.selectById(roomId);
            if (r == null || Integer.parseInt(r.getRoomNo()) / 100 != 2) continue;
            for (Bed b : bedsByRoom.get(roomId)) {
                if (bedAssignIdx < femaleActive.length) {
                    int pi = femaleActive[bedAssignIdx];
                    b.setElderName(ELDERS[pi][0]);
                    b.setStatus(pi == 11 ? "请假中" : "已入住");
                    bedMapper.updateById(b);
                    bedAssignIdx++;
                }
            }
        }
        for (int i = 0; i < 15; i++) {
            Contract c = new Contract();
            c.setContractNo("HT2048101015" + String.format("%06d", i + 1));
            c.setContractName(ELDERS[i][0] + "长住合同");
            c.setElderName(ELDERS[i][0]);
            c.setElderIdcard(ELDERS[i][1]);
            c.setCheckinNo("RZ2048101015" + String.format("%06d", i + 1));
            String status = CONTRACT_STATUS[i];
            LocalDate start, end;
            int offset = i * 20;
            switch (status) {
                case "未生效":
                    start = today.plusDays(30 + offset);
                    end = start.plusYears(1);
                    break;
                case "生效中":
                    start = today.minusDays(30 + offset);
                    end = today.plusDays(365 - offset);
                    break;
                case "已过期":
                    start = today.minusDays(400 + offset);
                    end = today.minusDays(30 + offset);
                    break;
                default: // 已失效
                    start = today.minusDays(90);
                    end = today.plusDays(275);
                    break;
            }
            c.setStartDate(start);
            c.setEndDate(end);
            c.setStatus(status);
            c.setSignDate(start);
            if ("已失效".equals(status)) {
                c.setThirdPartyName("范闲");
                c.setThirdPartyPhone("13678987777");
                c.setTerminateUser(CREATOR);
                c.setTerminateDate(today.minusDays(15));
                c.setTerminateFile("解除协议.pdf");
            }
            c.setContractFile(ELDERS[i][0] + "长住合同.pdf");
            c.setCreator(CREATOR);
            c.setCreateTime(LocalDateTime.now().minusDays(i));
            contractMapper.insert(c);
        }

    }

    private void seedCheckouts() {
        // 为合同已失效的老人创建退住记录（索引 8,9 为已失效）
        LocalDate today = LocalDate.now();
        int[] terminated = {8, 9};
        for (int idx : terminated) {
            Checkout co = new Checkout();
            co.setDocNo("TZ2048101015" + String.format("%06d", idx + 1));
            co.setElderName(ELDERS[idx][0]);
            co.setElderIdcard(ELDERS[idx][1]);
            co.setElderPhone(ELDERS[idx][3]);
            co.setFeeStart(today.minusDays(90));
            co.setFeeEnd(today);
            co.setNursingLevel("中度失能等级");
            co.setBedInfo("1楼" + (idx == 8 ? "101A" : "101B") + "床位");
            co.setContractName(ELDERS[idx][0] + "长住合同");
            co.setContractFile(ELDERS[idx][0] + "长住合同.pdf");
            co.setConsultant(CREATOR);
            co.setCaregivers(CREATOR + "、盛明兰");
            co.setCheckoutDate(today.minusDays(15));
            co.setReason("家属要求退住");
            co.setRemark("已和家属沟通");
            co.setApplicant(CREATOR);
            co.setApplyTime(today.minusDays(16).atTime(15, 0));
            co.setStep(7);
            co.setStepStatus("已完成");
            co.setFlowStatus("已完成");
            co.setTerminateDate(today.minusDays(15));
            co.setTerminateFile("解除协议.pdf");
            co.setCreator(CREATOR);
            co.setCreateTime(today.minusDays(15).atTime(10, 0));
            checkoutMapper.insert(co);
        }
    }

    private void seedCheckins() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 15; i++) {
            Checkin ci = new Checkin();
            ci.setDocNo("RZ2048101015" + String.format("%06d", i + 1));
            ci.setElderName(ELDERS[i][0]);
            ci.setElderIdcard(ELDERS[i][1]);
            ci.setElderPhone(ELDERS[i][3]);
            ci.setGender(i % 2 == 0 ? "男" : "女");
            ci.setBirthDate(LocalDate.of(1940 + i % 20, 1 + i % 12, 1 + i % 28));
            ci.setAge(80 - i);
            ci.setAddress("阳光社区" + (i + 1) + "栋");
            int ciOffset = i * 20;
            LocalDate ciDate = today.minusDays(30 + ciOffset);
            ci.setCheckinDate(ciDate);
            ci.setPeriodStart(ciDate);
            ci.setPeriodEnd(ciDate.plusYears(1));
            ci.setNursingLevel("中度失能等级");
            ci.setDeposit(new java.math.BigDecimal("3000"));
            ci.setNursingFee(new java.math.BigDecimal("1500"));
            ci.setBedFee(new java.math.BigDecimal("800"));
            ci.setStep(5);
            ci.setFlowStatus("已完成");
            ci.setStepStatus("已完成");
            ci.setFinishTime(LocalDateTime.now().minusDays(i));
            ci.setContractNo("HT2048101015" + String.format("%06d", i + 1));
            ci.setApplicant(CREATOR);
            ci.setApplyTime(LocalDateTime.now().minusDays(i * 5));
            ci.setCreator(CREATOR);
            ci.setCreateTime(LocalDateTime.now().minusDays(i));
            checkinMapper.insert(ci);
        }
    }

    private void seedLeaves() {
        // 为部分在住老人创建请假记录
        // 赵德福(0)→101A, 吴淑珍(5)→201B
        LocalDate today = LocalDate.now();
        String[][] leaveData = {
            {"0", "101A"}, {"5", "201B"}
        };
        for (String[] ld : leaveData) {
            int idx = Integer.parseInt(ld[0]);
            Leave l = new Leave();
            l.setDocNo("QJ2048101015" + String.format("%06d", idx + 1));
            l.setElderName(ELDERS[idx][0]);
            l.setElderIdcard(ELDERS[idx][1]);
            l.setElderPhone(ELDERS[idx][3]);
            l.setNursingLevel(NURSING_LEVEL);
            String floorPrefix = idx == 0 ? "1" : "2";
            l.setBedInfo(floorPrefix + "楼" + ld[1] + "床位");
            l.setCaregivers(CREATOR + "、盛明兰");
            l.setStartTime(today.atTime(15, 0));
            l.setExpectReturnTime(today.plusDays(10).atTime(15, 0));
            l.setStatus(idx == 7 ? "超时未归" : idx == 12 ? "已返回" : "请假中");
            if ("已返回".equals(l.getStatus())) {
                l.setActualReturnTime(today.atTime(18, 0));
                l.setReturnRemark("老人回来的时候，脚扭了");
                l.setCancelUser(CREATOR);
                l.setCancelTime(LocalDateTime.now());
            }
            l.setEscort("无");
            l.setLeaveDays(11);
            l.setReason("家人结婚，需要回家帮忙张罗");
            l.setApplicant(CREATOR);
            l.setApplyTime(LocalDateTime.of(2048, 10, 5, 15, 0));
            l.setCreator(CREATOR);
            l.setCreateTime(LocalDateTime.now().minusDays(idx));
            leaveMapper.insert(l);
        }
    }
}
