package com.simon.common.client;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.simon.common.model.s3.OSSUploadSignature;
import com.simon.common.model.s3.OssFileInfo;
import com.simon.common.model.s3.UploadConf;
import com.simon.common.util.OSSUtils;
import org.eclipse.jgit.errors.NotSupportedException;

/**
 * @Author yzy
 * @Date 2023/7/27
 */
public class OSSClient extends UploadClient {

    public OSSUtils ossUtils;

    public OSSClient(UploadConf uploadConf) {
        ossUtils = new OSSUtils(uploadConf.getEndPoint(), uploadConf.getBucket(), null,
            uploadConf.getAccessKeyId(), uploadConf.getSecretAccessKey());
        outEndpoint = uploadConf.getOutEndpoint();
        innerEndpoint = uploadConf.getEndPoint();
    }

    @Override
    public void upload(String var1, InputStream var2) throws Exception {
        throw new NotSupportedException("dont support!");
    }

    @Override
    public void upload(String var1, File var2) throws Exception {
        ossUtils.uploadFile(var2, var1);
    }

    @Override
    public void delete(String var1) throws Exception {
        ossUtils.deleteFile(var1);
    }

    @Override
    public InputStream download(String var1) throws Exception {
        throw new NotSupportedException("dont support!");
    }

    @Override
    public void download(String var1, String var2) throws Exception {
        ossUtils.download(var1, new File(var2));
    }

    @Override
    public String downloadUrl(String var1, Integer hours, Boolean isInner) {
        URL url = ossUtils.generatePreSignedUrl(var1, hours, null, null);
        if (isInner) {
            return replaceToInnerLink(url.toString());
        } else {
            return replaceToOutLink(url.toString());
        }
    }

    @Override
    public OssFileInfo getFileInfo(String ossPath) {
        OssFileInfo fileInfo = ossUtils.getFileInfo(ossPath);
        return fileInfo;
    }

    @Override
    public List<JSONObject> listDir(String path) {
        List<OssFileInfo> ossFileInfos = ossUtils.listDir(path, true);
        List<JSONObject> jsonObjects = JSON.parseArray(JSON.toJSONString(ossFileInfos), JSONObject.class);
        return jsonObjects;
    }

    @Override
    public JSONObject generatePostUploadSignature(String ossPath) throws Exception {
        OSSUploadSignature ossUploadSignature = ossUtils.generatePostOSSUploadSignature(ossPath, false, 1,
            OSSUtils.MB_100);
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(ossUploadSignature));
        return jsonObject;
    }

    @Override
    public void shutdown() throws Exception {
        ossUtils.ossClient.shutdown();
    }
}
