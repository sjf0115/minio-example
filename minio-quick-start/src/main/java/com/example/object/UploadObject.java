package com.example.object;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.example.bean.Constant.*;

/**
 * 功能：UploadObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 16:45
 */
public class UploadObject {
    public static void main(String[] args) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("test-bucket")
                        .object("test-object")
                        .filename("my-filename")
                        .build());
        System.out.println("my-filename is uploaded to my-objectname successfully");
    }
}
