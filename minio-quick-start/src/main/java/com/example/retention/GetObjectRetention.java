package com.example.retention;

import io.minio.GetObjectRetentionArgs;
import io.minio.MinioClient;
import io.minio.messages.Retention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：GetObjectRetention 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class GetObjectRetention {
    private static final Logger LOG = LoggerFactory.getLogger(GetObjectRetention.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-object-lock";
        String objectName = "object-1";

        GetObjectRetentionArgs retentionArgs = GetObjectRetentionArgs.builder().bucket(bucketName).object(objectName).build();
        Retention retention = minioClient.getObjectRetention(retentionArgs);
        LOG.info("Mode: {}, RetainUntilDate: {}", retention.mode(), retention.retainUntilDate());
    }
}
