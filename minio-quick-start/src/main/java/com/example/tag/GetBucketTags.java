package com.example.tag;

import io.minio.GetBucketTagsArgs;
import io.minio.MinioClient;
import io.minio.messages.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：SetBucketTags 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class GetBucketTags {
    private static final Logger LOG = LoggerFactory.getLogger(GetBucketTags.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        Tags tags = minioClient.getBucketTags(GetBucketTagsArgs.builder().bucket(bucketName).build());
        LOG.info("Bucket {} tags: {}", bucketName, tags.get());
    }
}
