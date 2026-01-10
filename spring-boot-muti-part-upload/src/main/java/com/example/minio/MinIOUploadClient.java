package com.example.minio;

import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Part;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 功能：Minio 分片上传客户端
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 14:17
 */
public class MinIOUploadClient extends MinioAsyncClient {
    public MinIOUploadClient(MinioAsyncClient client) {
        super(client);
    }

    /**
     * 创建分片上传任务
     */
    public CreateMultipartUploadResponse createMultipartUpload(CreateMultipartUploadArgs args) {
        try {
            return createMultipartUploadAsync(
                    args.bucket(),
                    args.region(),
                    args.object(),
                    args.extraHeaders(),
                    args.extraQueryParams()
            ).get();
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | IOException
                 | NoSuchAlgorithmException
                 | XmlParserException
                 | InterruptedException
                 | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<CreateMultipartUploadResponse> createMultipartUploadAsync(String bucketName, String region, String objectName, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.createMultipartUploadAsync(bucketName, region, objectName, headers, extraQueryParams);
    }

    /**
     * 合并分片
     */
    public ObjectWriteResponse completeMultipartUpload(CompleteMultipartUploadArgs args) {
        try {
            return completeMultipartUploadAsync(
                    args.bucket(),
                    args.region(),
                    args.object(),
                    args.uploadId(),
                    args.parts(),
                    args.extraHeaders(),
                    args.extraQueryParams()
            ).get();
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | IOException
                 | NoSuchAlgorithmException
                 | XmlParserException
                 | InterruptedException
                 | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<ObjectWriteResponse> completeMultipartUploadAsync(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.completeMultipartUploadAsync(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }

    /**
     * 终止分片上传
     */
    public AbortMultipartUploadResponse abortMultipartUpload(AbortMultipartUploadArgs args) {
        try {
            return abortMultipartUploadAsync(
                    args.bucket(),
                    args.region(),
                    args.object(),
                    args.uploadId(),
                    args.extraHeaders(),
                    args.extraQueryParams()
            ).get();
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | IOException
                 | NoSuchAlgorithmException
                 | XmlParserException
                 | InterruptedException
                 | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CompletableFuture<AbortMultipartUploadResponse> abortMultipartUploadAsync(String bucketName, String region, String objectName, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.abortMultipartUploadAsync(bucketName, region, objectName, uploadId, extraHeaders, extraQueryParams);
    }

    /**
     * 列出所有上传的分片
     */
    public ListPartsResponse listParts(ListPartsArgs args) {
        try {
            return listPartsAsync(
                    args.bucket(),
                    args.region(),
                    args.object(),
                    args.maxParts(),
                    args.partNumberMarker(),
                    args.uploadId(),
                    args.extraHeaders(),
                    args.extraQueryParams()
            ).get();
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | IOException
                 | NoSuchAlgorithmException
                 | XmlParserException
                 | InterruptedException
                 | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CompletableFuture<ListPartsResponse> listPartsAsync(String bucketName, String region, String objectName, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.listPartsAsync(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }

    /**
     * 上传分片
     */
    public UploadPartResponse uploadPart(UploadPartArgs args) {
        try {
            return uploadPartAsync(
                    args.bucket(),
                    args.region(),
                    args.object(),
                    args.partData(),
                    args.partSize(),
                    args.uploadId(),
                    args.partNumber(),
                    args.extraHeaders(),
                    args.extraQueryParams()
            ).get();
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | IOException
                 | NoSuchAlgorithmException
                 | XmlParserException
                 | InterruptedException
                 | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CompletableFuture<UploadPartResponse> uploadPartAsync(String bucketName, String region, String objectName, Object data, long length, String uploadId, int partNumber, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.uploadPartAsync(bucketName, region, objectName, data, length, uploadId, partNumber, extraHeaders, extraQueryParams);
    }
}
