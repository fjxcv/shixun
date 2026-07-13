package com.soft.controller;

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
 * 本地文件上传接口。
 * <p>
 * 将上传文件保存到运行目录下的 uploads，并返回可访问的 URL。
 * 普通附件与体检报告分接口限制类型与大小；静态访问由 UploadWebConfig 映射 /uploads/**。
 */
@RestController
public class FileController {

    private static final Set<String> ALLOWED_EXT = Set.of(".png", ".jpg", ".jpeg", ".pdf");
    private static final long MAX_SIZE = 2L * 1024 * 1024;
    private static final long MAX_REPORT_SIZE = 60L * 1024 * 1024;

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * 通用文件上传（头像、附件等）。
     * 限制：PNG/JPG/JPEG/PDF，最大 2MB。
     * 表单字段名必须为 mf。
     *
     * @param mf 上传文件
     * @return 可访问的完整 URL
     */
    @PostMapping("/upload")
    public Result<String> fileUpload(@RequestParam("mf") MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            return Result.fail("请选择文件");
        }
        if (mf.getSize() > MAX_SIZE) {
            return Result.fail("文件大小不能超过2M");
        }
        String oldName = mf.getOriginalFilename();
        String ext = ".jpg";
        if (oldName != null && oldName.contains(".")) {
            ext = oldName.substring(oldName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXT.contains(ext)) {
            return Result.fail("仅支持 PNG/JPG/JPEG/PDF");
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
            return Result.fail("上传失败：" + ex.getMessage());
        }
    }

    /**
     * 体检/评估报告上传（PDF）。
     * 限制：仅 PDF，最大 60MB，供入住流程挂载报告使用。
     *
     * @param mf 上传文件（字段名 mf）
     * @return 可访问的完整 URL
     */
    @PostMapping("/upload/report")
    public Result<String> reportUpload(@RequestParam("mf") MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            return Result.fail("请选择文件");
        }
        if (mf.getSize() > MAX_REPORT_SIZE) {
            return Result.fail("文件大小不能超过60M");
        }
        String oldName = mf.getOriginalFilename();
        String ext = ".pdf";
        if (oldName != null && oldName.contains(".")) {
            ext = oldName.substring(oldName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!".pdf".equals(ext)) {
            return Result.fail("仅支持 PDF 文件");
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
            return Result.fail("上传失败：" + ex.getMessage());
        }
    }

    /** 上传目录：进程工作目录下的 uploads。 */
    public static Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }
}
