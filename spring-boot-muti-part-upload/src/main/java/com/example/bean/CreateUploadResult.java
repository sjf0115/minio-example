package com.example.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 功能：创建上传分片任务返回结果
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 20:07
 */
@Data
@Builder
public class CreateUploadResult {
    private String uploadId;
    private String bucket;
    private String object;
}
