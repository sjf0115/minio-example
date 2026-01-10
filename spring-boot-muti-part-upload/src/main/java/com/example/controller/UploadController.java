package com.example.controller;

import com.example.bean.*;
import com.example.service.UploadService;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public UploadPartResult uploadPart(@RequestPart("file") MultipartFile file, @RequestParam String uploadId, @RequestParam String object, @RequestParam Integer partNumber) {
        UploadPart uploadPart = UploadPart.builder()
                .file(file)
                .uploadId(uploadId)
                .object(object)
                .partNumber(partNumber)
                .build();
        return uploadService.uploadPart(uploadPart);
    }

    @PostMapping(value = "/list")
    public ListPartsResult listParts(@RequestBody UploadPart uploadPart) {
        return uploadService.listParts(uploadPart);
    }

    @PostMapping(value = "/merge")
    public CompleteUploadResult complete(@RequestBody UploadPart uploadPart) {
        return uploadService.completeMultipartUpload(uploadPart);
    }

    @PostMapping(value = "/abort")
    public AbortUploadResult abort(@RequestBody UploadPart uploadPart) {
        return uploadService.abortMultipartUpload(uploadPart);
    }
}
