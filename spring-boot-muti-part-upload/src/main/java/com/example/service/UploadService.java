package com.example.service;

import com.example.bean.*;
import com.example.minio.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.minio.*;
import io.minio.messages.InitiateMultipartUploadResult;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能：上传服务
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 18:07
 */
@Slf4j
@Service
public class UploadService {
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private MinIOUploadClient minIOUploadClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * 初始化分片上传
     * @param object 对象名称
     */
    public CreateUploadResult createMultipartUpload(String object) {
        try {
            // 初始化分片上传
            CreateMultipartUploadResponse response = minIOUploadClient.createMultipartUpload(CreateMultipartUploadArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .build());
            InitiateMultipartUploadResult uploadResult = response.result();

            CreateUploadResult result = CreateUploadResult.builder()
                    .bucket(uploadResult.bucketName())
                    .object(uploadResult.objectName())
                    .uploadId(uploadResult.uploadId())
                    .build();
            log.info("初始化分片上传成功：{}", gson.toJson(result));
            return result;
        } catch (Exception e) {
            log.error("初始化分片上传失败：{}", e.getMessage());
            throw new RuntimeException("初始化分片上传失败");
        }
    }

    /**
     * 上传分片
     * @param uploadPart 分片上传请求信息
     * @return 上传分片结果
     */
    public UploadPartResult uploadPart(UploadPart uploadPart) {
        try {
            MultipartFile file = uploadPart.getFile();
            UploadPartResponse response = minIOUploadClient.uploadPart(UploadPartArgs.builder()
                    .bucket(bucket)
                    .object(uploadPart.getObject())
                    .uploadId(uploadPart.getUploadId())
                    .partData(file.getInputStream())
                    .partSize(file.getSize())
                    .partNumber(uploadPart.getPartNumber())
                    .build());

            UploadPartResult result = UploadPartResult.builder()
                    .uploadId(response.uploadId())
                    .bucket(bucket)
                    .object(uploadPart.getObject())
                    .partNumber(response.partNumber())
                    .etag(response.etag())
                    .build();
            log.info("上传分片成功：{}", uploadPart.getFile().getName());
            return result;
        } catch (Exception e) {
            log.error("上传分片失败：", e);
            throw new RuntimeException("上传分片失败", e);
        }
    }

    /**
     * 获取已上传的分片列表
     * @param uploadPart 请求信息
     */
    public ListPartsResult listParts(UploadPart uploadPart) {
        try {
            String object = uploadPart.getObject();
            String uploadId = uploadPart.getUploadId();
            ListPartsResponse response = minIOUploadClient.listParts(ListPartsArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .uploadId(uploadId).build());
            List<Part> partList = response.result().partList();
            List<ListPart> parts = partList.stream().map(part ->
                            ListPart.builder()
                                    .partNumber(part.partNumber())
                                    .etag(part.etag())
                                    .size(part.partSize())
                                    .build())
                    .collect(Collectors.toList());
            ListPartsResult result = ListPartsResult.builder()
                    .uploadId(uploadId)
                    .bucket(bucket)
                    .object(object)
                    .parts(parts)
                    .build();
            log.info("已上传分片数量：{}", partList.size());
            return result;
        } catch (Exception e) {
            log.error("获取分片列表失败：", e);
            throw new RuntimeException("获取分片列表失败", e);
        }
    }

    /**
     * 合并分片
     * @param uploadPart 分片上传请求信息
     */
    public CompleteUploadResult completeMultipartUpload(UploadPart uploadPart) {
        try {
            String uploadId = uploadPart.getUploadId();
            String object = uploadPart.getObject();
            // 1. 查询分片
            ListPartsResponse listResponse = minIOUploadClient.listParts(ListPartsArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .uploadId(uploadId).build());
            Part[] parts = listResponse.result().partList().toArray(new Part[0]);
            // 2. 合并分片：注，S3分片最小限制为5MB，也就是分片的大小必须大于5MB，同时小于5GB
            ObjectWriteResponse response = minIOUploadClient.completeMultipartUpload(CompleteMultipartUploadArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .uploadId(uploadId)
                    .parts(parts)
                    .build());

            CompleteUploadResult result = CompleteUploadResult.builder()
                    .uploadId(uploadId)
                    .bucket(bucket)
                    .object(object)
                    .versionId(response.versionId())
                    .etag(response.etag())
                    .build();
            log.info("合并 {} 个分片成功：{}", parts.length, uploadId);
            return result;
        } catch (Exception e) {
            log.error("合合并分片失败：", e);
            throw new RuntimeException("合并分片失败", e);
        }
    }

    /**
     * 取消分片上传
     * @param uploadPart 请求信息
     */
    public AbortUploadResult abortMultipartUpload(UploadPart uploadPart) {
        try {
            String uploadId = uploadPart.getUploadId();
            String object = uploadPart.getObject();
            minIOUploadClient.abortMultipartUpload(AbortMultipartUploadArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .uploadId(uploadId).build());

            AbortUploadResult result = AbortUploadResult.builder()
                    .uploadId(uploadId)
                    .bucket(bucket)
                    .object(object)
                    .build();
            log.info("取消分片任务上传成功: {}", uploadId);
            return result;
        } catch (Exception e) {
            log.error("取消分片上传失败：", e);
            throw new RuntimeException("取消分片上传失败", e);
        }
    }
}
