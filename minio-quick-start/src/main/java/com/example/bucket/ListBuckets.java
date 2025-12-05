package com.example.bucket;

import io.minio.MinioClient;
import io.minio.messages.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.example.bean.Constant.*;

/**
 * 功能：ListBuckets 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class ListBuckets {
    private static final Logger LOG = LoggerFactory.getLogger(ListBuckets.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        List<Bucket> bucketList = minioClient.listBuckets();
        for (Bucket bucket : bucketList) {
            LOG.info("bucket name: {}, createDate: {}", bucket.name(), bucket.creationDate());
        }
    }
}
