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

    // 15 浣嶈€佷汉鐨勭粺涓€鏁版嵁锛堝鍚嶃€佽韩浠借瘉鍙枫€佹€у埆銆佺數璇濓級
    private static final String[][] ELDERS = {
        {"璧靛痉绂?, "230203194801011001", "鐢?, "13800138001"},
        {"閽辩鑻?, "230203195202021002", "濂?, "13800138002"},
        {"瀛欏織寮?, "230203195103031003", "鐢?, "13800138003"},
        {"鏉庢鑺?, "230203194904041004", "濂?, "13800138004"},
        {"鍛ㄦ槑杩?, "230203195005051005", "鐢?, "13800138005"},
        {"鍚存窇鐝?, "230203195106061006", "濂?, "13800138006"},
        {"閮戞案鏄?, "230203194707071007", "鐢?, "13800138007"},
        {"鐜嬬帀鍏?, "230203195208081008", "濂?, "13800138008"},
        {"鍐浗鍗?, "230203194909091009", "鐢?, "13800138009"},
        {"闄堢礌鑺?, "230203195110101010", "濂?, "13800138010"},
        {"钂嬪瓭鏂?, "230203194611111011", "鐢?, "13800138011"},
        {"娌堢編鐞?, "230203195312121012", "濂?, "13800138012"},
        {"闊╁痉鑳?, "230203195001011013", "鐢?, "13800138013"},
        {"鏇规鑺?, "230203195102021014", "濂?, "13800138014"},
        {"璁搁暱灞?, "230203194903031015", "鐢?, "13800138015"}
    };

    // 15浜哄悎鍚岀姸鎬佸垎甯冿細鍓?浜虹敓鏁堜腑锛屽悗7浜烘湭鐢熸晥/宸茶繃鏈?宸插け鏁?    private static final String[] CONTRACT_STATUS = {
        "鐢熸晥涓?,"鐢熸晥涓?,"鐢熸晥涓?,"鐢熸晥涓?,"鏈敓鏁?,
        "鐢熸晥涓?,"鐢熸晥涓?,"宸茶繃鏈?,"宸插け鏁?,"宸插け鏁?,
        "鐢熸晥涓?,"鏈敓鏁?,"宸茶繃鏈?,"宸茶繃鏈?,"宸茶繃鏈?
    };
    private static final String[] LEAVE_STATUS = {"璇峰亣涓?, "瓒呮椂鏈綊", "宸茶繑鍥?};
    private static final String[] ROOM_TYPES = {"鍥涗汉闂?, "鍙屼汉闂?, "鍙屼汉闂?};
    private static final String NURSING_LEVEL = "涓害澶辫兘绛夌骇";
    private static final String CREATOR = "椤惧环鐑?;

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
        // 姣忔鍚姩閲嶆柊鐢熸垚璇峰亣鍜岄€€浣忔紨绀烘暟鎹紙琚?clearWorkflowDemoData 娓呯┖鍚庯級
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
        u.setDepartment("鍏ヤ綇閮?);
        u.setJob("閮ㄩ棬涓荤");
        u.setRole("鎶ょ悊缁勪富绠?);
        u.setPhone("13898988888");
        u.setSex("鐢?);
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
        String[] types = {"鍙傝棰勭害", "鎺㈣棰勭害"};
        String[] visitTypes = {"鍙傝鏉ヨ", "鎺㈣鏉ヨ"};
        for (int i = 0; i < 10; i++) {
            String[] rStatus = {"宸插畬鎴?, "寰呬笂闂?, "宸插彇娑?, "宸茶繃鏈?};
            Reservation r = new Reservation();
            r.setType(types[i % 2]);
            r.setVisitorName("璁垮" + (i + 1));
            r.setVisitorPhone("1387556889" + i);
            r.setElderName(ELDERS[(i + 2) % 15][0]);
            r.setAppointTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            r.setStatus(rStatus[i % 4]);
            r.setCreator(CREATOR);
            r.setCreateTime(LocalDateTime.now().minusDays(i));
            reservationMapper.insert(r);

            Visit v = new Visit();
            v.setType(visitTypes[i % 2]);
            v.setVisitorName("璁垮" + (i + 1));
            v.setVisitorPhone("1387556889" + i);
            v.setElderName(ELDERS[(i + 1) % 15][0]);
            v.setVisitTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            v.setCreator(CREATOR);
            v.setCreateTime(LocalDateTime.now().minusDays(i));
            visitMapper.insert(v);
        }
    }

    private void seedCustomers() {
        String[] nicknames = {"鑼冮棽", "鑼冩€濆摬", "鑼冭嫢鑻?, "鏋楀鍎?, "娴锋鏈垫湹"};
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer();
            c.setNickname(nicknames[i % nicknames.length]);
            c.setPhone("1380013800" + i);
            c.setSigned(i % 2);
            c.setOrderCount(i % 6);
            c.setElderNames("璧靛痉绂忋€侀挶绉€鑻?);
            c.setFirstLoginTime(LocalDateTime.now().minusDays(i * 2));
            customerMapper.insert(c);
        }
    }

    private void seedRoomTypes() {
        String[] names = {"鍥涗汉闂?, "涓変汉闂?, "鍙屼汉闂?, "鏅€氬崟浜洪棿", "璞崕鍗曚汉闂?};
        BigDecimal[] prices = {new BigDecimal("800"), new BigDecimal("1200"), new BigDecimal("1500"),
                new BigDecimal("2000"), new BigDecimal("2700")};
        for (int i = 0; i < 5; i++) {
            RoomType rt = new RoomType();
            rt.setName(names[i]);
            rt.setPrice(prices[i]);
            rt.setIntro("鎴块棿鍐呰鏈?4灏忔椂cctv鐩戞帶");
            rt.setStatus(i == 4 ? 0 : 1);
            rt.setCreator(CREATOR);
            rt.setCreateTime(LocalDateTime.now());
            roomTypeMapper.insert(rt);
        }
    }

    private void seedFloorsAndRooms() {
        // 姣忓眰 3 涓埧闂达紝鎴垮彿瑙勫垯锛氭ゼ灞?100 + j
        // 鎴块棿1=鍙屼汉闂?2搴?锛屾埧闂?=鍙屼汉闂?2搴?锛屾埧闂?=鍥涗汉闂?4搴?
        int[] bedsPerRoom = {2, 2, 4};
        String[] roomTypeLabels = {"鍙屼汉闂?, "鍙屼汉闂?, "鍥涗汉闂?};
        LocalDate today = LocalDate.now();
        // 鍏堢‘瀹氬摢浜涗汉鍚堝悓鐢熸晥涓?        boolean[] active = new boolean[15];
        for (int i = 0; i < 15; i++) {
            active[i] = "鐢熸晥涓?.equals(CONTRACT_STATUS[i]);
        }

        for (int floor = 1; floor <= 3; floor++) {
            Floor f = new Floor();
            f.setName(floor + "妤?);
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
                    bed.setStatus("绌洪棽");
                    bedMapper.insert(bed);
                }
            }
        }

        // 涓?15 浣嶈€佷汉鍒涘缓鍚堝悓锛堟寜 CONTRACT_STATUS 鍒嗗竷璁＄畻鏃ユ湡锛?        // 鎻愬彇鐢熸晥涓殑浜哄憳骞跺垎閰嶅埌搴婁綅锛堢敺/濂冲垎鎴块棿锛?        int activeIdx = 0;
        int[] activePeople = new int[15];
        for (int i = 0; i < 15; i++) {
            if (active[i]) activePeople[activeIdx++] = i;
        }
        // 鎸夌敺濂冲垎缁?        int[] maleActive = Arrays.stream(activePeople, 0, activeIdx).filter(i -> "鐢?.equals(ELDERS[i][2])).toArray();
        int[] femaleActive = Arrays.stream(activePeople, 0, activeIdx).filter(i -> "濂?.equals(ELDERS[i][2])).toArray();
        // 鑾峰彇鎵€鏈夌┖搴婁綅锛屾寜鎴块棿鍒嗙粍
        List<Bed> allBeds = bedMapper.selectList(null);
        Map<Integer, List<Bed>> bedsByRoom = allBeds.stream().collect(Collectors.groupingBy(Bed::getRoomId));
        // 瀹夋帓鐢锋€у埌1妤?        int bedAssignIdx = 0;
        List<Integer> roomIds = bedsByRoom.keySet().stream().sorted().collect(Collectors.toList());
        for (Integer roomId : roomIds) {
            Room r = roomMapper.selectById(roomId);
            if (r == null || Integer.parseInt(r.getRoomNo()) / 100 != 1) continue;
            for (Bed b : bedsByRoom.get(roomId)) {
                if (bedAssignIdx < maleActive.length) {
                    b.setElderName(ELDERS[maleActive[bedAssignIdx]][0]);
                    b.setStatus(maleActive[bedAssignIdx] == 0 ? "璇峰亣涓? : "宸插叆浣?);
                    bedMapper.updateById(b);
                    bedAssignIdx++;
                }
            }
        }
        // 瀹夋帓濂虫€у埌2妤?        bedAssignIdx = 0;
        for (Integer roomId : roomIds) {
            Room r = roomMapper.selectById(roomId);
            if (r == null || Integer.parseInt(r.getRoomNo()) / 100 != 2) continue;
            for (Bed b : bedsByRoom.get(roomId)) {
                if (bedAssignIdx < femaleActive.length) {
                    int pi = femaleActive[bedAssignIdx];
                    b.setElderName(ELDERS[pi][0]);
                    b.setStatus(pi == 11 ? "璇峰亣涓? : "宸插叆浣?);
                    bedMapper.updateById(b);
                    bedAssignIdx++;
                }
            }
        }
        for (int i = 0; i < 15; i++) {
            Contract c = new Contract();
            c.setContractNo("HT2048101015" + String.format("%06d", i + 1));
            c.setContractName(ELDERS[i][0] + "闀夸綇鍚堝悓");
            c.setElderName(ELDERS[i][0]);
            c.setElderIdcard(ELDERS[i][1]);
            c.setCheckinNo("RZ2048101015" + String.format("%06d", i + 1));
            String status = CONTRACT_STATUS[i];
            LocalDate start, end;
            int offset = i * 20;
            switch (status) {
                case "鏈敓鏁?:
                    start = today.plusDays(30 + offset);
                    end = start.plusYears(1);
                    break;
                case "鐢熸晥涓?:
                    start = today.minusDays(30 + offset);
                    end = today.plusDays(365 - offset);
                    break;
                case "宸茶繃鏈?:
                    start = today.minusDays(400 + offset);
                    end = today.minusDays(30 + offset);
                    break;
                default: // 宸插け鏁?                    start = today.minusDays(90);
                    end = today.plusDays(275);
                    break;
            }
            c.setStartDate(start);
            c.setEndDate(end);
            c.setStatus(status);
            c.setSignDate(start);
            if ("宸插け鏁?.equals(status)) {
                c.setThirdPartyName("鑼冮棽");
                c.setThirdPartyPhone("13678987777");
                c.setTerminateUser(CREATOR);
                c.setTerminateDate(today.minusDays(15));
                c.setTerminateFile("瑙ｉ櫎鍗忚.pdf");
            }
            c.setContractFile(ELDERS[i][0] + "闀夸綇鍚堝悓.pdf");
            c.setCreator(CREATOR);
            c.setCreateTime(LocalDateTime.now().minusDays(i));
            contractMapper.insert(c);
        }

    }

    private void seedCheckouts() {
        // 涓哄悎鍚屽凡澶辨晥鐨勮€佷汉鍒涘缓閫€浣忚褰曪紙绱㈠紩 8,9 涓哄凡澶辨晥锛?        LocalDate today = LocalDate.now();
        int[] terminated = {8, 9};
        for (int idx : terminated) {
            Checkout co = new Checkout();
            co.setDocNo("TZ2048101015" + String.format("%06d", idx + 1));
            co.setElderName(ELDERS[idx][0]);
            co.setElderIdcard(ELDERS[idx][1]);
            co.setElderPhone(ELDERS[idx][3]);
            co.setFeeStart(today.minusDays(90));
            co.setFeeEnd(today);
            co.setNursingLevel("涓害澶辫兘绛夌骇");
            co.setBedInfo("1妤? + (idx == 8 ? "101A" : "101B") + "搴婁綅");
            co.setContractName(ELDERS[idx][0] + "闀夸綇鍚堝悓");
            co.setContractFile(ELDERS[idx][0] + "闀夸綇鍚堝悓.pdf");
            co.setConsultant(CREATOR);
            co.setCaregivers(CREATOR + "銆佺洓鏄庡叞");
            co.setCheckoutDate(today.minusDays(15));
            co.setReason("瀹跺睘瑕佹眰閫€浣?);
            co.setRemark("宸插拰瀹跺睘娌熼€?);
            co.setApplicant(CREATOR);
            co.setApplyTime(today.minusDays(16).atTime(15, 0));
            co.setStep(7);
            co.setStepStatus("宸插畬鎴?);
            co.setFlowStatus("宸插畬鎴?);
            co.setTerminateDate(today.minusDays(15));
            co.setTerminateFile("瑙ｉ櫎鍗忚.pdf");
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
            ci.setGender(i % 2 == 0 ? "鐢? : "濂?);
            ci.setBirthDate(LocalDate.of(1940 + i % 20, 1 + i % 12, 1 + i % 28));
            ci.setAge(80 - i);
            ci.setAddress("闃冲厜绀惧尯" + (i + 1) + "鏍?);
            int ciOffset = i * 20;
            LocalDate ciDate = today.minusDays(30 + ciOffset);
            ci.setCheckinDate(ciDate);
            ci.setPeriodStart(ciDate);
            ci.setPeriodEnd(ciDate.plusYears(1));
            ci.setNursingLevel("涓害澶辫兘绛夌骇");
            ci.setDeposit(new java.math.BigDecimal("3000"));
            ci.setNursingFee(new java.math.BigDecimal("1500"));
            ci.setBedFee(new java.math.BigDecimal("800"));
            ci.setStep(5);
            ci.setFlowStatus("宸插畬鎴?);
            ci.setStepStatus("宸插畬鎴?);
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
        // 涓洪儴鍒嗗湪浣忚€佷汉鍒涘缓璇峰亣璁板綍
        // 璧靛痉绂?0)鈫?01A, 鍚存窇鐝?5)鈫?01B
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
            l.setBedInfo(floorPrefix + "妤? + ld[1] + "搴婁綅");
            l.setCaregivers(CREATOR + "銆佺洓鏄庡叞");
            l.setStartTime(today.atTime(15, 0));
            l.setExpectReturnTime(today.plusDays(10).atTime(15, 0));
            l.setStatus(idx == 7 ? "瓒呮椂鏈綊" : idx == 12 ? "宸茶繑鍥? : "璇峰亣涓?);
            if ("宸茶繑鍥?.equals(l.getStatus())) {
                l.setActualReturnTime(today.atTime(18, 0));
                l.setReturnRemark("鑰佷汉鍥炴潵鐨勬椂鍊欙紝鑴氭壄浜?);
                l.setCancelUser(CREATOR);
                l.setCancelTime(LocalDateTime.now());
            }
            l.setEscort("鏃?);
            l.setLeaveDays(11);
            l.setReason("瀹朵汉缁撳锛岄渶瑕佸洖瀹跺府蹇欏紶缃?);
            l.setApplicant(CREATOR);
            l.setApplyTime(LocalDateTime.of(2048, 10, 5, 15, 0));
            l.setCreator(CREATOR);
            l.setCreateTime(LocalDateTime.now().minusDays(idx));
            leaveMapper.insert(l);
        }
    }
}
