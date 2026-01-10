package com.example.object;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.example.bean.Constant.*;

/**
 * 功能：PutObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class PutObject {
    private static final Logger LOG = LoggerFactory.getLogger(PutObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-1";

        // 上传数据
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i < 700000; i++) {
            builder.append(1000000000 + i).append("\n");
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));

        // 上传
        PutObjectArgs objectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                .build();
        minioClient.putObject(objectArgs);
        byteArrayInputStream.close();
        LOG.info("Bucket: {} Object: {} is uploaded successfully", bucketName, objectName);
    }
}
