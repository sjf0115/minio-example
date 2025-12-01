package com.example.lock;

import io.minio.DeleteObjectLockConfigurationArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：DeleteObjectLockConfiguration 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class DeleteObjectLockConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteObjectLockConfiguration.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-object-lock";
        minioClient.deleteObjectLockConfiguration(DeleteObjectLockConfigurationArgs.builder().bucket(bucketName).build());
        LOG.info("Object-lock configuration is deleted successfully");
    }
}
