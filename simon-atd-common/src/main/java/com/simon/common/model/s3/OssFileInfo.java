package com.simon.common.model.s3;

import java.util.Date;
import java.util.Map;

import lombok.Data;

@Data
public class OssFileInfo {
    private String              key;
    private String              filename;
    private Long                fileSize;
    private Date                lastModified;
    private String              type;
    private Boolean             dirFile;
    private Map<String, String> userMetadata;
}
