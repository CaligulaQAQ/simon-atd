package com.simon.common.model.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author yzy
 * @Date 2023/5/30
 */
@ConfigurationProperties(prefix = "studio.oss")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSSProperties {
    private String endpoint;
    private String outEndpoint;
    private String bucket;
    private String ak;
    private String sk;
    private String type;
}
