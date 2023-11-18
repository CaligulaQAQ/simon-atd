package com.simon.common.client;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import com.simon.common.model.s3.OssFileInfo;
import com.simon.common.model.s3.UploadConf;
import org.eclipse.jgit.errors.NotSupportedException;

/**
 * @Author yzy
 * @Date 2023/6/19
 */
public abstract class UploadClient {
    protected UploadConf uploadConf;

    protected String outEndpoint;
    protected String innerEndpoint;

    public UploadClient() {
    }

    public String getOutEndpoint() {
        return outEndpoint;
    }

    public void setOutEndpoint(String outEndpoint) {
        this.outEndpoint = outEndpoint;
    }

    public String getInnerEndpoint() {
        return innerEndpoint;
    }

    public void setInnerEndpoint(String innerEndpoint) {
        this.innerEndpoint = innerEndpoint;
    }

    public UploadConf getUploadConf() {
        return this.uploadConf;
    }

    public abstract void upload(String var1, InputStream var2) throws Exception;

    public abstract void upload(String var1, File var2) throws Exception;

    public abstract void delete(String var1) throws Exception;

    public abstract InputStream download(String var1) throws Exception;

    public abstract void download(String var1, String var2) throws Exception;

    public abstract String downloadUrl(String var1, Integer hours, Boolean isInner);

    public void setFilePublicDownload(String path) throws Exception {
        throw new NotSupportedException("not support!");
    }

    public abstract OssFileInfo getFileInfo(String ossPath);

    public abstract List<JSONObject> listDir(String path);

    public abstract JSONObject generatePostUploadSignature(String ossPath) throws Exception;

    public abstract void shutdown() throws Exception;

    public String replaceToInnerLink(String ossPath) {
        if (ossPath.contains(outEndpoint)) {
            return ossPath.replace(outEndpoint, innerEndpoint);
        }
        return ossPath;
    }

    public String replaceToOutLink(String ossPath) {
        if (ossPath.contains(innerEndpoint)) {
            return ossPath.replace(innerEndpoint, outEndpoint);
        }
        return ossPath;
    }

}
