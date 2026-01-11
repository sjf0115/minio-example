package com.example.service;

import com.example.bean.*;
import com.example.minio.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.minio.*;
import io.minio.messages.InitiateMultipartUploadResult;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.input.BoundedInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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

    @Value("${minio.part.size}")
    private Long partSize;

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
            // 初始化分片上传返回结果
            CreateUploadResult result = CreateUploadResult.builder()
                    .bucket(uploadResult.bucketName())
                    .object(uploadResult.objectName())
                    .uploadId(uploadResult.uploadId())
                    .build();
            log.info("初始化分片上传成功，上传ID：{}", uploadResult.uploadId());
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
            String uploadId = uploadPart.getUploadId();
            MultipartFile file = uploadPart.getFile();
            long fileSize = file.getSize();
            int partNumber = uploadPart.getPartNumber();
            String object = uploadPart.getObject();
            List<Integer> partNumbers = Lists.newArrayList();
            List<String> etags = Lists.newArrayList();
            for (long start = 0; start < fileSize; start += partSize) {
                long curPartSize = Math.min(partSize, fileSize - start);
                // 读取文件分片并上传
                try (InputStream is = file.getInputStream()) {
                    is.skip(start);
                    BoundedInputStream boundedInputStream = new BoundedInputStream(is, curPartSize);
                    // 上传分片
                    UploadPartResponse response = minIOUploadClient.uploadPart(UploadPartArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .uploadId(uploadId)
                            .partData(boundedInputStream)
                            .partSize(curPartSize)
                            .partNumber(partNumber)
                            .build());
                    partNumbers.add(partNumber);
                    etags.add(response.etag());
                    log.info("文件 {} 上传分片 {} 成功, ETag: {}", file.getName(), partNumber, response.etag());
                } catch (RuntimeException e) {
                    log.error("上传分片 {} 失败：", partNumber, e);
                    throw new RuntimeException("上传分片失败", e);
                }
                partNumber+=1;
            }

            // 上传分片返回结果
            UploadPartResult result = UploadPartResult.builder()
                    .uploadId(uploadId)
                    .bucket(bucket)
                    .object(object)
                    .partNumbers(partNumbers)
                    .etags(etags)
                    .build();
            return result;
        } catch (Exception e) {
            log.error("上传分片失败：", e);
            throw new RuntimeException("上传分片失败", e);
        }
    }

    /**
     * 列举上传ID所属的所有已经上传成功的分片
     * @param uploadPart 请求信息
     */
    public ListPartsResult listParts(UploadPart uploadPart) {
        try {
            String object = uploadPart.getObject();
            String uploadId = uploadPart.getUploadId();
            // 列举分片
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
            // 列举分片返回结果
            ListPartsResult result = ListPartsResult.builder()
                    .uploadId(uploadId)
                    .bucket(bucket)
                    .object(object)
                    .parts(parts)
                    .build();
            log.info("已上传 {} 个分片：{}", parts.size(), parts.stream().map(ListPart::getPartNumber).collect(Collectors.toList()));
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
            // 合并分片返回结果
            CompleteUploadResult result = CompleteUploadResult.builder()
                    .uploadId(uploadId)
                    .bucket(bucket)
                    .object(object)
                    .versionId(response.versionId())
                    .etag(response.etag())
                    .build();
            log.info("完成分片上传成功, 上传ID: {}, 分片个数: {}", uploadId, parts.length);
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
            // 取消分片返回结果
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
