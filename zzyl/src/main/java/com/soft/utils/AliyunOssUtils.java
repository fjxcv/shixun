package com.soft.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

@Component
public class AliyunOssUtils {


    // Endpoint以西南1（成都），其它Region请按实际情况填写。
    //String endpoint = "https://oss-cn-chengdu.aliyuncs.com";
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    // 填写Bucket名称，例如examplebucket。
    //String bucketName = "zzyl-706";
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    // 填写Object完整路径，保存到oss服务的对象名字。
    //String objectName = "001.jpg";
    // 填写Bucket所在地域。以西南1（成都）为例，Region填写为cn-hangzhou。
    //String region = "cn-chengdu";
    @Value("${aliyun.oss.region}")
    private String region;


    /*封装将本地文件上传到阿里云oss服务器*/
    public String uploadFile(String objectName,byte[] content){
        OSS ossClient=null;
        try{

            // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量
            // OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
            EnvironmentVariableCredentialsProvider credentialsProvider
                    = CredentialsProviderFactory
                    .newEnvironmentVariableCredentialsProvider();
            System.out.println(credentialsProvider);

            // 创建OSSClient实例。
            ClientBuilderConfiguration clientBuilderConfiguration =
                    new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(region)
                    .build();

            ossClient.putObject(bucketName,objectName,new ByteArrayInputStream(content));

        }catch(Exception ex){
            ex.printStackTrace();
        }finally {
            ossClient.shutdown();
        }
        //https://zzyl-706.oss-cn-chengdu.aliyuncs.com/1.png
        String  url=endpoint.split("//")[0] + "//" + bucketName + "."
                + endpoint.split("//")[1] + "/" + objectName;

        return url;
    }
}
