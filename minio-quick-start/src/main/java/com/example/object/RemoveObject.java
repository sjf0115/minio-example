package com.example.object;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：RemoveObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class RemoveObject {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-1";
        RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build();
        minioClient.removeObject(objectArgs);
        LOG.info("Bucket: {}, Object: {} is removed successfully", bucketName, objectName);
    }
}
