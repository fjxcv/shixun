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

    @Override
    public void run(String... args) {
        ensureSchema();
        seedUserIfNeeded();
        clearWorkflowDemoData();
        if (reservationMapper.selectCount(null) == 0) {
            seedData();
            return;
        }
        if (floorMapper.selectCount(null) == 0) {
            seedFloorsOnly();
        }
    }

    /** 清理协同相关演示种子（入退/请假），避免污染待办与申请列表 */
    private void clearWorkflowDemoData() {
        try {
            jdbcTemplate.update("DELETE FROM t_leave WHERE doc_no LIKE 'QJ2048%'");
            jdbcTemplate.update("DELETE FROM t_checkout WHERE doc_no LIKE 'TZ2048%'");
            jdbcTemplate.update("DELETE FROM t_checkin WHERE elder_idcard LIKE '23020319970122102%'");
        } catch (Exception ignored) {
        }
    }

    private void ensureSchema() {
        tryAddColumn("t_checkout", "flow_status", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkout", "approval_result", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkout", "approval_comment", "VARCHAR(500) DEFAULT NULL");
        tryAddColumn("t_checkout", "bill_receivable", "DECIMAL(12,2) DEFAULT NULL");
        tryAddColumn("t_checkout", "bill_arrears", "DECIMAL(12,2) DEFAULT NULL");
        tryAddColumn("t_checkout", "bill_balance", "DECIMAL(12,2) DEFAULT NULL");
        tryAddColumn("t_checkin", "ethnicity", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkin", "political_status", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkin", "religion", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkin", "marital_status", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkin", "education_level", "VARCHAR(10) DEFAULT NULL");
        tryAddColumn("t_checkin", "income_source", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkin", "medical_insurance", "VARCHAR(30) DEFAULT NULL");
        tryAddColumn("t_checkin", "hobbies", "VARCHAR(20) DEFAULT NULL");
        tryAddColumn("t_checkin", "medical_insurance_no", "VARCHAR(19) DEFAULT NULL");
    }

    private void tryAddColumn(String table, String column, String definition) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?",
                    Integer.class, table, column);
            if (cnt != null && cnt == 0) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        } catch (Exception ignored) {
        }
    }

    private void seedUserIfNeeded() {
        if (userMapper.selectCount(null) > 0) return;
        User u = new User();
        u.setAccount("admin");
        u.setUpwd("123456");
        u.setRealname("\u987e\u5ef7\u70ec");
        u.setEmail("gutingye@zzyl.com");
        u.setDepartment("\u5165\u4f4f\u90e8");
        u.setJob("\u90e8\u95e8\u4e3b\u7ba1");
        u.setRole("\u62a4\u7406\u7ec4\u4e3b\u7ba1");
        u.setPhone("13898988888");
        u.setSex("\u7537");
        u.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        u.setIslock(0);
        userMapper.insert(u);
    }

    private void seedFloorsOnly() {
        String[] roomTypes = {"\u56db\u4eba\u95f4", "\u4e09\u4eba\u95f4", "\u4e24\u4eba\u95f4", "\u666e\u901a\u5355\u4eba\u95f4", "\u8c6a\u534e\u5355\u4eba\u95f4"};
        String[] names = {"\u5b89\u6b23", "\u9ad8\u542f\u5f3a", "\u9ad8\u542f\u76db", "\u9648\u4e66\u5a77", "\u9ad8\u6653\u6668"};
        for (int i = 1; i <= 3; i++) {
            Floor f = new Floor();
            f.setName(i + "\u697c");
            f.setSortNum(i);
            floorMapper.insert(f);
            for (int j = 1; j <= 3; j++) {
                Room room = new Room();
                room.setFloorId(f.getId());
                room.setRoomNo(String.valueOf(100 + j + (i - 1) * 10));
                room.setRoomTypeId((j % 5) + 1);
                room.setRoomTypeName(roomTypes[j % 5]);
                roomMapper.insert(room);
                int bedCount = j == 1 ? 4 : 1;
                for (int k = 1; k <= bedCount; k++) {
                    Bed bed = new Bed();
                    bed.setRoomId(room.getId());
                    bed.setBedNo(room.getRoomNo() + k);
                    if (k <= 2 && j == 1) {
                        bed.setElderName(names[k % names.length]);
                        bed.setStatus(k == 2 && i == 1 ? "\u8bf7\u5047\u4e2d" : "\u5df2\u5165\u4f4f");
                    } else {
                        bed.setStatus("\u7a7a\u95f2");
                    }
                    bedMapper.insert(bed);
                }
            }
        }
    }

    private void seedCheckinOnly() {
        String[] names = {"\u5b89\u6b23", "\u9ad8\u542f\u5f3a", "\u9ad8\u542f\u76db", "\u9648\u4e66\u5a77", "\u9ad8\u6653\u6668"};
        String[] flowStatus = {"\u7533\u8bf7\u4e2d", "\u5df2\u5b8c\u6210", "\u5df2\u5173\u95ed"};
        for (int i = 0; i < 10; i++) {
            Checkin ci = new Checkin();
            ci.setDocNo("RZ" + System.currentTimeMillis() + String.format("%04d", i));
            ci.setElderName(names[i % names.length]);
            ci.setElderIdcard("23020319970122102" + i);
            ci.setElderPhone("1389898888" + i);
            ci.setGender(i % 2 == 0 ? "\u7537" : "\u5973");
            ci.setBirthDate(LocalDate.of(1977, 2, 22));
            ci.setAge(71);
            ci.setAddress("24\u53f7\u697c3\u5355\u5143401\u5ba4");
            ci.setBedNo("101" + (i % 4 + 1));
            LocalDate cd = LocalDate.now().minusDays(i * 5);
            ci.setCheckinDate(cd);
            ci.setPeriodStart(cd);
            ci.setPeriodEnd(cd.plusYears(1));
            ci.setNursingLevel("\u4e2d\u5ea6\u5931\u80fd\u7b49\u7ea7");
            ci.setDeposit(new java.math.BigDecimal("3000"));
            ci.setNursingFee(new java.math.BigDecimal("1500"));
            ci.setBedFee(new java.math.BigDecimal("800"));
            ci.setStep(i % 5 + 1);
            ci.setFlowStatus(flowStatus[i % 3]);
            ci.setStepStatus(ci.getFlowStatus());
            if ("\u5df2\u5b8c\u6210".equals(ci.getFlowStatus())) {
                ci.setFinishTime(LocalDateTime.now().minusDays(i));
            }
            ci.setContractNo("HT" + System.currentTimeMillis() + String.format("%05d", i));
            ci.setApplicant("\u987e\u5ef7\u70ec");
            ci.setApplyTime(LocalDateTime.now().minusDays(i * 5));
            ci.setCreator("\u987e\u5ef7\u70ec");
            ci.setCreateTime(LocalDateTime.now().minusDays(i));
            checkinMapper.insert(ci);
        }
    }

    private void seedData() {
        String[] names = {"\u5b89\u6b23", "\u9ad8\u542f\u5f3a", "\u9ad8\u542f\u76db", "\u9648\u4e66\u5a77", "\u9ad8\u6653\u6668"};
        String[] types = {"\u53c2\u89c2\u9884\u7ea6", "\u63a2\u8bbf\u9884\u7ea6"};
        String[] visitTypes = {"\u53c2\u89c2\u6765\u8bbf", "\u63a2\u8bbf\u6765\u8bbf"};
        for (int i = 0; i < 10; i++) {
            Reservation r = new Reservation();
            r.setType(types[i % 2]);
            r.setVisitorName(names[i % names.length]);
            r.setVisitorPhone("1387556889" + i);
            r.setElderName(names[(i + 2) % names.length]);
            r.setAppointTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            String[] resStatus = {"\u5df2\u5b8c\u6210", "\u5f85\u4e0a\u95e8", "\u5df2\u53d6\u6d88", "\u5df2\u8fc7\u671f"};
            r.setStatus(resStatus[i % 4]);
            r.setCreator("\u987e\u5ef7\u70ec");
            r.setCreateTime(LocalDateTime.now().minusDays(i));
            reservationMapper.insert(r);

            Visit v = new Visit();
            v.setType(visitTypes[i % 2]);
            v.setVisitorName(names[i % names.length]);
            v.setVisitorPhone("1387556889" + i);
            v.setElderName(names[(i + 1) % names.length]);
            v.setVisitTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            v.setCreator("\u987e\u5ef7\u70ec");
            v.setCreateTime(LocalDateTime.now().minusDays(i));
            visitMapper.insert(v);
        }

        String[] customers = {"\u8303\u95f2", "\u8303\u601d\u54f2", "\u8303\u82e5\u82e5", "\u6797\u5a49\u513f", "\u6d77\u68a0\u6735\u6735"};
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer();
            c.setNickname(customers[i % customers.length]);
            c.setPhone("1380013800" + i);
            c.setSigned(i % 2);
            c.setOrderCount(i % 6);
            c.setElderNames("\u9ad8\u542f\u5f3a\u3001\u5b89\u6b23");
            c.setFirstLoginTime(LocalDateTime.now().minusDays(i * 2));
            customerMapper.insert(c);
        }

        String[] statuses = {"\u672a\u751f\u6548", "\u751f\u6548\u4e2d", "\u5df2\u5931\u6548", "\u5df2\u8fc7\u671f"};
        for (int i = 0; i < 10; i++) {
            Contract c = new Contract();
            c.setContractNo("HT204810101500000" + (i + 1));
            c.setContractName(names[i % names.length] + "\u8bd5\u4f4f\u5408\u540c");
            c.setElderName(names[i % names.length]);
            c.setElderIdcard("23020319970122102" + i);
            c.setCheckinNo("RZ204810101500000" + (i + 1));
            c.setStartDate(LocalDate.of(2048, 10, 10));
            c.setEndDate(LocalDate.of(2049, 10, 9));
            c.setStatus(statuses[i % statuses.length]);
            c.setSignDate(LocalDate.of(2048, 10, 10));
            if ("\u5df2\u5931\u6548".equals(c.getStatus())) {
                c.setThirdPartyName("\u8303\u95f2");
                c.setThirdPartyPhone("13678987777");
                c.setTerminateUser("\u987e\u5ef7\u70ec");
                c.setTerminateDate(LocalDate.of(2049, 5, 5));
                c.setTerminateFile("\u89e3\u9664\u534f\u8bae.pdf");
            }
            c.setContractFile(c.getElderName() + "\u957f\u4f4f\u5408\u540c.pdf");
            c.setCreator("\u987e\u5ef7\u70ec");
            c.setCreateTime(LocalDateTime.now().minusDays(i));
            contractMapper.insert(c);
        }

        String[] roomTypes = {"\u56db\u4eba\u95f4", "\u4e09\u4eba\u95f4", "\u4e24\u4eba\u95f4", "\u666e\u901a\u5355\u4eba\u95f4", "\u8c6a\u534e\u5355\u4eba\u95f4"};
        BigDecimal[] prices = {new BigDecimal("800"), new BigDecimal("1200"), new BigDecimal("1500"),
                new BigDecimal("2000"), new BigDecimal("2700")};
        for (int i = 0; i < 5; i++) {
            RoomType rt = new RoomType();
            rt.setName(roomTypes[i]);
            rt.setPrice(prices[i]);
            rt.setIntro("\u623f\u95f4\u5185\u8bbe\u670924\u5c0f\u65f6cctv\u76d1\u63a7");
            rt.setStatus(i == 4 ? 0 : 1);
            rt.setCreator("\u987e\u5ef7\u70ec");
            rt.setCreateTime(LocalDateTime.now());
            roomTypeMapper.insert(rt);
        }

        for (int i = 1; i <= 3; i++) {
            Floor f = new Floor();
            f.setName(i + "\u697c");
            f.setSortNum(i);
            floorMapper.insert(f);
            for (int j = 1; j <= 3; j++) {
                Room room = new Room();
                room.setFloorId(f.getId());
                room.setRoomNo(String.valueOf(100 + j + (i - 1) * 10));
                room.setRoomTypeId((j % 5) + 1);
                room.setRoomTypeName(roomTypes[j % 5]);
                roomMapper.insert(room);
                int bedCount = j == 1 ? 4 : 1;
                for (int k = 1; k <= bedCount; k++) {
                    Bed bed = new Bed();
                    bed.setRoomId(room.getId());
                    bed.setBedNo(room.getRoomNo() + k);
                    if (k <= 2 && j == 1) {
                        bed.setElderName(names[k % names.length]);
                        bed.setStatus(k == 2 && i == 1 ? "\u8bf7\u5047\u4e2d" : "\u5df2\u5165\u4f4f");
                    } else {
                        bed.setStatus("\u7a7a\u95f2");
                    }
                    bedMapper.insert(bed);
                }
            }
        }
        // 不再种子请假/退住/入住演示数据，协同待办与申请以真实业务单据为准
    }
}
