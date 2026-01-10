package com.example.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 功能：查询分片返回结果
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 20:19
 */
@Data
@Builder
public class ListPartsResult {
    private String uploadId;
    private String bucket;
    private String object;
}
