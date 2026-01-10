package com.example.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 功能：ListPart
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 22:24
 */
@Data
@Builder
public class ListPart {
    private int partNumber;
    private String etag;
    private Long size;
}
