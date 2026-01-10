package com.example.minio;

import io.minio.ObjectArgs;

import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 功能：UploadPartArgs
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/1/10 16:38
 */
public class UploadPartArgs extends ObjectArgs {
    protected String uploadId;  // 上传id
    protected int partNumber;   // 分片编号,从1开始
    protected Object partData;  // 分片数据，示例：partData must be InputStream, RandomAccessFile, byte[] or String
    protected long partSize;    // 分片数据长度

    public String uploadId() {
        return uploadId;
    }

    public int partNumber() {
        return partNumber;
    }

    public Object partData() {
        return partData;
    }

    public long partSize() {
        return partSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ObjectArgs.Builder<Builder, UploadPartArgs> {
        @Override
        protected void validate(UploadPartArgs args) {
            super.validate(args);
            validateUploadId(args.uploadId);
            validatePartData(args.partData);
            validatePartNumber(args.partNumber);
            validatePartSize(args.partSize);
        }

        private void validateUploadId(String uploadId) {
            validateNotEmptyString(uploadId, "uploadId");
        }

        private void validatePartNumber(int partNumber) {
            if (partNumber < 1) {
                throw new IllegalArgumentException("partNumber must be greater than 0, but was: " + partNumber);
            }
        }

        private void validatePartSize(long partSize) {
            if (partSize < 1) {
                throw new IllegalArgumentException("partSize must be greater than 0, but was: " + partSize);
            }
        }

        private void validatePartData(Object partData) {
            if (!(partData instanceof InputStream
                    || partData instanceof RandomAccessFile
                    || partData instanceof byte[]
                    || partData instanceof CharSequence)) {
                throw new IllegalArgumentException(
                        "data must be InputStream, RandomAccessFile, byte[] or String");
            }
        }

        public Builder uploadId(String uploadId) {
            validateUploadId(uploadId);
            operations.add(args -> args.uploadId = uploadId);
            return this;
        }

        public Builder partNumber(int partNumber) {
            validatePartNumber(partNumber);
            operations.add(args -> args.partNumber = partNumber);
            return this;
        }

        public Builder partData(InputStream stream) {
            validatePartData(stream);
            operations.add(args -> args.partData = stream);
            return this;
        }

        public Builder partData(RandomAccessFile raf) {
            validatePartData(raf);
            operations.add(args -> args.partData = raf);
            return this;
        }

        public Builder partSize(long partSize) {
            validatePartSize(partSize);
            operations.add(args -> args.partSize = partSize);
            return this;
        }
    }
}
