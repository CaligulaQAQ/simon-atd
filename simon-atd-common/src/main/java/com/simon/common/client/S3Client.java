package com.simon.common.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.simon.common.model.s3.OssFileInfo;
import com.simon.common.model.s3.S3UploadPolicySignatureGenerator;
import com.simon.common.model.s3.UploadConf;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @Author yzy
 * @Date 2023/1/6
 */
@Slf4j
public class S3Client extends UploadClient {
    private final String IP_PORT_REGEX
        = "^((1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\."
        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])(\\:"
        + "(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|([1-5]\\d{0,4}|\\d{0,4})))?)$";
    private AmazonS3                         s3Client;
    private AWSCredentials                   awsCredentials;
    private S3UploadPolicySignatureGenerator generator;

    public S3Client(UploadConf uploadConf) {
        System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        generator = new S3UploadPolicySignatureGenerator(uploadConf.getAccessKeyId(),
            uploadConf.getSecretAccessKey(),
            uploadConf.getBucket());
        super.uploadConf = uploadConf;
        awsCredentials = new BasicAWSCredentials(uploadConf.getAccessKeyId(),
            uploadConf.getSecretAccessKey());
        this.s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withClientConfiguration(clientConfig)
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(
                new EndpointConfiguration(uploadConf.getEndPoint(), Regions.US_EAST_1.getName())).build();
        outEndpoint = uploadConf.getOutEndpoint();
        innerEndpoint = uploadConf.getEndPoint();
    }

    @Override
    public void upload(String path, InputStream inputStream) throws Exception {
        this.s3Client.putObject(this.uploadConf.getBucket(), path, inputStream, null);
    }

    @Override
    public void upload(String path, File file) throws Exception {
        this.s3Client.putObject(this.uploadConf.getBucket(), path, file);
    }

    @Override
    public void delete(String path) throws Exception {
        this.s3Client.deleteObject(this.uploadConf.getBucket(), path);
    }

    @Override
    public InputStream download(String path) throws Exception {
        S3Object object = this.s3Client.getObject(this.uploadConf.getBucket(), path);
        return object.getObjectContent();
    }

    @Override
    public void download(String var1, String var2) throws Exception {
        S3Object s3Object = this.s3Client.getObject(this.uploadConf.getBucket(), var1);
        BufferedInputStream bis = new BufferedInputStream(s3Object.getObjectContent());
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(var2)));
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        // 关闭流
        bis.close();
        bos.close();
    }

    @Override
    public String downloadUrl(String path, Integer hours, Boolean isInner) {
        // 获取当前时间
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, hours);
        Date afterOneMonth = calendar.getTime();
        URL url = this.s3Client.generatePresignedUrl(this.uploadConf.getBucket(), path, afterOneMonth);
        if (isInner) {
            return replaceToInnerLink(url.toString());
        } else {
            return replaceToOutLink(url.toString());
        }
    }

    @Override
    public OssFileInfo getFileInfo(String ossPath) {
        ossPath = ossPath.endsWith("/") ? ossPath + "/" : ossPath;
        if (this.s3Client.doesObjectExist(this.uploadConf.getBucket(), ossPath)) {
            ObjectMetadata metadata = this.s3Client.getObjectMetadata(this.uploadConf.getBucket(), ossPath);
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
            log.warn("s3 file not exist: {}", ossPath);
            return null;
        }
    }

    @Override
    public JSONObject generatePostUploadSignature(String ossPath) throws Exception {
        String policy = generator.generatePolicy(ossPath);
        String signature = generator.generateSignature0(policy);
        Pattern pattern = Pattern.compile(IP_PORT_REGEX);
        String url;
        //if (pattern.matcher(uploadConf.getEndPoint()).matches()) {
        url = String.format("%s/%s", uploadConf.getEndPoint(), uploadConf.getBucket());
        //} else {
        //    url = String.format("%s.%s", uploadConf.getBucket(), uploadConf.getEndPoint());
        //}
        JSONObject jsonObject = new JSONObject() {{
            this.put("key", ossPath);
            this.put("x-amz-signature", signature);
            this.put("X-Amz-Algorithm", "AWS4-HMAC-SHA256");
            this.put("success_action_status", "200");
            this.put("policy", policy);
            this.put("X-Amz-Credential", generator.getCredential());
            this.put("x-amz-date", S3UploadPolicySignatureGenerator.formatDate2(LocalDateTime.now()));
            this.put("acl", "private");
            this.put("address", url);
        }};
        return jsonObject;
    }

    public void uploadFile(String objectKey, File file, String fileMd5) throws Exception {

        String policy = generator.generatePolicy(objectKey);
        String signature = generator.generateSignature0(policy);
        //String url = String.format("http://%s/%s", uploadConf.getEndPoint(), uploadConf.getBucket());
        String url = "http://opt-mdp-daily.oss-cn-beijing.aliyuncs.com";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(url);
        FormBodyPart build = FormBodyPartBuilder.create("file", new FileBody(file, ContentType.DEFAULT_BINARY)).build();
        FormBodyPart build2 = FormBodyPartBuilder.create("x-amz-signature",
                new StringBody(signature, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)))
            .build();
        FormBodyPart build3 = FormBodyPartBuilder.create("X-Amz-Algorithm",
            new StringBody("AWS4-HMAC-SHA256", ContentType.TEXT_PLAIN.withCharset(StandardCharsets
                .UTF_8))).build();
        FormBodyPart build4 = FormBodyPartBuilder.create("key",
                new StringBody(objectKey, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)))
            .build();
        FormBodyPart build5 = FormBodyPartBuilder.create("success_action_status",
            new StringBody("200", ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8))).build();
        FormBodyPart build6 = FormBodyPartBuilder.create("policy",
            new StringBody(policy, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8))).build();
        FormBodyPart build7 = FormBodyPartBuilder.create("X-Amz-Credential",
                new StringBody(generator.getCredential(), ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)))
            .build();
        FormBodyPart build8 = FormBodyPartBuilder.create("x-amz-date",
            new StringBody(S3UploadPolicySignatureGenerator.formatDate2(LocalDateTime.now()),
                ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8))).build();
        FormBodyPart build9 = FormBodyPartBuilder.create("acl",
                new StringBody("private", ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)))
            .build();
        HashMap<String, String> hashMap = new HashMap<String, String>() {{
            this.put("file", "");
            this.put("x-amz-signature", signature);
            this.put("X-Amz-Algorithm", "AWS4-HMAC-SHA256");
            this.put("key", objectKey);
            this.put("success_action_status", "200");
            this.put("policy", policy);
            this.put("X-Amz-Credential", generator.getCredential());
            this.put("x-amz-date", S3UploadPolicySignatureGenerator.formatDate2(LocalDateTime.now()));
            this.put("acl", "private");
        }};
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addPart(build4)
            .addPart(build2)
            .addPart(build3)
            .addPart(build5)
            .addPart(build6)
            .addPart(build7)
            .addPart(build8)
            .addPart(build9)
            .addPart(build);
        HttpEntity multipart = entityBuilder.build();
        postRequest.setEntity(multipart);
        HttpResponse response = httpClient.execute(postRequest);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            // 请求成功
            System.out.println("上传成功");
        } else {
            // 请求失败
            System.out.println("上传失败");
        }
        httpClient.close();
    }

    @Override
    public void shutdown() throws Exception {
        this.s3Client.shutdown();
    }

    public byte[] convert(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }

    @Override
    public List<JSONObject> listDir(String path) {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
            .withBucketName(this.uploadConf.getBucket())
            .withPrefix(path)
            .withDelimiter("/");
        List<S3ObjectSummary> objectSummaries = this.s3Client.listObjectsV2(listObjectsV2Request).getObjectSummaries();
        return JSONObject.parseArray(JSON.toJSONString(objectSummaries), JSONObject.class);
    }

}
