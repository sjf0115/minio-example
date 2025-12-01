package com.example.lock;

import io.minio.MinioClient;
import io.minio.SetObjectLockConfigurationArgs;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.RetentionDurationDays;
import io.minio.messages.RetentionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：SetObjectLockConfiguration 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class SetObjectLockConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SetObjectLockConfiguration.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-object-lock";
        // 对象锁配置
        ObjectLockConfiguration config = new ObjectLockConfiguration(RetentionMode.COMPLIANCE, new RetentionDurationDays(100));

        minioClient.setObjectLockConfiguration(
                SetObjectLockConfigurationArgs.builder()
                        .bucket(bucketName)
                        .config(config)
                        .build());

        LOG.info("Bucket's Object-lock configuration, mode: {}, duration: {}", config.mode(), config.duration());
    }
}
