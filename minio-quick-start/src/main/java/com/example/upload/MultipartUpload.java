package com.example.upload;

import io.minio.ObjectWriteResponse;
import io.minio.UploadPartResponse;
import io.minio.messages.InitiateMultipartUploadResult;
import io.minio.messages.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.bean.Constant.*;

/**
 * 功能：MinIO 分片上传（Multipart Upload）基础示例 —— 仅分片上传
 *      <ol>
 *          <li>初始化分片上传 CreateMultipartUpload：获取唯一 uploadId 作为任务标识</li>
 *          <li>并发上传分片 UploadPart：按分片大小切分文件，线程池并发上传，单分片失败自动重试</li>
 *          <li>完成分片上传 CompleteMultipartUpload：按分片号升序合并为完整对象</li>
 *      </ol>
 *      注意：MinIO/S3 约束——除最后一个分片外，其余分片最小 5MB、最大 5GB，分片总数最多 10000 个。
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/3 14:50
 */
public class MultipartUpload {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartUpload.class);

    /** 最小分片大小：5MB。除最后一个分片外，其余分片大小不得小于 5MB */
    private static final long MIN_PART_SIZE = 5L * 1024 * 1024;
    /** 最大分片大小：5GB */
    private static final long MAX_PART_SIZE = 5L * 1024 * 1024 * 1024;
    /** 最大分片数量：10000 */
    private static final int MAX_PART_COUNT = 10000;
    /** 并发上传线程数：需结合网络带宽与机器负载合理设置，过大过小都会影响整体性能 */
    private static final int CONCURRENCY = 5;
    /** 单个分片上传失败后的最大重试次数：应对网络不稳定，失败仅重传该分片（注意：这是单次上传内的重试，非断点续传） */
    private static final int MAX_RETRY = 3;

    /** 自定义扩展的 MinIO 异步客户端，用于调用 SDK 中 protected 的分片上传方法 */
    private final MinioAsyncClient minioClient;
    /** 固定大小线程池：天然限制并发分片数量 */
    private final ExecutorService executor;

    public MultipartUpload(MinioAsyncClient minioClient) {
        this.minioClient = minioClient;
        this.executor = Executors.newFixedThreadPool(CONCURRENCY);
    }

    /**
     * 分片上传完整流程（仅分片上传，无断点续传）：
     * 初始化 -> 并发上传全部分片 -> 合并完成；任一分片最终失败则取消整个上传任务并清理碎片。
     * @param bucket   存储桶
     * @param region   区域（可选，MinIO 默认 us-east-1）
     * @param object   对象名
     * @param filename 本地文件路径
     * @return 合并完成后的对象写入响应
     */
    public ObjectWriteResponse uploadFile(String bucket, String region, String object, String filename) {
        // 准备文件
        File file = new File(filename);
        if (!file.isFile()) {
            throw new IllegalArgumentException(filename + " 不是一个有效的文件");
        }
        long fileSize = file.length();
        if (fileSize == 0) {
            throw new IllegalArgumentException("空文件不适用分片上传，请改用 putObject");
        }

        // 步骤 1：初始化分片上传，获取 uploadId
        String uploadId;
        try {
            InitiateMultipartUploadResult result = minioClient
                    .createMultipartUploadAsync(bucket, region, object, null, null)
                    .get()
                    .result();
            uploadId = result.uploadId();
            LOG.info("初始化分片上传成功，bucket:{}, object:{}, uploadId:{}", result.bucketName(), result.objectName(), uploadId);
        } catch (Exception e) {
            throw new RuntimeException("初始化分片上传失败", e);
        }

        // 步骤 2：并发上传所有分片（每次都从头上传全部分片，不跳过、不复用历史分片）
        Part[] parts;
        try {
            // 根据文件大小计算合理的分片大小与分片数量
            long partSize = calculatePartSize(fileSize);
            int partCount = (int) ((fileSize + partSize - 1) / partSize);
            LOG.info("准备分片上传 object:{}, fileSize:{} 字节, partSize:{} 字节, partCount:{}, concurrency:{}",
                    object, fileSize, partSize, partCount, CONCURRENCY);
            parts = uploadParts(bucket, region, object, file, uploadId, partSize, partCount);
        } catch (Exception e) {
            throw new RuntimeException("并发上传所有分片失败", e);
        }

        // 步骤 3：完成分片上传，按分片号合并为完整对象
        try {
            // 完成上传
            ObjectWriteResponse response = minioClient
                    .completeMultipartUploadAsync(bucket, region, object, uploadId, parts, null, null)
                    .get();
            LOG.info("分片上传全部完成 object:{}, etag:{}, versionId:{}", object, response.etag(), response.versionId());
            return response;
        } catch (Exception e) {
            LOG.error("分片上传失败 uploadId:{}，将取消上传并清理碎片", uploadId, e);
            // 清理碎片
            try {
                minioClient.abortMultipartUploadAsync(bucket, region, object, uploadId, null, null).get();
                LOG.info("取消分片上传成功，已清理碎片 uploadId:{}", uploadId);
                throw new RuntimeException("取消分片上传成功 uploadId:" + uploadId, e);
            } catch (Exception ex) {
                LOG.error("取消分片上传失败 uploadId:{}", uploadId, ex);
                throw new RuntimeException("取消分片上传失败, uploadId:" + uploadId, ex);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 步骤 2：并发上传分片
     * 将文件按 partSize 切分为 partCount 个分片，提交到线程池并发上传，单个分片失败自动重试。
     * 与断点续传版本不同：这里不接收已上传分片信息，也不会跳过任何分片。
     *
     * @param file      本地文件
     * @param uploadId  上传任务 ID
     * @param partSize  分片大小
     * @param partCount 分片总数
     * @return 按分片号升序排列的分片数组，用于合并
     */
    public Part[] uploadParts(String bucket, String region, String object, File file, String uploadId, long partSize, int partCount) {
        long fileSize = file.length();
        List<CompletableFuture<Part>> futures = new ArrayList<>();
        // 完成进度
        AtomicInteger finished = new AtomicInteger(0);
        // 分片上传
        for (int partNumber = 1; partNumber <= partCount; partNumber++) {
            // 计算当前分片在文件中的起始偏移
            final long startPos = (long) (partNumber - 1) * partSize;
            // 计算当前分片实际大小：最后一个分片可能不足 partSize
            final long curPartSize = Math.min(partSize, fileSize - startPos);
            // 当前第几个分片
            final int partIndex = partNumber;

            // 提交到线程池并发上传，线程池大小即最大并发数
            CompletableFuture<Part> future = CompletableFuture.supplyAsync(() -> {
                // 最多重试 MAX_RETRY 次
                for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
                    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                        raf.seek(startPos);
                        // 上传分片
                        UploadPartResponse response = minioClient
                                .uploadPartAsync(bucket, region, object, raf, curPartSize, uploadId, partIndex, null, null)
                                .get();
                        LOG.info("【{}】分片 {}/{} 上传完成，当前进度 {}/{}", Thread.currentThread().getId(), partIndex, partCount, finished.incrementAndGet(), partCount);
                        return new Part(partIndex, response.etag());
                    } catch (Exception e) {
                        LOG.warn("分片 {} 第 {}/{} 次上传失败: {}", partIndex, attempt, MAX_RETRY, e.getMessage());
                        sleepBackoff(attempt);
                    }
                }
                throw new CompletionException(new RuntimeException("分片 " + partIndex + " 上传失败，已重试 " + MAX_RETRY + " 次"));
            }, executor);
            futures.add(future);
        }

        // 等待全部并发任务完成；任一分片最终失败则整体失败
        List<Part> allParts = new ArrayList<>();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Part> future : futures) {
                allParts.add(future.join());
            }
        } catch (CompletionException e) {
            // 快速失败：尽力取消尚未开始的分片任务
            futures.forEach(f -> f.cancel(true));
            throw e;
        }

        // CompleteMultipartUpload 要求分片按分片号升序排列
        allParts.sort(Comparator.comparingInt(Part::partNumber));
        return allParts.toArray(new Part[0]);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 根据文件总大小计算合理的分片大小：
     * 优先使用 5MB；若分片数会超过上限（10000），则倍增分片大小，且不超过 5GB。
     */
    private long calculatePartSize(long fileSize) {
        long partSize = MIN_PART_SIZE;
        while ((fileSize + partSize - 1) / partSize > MAX_PART_COUNT) {
            partSize *= 2;
        }
        return Math.min(partSize, MAX_PART_SIZE);
    }

    /**
     * 指数退避：第 attempt 次失败后等待 2^(attempt-1) 秒再重试。
     */
    private void sleepBackoff(int attempt) {
        try {
            TimeUnit.SECONDS.sleep(1L << (attempt - 1));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 释放线程池资源。
     */
    public void shutdown() {
        executor.shutdown();
    }

    public static void main(String[] args) {
        // 构建 MinIO 异步客户端（自定义扩展类，暴露 SDK 中 protected 的分片上传方法）
        MinioAsyncClient client = new MinioAsyncClient(MinioAsyncClient.builder()
                .endpoint(ENDPOINT)
                .credentials(AK, SK)
                .build());

        MultipartUpload multipartUpload = new MultipartUpload(client);

        String bucket = "bucket-1";
        String region = "us-east-1";
        String object = "multipart/order_detail.txt";
        String filename = "/opt/data/order_detail.txt";

        try {
            // 分片上传（自动分片 + 并发上传 + 失败重试 + 合并）；失败会自动取消并清理碎片
            ObjectWriteResponse response = multipartUpload.uploadFile(bucket, region, object, filename);
            LOG.info("上传成功 bucket:{}, object:{}, etag:{}, versionId:{}", response.bucket(), response.object(), response.etag(), response.versionId());
        } finally {
            // 释放线程池
            multipartUpload.shutdown();
        }
    }
}
