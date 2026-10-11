package com.example.upload;

import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.UploadPartResponse;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.InitiateMultipartUploadResult;
import io.minio.messages.ListPartsResult;
import io.minio.messages.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.bean.Constant.*;

/**
 * 功能：MinIO 分片上传（Multipart Upload）完整示例
 *      分片上传将大文件切分为多个小分片并发传输，单个分片失败仅需重传该分片，
 *      从而提供断点续传能力与传输性能优化，有效应对网络不稳定环境下的大文件传输。
 *      参考阿里云 OSS 分片上传流程，结合 MinIO SDK 实现完整生命周期：
 *      <ol>
 *          <li>初始化分片上传 CreateMultipartUpload：获取唯一 uploadId 作为任务标识</li>
 *          <li>并发上传分片 UploadPart：按分片大小切分文件，线程池并发上传，失败自动重试</li>
 *          <li>完成分片上传 CompleteMultipartUpload：按分片号升序合并为完整对象</li>
 *          <li>断点续传 ListParts：列举已上传分片，跳过已完成分片，仅重传缺失分片</li>
 *          <li>取消分片上传 AbortMultipartUpload：清理未完成碎片，避免持续占用存储</li>
 *      </ol>
 *      注意：MinIO/S3 约束——除最后一个分片外，其余分片最小 5MB、最大 5GB，分片总数最多 10000 个。
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/3 14:50
 */
