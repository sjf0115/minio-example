package com.example.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 功能：Statement
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/30 23:10
 */
public class Statement {
    @SerializedName("Action")
    private List<String> action;
    @SerializedName("Effect")
    private String effect;
    @SerializedName("Principal")
    private String principal;
    @SerializedName("Resource")
    private String resource;

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
