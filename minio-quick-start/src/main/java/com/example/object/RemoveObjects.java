package com.example.object;

import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.example.bean.Constant.*;

/**
 * 功能：RemoveObjects 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class RemoveObjects {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveObjects.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        List<DeleteObject> objects = new LinkedList<>();
        objects.add(new DeleteObject("object-1"));
        objects.add(new DeleteObject("object-2"));

        RemoveObjectsArgs objectsArgs = RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(objectsArgs);
        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            LOG.info("Error in deleting object {}, {}", error.objectName(), error.message());
        }
    }
}
