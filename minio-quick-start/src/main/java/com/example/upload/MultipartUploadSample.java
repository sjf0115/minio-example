package com.example.upload;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static com.example.bean.Constant.*;

/**
 * 功能：分片上传示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/3 14:50
 */
public class MultipartUploadSample {
    private static MinioAsyncClient minioClient;

    public ObjectWriteResponse uploadFile(UploadObjectArgs args) {
        {
            String uploadId = null;
            ObjectWriteResponse response = null;
            try {
                // 第一步：初始化
                Multimap<String, String> headers = newMultimap(args.extraHeaders());
                headers.putAll(args.genHeaders());
                if (!headers.containsKey("Content-Type")) headers.put("Content-Type", args.contentType());

                CreateMultipartUploadResponse createMultipartUploadResponse = minioClient.createMultipartUploadAsync(
                                args.bucket(),
                                args.region(),
                                args.object(),
                                headers,
                                args.extraQueryParams())
                                .get();
                uploadId = createMultipartUploadResponse.result().uploadId();

                // 第二步：上传
                final RandomAccessFile file = new RandomAccessFile(args.filename(), "r");
                /*PartReader partReader = minioClient.newPartReader(file, args.objectSize(), args.partSize(), args.partCount());
                if (partReader == null) {
                    throw new IllegalArgumentException("data must be RandomAccessFile or InputStream");
                }*/
                //Part[] parts = minioClient.uploadParts(args, uploadId, null, null);

                // 第三步：上传完成
                response = minioClient.completeMultipartUploadAsync(
                        args.bucket(), args.region(), args.object(), uploadId, null,
                                null, null)
                                .get();
            } catch (InsufficientDataException
                     | InternalException
                     | InvalidKeyException
                     | IOException
                     | NoSuchAlgorithmException
                     | XmlParserException
                     | InterruptedException
                     | ExecutionException e) {
                Throwable throwable = e;
                if (throwable instanceof ExecutionException) {
                    throwable = ((ExecutionException) throwable).getCause();
                }
                if (throwable instanceof CompletionException) {
                    throwable = ((CompletionException) throwable).getCause();
                }
                if (uploadId == null) {
                    throw new CompletionException(throwable);
                }
                try {
                    minioClient.abortMultipartUploadAsync(
                            args.bucket(), args.region(), args.object(), uploadId, null, null)
                            .get();
                } catch (InsufficientDataException
                         | InternalException
                         | InvalidKeyException
                         | IOException
                         | NoSuchAlgorithmException
                         | XmlParserException
                         | InterruptedException
                         | ExecutionException ex) {
                    throwable = ex;
                    if (throwable instanceof ExecutionException) {
                        throwable = ((ExecutionException) throwable).getCause();
                    }
                    if (throwable instanceof CompletionException) {
                        throwable = ((CompletionException) throwable).getCause();
                    }
                }
                throw new CompletionException(throwable);
            }
            return response;
        }
    }

    protected Multimap<String, String> newMultimap(Multimap<String, String> map) {
        return (map != null) ? HashMultimap.create(map) : HashMultimap.create();
    }

    public static void main(String[] args) {
        // MinIO 客户端
        minioClient = new MinioAsyncClient(MinioAsyncClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build());
    }

}
