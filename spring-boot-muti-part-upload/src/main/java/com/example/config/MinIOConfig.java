package com.example.config;

import com.example.minio.MinIOUploadClient;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 功能：MinIOConfig
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/29 20:30
 */
@Configuration
@RequiredArgsConstructor
public class MinIOConfig {
    private final MinioProperties minioProperties;

    // 配置 Minio 客户端
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    // 配置 Minio 异步客户端
    @Bean
    public MinioAsyncClient minioAsyncClient() {
        return MinioAsyncClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    // 配置 Minio 分片上传自定义客户端
    @Bean
    public MinIOUploadClient minIOUploadClient() {
        return new MinIOUploadClient(minioAsyncClient());
    }
}
