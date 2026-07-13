# -*- coding: utf-8 -*-
from pathlib import Path
CTRL = Path(r"D:/vsc-maven/zzyl-project/zzyl/src/main/java/com/soft/controller")

def w(name, content):
    (CTRL / name).write_text(content, encoding="utf-8", newline="\n")
    print("wrote", name, "ok", "\u63a5\u53e3" in content or "Controller" in content)

# --- CustomerController ---
w("CustomerController.java", """package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.mapper.CustomerMapper;
import com.soft.pojo.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * \u5ba2\u6237\u4fe1\u606f\u63a5\u53e3\u3002
 * <p>
 * \u5bf9\u5e94\u524d\u7aef\u300c\u5ba2\u6237\u4fe1\u606f\u300d\u9875\u9762\uff0c\u63d0\u4f9b\u5ba2\u6237\u5206\u9875\u68c0\u7d22\u80fd\u529b\u3002
 * \u5ba2\u6237\u4e00\u822c\u7531\u5c0f\u7a0b\u5e8f/\u6ce8\u518c\u6d41\u7a0b\u5199\u5165\uff0c\u672c\u63a7\u5236\u5668\u4fa7\u91cd\u540e\u53f0\u67e5\u8be2\u5c55\u793a\u3002
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired private CustomerMapper customerMapper;

    /**
     * \u5ba2\u6237\u5206\u9875\u5217\u8868\u3002
     * \u652f\u6301\u6309\u6635\u79f0\u3001\u624b\u673a\u53f7\u6a21\u7cca\u67e5\u8be2\uff1b\u6309\u9996\u6b21\u767b\u5f55\u65f6\u95f4\u5012\u5e8f\u3002
     *
     * @param q \u5206\u9875\u4e0e\u7b5b\u9009\u6761\u4ef6\uff08pageNum/pageSize/nickname/phone\uff09
     * @return \u5f53\u524d\u9875\u5ba2\u6237\u5217\u8868\u4e0e\u603b\u6570
     */
    @PostMapping("/page")
    public Result<List<Customer>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getNickname())) w.like(Customer::getNickname, q.getNickname());
        if (StringUtils.hasText(q.getPhone())) w.like(Customer::getPhone, q.getPhone());
        w.orderByDesc(Customer::getFirstLoginTime);
        Page<Customer> page = customerMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }
}
""")

# --- ContractController ---
w("ContractController.java", """package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft.common.Result;
import com.soft.dto.PageQueryDto;
import com.soft.config.DemoDataFill;
import com.soft.mapper.ContractMapper;
import com.soft.pojo.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * \u5408\u540c\u7ba1\u7406\u63a5\u53e3\u3002
 * <p>
 * \u63d0\u4f9b\u5408\u540c\u5206\u9875\u67e5\u8be2\u4e0e\u8be6\u60c5\u67e5\u770b\uff0c\u4f9b\u300c\u5408\u540c\u8ddf\u8e2a\u300d\u300c\u5408\u540c\u8be6\u60c5\u300d\u9875\u9762\u4f7f\u7528\u3002
 * \u8be6\u60c5\u5728\u6570\u636e\u7f3a\u5931\u65f6\u4f1a\u56de\u843d\u6f14\u793a\u6570\u636e\u5e76\u8865\u9f50\u5c55\u793a\u5b57\u6bb5\uff0c\u4fbf\u4e8e\u6f14\u793a\u73af\u5883\u8054\u8c03\u3002
 */
@RestController
@RequestMapping("/contract")
public class ContractController {
    @Autowired private ContractMapper contractMapper;

    /**
     * \u5408\u540c\u5206\u9875\u5217\u8868\u3002
     * \u652f\u6301\u6309\u5408\u540c\u53f7\u3001\u8001\u4eba\u59d3\u540d\u6a21\u7cca\u67e5\u8be2\uff0c\u4ee5\u53ca\u6309\u72b6\u6001\u7cbe\u786e\u7b5b\u9009\uff1b\u6309\u521b\u5efa\u65f6\u95f4\u5012\u5e8f\u3002
     *
     * @param q \u5206\u9875\u4e0e\u7b5b\u9009\u6761\u4ef6\uff08pageNum/pageSize/contractNo/elderName/status\uff09
     * @return \u5f53\u524d\u9875\u5408\u540c\u5217\u8868\u4e0e\u603b\u6570
     */
    @PostMapping("/page")
    public Result<List<Contract>> page(@RequestBody PageQueryDto q) {
        LambdaQueryWrapper<Contract> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getContractNo())) w.like(Contract::getContractNo, q.getContractNo());
        if (StringUtils.hasText(q.getElderName())) w.like(Contract::getElderName, q.getElderName());
        if (StringUtils.hasText(q.getStatus())) w.eq(Contract::getStatus, q.getStatus());
        w.orderByDesc(Contract::getCreateTime);
        Page<Contract> page = contractMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * \u5408\u540c\u8be6\u60c5\u3002
     * \u4f18\u5148\u6309 id \u67e5\u8be2\uff1b\u67e5\u4e0d\u5230\u5219\u53d6\u5e93\u4e2d\u4efb\u610f\u4e00\u6761\uff1b\u4ecd\u65e0\u6570\u636e\u5219\u751f\u6210\u6f14\u793a\u5408\u540c\u3002
     * \u8fd4\u56de\u524d\u7edf\u4e00\u8c03\u7528 DemoDataFill \u8865\u9f50\u5c55\u793a\u5b57\u6bb5\u3002
     *
     * @param id \u5408\u540c ID
     */
    @GetMapping("/detail")
    public Result<Contract> detail(@RequestParam("id") Long id) {
        Contract c = contractMapper.selectById(id);
        if (c == null) {
            c = contractMapper.selectOne(new LambdaQueryWrapper<Contract>().last("limit 1"));
        }
        if (c == null) {
            c = DemoDataFill.demoContract(id);
        }
        DemoDataFill.fillContract(c);
        return Result.ok(c);
    }
}
""")

