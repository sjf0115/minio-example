/*
 * MinIO Java SDK for Amazon S3 Compatible Cloud Storage, (C) 2025 MinIO, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.minio;

import java.util.Objects;

public class UploadPartArgs2 extends PutObjectAPIBaseArgs {
    private String uploadId;
    private int partNumber;

    public String uploadId() {
    return uploadId;
    }

    public int partNumber() {
    return partNumber;
    }

    public static Builder builder() {
    return new Builder();
  }

    public static final class Builder extends PutObjectAPIBaseArgs.Builder<Builder, UploadPartArgs2> {
        @Override
        protected void validate(UploadPartArgs2 args) {
            super.validate(args);
            // Utils.validateNotEmptyString(args.uploadId, "upload ID");
            if (args.partNumber <= 0) {
                throw new IllegalArgumentException("valid part number must be provided");
            }
        }

        public Builder uploadId(String uploadId) {
            // Utils.validateNotEmptyString(uploadId, "upload ID");
            operations.add(args -> args.uploadId = uploadId);
            return this;
        }

        public Builder partNumber(int partNumber) {
            if (partNumber <= 0) throw new IllegalArgumentException("valid part number must be provided");
            operations.add(args -> args.partNumber = partNumber);
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadPartArgs2)) return false;
        if (!super.equals(o)) return false;
        UploadPartArgs2 that = (UploadPartArgs2) o;
        return Objects.equals(uploadId, that.uploadId) && partNumber == that.partNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uploadId, partNumber);
    }
}