public class MultipartCheckpointUpload {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartCheckpointUpload.class);

    /** 最小分片大小：5MB。除最后一个分片外，其余分片大小不得小于 5MB */
    private static final long MIN_PART_SIZE = 5L * 1024 * 1024;
    /** 最大分片大小：5GB */
    private static final long MAX_PART_SIZE = 5L * 1024 * 1024 * 1024;
    /** 最大分片数量：10000 */
    private static final int MAX_PART_COUNT = 10000;
    /** 单次 ListParts 请求返回的最大分片数量：1000 */
    private static final int MAX_LIST_PARTS = 1000;
    /** 并发上传线程数：需结合网络带宽与机器负载合理设置，过大过小都会影响整体性能 */
    private static final int CONCURRENCY = 5;
    /** 单个分片上传失败后的最大重试次数：应对网络不稳定，失败仅重传该分片 */
    private static final int MAX_RETRY = 3;

    /** 自定义扩展的 MinIO 异步客户端，用于调用 SDK 中 protected 的分片上传方法 */
    private final MinioAsyncClient minioClient;
    /** 固定大小线程池：天然限制并发分片数量 */
    private final ExecutorService executor;

    public MultipartCheckpointUpload(MinioAsyncClient minioClient) {
        this.minioClient = minioClient;
        this.executor = Executors.newFixedThreadPool(CONCURRENCY);
    }

    // ==================== 一站式分片上传（并发 + 失败重试 + 断点续传） ====================

    /**
     * 全新分片上传：内部自动初始化 uploadId。
     *
     * @param bucket   存储桶
     * @param region   区域（可选，MinIO 默认 us-east-1）
     * @param object   对象名
     * @param filename 本地文件路径
     * @return 合并完成后的对象写入响应
     */
    public ObjectWriteResponse uploadFile(String bucket, String region, String object, String filename) {
        return uploadFile(bucket, region, object, filename, null);
    }

    /**
     * 分片上传完整流程。
     *
     * @param bucket   存储桶
     * @param region   区域（可选）
     * @param object   对象名
     * @param filename 本地文件路径
     * @param uploadId 为空表示全新上传；非空表示断点续传（复用已有上传任务，仅重传缺失分片）
     * @return 合并完成后的对象写入响应
     */
    public ObjectWriteResponse uploadFile(String bucket, String region, String object, String filename, String uploadId) {

        // 准备文件
        File file = new File(filename);
        if (!file.isFile()) {
            throw new IllegalArgumentException(filename + " 不是一个有效的文件");
        }
        long fileSize = file.length();
        if (fileSize == 0) {
            throw new IllegalArgumentException("空文件不适用分片上传，请改用 putObject");
        }
        // 根据文件大小计算合理的分片大小与分片数量
        long partSize = calculatePartSize(fileSize);
        int partCount = (int) ((fileSize + partSize - 1) / partSize);

        boolean resume = uploadId != null && !uploadId.isEmpty();
        LOG.info("准备分片上传 object={}, fileSize={} 字节, partSize={} 字节, partCount={}, concurrency={}, resume={}", object, fileSize, partSize, partCount, CONCURRENCY, resume);

        // 步骤 1：初始化分片上传，获取 uploadId（断点续传时复用已有 uploadId）
        if (!resume) {
            uploadId = createMultipartUpload(bucket, region, object);
        } else {
            LOG.info("断点续传模式，复用 uploadId={}", uploadId);
        }

        try {
            // 断点续传：列举该 uploadId 下已成功上传的分片，避免重复上传
            Map<Integer, String> uploadedParts = resume ? listUploadedParts(bucket, region, object, uploadId) : new HashMap<>();

            // 步骤 2：并发上传分片（单个分片失败自动重试，仅重传失败分片）
            Part[] parts = uploadParts(bucket, region, object, file, uploadId, partSize, partCount, uploadedParts);

            // 步骤 3：完成分片上传，按分片号合并为完整对象
            ObjectWriteResponse response = completeMultipartUpload(bucket, region, object, uploadId, parts);
            LOG.info("分片上传全部完成 object={}, etag={}, versionId={}", object, response.etag(), response.versionId());
            return response;
        } catch (RuntimeException e) {
            // 上传失败：保留 uploadId 与已上传分片（碎片），后续可用同一 uploadId 断点续传；
            // 若确认不再续传，应调用 abortMultipartUpload 清理碎片，避免持续占用存储产生费用。
            LOG.error("分片上传失败 uploadId={}。可稍后用该 uploadId 断点续传，或调用 abortMultipartUpload 清理碎片", uploadId, e);
            throw new RuntimeException("分片上传失败, uploadId=" + uploadId, e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 步骤 1：初始化分片上传
     * 初始化分片上传（InitiateMultipartUpload），返回唯一的 uploadId 作为整个上传任务的标识。
     */
    public String createMultipartUpload(String bucket, String region, String object) {
        try {
            InitiateMultipartUploadResult result = minioClient
                    .createMultipartUploadAsync(bucket, region, object, null, null)
                    .get()
                    .result();
            String uploadId = result.uploadId();
            LOG.info("初始化分片上传成功，bucket:{}, object:{}, uploadId:{}", result.bucketName(), result.objectName(), uploadId);
            return uploadId;
        } catch (Exception e) {
            throw new RuntimeException("初始化分片上传失败", e);
        }
    }

    /**
     * 步骤2：上传分片
     * 并发上传所有分片。
     * 已上传分片（断点续传）直接复用其 ETag；其余分片提交到线程池并发上传，单个分片失败自动重试。
     * @param file          本地文件
     * @param uploadId      上传任务 ID
     * @param partSize      分片大小
     * @param partCount     分片总数
     * @param uploadedParts 已上传分片 partNumber -> etag（断点续传时非空）
     * @return 按分片号升序排列的分片数组，用于合并
     */
    public Part[] uploadParts(String bucket, String region, String object, File file, String uploadId, long partSize, int partCount, Map<Integer, String> uploadedParts) {
        long fileSize = file.length();
        final int total = partCount;
        List<Part> reusedParts = new ArrayList<>();
        List<CompletableFuture<Part>> futures = new ArrayList<>();
        AtomicInteger finished = new AtomicInteger(uploadedParts.size());

        for (int partNumber = 1; partNumber <= partCount; partNumber++) {
            // 断点续传：已上传成功的分片无需重传，直接复用其 ETag
            String etag = uploadedParts.get(partNumber);
            if (etag != null) {
                LOG.info("分片 {}/{} 已上传，跳过（断点续传）", partNumber, total);
                reusedParts.add(new Part(partNumber, etag));
                continue;
            }

            // 计算当前分片在文件中的起始偏移与实际大小（最后一个分片可能不足 partSize）
            final long startPos = (long) (partNumber - 1) * partSize;
            final long curPartSize = Math.min(partSize, fileSize - startPos);
            final int pn = partNumber;

            // 提交到线程池并发上传，线程池大小即最大并发数
            CompletableFuture<Part> future = CompletableFuture.supplyAsync(() -> {
                Part part = uploadPartWithRetry(bucket, region, object, file, uploadId, pn, startPos, curPartSize);
                LOG.info("分片 {}/{} 上传完成，当前进度 {}/{}", pn, total, finished.incrementAndGet(), total);
                return part;
            }, executor);
            futures.add(future);
        }

        // 等待全部并发任务完成；任一分片最终失败则整体失败
        List<Part> allParts = new ArrayList<>(reusedParts);
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

    /**
     * 上传单个分片，失败自动重试（指数退避）。
     * 关键点：每个分片使用独立的 {@link RandomAccessFile} 并 seek 到分片起始位置，保证并发上传的线程安全；
     * uploadPartAsync 会从文件当前指针位置读取 partSize 字节。
     */
    private Part uploadPartWithRetry(String bucket, String region, String object, File file, String uploadId, int partNumber, long startPos, long partSize) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "r");
                raf.seek(startPos);
                UploadPartResponse response = minioClient
                        .uploadPartAsync(bucket, region, object, raf, partSize, uploadId, partNumber, null, null)
                        .get();
                return new Part(partNumber, response.etag());
            } catch (InsufficientDataException | InternalException | InvalidKeyException | IOException
                     | NoSuchAlgorithmException | XmlParserException | InterruptedException | ExecutionException e) {
                LOG.warn("分片 {} 第 {}/{} 次上传失败: {}", partNumber, attempt, MAX_RETRY, e.getMessage());
                if (attempt >= MAX_RETRY) {
                    throw new CompletionException(
                            new RuntimeException("分片 " + partNumber + " 上传失败，已重试 " + MAX_RETRY + " 次", e));
                }
                sleepBackoff(attempt);
            } finally {
                closeQuietly(raf);
            }
        }
        // 理论上不可达
        throw new CompletionException(new RuntimeException("分片 " + partNumber + " 上传失败"));
    }

    // ==================== 断点续传：列举已上传分片 ====================

    /**
     * 断点续传核心：列举指定 uploadId 下已成功上传的分片（含分页），返回 partNumber -> etag 映射。
     */
    public Map<Integer, String> listUploadedParts(String bucket, String region, String object, String uploadId) {
        Map<Integer, String> uploadedParts = new HashMap<>();
        Integer partNumberMarker = null;
        boolean isTruncated;
        try {
            do {
                ListPartsResponse response = minioClient
                        .listPartsAsync(bucket, region, object, MAX_LIST_PARTS, partNumberMarker, uploadId, null, null)
                        .get();
                ListPartsResult result = response.result();
                for (Part part : result.partList()) {
                    uploadedParts.put(part.partNumber(), part.etag());
                }
                isTruncated = result.isTruncated();
                partNumberMarker = result.nextPartNumberMarker();
            } while (isTruncated);
        } catch (Exception e) {
            throw new RuntimeException("列举已上传分片失败, uploadId=" + uploadId, e);
        }
        LOG.info("断点续传：检测到 {} 个已上传分片 uploadId={}", uploadedParts.size(), uploadId);
        return uploadedParts;
    }

    // ==================== 步骤 3：完成分片上传 ====================

    /**
     * 完成分片上传（CompleteMultipartUpload），将所有分片按序号合并为完整对象。
     */
    public ObjectWriteResponse completeMultipartUpload(String bucket, String region, String object, String uploadId, Part[] parts) {
        try {
            ObjectWriteResponse response = minioClient
                    .completeMultipartUploadAsync(bucket, region, object, uploadId, parts, null, null)
                    .get();
            LOG.info("完成分片上传 object={}, uploadId={}, 合并分片数={}", object, uploadId, parts.length);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("完成分片上传失败, uploadId=" + uploadId, e);
        }
    }

    // ==================== 取消分片上传（清理碎片） ====================

    /**
     * 取消分片上传（AbortMultipartUpload），删除已上传的分片碎片，避免持续占用存储产生费用。
     */
    public void abortMultipartUpload(String bucket, String region, String object, String uploadId) {
        try {
            minioClient.abortMultipartUploadAsync(bucket, region, object, uploadId, null, null).get();
            LOG.info("取消分片上传成功，已清理碎片 uploadId={}", uploadId);
        } catch (InsufficientDataException | InternalException | InvalidKeyException | IOException
                 | NoSuchAlgorithmException | XmlParserException | InterruptedException | ExecutionException e) {
            LOG.error("取消分片上传失败 uploadId={}", uploadId, e);
            throw new RuntimeException("取消分片上传失败, uploadId=" + uploadId, e);
        }
    }

    // ==================== 工具方法 ====================

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
     * 静默关闭 RandomAccessFile，忽略关闭异常。
     */
    private void closeQuietly(RandomAccessFile raf) {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException ignored) {
                // 忽略关闭异常
            }
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

        MultipartCheckpointUpload multipartUpload = new MultipartCheckpointUpload(client);

        String bucket = "bucket-1";
        String region = "us-east-1";
        String object = "multipart/bigfile.zip";
        String filename = "/opt/data/bigfile.zip";

        try {
            // 场景一：全新分片上传（自动分片 + 并发上传 + 失败重试 + 合并）
            ObjectWriteResponse response = multipartUpload.uploadFile(bucket, region, object, filename);
            LOG.info("上传成功 bucket={}, object={}, etag={}, versionId={}",
                    response.bucket(), response.object(), response.etag(), response.versionId());

            // 场景二：断点续传。若上次上传中途失败，日志会打印保留的 uploadId，
            //        将其传入即可复用已上传分片，仅重传缺失分片：
            // String savedUploadId = "上次失败日志中的 uploadId";
            // multipartUpload.uploadFile(bucket, region, object, filename, savedUploadId);

            // 场景三：取消分片上传，清理未完成的碎片文件：
            // String uploadId = multipartUpload.createMultipartUpload(bucket, region, object);
            // multipartUpload.abortMultipartUpload(bucket, region, object, uploadId);
        } finally {
            // 释放线程池
            multipartUpload.shutdown();
        }
    }
}