# --- FileController ---
w("FileController.java", """package com.soft.controller;

import com.soft.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * \u672c\u5730\u6587\u4ef6\u4e0a\u4f20\u63a5\u53e3\u3002
 * <p>
 * \u5c06\u4e0a\u4f20\u6587\u4ef6\u4fdd\u5b58\u5230\u8fd0\u884c\u76ee\u5f55\u4e0b\u7684 uploads\uff0c\u5e76\u8fd4\u56de\u53ef\u8bbf\u95ee\u7684 URL\u3002
 * \u666e\u901a\u9644\u4ef6\u4e0e\u4f53\u68c0\u62a5\u544a\u5206\u63a5\u53e3\u9650\u5236\u7c7b\u578b\u4e0e\u5927\u5c0f\uff1b\u9759\u6001\u8bbf\u95ee\u7531 UploadWebConfig \u6620\u5c04 /uploads/**\u3002
 */
@RestController
public class FileController {

    private static final Set<String> ALLOWED_EXT = Set.of(".png", ".jpg", ".jpeg", ".pdf");
    private static final long MAX_SIZE = 2L * 1024 * 1024;
    private static final long MAX_REPORT_SIZE = 60L * 1024 * 1024;

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * \u901a\u7528\u6587\u4ef6\u4e0a\u4f20\uff08\u5934\u50cf\u3001\u9644\u4ef6\u7b49\uff09\u3002
     * \u9650\u5236\uff1aPNG/JPG/JPEG/PDF\uff0c\u6700\u5927 2MB\u3002
     * \u8868\u5355\u5b57\u6bb5\u540d\u5fc5\u987b\u4e3a mf\u3002
     *
     * @param mf \u4e0a\u4f20\u6587\u4ef6
     * @return \u53ef\u8bbf\u95ee\u7684\u5b8c\u6574 URL
     */
    @PostMapping("/upload")
    public Result<String> fileUpload(@RequestParam("mf") MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            return Result.fail("\u8bf7\u9009\u62e9\u6587\u4ef6");
        }
        if (mf.getSize() > MAX_SIZE) {
            return Result.fail("\u6587\u4ef6\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc72M");
        }
        String oldName = mf.getOriginalFilename();
        String ext = ".jpg";
        if (oldName != null && oldName.contains(".")) {
            ext = oldName.substring(oldName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXT.contains(ext)) {
            return Result.fail("\u4ec5\u652f\u6301 PNG/JPG/JPEG/PDF");
        }
        try {
            Path dir = uploadDir();
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + ext;
            Path target = dir.resolve(name);
            mf.transferTo(target.toFile());
            String url = "http://localhost:" + serverPort + "/uploads/" + name;
            return Result.ok(url);
        } catch (IOException ex) {
            ex.printStackTrace();
            return Result.fail("\u4e0a\u4f20\u5931\u8d25\uff1a" + ex.getMessage());
        }
    }

    /**
     * \u4f53\u68c0/\u8bc4\u4f30\u62a5\u544a\u4e0a\u4f20\uff08PDF\uff09\u3002
     * \u9650\u5236\uff1a\u4ec5 PDF\uff0c\u6700\u5927 60MB\uff0c\u4f9b\u5165\u4f4f\u6d41\u7a0b\u6302\u8f7d\u62a5\u544a\u4f7f\u7528\u3002
     *
     * @param mf \u4e0a\u4f20\u6587\u4ef6\uff08\u5b57\u6bb5\u540d mf\uff09
     * @return \u53ef\u8bbf\u95ee\u7684\u5b8c\u6574 URL
     */
    @PostMapping("/upload/report")
    public Result<String> reportUpload(@RequestParam("mf") MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            return Result.fail("\u8bf7\u9009\u62e9\u6587\u4ef6");
        }
        if (mf.getSize() > MAX_REPORT_SIZE) {
            return Result.fail("\u6587\u4ef6\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc760M");
        }
        String oldName = mf.getOriginalFilename();
        String ext = ".pdf";
        if (oldName != null && oldName.contains(".")) {
            ext = oldName.substring(oldName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!".pdf".equals(ext)) {
            return Result.fail("\u4ec5\u652f\u6301 PDF \u6587\u4ef6");
        }
        try {
            Path dir = uploadDir();
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + ext;
            Path target = dir.resolve(name);
            mf.transferTo(target.toFile());
            String url = "http://localhost:" + serverPort + "/uploads/" + name;
            return Result.ok(url);
        } catch (IOException ex) {
            ex.printStackTrace();
            return Result.fail("\u4e0a\u4f20\u5931\u8d25\uff1a" + ex.getMessage());
        }
    }

    /** \u4e0a\u4f20\u76ee\u5f55\uff1a\u8fdb\u7a0b\u5de5\u4f5c\u76ee\u5f55\u4e0b\u7684 uploads\u3002 */
    public static Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }
}
""")

print("part1 done")