package com.simon.common.model.s3;

import lombok.Data;

/**
 * @Author yzy
 * @Date 2023/5/30
 */
@Data
public class OSSUploadSignature {
    private String host;
    private String ossPath;
    private String policyBase64;
    private String accessId;
    private String signature;
    private String expire;
}
