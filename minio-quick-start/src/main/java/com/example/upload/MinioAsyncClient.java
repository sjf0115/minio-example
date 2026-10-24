package com.example.upload;

import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Part;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 功能：自定义扩展 Minio Client
 *      扩展 MinioAsyncClient 主要为了解决部分分片上传方法为 protected 无法调用的问题。
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/3 17:42
 */
public class MinioAsyncClient extends io.minio.MinioAsyncClient {
    /**
     * 包装一个已构建好的 SDK 异步客户端，复用其连接与凭证配置
     * 用途：以继承方式为同包实现类透出 SDK 中 protected 的分片上传方法
     * @param client 已配置好 endpoint 与凭证的 io.minio.MinioAsyncClient 实例
     */
    protected MinioAsyncClient(io.minio.MinioAsyncClient client) {
        super(client);
    }

    /**
     * 初始化分片上传，返回后续所有操作所需的 uploadId
     * 用途：分片上传生命周期第一步，向服务端登记一次上传任务并领取唯一 uploadId
     * @param bucketName       存储桶
     * @param region           区域(可选)
     * @param objectName       对象名
     * @param headers          消息头(可选)
     * @param extraQueryParams 额外查询参数(可选)
     */
    @Override
    public CompletableFuture<CreateMultipartUploadResponse> createMultipartUploadAsync(String bucketName, String region, String objectName, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.createMultipartUploadAsync(bucketName, region, objectName, headers, extraQueryParams);
    }

    /**
     * 上传单个分片，返回该分片的 ETag
     * 用途：分片上传生命周期第二步；并发场景下每个分片应使用独立的 RandomAccessFile 并各自 seek 到起始偏移
     * @param bucketName       存储桶
     * @param region           区域(可选)
     * @param objectName       对象名
     * @param data             分片数据(RandomAccessFile 或 InputStream)，SDK 从当前指针读取 length 字节
     * @param length           分片数据长度
     * @param uploadId         上传ID
     * @param partNumber       分片编号(从 1 开始)
     * @param extraHeaders     额外消息头(可选)
     * @param extraQueryParams 额外查询参数(可选)
     */
    @Override
    protected CompletableFuture<UploadPartResponse> uploadPartAsync(String bucketName, String region, String objectName, Object data, long length, String uploadId, int partNumber, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.uploadPartAsync(bucketName, region, objectName, data, length, uploadId, partNumber, extraHeaders, extraQueryParams);
    }

    /**
     * 完成分片上传，将各分片合并为完整对象
     * 用途：分片上传生命周期最后一步；parts 须按 partNumber 升序且携带各分片 ETag，否则合并失败
     * @param bucketName       存储桶
     * @param region           区域(可选)
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param parts            已上传分片列表(须按 partNumber 升序，含 ETag)
     * @param extraHeaders     额外消息头(可选)
     * @param extraQueryParams 额外查询参数(可选)
     */
    @Override
    public CompletableFuture<ObjectWriteResponse> completeMultipartUploadAsync(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.completeMultipartUploadAsync(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }

    /**
     * 终止分片上传，丢弃该 uploadId 已上传的全部分片
     * 用途：上传失败或放弃续传时清理碎片，避免未完成分片长期占用存储产生费用
     * @param bucketName       存储桶
     * @param region           区域(可选)
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param extraHeaders     额外消息头(可选)
     * @param extraQueryParams 额外查询参数(可选)
     */
    @Override
    protected CompletableFuture<AbortMultipartUploadResponse> abortMultipartUploadAsync(String bucketName, String region, String objectName, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.abortMultipartUploadAsync(bucketName, region, objectName, uploadId, extraHeaders, extraQueryParams);
    }

    /**
     * 列举指定 uploadId 下所有已经上传成功的分片
     * 用途：断点续传时查出已成功的 partNumber 与 ETag，以便跳过已传分片、仅补传缺失分片；合并所需的分片 ETag 也由此获得
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param maxParts         单次返回的最大分片数(可选，上限 1000)
     * @param partNumberMarker 分片号标记，仅返回编号大于它的分片(可选，用于分页)
     * @param uploadId         上传ID
     * @param extraHeaders     额外消息头(可选)
     * @param extraQueryParams 额外查询参数(可选)
     */
    @Override
    public CompletableFuture<ListPartsResponse> listPartsAsync(String bucketName, String region, String objectName, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.listPartsAsync(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }

    /**
     * 列举存储桶下所有进行中的分片上传任务：即已经初始化但尚未完成或者尚未被中止的分片上传任务
     * 用途：运维清理未完成的孤儿碎片(配合 Abort)，或在 uploadId 丢失时按前缀找回候选任务；注意它只到任务级，不返回分片 ETag
     * @param bucketName       存储桶
     * @param region           区域(可选)
     * @param delimiter        分隔符(可选)
     * @param encodingType     编码类型(可选)
     * @param keyMarker        对象键标记，从该键之后开始列举(可选，用于分页)
     * @param maxUploads       单次返回的最大上传任务数(可选)
     * @param prefix           对象键前缀过滤(可选)
     * @param uploadIdMarker   上传ID标记，从该 uploadId 之后开始列举(可选，用于分页)
     * @param extraHeaders     额外消息头(可选)
     * @param extraQueryParams 额外查询参数(可选)
     */
    @Override
    public CompletableFuture<ListMultipartUploadsResponse> listMultipartUploadsAsync(String bucketName, String region, String delimiter, String encodingType, String keyMarker, Integer maxUploads, String prefix, String uploadIdMarker, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.listMultipartUploadsAsync(bucketName, region, delimiter, encodingType, keyMarker, maxUploads, prefix, uploadIdMarker, extraHeaders, extraQueryParams);
    }

    /**
     * 计算分片数量：根据组合源信息推算合适的分片数
     * 用途：供服务端组合对象(ComposeObject)内部推算分片，与分片上传生命周期无关，一般无需直接调用
     * @param sources 组合源(ComposeSource)列表
     */
    @Override
    protected CompletableFuture<Integer> calculatePartCountAsync(List<ComposeSource> sources) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.calculatePartCountAsync(sources);
    }
}
