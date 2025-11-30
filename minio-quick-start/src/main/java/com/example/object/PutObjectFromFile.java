package com.example.object;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

import static com.example.bean.Constant.*;

/**
 * 功能：从本地文件上传创建 Object 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class PutObjectFromFile {
    private static final Logger LOG = LoggerFactory.getLogger(PutObjectFromFile.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-2";

        // 上传数据
        File file = new File("/opt/data/province_info.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        // 上传
        PutObjectArgs objectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(fileInputStream, file.length(), -1)
                .build();
        minioClient.putObject(objectArgs);
        fileInputStream.close();
        LOG.info("Bucket: {} Object: {} is uploaded successfully", bucketName, objectName);
    }
}
