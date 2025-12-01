package com.example.tag;

import io.minio.DeleteBucketTagsArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：DeleteBucketTags 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class DeleteBucketTags {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteBucketTags.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        minioClient.deleteBucketTags(DeleteBucketTagsArgs.builder().bucket(bucketName).build());
        LOG.info("{} delete bucket tags successfully", bucketName);
    }
}
