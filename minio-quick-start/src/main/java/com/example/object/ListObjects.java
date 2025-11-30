package com.example.object;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：ListObjects 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class ListObjects {
    private static final Logger LOG = LoggerFactory.getLogger(ListObjects.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        ListObjectsArgs objectsArgs = ListObjectsArgs.builder().bucket(bucketName).recursive(true).build();
        Iterable<Result<Item>> results = minioClient.listObjects(objectsArgs);
        for (Result<Item> result : results) {
            Item item = result.get();
            LOG.info("Object: {}, Size: {}, LastModified: {}", item.objectName(), item.size(), item.lastModified());
        }
    }
}
