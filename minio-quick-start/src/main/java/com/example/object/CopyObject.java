package com.example.object;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：CopyObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 16:45
 */
public class CopyObject {
    private static final Logger LOG = LoggerFactory.getLogger(CopyObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String sourceBucketName = "bucket-1";
        String sourceObjectName = "object-3";
        String targetBucketName = "bucket-1";
        String targetObjectName = "object-4";

        // 复制
        CopySource copySource = CopySource.builder().bucket(sourceBucketName)
                .object(sourceObjectName).build();
        CopyObjectArgs objectArgs = CopyObjectArgs.builder().bucket(targetBucketName).object(targetObjectName)
                .source(copySource).build();
        minioClient.copyObject(objectArgs);
        LOG.info("{}/{} copied to {}/{} successfully", sourceObjectName, sourceBucketName, targetObjectName, targetBucketName);
    }
}
