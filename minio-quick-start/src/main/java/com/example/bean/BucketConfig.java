package com.example.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 功能：BucketConfig
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:16
 */
public class BucketConfig {
    @SerializedName("Statement")
    private List<Statement> statement;
    @SerializedName("Version")
    private String version;

    public List<Statement> getStatement() {
        return statement;
    }

    public void setStatement(List<Statement> statement) {
        this.statement = statement;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
