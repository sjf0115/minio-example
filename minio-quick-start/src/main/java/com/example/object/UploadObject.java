package com.example.object;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：UploadObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 16:45
 */
public class UploadObject {
    private static final Logger LOG = LoggerFactory.getLogger(UploadObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-1";
        String fileName = "/opt/data/province_info.txt";
        // 上传文件
        UploadObjectArgs objectArgs = UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(fileName)
                .build();
        ObjectWriteResponse response = minioClient.uploadObject(objectArgs);
        LOG.info("{} is uploaded to {}({}) successfully", fileName, response.object(), response.bucket());
    }
}
