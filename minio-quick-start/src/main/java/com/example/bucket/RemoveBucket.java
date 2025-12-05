package com.example.bucket;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：RemoveBucket 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class RemoveBucket {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveBucket.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        // 构建参数
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        RemoveBucketArgs bucketArgs = RemoveBucketArgs.builder().bucket(bucketName).build();
        // Bucket 存在则删除
        if (minioClient.bucketExists(bucketExistsArgs)) {
            minioClient.removeBucket(bucketArgs);
            LOG.info("bucket {} is removed successfully", bucketName);
        } else {
            LOG.info("bucket {} does not exist", bucketName);
        }
    }
}
