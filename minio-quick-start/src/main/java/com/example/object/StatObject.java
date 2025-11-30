package com.example.object;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：StatObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 21:46
 */
public class StatObject {
    private static final Logger LOG = LoggerFactory.getLogger(PutObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-1";
        // 详情
        StatObjectArgs objectArgs = StatObjectArgs.builder().bucket(bucketName).object(objectName).build();
        StatObjectResponse objectResponse = minioClient.statObject(objectArgs);
        LOG.info("Bucket: {} Object: {}, Size: {}", objectResponse.bucket(), objectResponse.object(), objectResponse.size());
    }
}
