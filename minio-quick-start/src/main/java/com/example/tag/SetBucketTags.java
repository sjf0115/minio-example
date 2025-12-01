package com.example.tag;

import io.minio.MinioClient;
import io.minio.SetBucketTagsArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.example.bean.Constant.*;

/**
 * 功能：SetBucketTags 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:49
 */
public class SetBucketTags {
    private static final Logger LOG = LoggerFactory.getLogger(SetBucketTags.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        // Tag
        Map<String, String> map = new HashMap<>();
        map.put("Project", "Test");
        map.put("Date", "20251201");
        minioClient.setBucketTags(SetBucketTagsArgs.builder().bucket(bucketName).tags(map).build());
        LOG.info("{} set bucket tags successfully", bucketName);
    }
}
