package com.soft.config;

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

/**
 * 启动时数据初始化（方案 C：折中版）。
 * <p>
 * 职责：补齐表结构、演示账号、预约/客户/房型/楼层床位等基础数据；
 * 协同相关演示单据采用「选择性清理」，避免全表清空误伤真实业务，
 * 同时启动时不再反复写入请假/退住/入住，防止污染「我的待办/申请」。
 * </p>
 */
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

    /** 15 位老人统一主数据：姓名、身份证号、性别、电话 */
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

    /** 15 人合同状态：前若干「生效中」，其余为未生效/已过期/已失效 */
    private static final String[] CONTRACT_STATUS = {
        "生效中", "生效中", "生效中", "生效中", "未生效",
        "生效中", "生效中", "已过期", "已失效", "已失效",
        "生效中", "未生效", "已过期", "已过期", "已过期"
    };

    private static final String[] LEAVE_STATUS = {"请假中", "超时未归", "已返回"};
    private static final String[] ROOM_TYPES = {"四人间", "双人间", "双人间"};
    private static final String NURSING_LEVEL = "中度失能等级";
    /** 演示数据创建人（与远端协同账号一致） */
    private static final String CREATOR = "顾廷烨";

    /**
     * 启动入口：先保证结构与账号，再按选择性策略清理旧演示协同单；
     * 仅在空库时做全量种子；已有预约时最多补楼层床位，不反复写请假/退住/入住。
     */
    @Override
    public void run(String... args) {
        ensureSchema();
        seedUserIfNeeded();
        clearWorkflowDemoData();
        // 空库：一次写全基础演示数据后结束
        if (reservationMapper.selectCount(null) == 0) {
            seedAll();
            return;
        }
        // 非空库：仅在缺少楼层时补建楼层/房间/床位与合同分配
        if (floorMapper.selectCount(null) == 0) {
            seedFloorsAndRooms();
        }
        // 方案 C：不在每次启动时 seedLeaves / seedCheckouts / seedCheckins，避免污染协同待办
    }

    /**
     * 选择性清理协同相关演示种子，避免污染待办与申请列表。
     * 只删除历史脚本写入的 QJ2048/TZ2048 前缀演示单，以及旧演示身份证前缀的入住单；
     * 不清空全表，以免误删真实业务单据。
     */
    private void clearWorkflowDemoData() {
        try {
            jdbcTemplate.update("DELETE FROM t_leave WHERE doc_no LIKE 'QJ2048%'");
            jdbcTemplate.update("DELETE FROM t_checkout WHERE doc_no LIKE 'TZ2048%'");
            jdbcTemplate.update("DELETE FROM t_checkin WHERE elder_idcard LIKE '23020319970122102%'");
        } catch (Exception ignored) {}
    }

    /** 为入住/退住表补齐可能缺失的列（幂等，已存在则跳过） */
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

    /** 若列不存在则 ALTER TABLE 追加；失败静默忽略（兼容不同库环境） */
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

    /** 无用户时写入默认管理员，真实姓名使用 CREATOR（顾廷烨） */
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

    /**
     * 空库全量种子：预约、来访、客户、房型、楼层房间床位（含男女分楼与合同）。
     * 不写入请假/退住演示单；可选写入「已完成」入住，便于请假等业务选老人且不进协同待办。
     */
    private void seedAll() {
        seedReservationAndVisit();
        seedCustomers();
        seedRoomTypes();
        seedFloorsAndRooms();
        seedCheckins();
    }

    /** 写入预约与来访演示数据（与 ELDERS 关联） */
    private void seedReservationAndVisit() {
        String[] types = {"参观预约", "探访预约"};
        String[] visitTypes = {"参观来访", "探访来访"};
        String[] rStatus = {"已完成", "待上门", "已取消", "已过期"};
        for (int i = 0; i < 10; i++) {
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

    /** 写入客户（小程序侧）演示数据 */
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

    /** 写入五种房型及价格；豪华单人间默认停用 */
    private void seedRoomTypes() {
        String[] names = {"四人间", "三人间", "双人间", "普通单人间", "豪华单人间"};
        BigDecimal[] prices = {
            new BigDecimal("800"), new BigDecimal("1200"), new BigDecimal("1500"),
            new BigDecimal("2000"), new BigDecimal("2700")
        };
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

    /**
     * 创建 3 层楼、每层 3 间房及床位（床位号 101A 风格）；
     * 合同生效中的老人按性别分楼入住（男 1 楼、女 2 楼），并写入 15 份合同种子。
     */
    private void seedFloorsAndRooms() {
        // 每层 3 个房间：房间1/2=双人间(2床)，房间3=四人间(4床)
        int[] bedsPerRoom = {2, 2, 4};
        String[] roomTypeLabels = {"双人间", "双人间", "四人间"};
        LocalDate today = LocalDate.now();
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
                    // 床位号：房号 + A/B/C/D，如 101A
                    bed.setBedNo(room.getRoomNo() + String.valueOf((char) ('A' + k - 1)));
                    bed.setStatus("空闲");
                    bedMapper.insert(bed);
                }
            }
        }

        // 生效中人员按男女分组后分配到床位
        int activeIdx = 0;
        int[] activePeople = new int[15];
        for (int i = 0; i < 15; i++) {
            if (active[i]) activePeople[activeIdx++] = i;
        }
        int[] maleActive = Arrays.stream(activePeople, 0, activeIdx)
            .filter(i -> "男".equals(ELDERS[i][2])).toArray();
        int[] femaleActive = Arrays.stream(activePeople, 0, activeIdx)
            .filter(i -> "女".equals(ELDERS[i][2])).toArray();

        List<Bed> allBeds = bedMapper.selectList(null);
        Map<Integer, List<Bed>> bedsByRoom = allBeds.stream()
            .collect(Collectors.groupingBy(Bed::getRoomId));
        List<Integer> roomIds = bedsByRoom.keySet().stream().sorted().collect(Collectors.toList());

        // 男性安排到 1 楼
        int bedAssignIdx = 0;
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
        // 女性安排到 2 楼
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

        // 为 15 位老人写入合同（日期按状态推算）
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

    /**
     * 空库初始化时写入 15 条「已完成」入住记录，方便请假等业务选择老人；
     * 全部 flowStatus=已完成，不会进入协同待办。
     * 注意：不在每次启动调用，仅由 seedAll 触发。
     */
    private void seedCheckins() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 15; i++) {
            Checkin ci = new Checkin();
            ci.setDocNo("RZ2048101015" + String.format("%06d", i + 1));
            ci.setElderName(ELDERS[i][0]);
            ci.setElderIdcard(ELDERS[i][1]);
            ci.setElderPhone(ELDERS[i][3]);
            ci.setGender(ELDERS[i][2]);
            ci.setBirthDate(LocalDate.of(1940 + i % 20, 1 + i % 12, 1 + i % 28));
            ci.setAge(80 - i);
            ci.setAddress("阳光社区" + (i + 1) + "栋");
            int ciOffset = i * 20;
            LocalDate ciDate = today.minusDays(30 + ciOffset);
            ci.setCheckinDate(ciDate);
            ci.setPeriodStart(ciDate);
            ci.setPeriodEnd(ciDate.plusYears(1));
            ci.setNursingLevel(NURSING_LEVEL);
            ci.setDeposit(new BigDecimal("3000"));
            ci.setNursingFee(new BigDecimal("1500"));
            ci.setBedFee(new BigDecimal("800"));
            ci.setStep(5);
            ci.setFlowStatus("已完成");
            ci.setStepStatus("已完成");
            ci.setFinishTime(LocalDateTime.now().minusDays(i));
            ci.setContractNo("HT2048101015" + String.format("%06d", i + 1));
            ci.setApplicant(CREATOR);
            ci.setApplyTime(LocalDateTime.now().minusDays(i * 5L));
            ci.setCreator(CREATOR);
            ci.setCreateTime(LocalDateTime.now().minusDays(i));
            checkinMapper.insert(ci);
        }
    }

    /**
     * 请假演示种子（保留方法供手工/脚本调用）。
     * 方案 C 下启动与 seedAll 均不自动调用，以免协同待办被演示单污染。
     */
    @SuppressWarnings("unused")
    private void seedLeaves() {
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
            l.setStatus(LEAVE_STATUS[0]);
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

    /**
     * 退住演示种子（保留方法供手工/脚本调用）。
     * 方案 C 下启动与 seedAll 均不自动调用。
     */
    @SuppressWarnings("unused")
    private void seedCheckouts() {
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
            co.setNursingLevel(NURSING_LEVEL);
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
}
