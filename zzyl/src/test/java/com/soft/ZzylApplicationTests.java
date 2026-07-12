package com.soft;

import com.soft.utils.AliyunOssUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
class ZzylApplicationTests {

    @Autowired
    private AliyunOssUtils aliyunOssUtils;

    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;

    @Test
    public void updateFileUtil() throws IOException {
        Assumptions.assumeTrue(
                StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret),
                () -> "skip OSS upload test: aliyun.oss.accessKeyId / accessKeySecret not configured"
        );

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-upload.png")) {
            if (inputStream == null) {
                throw new IllegalStateException("test-upload.png not found in test resources");
            }
            byte[] bytes = inputStream.readAllBytes();
            String url = aliyunOssUtils.uploadFile("3.png", bytes);
            System.out.println("url=" + url);
        }
    }
}
