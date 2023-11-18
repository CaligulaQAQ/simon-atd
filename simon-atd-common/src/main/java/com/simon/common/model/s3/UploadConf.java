package com.simon.common.model.s3;

/**
 * @Author yzy
 * @Date 2023/6/19
 */
public class UploadConf {
    private String type;
    private String accessKeyId;
    private String secretAccessKey;
    private String endPoint;
    private String outEndpoint;
    private String bucket;

    public UploadConf() {
    }

    public UploadConf(String accessKeyId, String secretAccessKey, String endPoint, String bucket) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.endPoint = endPoint;
        this.bucket = bucket;
    }

    public UploadConf(String type, String accessKeyId, String secretAccessKey, String endPoint, String bucket) {
        this.type = type;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.endPoint = endPoint;
        this.bucket = bucket;
    }

    public UploadConf(UploadConf uploadConf) {
        this(uploadConf.getAccessKeyId(), uploadConf.getSecretAccessKey(), uploadConf.getEndPoint(),
            uploadConf.getBucket());
    }

    public UploadConf(OSSProperties ossProperties) {
        this.accessKeyId = ossProperties.getAk();
        this.secretAccessKey = ossProperties.getSk();
        this.bucket = ossProperties.getBucket();
        this.endPoint = ossProperties.getEndpoint();
        this.type = ossProperties.getType();
        this.outEndpoint = ossProperties.getOutEndpoint();
    }

    public String getAccessKeyId() {
        return this.accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return this.secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getBucket() {
        return this.bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOutEndpoint() {
        return outEndpoint;
    }

    public void setOutEndpoint(String outEndpoint) {
        this.outEndpoint = outEndpoint;
    }
}
