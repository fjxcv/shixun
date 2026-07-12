package com.soft;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.soft.utils.AliyunOssUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@SpringBootTest
class ZzylApplicationTests {


    @Autowired
    private AliyunOssUtils aliyunOssUtils;
    @Test
    public void updateFileUtil(){

        //将本地需要上传的文件封装为对象
        File file=new File("D:/zzyl.png");
        //将本地文件对象，转化为字节数组
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String s = aliyunOssUtils.uploadFile("3.png", bytes);
            System.out.println("url="+s);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
