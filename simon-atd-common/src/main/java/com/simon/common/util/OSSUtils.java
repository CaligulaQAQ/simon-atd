package com.simon.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.CheckedInputStream;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.common.utils.CRC64;
import com.aliyun.oss.model.DownloadFileRequest;
import com.aliyun.oss.model.DownloadFileResult;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.aliyun.oss.model.UploadFileRequest;
import com.aliyun.oss.model.UploadFileResult;
import com.aliyuncs.utils.StringUtils;
import com.simon.common.model.s3.OSSProperties;
import com.simon.common.model.s3.OSSUploadSignature;
import com.simon.common.model.s3.OssFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @Author yzy
 * @Date 2023/11/13
 */
@Slf4j
public class OSSUtils {
    public static final long MB_100 = 104857600L * 10;
    public final OSS ossClient;
    private final String bucketName;
    private final String homeDirName;
    private final OSSProperties ossProperties = new OSSProperties();

    public OSSUtils(String endpoint, String bucketName, String homeDirName, String accessKeyId,
        String accessKeySecret) {
        this.bucketName = bucketName;
        this.homeDirName = homeDirName;
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossProperties.setEndpoint(endpoint);
        ossProperties.setAk(accessKeyId);
    }

    public static boolean downloadToLocal(String remoteOssPath, String localPath, OSSProperties ossProperties) {
        OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAk(),
            ossProperties.getSk());
        ObjectMetadata object = ossClient.getObject(new GetObjectRequest(ossProperties.getBucket(), remoteOssPath),
            new File(localPath));
        return true;
    }

    /**
     * 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
     *
     * @param ossPath
     * @param hours              过期时间（小时）
     * @param contentDisposition 指示接收方如何处理消息内容：inline显示，attachment下载
     * @param contentType        设置ContentType
     * @return
     */
    public URL generatePreSignedUrl(String ossPath, Integer hours, String contentDisposition, String contentType) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        if (!StringUtils.isEmpty(ossPath) && ossClient.doesObjectExist(bucketName, ossPath)) {
            int h = null == hours ? 3 : hours;
            Date expiration = new Date(System.currentTimeMillis() + h * 3600L * 1000);
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
                ossPath);
            generatePresignedUrlRequest.setExpiration(expiration);
            ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
            if (!StringUtils.isEmpty(contentDisposition)) {
                overrides.setContentDisposition(contentDisposition);
            }
            if (!StringUtils.isEmpty(contentType)) {
                overrides.setContentType(contentType);
            }
            generatePresignedUrlRequest.setResponseHeaders(overrides);
            return ossClient.generatePresignedUrl(generatePresignedUrlRequest);
        } else {
            log.warn("generatePreSignedUrl failed: illegal ossPath: {}", ossPath);
            return null;
        }
    }

    /**
     * 使用签名URL上传文件。
     *
     * @param ossPath
     * @param hours
     * @return
     */
    public URL generatePreSignedUploadUrl(String ossPath, int hours) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, ossPath, HttpMethod.PUT);
        Date expiration = new Date(System.currentTimeMillis() + hours * 3600L * 1000);
        request.setExpiration(expiration);
        return ossClient.generatePresignedUrl(request);
    }

    /**
     * 判断文件是否存在
     *
     * @param ossPath
     * @return
     */
    public boolean doesFileExist(String ossPath) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        return ossClient.doesObjectExist(bucketName, ossPath);
    }

    /**
     * 判断文件夹是否存在
     *
     * @param ossPath
     * @return
     */
    public boolean doesDirExist(String ossPath) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        if (!ossPath.endsWith("/")) {
            ossPath += "/";
        }
        return ossClient.doesObjectExist(bucketName, ossPath);
    }

    /**
     * 获取文件大小，单位：字节
     *
     * @param ossFilePath
     * @return
     */
    public long getFileSize(String ossFilePath) {
        String ossPath = new File(homeDirName, ossFilePath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        ossPath = ossFilePath.endsWith("/") ? ossPath + "/" : ossPath;
        if (ossClient.doesObjectExist(bucketName, ossPath)) {
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, ossPath);
            return metadata.getContentLength();
        } else {
            log.warn("oss file not exist: {}", ossPath);
            return -1;
        }
    }

    public long getDirSize(String ossFilePath) {
        String ossPath = new File(homeDirName, ossFilePath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        ossPath = ossFilePath.endsWith("/") ? ossPath : ossPath + "/";
        return getDirSizeRecursion(ossPath);
    }

    public long getDirSizeRecursion(String ossPath) {
        AtomicLong size = new AtomicLong();
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
        // 设置正斜线（/）为文件夹的分隔符。
        listObjectsRequest.setDelimiter("/");
        listObjectsRequest.setPrefix(ossPath);
        ObjectListing listObjects = ossClient.listObjects(listObjectsRequest);
        // 遍历所有文件
        listObjects.getObjectSummaries().forEach(ossObjectSummary -> {
            size.addAndGet(ossObjectSummary.getSize());
        });
        // 遍历所有子文件夹
        listObjects.getCommonPrefixes().forEach(prefix -> {
            size.addAndGet(getDirSizeRecursion(prefix));
        });
        return size.get();
    }

    public void uploadFile(File cf, String ossPath) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        breakpointUpload(ossPath, cf, 3);
    }

    public void upload(File localFile, String ossDir, Set<String> uploadedOssFiles) {
        File ossFile = new File(ossDir, localFile.getName());
        if (localFile.isFile()) {
            uploadFile(localFile, ossFile.getPath());
            if (null != uploadedOssFiles) {
                uploadedOssFiles.add(ossFile.getPath());
            }
        } else {
            File[] files = localFile.listFiles();
            if (null != files) {
                for (File f : files) {
                    upload(f, ossFile.getPath(), uploadedOssFiles);
                }
            }
        }
    }

    public void deleteFile(String ossPath) {
        boolean isDir = ossPath.endsWith("/");
        ossPath = new File(homeDirName, ossPath).getPath() + (isDir ? "/" : "");
        ossPath = toLinuxLikeFilePath(ossPath);
        if (ossClient.doesObjectExist(bucketName, ossPath)) {
            ossClient.deleteObject(bucketName, ossPath);
        }
    }

    /**
     * list oss dir files only, sub dir are not included
     *
     * @param ossPath
     * @return
     */
    public List<OssFileInfo> listDir(String ossPath, boolean onlyFiles) {
        return listDir(ossPath, onlyFiles, false);
    }

    public List<OssFileInfo> listDir(String ossPath, boolean onlyFiles, boolean incSubDir) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
        if (!incSubDir) {
            listObjectsRequest.setDelimiter("/");
        }
        if (!ossPath.endsWith("/")) {
            ossPath += "/";
        }
        listObjectsRequest.setPrefix(ossPath);
        listObjectsRequest.setMaxKeys(1000);

        ObjectListing listing;

        String nextMarker = null;
        List<OssFileInfo> fileInfos = new LinkedList<>();
        do {
            listObjectsRequest.setMarker(nextMarker);
            listing = ossClient.listObjects(listObjectsRequest);
            if (!onlyFiles) {
                fileInfos.addAll(listing.getCommonPrefixes().stream()
                    .map(prefix -> {
                        OssFileInfo fileInfo = new OssFileInfo();
                        fileInfo.setDirFile(true);
                        fileInfo.setKey(prefix);
                        fileInfo.setFilename(new File(prefix).getName());
                        return fileInfo;
                    }).collect(Collectors.toList()));
            }
            fileInfos.addAll(listing.getObjectSummaries().stream()
                .map(s -> {
                    OssFileInfo fileInfo = new OssFileInfo();
                    fileInfo.setKey(s.getKey());
                    fileInfo.setType(s.getType());
                    fileInfo.setLastModified(s.getLastModified());
                    fileInfo.setFileSize(s.getSize());
                    fileInfo.setDirFile(false);
                    File f = new File(s.getKey());
                    fileInfo.setFilename(f.getName());
                    return fileInfo;
                }).filter(i -> !i.getKey().endsWith("/")).collect(Collectors.toList()));
            nextMarker = listing.getNextMarker();
        } while (listing.isTruncated());

        return fileInfos;
    }

    public void mkDir(String ossDirPath) {
        String ossPath = new File(homeDirName, ossDirPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);

        if (!ossPath.endsWith("/")) {
            ossPath += "/";
        }
        ossClient.putObject(bucketName, ossPath, new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
    }

    public void cpFileTo(String fromPath, String toPath) {
        String ossFromPath = new File(homeDirName, fromPath).getPath();
        String ossToPath = new File(homeDirName, toPath).getPath();
        ossFromPath = toLinuxLikeFilePath(ossFromPath);
        ossToPath = toLinuxLikeFilePath(ossToPath);

        ossClient.copyObject(bucketName, ossFromPath, bucketName, ossToPath);
    }

    public OSSUploadSignature generatePostOSSUploadSignature(String ossPath, boolean asDir, Integer hours,
        Long maxFileSize) {
        Map<String, String> ret = generatePostSignature(ossPath, asDir, hours, maxFileSize);

        OSSUploadSignature signature = new OSSUploadSignature();
        signature.setSignature(ret.get("signature"));
        signature.setAccessId(ret.get("accessId"));
        signature.setPolicyBase64(ret.get("policy"));
        signature.setOssPath(ret.get("ossPath"));
        signature.setHost(ret.get("host"));
        signature.setExpire(ret.get("expire"));

        return signature;
    }

    public Map<String, String> generatePostSignature(String ossPath, boolean asDir, Integer hours, Long maxFileSize) {
        ossPath = new File(homeDirName, ossPath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        try {
            maxFileSize = null == maxFileSize ? MB_100 : maxFileSize;
            int h = null == hours ? 3 : hours;
            Date expiration = new Date(System.currentTimeMillis() + h * 3600L * 1000);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, maxFileSize);
            if (asDir) {
                policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossPath);
            } else {
                policyConds.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, ossPath);
            }

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<>();
            respMap.put("accessId", ossProperties.getAk());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("ossPath", ossPath);
            respMap.put("host", "//" + bucketName + "." + ossProperties.getEndpoint());
            respMap.put("expire", String.valueOf(expiration.getTime() / 1000));
            return respMap;
        } catch (Exception e) {
            log.error("Exception happened", e);
            return null;
        }
    }

    public OssFileInfo getFileInfo(String ossFilePath) {
        String ossPath = new File(homeDirName, ossFilePath).getPath();
        ossPath = toLinuxLikeFilePath(ossPath);
        ossPath = ossFilePath.endsWith("/") ? ossPath + "/" : ossPath;
        if (ossClient.doesObjectExist(bucketName, ossPath)) {
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, ossPath);
            OssFileInfo info = new OssFileInfo();
            info.setKey(ossPath);
            info.setFilename(new File(ossPath).getName());
            info.setDirFile(ossPath.endsWith("/"));
            info.setFileSize(metadata.getContentLength());
            info.setLastModified(metadata.getLastModified());
            info.setType(metadata.getContentType());
            info.setUserMetadata(metadata.getUserMetadata());
            return info;
        } else {
            log.warn("oss file not exist: {}", ossPath);
            return null;
        }
    }

    private UploadFileResult breakpointUpload(String remoteOssPath, File localFile, int retry) {
        return RetryUtils.retry(() -> {
            UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, remoteOssPath);
            uploadFileRequest.setUploadFile(localFile.getAbsolutePath());
            uploadFileRequest.setTaskNum(3);
            uploadFileRequest.setEnableCheckpoint(true);
            try {
                return ossClient.uploadFile(uploadFileRequest);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }, retry, "Breakpoint Upload file failed: " + localFile.getAbsolutePath());
    }

    private ObjectMetadata breakpointDownload(String remoteOssPath, File localFile, int retry) {
        return RetryUtils.retry(() -> {
            DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, remoteOssPath);
            downloadFileRequest.setDownloadFile(localFile.getAbsolutePath());
            downloadFileRequest.setTaskNum(3);
            downloadFileRequest.setEnableCheckpoint(true);
            try {
                DownloadFileResult downloadRes = ossClient.downloadFile(downloadFileRequest);
                if (!validateFile(localFile, downloadRes.getObjectMetadata())) {
                    log.warn("download file {} content not match", localFile.getName());
                    FileUtils.deleteQuietly(localFile);
                    throw new RuntimeException("download file " + localFile.getName() + " md5 not match");
                }
                return downloadRes.getObjectMetadata();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }, retry, "Breakpoint Download file failed: " + localFile.getAbsolutePath());
    }

    public void download(String objectName, File localFile) throws Exception {
        File dir = localFile.getParentFile();
        if (!dir.exists()) {dir.mkdirs();}
        ossClient.getObject(new GetObjectRequest(bucketName, objectName), localFile);
    }

    private boolean validateFile(File localFile, ObjectMetadata metadata) {
        boolean valid = true;
        Long serverCrc = metadata.getServerCRC();
        if (localFile.isFile()) {
            if (null != serverCrc) {
                try (CheckedInputStream in = new CheckedInputStream(new FileInputStream(localFile), new CRC64())) {
                    byte[] buffer = new byte[8192];
                    while (in.read(buffer, 0, buffer.length) >= 0) {}
                    long localCrc = in.getChecksum().getValue();
                    valid = serverCrc.equals(localCrc);
                } catch (Exception e) {
                    valid = false;
                }
            } else {
                log.warn("content crc is empty, skip validate file crc: {}", localFile.getName());
                valid = validateFileMd5(localFile, metadata.getContentMD5());
            }
        }
        return valid;
    }

    private boolean validateFileMd5(File localFile, String contentMd5) {
        boolean valid = true;
        if (localFile.isFile()) {
            if (!StringUtils.isEmpty(contentMd5)) {
                try {
                    String fileContentMd5 = calcFileMd5(localFile);
                    valid = fileContentMd5.equalsIgnoreCase(contentMd5);
                } catch (Exception e) {
                    valid = false;
                }
            } else {
                log.warn("content md5 is empty, skip validate file md5: {}", localFile.getName());
                valid = true;
            }
        }
        return valid;
    }

    private String calcFileMd5(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] myChecksum = DigestUtils.md5(in);
            return BinaryUtil.toBase64String(myChecksum);
        }
    }

    private String toLinuxLikeFilePath(String filePath) {
        return filePath.replaceAll("\\\\", "/");
    }
}


