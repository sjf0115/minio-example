package com.example.bean;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 功能：MergePart
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 19:36
 */
@Data
public class UploadPart {
    private String uploadId;
    private String object;
    private int partNumber;
    private MultipartFile file;
}
