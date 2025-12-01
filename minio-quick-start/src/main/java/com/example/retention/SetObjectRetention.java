package com.example.retention;

import io.minio.MinioClient;
import io.minio.SetObjectRetentionArgs;
import io.minio.Time;
import io.minio.messages.Retention;
import io.minio.messages.RetentionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static com.example.bean.Constant.*;

/**
 * 功能：SetObjectRetention 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class SetObjectRetention {
    private static final Logger LOG = LoggerFactory.getLogger(SetObjectRetention.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

         String bucketName = "bucket-1";
//        String bucketName = "bucket-object-lock";
        String objectName = "object-1";

        ZonedDateTime retentionUntil = ZonedDateTime.now(Time.UTC).plusDays(3).withNano(0);
        Retention config = new Retention(RetentionMode.COMPLIANCE, retentionUntil);

        SetObjectRetentionArgs retentionArgs = SetObjectRetentionArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .config(config)
                .bypassGovernanceMode(true)
                .build();
        minioClient.setObjectRetention(retentionArgs);

        LOG.info("{}/{} set object retention successfully", objectName, bucketName);
    }
}
