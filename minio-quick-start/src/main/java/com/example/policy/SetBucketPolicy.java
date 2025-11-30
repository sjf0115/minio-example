package com.example.policy;

import com.example.bean.BucketConfig;
import com.example.bean.Statement;
import com.example.object.CopyObject;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.bean.Constant.*;

/**
 * 功能：SetBucketPolicy 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:08
 */
public class SetBucketPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(SetBucketPolicy.class);
    private static final Gson gson = new GsonBuilder().create();

    public static void main(String[] args) throws Exception {
        // MinIO 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build();

        String bucketName = "bucket-1";

        /*StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("    \"Statement\": [\n");
        builder.append("        {\n");
        builder.append("            \"Action\": [\n");
        builder.append("                \"s3:GetBucketLocation\",\n");
        builder.append("                \"s3:ListBucket\"\n");
        builder.append("            ],\n");
        builder.append("            \"Effect\": \"Allow\",\n");
        builder.append("            \"Principal\": \"*\",\n");
        builder.append("            \"Resource\": \"arn:aws:s3:::bucket-1\"\n");
        builder.append("        },\n");
        builder.append("        {\n");
        builder.append("            \"Action\": [\"s3:GetObject\"],\n");
        builder.append("            \"Effect\": \"Allow\",\n");
        builder.append("            \"Principal\": \"*\",\n");
        builder.append("            \"Resource\": \"arn:aws:s3:::bucket-1/object-1*\"\n");
        builder.append("        }\n");
        builder.append("    ],\n");
        builder.append("    \"Version\": \"2012-10-17\"\n");
        builder.append("}\n");
        String configJson = builder.toString();*/

        // 生成策略
        Statement s1 = new Statement();
        s1.setAction(Lists.newArrayList("s3:GetBucketLocation", "s3:ListBucket"));
        s1.setEffect("Allow");
        s1.setPrincipal("*");
        s1.setResource("arn:aws:s3:::" + bucketName);

        Statement s2 = new Statement();
        s2.setAction(Lists.newArrayList("s3:GetObject"));
        s2.setEffect("Allow");
        s2.setPrincipal("*");
        s2.setResource("arn:aws:s3:::" + bucketName + "/object-1");

        BucketConfig config = new BucketConfig();
        config.setStatement(Lists.newArrayList(s1, s2));
        config.setVersion("2012-10-17");
        String configJson = gson.toJson(config);
        LOG.info("Config: {}", configJson);

        // 更新策略
        SetBucketPolicyArgs policyArgs = SetBucketPolicyArgs.builder().bucket(bucketName).config(configJson).build();
        minioClient.setBucketPolicy(policyArgs);

        LOG.info("{} successfully", bucketName);
    }
}
