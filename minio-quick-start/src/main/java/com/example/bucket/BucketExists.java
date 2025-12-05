package com.example.bucket;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：判断 Bucket 是否存在示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:50
 */
public class BucketExists {
    private static final Logger LOG = LoggerFactory.getLogger(MakeBucket.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        // 构建参数
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        // 判断 Bucket 是否存
        boolean exists = minioClient.bucketExists(bucketExistsArgs);
        if (exists) {
            LOG.info("bucket {} exists", bucketName);
        } else {
            LOG.info("bucket {} does not exist", bucketName);
        }
    }
}
