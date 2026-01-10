package com.example.controller;

import com.example.bean.*;
import com.example.service.UploadService;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能：UploadController 示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/26 21:45
 */
@Slf4j
@RestController
@RequestMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @GetMapping(value = "/create")
    public CreateUploadResult create(@RequestParam String object) {
        return uploadService.createMultipartUpload(object);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public UploadPartResult uploadPart(@RequestParam UploadPart uploadPart) {
        return uploadService.uploadPart(uploadPart);
    }

    @PostMapping(value = "/list")
    public List<Part> listParts(@RequestParam UploadPart uploadPart) {
        return uploadService.listParts(uploadPart);
    }

    @PostMapping(value = "/merge")
    public CompleteUploadResult complete(@RequestParam UploadPart uploadPart) {
        return uploadService.completeMultipartUpload(uploadPart);
    }

    @PostMapping(value = "/abort")
    public AbortUploadResult abort(@RequestParam UploadPart uploadPart) {
        return uploadService.abortMultipartUpload(uploadPart);
    }
}
