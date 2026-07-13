package com.soft.config;

import com.soft.pojo.Checkin;
import com.soft.pojo.Checkout;
import com.soft.pojo.Contract;
import com.soft.pojo.Leave;
import com.soft.pojo.User;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DemoDataFill {

    private DemoDataFill() {}

    public static void fillUser(User user) {
        if (user == null) return;
        if (!StringUtils.hasText(user.getRealname())) user.setRealname("顾廷烬");
        if (!StringUtils.hasText(user.getEmail())) user.setEmail("gutingye@zzyl.com");
        if (!StringUtils.hasText(user.getDepartment())) user.setDepartment("入住部");
        if (!StringUtils.hasText(user.getJob())) user.setJob("部门主管");
        if (!StringUtils.hasText(user.getRole())) user.setRole("护理组主管");
        if (!StringUtils.hasText(user.getPhone())) user.setPhone("13898988888");
        if (!StringUtils.hasText(user.getSex())) user.setSex("男");
        if (!StringUtils.hasText(user.getImage())) {
            user.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
    }

    public static User demoUser() {
        User user = new User();
        user.setId(1);
        user.setAccount("admin");
        user.setRealname("顾廷烬");
        user.setEmail("gutingye@zzyl.com");
        user.setDepartment("入住部");
        user.setJob("部门主管");
        user.setRole("护理组主管");
        user.setPhone("13898988888");
        user.setSex("男");
        user.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        return user;
    }

    public static void fillCheckin(Checkin c) {
        if (c == null) return;
        if (!StringUtils.hasText(c.getDocNo())) c.setDocNo("RZ2048101015000001");
        if (!StringUtils.hasText(c.getElderName())) c.setElderName("高启强");
        if (!StringUtils.hasText(c.getElderIdcard())) c.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(c.getElderPhone())) c.setElderPhone("13898988888");
        if (!StringUtils.hasText(c.getGender())) c.setGender("男");
        if (c.getBirthDate() == null) c.setBirthDate(LocalDate.of(1977, 2, 22));
        if (c.getAge() == null) c.setAge(71);
        if (!StringUtils.hasText(c.getAddress())) c.setAddress("24号楼3单元401室");
        if (!StringUtils.hasText(c.getBedNo())) c.setBedNo("1011");
        if (c.getCheckinDate() == null) c.setCheckinDate(LocalDate.of(2048, 10, 10));
        if (c.getPeriodStart() == null) c.setPeriodStart(LocalDate.of(2048, 10, 10));
        if (c.getPeriodEnd() == null) c.setPeriodEnd(LocalDate.of(2049, 10, 9));
        if (!StringUtils.hasText(c.getNursingLevel())) c.setNursingLevel("中度失能等级");
        if (c.getDeposit() == null) c.setDeposit(new BigDecimal("3000"));
        if (c.getNursingFee() == null) c.setNursingFee(new BigDecimal("1500"));
        if (c.getBedFee() == null) c.setBedFee(new BigDecimal("800"));
        if (c.getStep() == null) c.setStep(1);
        if (!StringUtils.hasText(c.getFlowStatus())) c.setFlowStatus("申请中");
        if (!StringUtils.hasText(c.getEthnicity())) c.setEthnicity("汉族");
        if (!StringUtils.hasText(c.getPoliticalStatus())) c.setPoliticalStatus("群众");
        if (!StringUtils.hasText(c.getReligion())) c.setReligion("佛教");
        if (!StringUtils.hasText(c.getMaritalStatus())) c.setMaritalStatus("已婚");
        if (!StringUtils.hasText(c.getEducationLevel())) c.setEducationLevel("大专");
        if (!StringUtils.hasText(c.getIncomeSource())) c.setIncomeSource("退休金");
        if (!StringUtils.hasText(c.getMedicalInsurance())) c.setMedicalInsurance("城镇职工基本医疗保险");
        if (!StringUtils.hasText(c.getHobbies())) c.setHobbies("下棋、唱歌");
        if (!StringUtils.hasText(c.getMedicalInsuranceNo())) c.setMedicalInsuranceNo("");
        if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("顾廷烬");
        if (c.getApplyTime() == null) c.setApplyTime(LocalDateTime.of(2048, 10, 10, 15, 0));
        if (c.getCreateTime() == null) c.setCreateTime(LocalDateTime.of(2048, 10, 10, 15, 0));
        if (!StringUtils.hasText(c.getContractNo())) c.setContractNo("HT2048101015000001");
        if (!StringUtils.hasText(c.getContractName())) c.setContractName("高启强试住合同");
        if (c.getSignDate() == null) c.setSignDate(LocalDate.of(2048, 10, 10));
    }

    public static void fillCheckout(Checkout c) {
        if (c == null) return;
        if (!StringUtils.hasText(c.getDocNo())) c.setDocNo("TZ2048101015000001");
        if (!StringUtils.hasText(c.getElderName())) c.setElderName("高启强");
        if (!StringUtils.hasText(c.getElderIdcard())) c.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(c.getElderPhone())) c.setElderPhone("13898988888");
        if (c.getFeeStart() == null) c.setFeeStart(LocalDate.of(2048, 10, 10));
        if (c.getFeeEnd() == null) c.setFeeEnd(LocalDate.of(2049, 10, 10));
        if (!StringUtils.hasText(c.getNursingLevel())) c.setNursingLevel("特级护理等级");
        if (!StringUtils.hasText(c.getBedInfo())) c.setBedInfo("101床位");
        if (!StringUtils.hasText(c.getContractName())) c.setContractName("高启强长住合同.pdf");
        if (!StringUtils.hasText(c.getConsultant())) c.setConsultant("顾廷烬");
        if (!StringUtils.hasText(c.getCaregivers())) c.setCaregivers("盛长柏、盛明兰");
        if (c.getCheckoutDate() == null) c.setCheckoutDate(LocalDate.of(2048, 10, 15));
        if (!StringUtils.hasText(c.getReason())) c.setReason("服务不周");
        if (!StringUtils.hasText(c.getRemark())) c.setRemark("无");
        if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("顾廷烬");
        if (c.getApplyTime() == null) c.setApplyTime(LocalDateTime.of(2048, 10, 5, 15, 0));
        if (c.getStep() == null) c.setStep(1);
        if (!StringUtils.hasText(c.getStepStatus())) c.setStepStatus("进行中");
        if (!StringUtils.hasText(c.getFlowStatus())) c.setFlowStatus("申请中");
    }

    public static void fillLeave(Leave l) {
        if (l == null) return;
        if (!StringUtils.hasText(l.getDocNo())) l.setDocNo("QJ2048101015000001");
        if (!StringUtils.hasText(l.getElderName())) l.setElderName("高启强");
        if (!StringUtils.hasText(l.getElderIdcard())) l.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(l.getElderPhone())) l.setElderPhone("13898988888");
        if (!StringUtils.hasText(l.getNursingLevel())) l.setNursingLevel("重度失能等级");
        if (!StringUtils.hasText(l.getBedInfo())) l.setBedInfo("1楼101房间1041床位");
        if (!StringUtils.hasText(l.getCaregivers())) l.setCaregivers("顾廷烬、盛明兰");
        if (l.getStartTime() == null) l.setStartTime(LocalDateTime.of(2048, 10, 10, 15, 0));
        if (l.getExpectReturnTime() == null) l.setExpectReturnTime(LocalDateTime.of(2048, 10, 20, 15, 0));
        if (!StringUtils.hasText(l.getStatus())) l.setStatus("请假中");
        if (!StringUtils.hasText(l.getEscort())) l.setEscort("无");
        if (l.getLeaveDays() == null) l.setLeaveDays(10);
        if (!StringUtils.hasText(l.getReason())) l.setReason("回家探望");
        if (!StringUtils.hasText(l.getApplicant())) l.setApplicant("顾廷烬");
        if (l.getApplyTime() == null) l.setApplyTime(LocalDateTime.of(2048, 10, 5, 15, 0));
        if (!StringUtils.hasText(l.getCancelUser())) l.setCancelUser("盛明兰");
        if (l.getCancelTime() == null) l.setCancelTime(LocalDateTime.of(2048, 10, 20, 16, 0));
        if (!StringUtils.hasText(l.getReturnRemark())) l.setReturnRemark("已安全返回");
        if (l.getActualReturnTime() == null && "已返回".equals(l.getStatus())) {
            l.setActualReturnTime(LocalDateTime.of(2048, 10, 20, 15, 30));
        }
    }

    public static Checkin demoCheckin(Long id) {
        Checkin c = new Checkin();
        c.setId(id);
        fillCheckin(c);
        return c;
    }

    public static Checkout demoCheckout(Long id) {
        Checkout c = new Checkout();
        c.setId(id);
        fillCheckout(c);
        return c;
    }

    public static Leave demoLeave(Long id) {
        Leave l = new Leave();
        l.setId(id);
        fillLeave(l);
        return l;
    }

    public static Contract demoContract(Long id) {
        Contract c = new Contract();
        c.setId(id);
        fillContract(c);
        return c;
    }

    public static void fillContract(Contract c) {
        if (c == null) return;
        if (!StringUtils.hasText(c.getContractNo())) c.setContractNo("HT2048101015000001");
        if (!StringUtils.hasText(c.getContractName())) c.setContractName("高启强试住合同");
        if (!StringUtils.hasText(c.getElderName())) c.setElderName("高启强");
        if (!StringUtils.hasText(c.getElderIdcard())) c.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(c.getCheckinNo())) c.setCheckinNo("RZ2048101015000001");
        if (c.getStartDate() == null) c.setStartDate(LocalDate.of(2048, 10, 10));
        if (c.getEndDate() == null) c.setEndDate(LocalDate.of(2049, 10, 9));
        if (!StringUtils.hasText(c.getStatus())) c.setStatus("生效中");
        if (c.getSignDate() == null) c.setSignDate(LocalDate.of(2048, 10, 10));
        if (!StringUtils.hasText(c.getContractFile())) c.setContractFile("高启强长住合同.pdf");
        if (!StringUtils.hasText(c.getCreator())) c.setCreator("顾廷烬");
    }
}
