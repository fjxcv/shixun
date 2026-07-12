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

@RestController
public class FileController {

    private static final Set<String> ALLOWED_EXT = Set.of(".png", ".jpg", ".jpeg", ".pdf");
    private static final long MAX_SIZE = 2L * 1024 * 1024;

    @Value("${server.port:8080}")
    private int serverPort;

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

    public static Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }
}
