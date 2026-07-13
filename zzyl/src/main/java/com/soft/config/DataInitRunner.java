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

/**
 * 启动时数据初始化：补齐表结构、演示账号/楼层床位等基础数据。
 * clearWorkflowDemoData 会清理协同相关演示种子，避免污染「我的待办/申请」。
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

    /**
     * 清理协同相关演示种子（入退/请假），避免污染待办与申请列表。
     * 删除历史脚本写入的 QJ2048/TZ2048 前缀演示单，以及固定身份证前缀的入住演示单。
     */
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
        u.setRealname("顾廷烬");
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

    private void seedFloorsOnly() {
        String[] roomTypes = {"四人间", "三人间", "两人间", "普通单人间", "豪华单人间"};
        String[] names = {"安欣", "高启强", "高启盛", "陈书婷", "高晓晨"};
        for (int i = 1; i <= 3; i++) {
            Floor f = new Floor();
            f.setName(i + "楼");
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
                        bed.setStatus(k == 2 && i == 1 ? "请假中" : "已入住");
                    } else {
                        bed.setStatus("空闲");
                    }
                    bedMapper.insert(bed);
                }
            }
        }
    }

    private void seedCheckinOnly() {
        String[] names = {"安欣", "高启强", "高启盛", "陈书婷", "高晓晨"};
        String[] flowStatus = {"申请中", "已完成", "已关闭"};
        for (int i = 0; i < 10; i++) {
            Checkin ci = new Checkin();
            ci.setDocNo("RZ" + System.currentTimeMillis() + String.format("%04d", i));
            ci.setElderName(names[i % names.length]);
            ci.setElderIdcard("23020319970122102" + i);
            ci.setElderPhone("1389898888" + i);
            ci.setGender(i % 2 == 0 ? "男" : "女");
            ci.setBirthDate(LocalDate.of(1977, 2, 22));
            ci.setAge(71);
            ci.setAddress("24号楼3单元401室");
            ci.setBedNo("101" + (i % 4 + 1));
            LocalDate cd = LocalDate.now().minusDays(i * 5);
            ci.setCheckinDate(cd);
            ci.setPeriodStart(cd);
            ci.setPeriodEnd(cd.plusYears(1));
            ci.setNursingLevel("中度失能等级");
            ci.setDeposit(new java.math.BigDecimal("3000"));
            ci.setNursingFee(new java.math.BigDecimal("1500"));
            ci.setBedFee(new java.math.BigDecimal("800"));
            ci.setStep(i % 5 + 1);
            ci.setFlowStatus(flowStatus[i % 3]);
            ci.setStepStatus(ci.getFlowStatus());
            if ("已完成".equals(ci.getFlowStatus())) {
                ci.setFinishTime(LocalDateTime.now().minusDays(i));
            }
            ci.setContractNo("HT" + System.currentTimeMillis() + String.format("%05d", i));
            ci.setApplicant("顾廷烬");
            ci.setApplyTime(LocalDateTime.now().minusDays(i * 5));
            ci.setCreator("顾廷烬");
            ci.setCreateTime(LocalDateTime.now().minusDays(i));
            checkinMapper.insert(ci);
        }
    }

    private void seedData() {
        String[] names = {"安欣", "高启强", "高启盛", "陈书婷", "高晓晨"};
        String[] types = {"参观预约", "探访预约"};
        String[] visitTypes = {"参观来访", "探访来访"};
        for (int i = 0; i < 10; i++) {
            Reservation r = new Reservation();
            r.setType(types[i % 2]);
            r.setVisitorName(names[i % names.length]);
            r.setVisitorPhone("1387556889" + i);
            r.setElderName(names[(i + 2) % names.length]);
            r.setAppointTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            String[] resStatus = {"已完成", "待上门", "已取消", "已过期"};
            r.setStatus(resStatus[i % 4]);
            r.setCreator("顾廷烬");
            r.setCreateTime(LocalDateTime.now().minusDays(i));
            reservationMapper.insert(r);

            Visit v = new Visit();
            v.setType(visitTypes[i % 2]);
            v.setVisitorName(names[i % names.length]);
            v.setVisitorPhone("1387556889" + i);
            v.setElderName(names[(i + 1) % names.length]);
            v.setVisitTime(LocalDateTime.of(2048, 10, 10, 15, 0).plusDays(i));
            v.setCreator("顾廷烬");
            v.setCreateTime(LocalDateTime.now().minusDays(i));
            visitMapper.insert(v);
        }

        String[] customers = {"范闲", "范思哲", "范若若", "林婉儿", "海梠朵朵"};
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer();
            c.setNickname(customers[i % customers.length]);
            c.setPhone("1380013800" + i);
            c.setSigned(i % 2);
            c.setOrderCount(i % 6);
            c.setElderNames("高启强、安欣");
            c.setFirstLoginTime(LocalDateTime.now().minusDays(i * 2));
            customerMapper.insert(c);
        }

        String[] statuses = {"未生效", "生效中", "已失效", "已过期"};
        for (int i = 0; i < 10; i++) {
            Contract c = new Contract();
            c.setContractNo("HT204810101500000" + (i + 1));
            c.setContractName(names[i % names.length] + "试住合同");
            c.setElderName(names[i % names.length]);
            c.setElderIdcard("23020319970122102" + i);
            c.setCheckinNo("RZ204810101500000" + (i + 1));
            c.setStartDate(LocalDate.of(2048, 10, 10));
            c.setEndDate(LocalDate.of(2049, 10, 9));
            c.setStatus(statuses[i % statuses.length]);
            c.setSignDate(LocalDate.of(2048, 10, 10));
            if ("已失效".equals(c.getStatus())) {
                c.setThirdPartyName("范闲");
                c.setThirdPartyPhone("13678987777");
                c.setTerminateUser("顾廷烬");
                c.setTerminateDate(LocalDate.of(2049, 5, 5));
                c.setTerminateFile("解除协议.pdf");
            }
            c.setContractFile(c.getElderName() + "长住合同.pdf");
            c.setCreator("顾廷烬");
            c.setCreateTime(LocalDateTime.now().minusDays(i));
            contractMapper.insert(c);
        }

        String[] roomTypes = {"四人间", "三人间", "两人间", "普通单人间", "豪华单人间"};
        BigDecimal[] prices = {new BigDecimal("800"), new BigDecimal("1200"), new BigDecimal("1500"),
                new BigDecimal("2000"), new BigDecimal("2700")};
        for (int i = 0; i < 5; i++) {
            RoomType rt = new RoomType();
            rt.setName(roomTypes[i]);
            rt.setPrice(prices[i]);
            rt.setIntro("房间内设有24小时cctv监控");
            rt.setStatus(i == 4 ? 0 : 1);
            rt.setCreator("顾廷烬");
            rt.setCreateTime(LocalDateTime.now());
            roomTypeMapper.insert(rt);
        }

        for (int i = 1; i <= 3; i++) {
            Floor f = new Floor();
            f.setName(i + "楼");
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
                        bed.setStatus(k == 2 && i == 1 ? "请假中" : "已入住");
                    } else {
                        bed.setStatus("空闲");
                    }
                    bedMapper.insert(bed);
                }
            }
        }
        // 不再种子请假/退住/入住演示数据；协同待办与申请以真实业务单据为准，避免与 clearWorkflowDemoData 清理策略冲突
    }
}
