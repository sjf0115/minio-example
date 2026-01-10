package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 功能：MinIO 配置对象
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 14:09
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    // 连接地址
    private String endpoint;
    // 账号
    private String accessKey;
    // 密码
    private String secretKey;
    // 桶
    private String bucket;
}
