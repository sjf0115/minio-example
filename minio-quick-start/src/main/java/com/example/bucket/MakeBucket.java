package com.example.bucket;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：MakeBucket 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class MakeBucket {
    private static final Logger LOG = LoggerFactory.getLogger(MakeBucket.class);

    /**
     * 创建默认 Bucket
     */
    public static void markBucket () throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-part";
        // 构建参数
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
        // Bucket 不存在则创建
        if (!minioClient.bucketExists(bucketExistsArgs)) {
            minioClient.makeBucket(makeBucketArgs);
            LOG.info("bucket {} is created successfully", bucketName);
        } else {
            LOG.info("bucket {} is already created", bucketName);
        }
    }

    /**
     * 启用对象锁定创建 Bucket
     * @throws Exception
     */
    public static void markBucketWithObjectLock () throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-object-lock";
        // 构建参数
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).objectLock(true).build();
        // Bucket 不存在则创建
        if (!minioClient.bucketExists(bucketExistsArgs)) {
            minioClient.makeBucket(makeBucketArgs);
            LOG.info("bucket {} is created successfully", bucketName);
        } else {
            LOG.info("bucket {} is already created", bucketName);
        }
    }

    public static void main(String[] args) throws Exception {
        markBucket();
        // markBucketWithObjectLock();
    }
}
