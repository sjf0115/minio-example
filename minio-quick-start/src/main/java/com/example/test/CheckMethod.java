package com.example.test;

import io.minio.MinioClient;

/**
 * 功能：示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/3 16:43
 */
public class CheckMethod {
    public static void main(String[] args) {
        try {
            // 检查 MinIO 客户端版本
            Package pkg = MinioClient.class.getPackage();
            System.out.println("MinIO SDK 版本: " + pkg.getImplementationVersion());
            System.out.println("=====================================");

            // 列出可能的方法
            System.out.println("可用的多部分上传方法:");
            for (java.lang.reflect.Method method : MinioClient.class.getMethods()) {
                if (method.getName().toLowerCase().contains("multipart") ||
                        method.getName().toLowerCase().contains("object")) {
                    System.out.println("  - " + method.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
