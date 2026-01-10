package com.example;

import com.example.minio.*;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.UploadPartResponse;
import io.minio.messages.Part;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class MinIOUploadClientTest extends TestCase {
    @Autowired
    private MinIOUploadClient client;

    private String bucket = "bucket-part";
    private String object = "object-part";

    // 服务端上传分片
    public void testServerUploadPart() throws IOException {
        // 初始化分片上传
        String uploadId = client.createMultipartUpload(CreateMultipartUploadArgs.builder()
                        .bucket(bucket)
                        .object(object)
                        .build())
                .result()
                .uploadId();

        // 2 上传分片-通过服务端上传分片
        // 2.1 分片1
        Path path1 = Paths.get("/opt/data/minio.txt");
        client.uploadPart(UploadPartArgs.builder()
                .bucket(bucket)
                .object(object)
                .uploadId(uploadId)
                .partData(Files.newInputStream(path1))
                .partSize(Files.size(path1))
                .partNumber(1)
                .build());

        // 2.2 分片2
        Path path2 = Paths.get("/opt/data/province_info.txt");
        client.uploadPart(UploadPartArgs.builder()
                .bucket(bucket)
                .object(object)
                .uploadId(uploadId)
                .partData(Files.newInputStream(path2))
                .partSize(Files.size(path2))
                .partNumber(2)
                .build());

        // 3. 列出分片上传
        ListPartsResponse listPartsResponse = client.listParts(ListPartsArgs.builder()
                .bucket(bucket)
                .object(object)
                .uploadId(uploadId).build());
        Part[] parts = listPartsResponse.result().partList().stream().toArray(Part[]::new);
        int partSize = parts.length;
        if (partSize == 0) {
            throw new RuntimeException("分片数量为[0].");
        }
        for (Part part : parts) {
            log.info("完成第 {} 个分片上传, etag: {}", part.partNumber(), part.etag());
        }

        // 4. 完成分片上传
        client.completeMultipartUpload(CompleteMultipartUploadArgs.builder()
                .bucket(bucket)
                .object(object)
                .uploadId(uploadId)
                .parts(parts)
                .build());
        log.info("完成分片合并: {}/{}", bucket, object);
    }
}
