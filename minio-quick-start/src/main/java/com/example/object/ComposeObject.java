package com.example.object;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.example.bean.Constant.*;

/**
 * 功能：ComposeObject 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 09:35
 */
public class ComposeObject {
    private static final Logger LOG = LoggerFactory.getLogger(ComposeObject.class);

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        // 单 Object 限制>=5M
        List<ComposeSource> sources = new ArrayList<ComposeSource>();
        String sourceBucketName = "bucket-1";
        String object1 = "object-1";
        String object2 = "object-2";
        sources.add(ComposeSource.builder().bucket(sourceBucketName).object(object1).build());
        sources.add(ComposeSource.builder().bucket(sourceBucketName).object(object2).build());

        String targetBucketName = "bucket-1";
        String targetObjectName = "object-merge";
        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(targetBucketName)
                        .object(targetObjectName)
                        .sources(sources)
                        .build());
        LOG.info("{}/{} merge to {}/{} successfully", object1, object2, targetObjectName, targetBucketName);
    }
}
