package com.example.lock;

import io.minio.GetObjectLockConfigurationArgs;
import io.minio.MinioClient;
import io.minio.messages.ObjectLockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：GetObjectLockConfiguration 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class GetObjectLockConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(GetObjectLockConfiguration.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-object-lock";
        ObjectLockConfiguration config =
                minioClient.getObjectLockConfiguration(
                        GetObjectLockConfigurationArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

        LOG.info("Bucket's Object-lock configuration, mode: {}, duration: {}", config.mode(), config.duration());
    }
}
