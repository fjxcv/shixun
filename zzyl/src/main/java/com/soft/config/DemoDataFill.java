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
        if (!StringUtils.hasText(user.getRealname())) user.setRealname("\u987e\u5ef7\u70ec");
        if (!StringUtils.hasText(user.getEmail())) user.setEmail("gutingye@zzyl.com");
        if (!StringUtils.hasText(user.getDepartment())) user.setDepartment("\u5165\u4f4f\u90e8");
        if (!StringUtils.hasText(user.getJob())) user.setJob("\u90e8\u95e8\u4e3b\u7ba1");
        if (!StringUtils.hasText(user.getRole())) user.setRole("\u62a4\u7406\u7ec4\u4e3b\u7ba1");
        if (!StringUtils.hasText(user.getPhone())) user.setPhone("13898988888");
        if (!StringUtils.hasText(user.getSex())) user.setSex("\u7537");
        if (!StringUtils.hasText(user.getImage())) {
            user.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
    }

    public static User demoUser() {
        User user = new User();
        user.setId(1);
        user.setAccount("admin");
        user.setRealname("\u987e\u5ef7\u70ec");
        user.setEmail("gutingye@zzyl.com");
        user.setDepartment("\u5165\u4f4f\u90e8");
        user.setJob("\u90e8\u95e8\u4e3b\u7ba1");
        user.setRole("\u62a4\u7406\u7ec4\u4e3b\u7ba1");
        user.setPhone("13898988888");
        user.setSex("\u7537");
        user.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        return user;
    }

    public static void fillCheckin(Checkin c) {
        if (c == null) return;
        if (!StringUtils.hasText(c.getDocNo())) c.setDocNo("RZ2048101015000001");
        if (!StringUtils.hasText(c.getElderName())) c.setElderName("\u9ad8\u542f\u5f3a");
        if (!StringUtils.hasText(c.getElderIdcard())) c.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(c.getElderPhone())) c.setElderPhone("13898988888");
        if (!StringUtils.hasText(c.getGender())) c.setGender("\u7537");
        if (c.getBirthDate() == null) c.setBirthDate(LocalDate.of(1977, 2, 22));
        if (c.getAge() == null) c.setAge(71);
        if (!StringUtils.hasText(c.getAddress())) c.setAddress("24\u53f7\u697c3\u5355\u5143401\u5ba4");
        if (!StringUtils.hasText(c.getBedNo())) c.setBedNo("1011");
        if (c.getCheckinDate() == null) c.setCheckinDate(LocalDate.of(2048, 10, 10));
        if (c.getPeriodStart() == null) c.setPeriodStart(LocalDate.of(2048, 10, 10));
        if (c.getPeriodEnd() == null) c.setPeriodEnd(LocalDate.of(2049, 10, 9));
        if (!StringUtils.hasText(c.getNursingLevel())) c.setNursingLevel("\u4e2d\u5ea6\u5931\u80fd\u7b49\u7ea7");
        if (c.getDeposit() == null) c.setDeposit(new BigDecimal("3000"));
        if (c.getNursingFee() == null) c.setNursingFee(new BigDecimal("1500"));
        if (c.getBedFee() == null) c.setBedFee(new BigDecimal("800"));
        if (c.getStep() == null) c.setStep(1);
        if (!StringUtils.hasText(c.getFlowStatus())) c.setFlowStatus("\u7533\u8bf7\u4e2d");
        if (!StringUtils.hasText(c.getEthnicity())) c.setEthnicity("\u6c49\u65cf");
        if (!StringUtils.hasText(c.getPoliticalStatus())) c.setPoliticalStatus("\u7fa4\u4f17");
        if (!StringUtils.hasText(c.getReligion())) c.setReligion("\u4f5b\u6559");
        if (!StringUtils.hasText(c.getMaritalStatus())) c.setMaritalStatus("\u5df2\u5a5a");
        if (!StringUtils.hasText(c.getEducationLevel())) c.setEducationLevel("\u5927\u4e13");
        if (!StringUtils.hasText(c.getIncomeSource())) c.setIncomeSource("\u9000\u4f11\u91d1");
        if (!StringUtils.hasText(c.getMedicalInsurance())) c.setMedicalInsurance("\u57ce\u9547\u804c\u5de5\u57fa\u672c\u533b\u7597\u4fdd\u9669");
        if (!StringUtils.hasText(c.getHobbies())) c.setHobbies("\u4e0b\u68cb\u3001\u5531\u6b4c");
        if (!StringUtils.hasText(c.getMedicalInsuranceNo())) c.setMedicalInsuranceNo("");
        if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("\u987e\u5ef7\u70ec");
        if (c.getApplyTime() == null) c.setApplyTime(LocalDateTime.of(2048, 10, 10, 15, 0));
        if (c.getCreateTime() == null) c.setCreateTime(LocalDateTime.of(2048, 10, 10, 15, 0));
        if (!StringUtils.hasText(c.getContractNo())) c.setContractNo("HT2048101015000001");
        if (!StringUtils.hasText(c.getContractName())) c.setContractName("\u9ad8\u542f\u5f3a\u8bd5\u4f4f\u5408\u540c");
        if (c.getSignDate() == null) c.setSignDate(LocalDate.of(2048, 10, 10));
    }

    public static void fillCheckout(Checkout c) {
        if (c == null) return;
        if (!StringUtils.hasText(c.getDocNo())) c.setDocNo("TZ2048101015000001");
        if (!StringUtils.hasText(c.getElderName())) c.setElderName("\u9ad8\u542f\u5f3a");
        if (!StringUtils.hasText(c.getElderIdcard())) c.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(c.getElderPhone())) c.setElderPhone("13898988888");
        if (c.getFeeStart() == null) c.setFeeStart(LocalDate.of(2048, 10, 10));
        if (c.getFeeEnd() == null) c.setFeeEnd(LocalDate.of(2049, 10, 10));
        if (!StringUtils.hasText(c.getNursingLevel())) c.setNursingLevel("\u7279\u7ea7\u62a4\u7406\u7b49\u7ea7");
        if (!StringUtils.hasText(c.getBedInfo())) c.setBedInfo("101\u5e8a\u4f4d");
        if (!StringUtils.hasText(c.getContractName())) c.setContractName("\u9ad8\u542f\u5f3a\u957f\u4f4f\u5408\u540c.pdf");
        if (!StringUtils.hasText(c.getConsultant())) c.setConsultant("\u987e\u5ef7\u70ec");
        if (!StringUtils.hasText(c.getCaregivers())) c.setCaregivers("\u76db\u957f\u67cf\u3001\u76db\u660e\u5170");
        if (c.getCheckoutDate() == null) c.setCheckoutDate(LocalDate.of(2048, 10, 15));
        if (!StringUtils.hasText(c.getReason())) c.setReason("\u670d\u52a1\u4e0d\u5468");
        if (!StringUtils.hasText(c.getRemark())) c.setRemark("\u65e0");
        if (!StringUtils.hasText(c.getApplicant())) c.setApplicant("\u987e\u5ef7\u70ec");
        if (c.getApplyTime() == null) c.setApplyTime(LocalDateTime.of(2048, 10, 5, 15, 0));
        if (c.getStep() == null) c.setStep(1);
        if (!StringUtils.hasText(c.getStepStatus())) c.setStepStatus("\u8fdb\u884c\u4e2d");
        if (!StringUtils.hasText(c.getFlowStatus())) c.setFlowStatus("\u7533\u8bf7\u4e2d");
    }

    public static void fillLeave(Leave l) {
        if (l == null) return;
        if (!StringUtils.hasText(l.getDocNo())) l.setDocNo("QJ2048101015000001");
        if (!StringUtils.hasText(l.getElderName())) l.setElderName("\u9ad8\u542f\u5f3a");
        if (!StringUtils.hasText(l.getElderIdcard())) l.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(l.getElderPhone())) l.setElderPhone("13898988888");
        if (!StringUtils.hasText(l.getNursingLevel())) l.setNursingLevel("\u91cd\u5ea6\u5931\u80fd\u7b49\u7ea7");
        if (!StringUtils.hasText(l.getBedInfo())) l.setBedInfo("1\u697c101\u623f\u95f41041\u5e8a\u4f4d");
        if (!StringUtils.hasText(l.getCaregivers())) l.setCaregivers("\u987e\u5ef7\u70ec\u3001\u76db\u660e\u5170");
        if (l.getStartTime() == null) l.setStartTime(LocalDateTime.of(2048, 10, 10, 15, 0));
        if (l.getExpectReturnTime() == null) l.setExpectReturnTime(LocalDateTime.of(2048, 10, 20, 15, 0));
        if (!StringUtils.hasText(l.getStatus())) l.setStatus("\u8bf7\u5047\u4e2d");
        if (!StringUtils.hasText(l.getEscort())) l.setEscort("\u65e0");
        if (l.getLeaveDays() == null) l.setLeaveDays(10);
        if (!StringUtils.hasText(l.getReason())) l.setReason("\u56de\u5bb6\u63a2\u671b");
        if (!StringUtils.hasText(l.getApplicant())) l.setApplicant("\u987e\u5ef7\u70ec");
        if (l.getApplyTime() == null) l.setApplyTime(LocalDateTime.of(2048, 10, 5, 15, 0));
        if (!StringUtils.hasText(l.getCancelUser())) l.setCancelUser("\u76db\u660e\u5170");
        if (l.getCancelTime() == null) l.setCancelTime(LocalDateTime.of(2048, 10, 20, 16, 0));
        if (!StringUtils.hasText(l.getReturnRemark())) l.setReturnRemark("\u5df2\u5b89\u5168\u8fd4\u56de");
        if (l.getActualReturnTime() == null && "\u5df2\u8fd4\u56de".equals(l.getStatus())) {
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
        if (!StringUtils.hasText(c.getContractName())) c.setContractName("\u9ad8\u542f\u5f3a\u8bd5\u4f4f\u5408\u540c");
        if (!StringUtils.hasText(c.getElderName())) c.setElderName("\u9ad8\u542f\u5f3a");
        if (!StringUtils.hasText(c.getElderIdcard())) c.setElderIdcard("230203197702221029");
        if (!StringUtils.hasText(c.getCheckinNo())) c.setCheckinNo("RZ2048101015000001");
        if (c.getStartDate() == null) c.setStartDate(LocalDate.of(2048, 10, 10));
        if (c.getEndDate() == null) c.setEndDate(LocalDate.of(2049, 10, 9));
        if (!StringUtils.hasText(c.getStatus())) c.setStatus("\u751f\u6548\u4e2d");
        if (c.getSignDate() == null) c.setSignDate(LocalDate.of(2048, 10, 10));
        if (!StringUtils.hasText(c.getContractFile())) c.setContractFile("\u9ad8\u542f\u5f3a\u957f\u4f4f\u5408\u540c.pdf");
        if (!StringUtils.hasText(c.getCreator())) c.setCreator("\u987e\u5ef7\u70ec");
    }
}
