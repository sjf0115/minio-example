package com.example.object;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：DownloadObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 16:45
 */
public class DownloadObject {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-1";
        String fileName = "/opt/data/minio.txt";

        // 下载
        DownloadObjectArgs objectArgs = DownloadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(fileName)
                .build();
        minioClient.downloadObject(objectArgs);
        LOG.info("{} is successfully downloaded to {}", objectName, fileName);
    }
}
