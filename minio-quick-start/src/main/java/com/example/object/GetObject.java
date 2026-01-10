package com.example.object;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.example.bean.Constant.*;

/**
 * 功能：GetObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 17:12
 */
public class GetObject {
    private static final Logger LOG = LoggerFactory.getLogger(GetObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";
        String objectName = "object-2";

        // 从 Object 中获取输入流
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
        InputStream stream = minioClient.getObject(objectArgs);

        // 从输入流中读取输出到控制台
        byte[] buf = new byte[16384];
        int bytesRead;
        while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
            String result = new String(buf, 0, bytesRead, StandardCharsets.UTF_8);
            LOG.info("result: {}", result);
        }
        stream.close();
    }
}
