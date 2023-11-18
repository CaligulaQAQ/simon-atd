package com.simon.common.model.s3;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.amazonaws.regions.Regions;
import org.apache.commons.codec.binary.Hex;

/**
 * @Author yzy
 * @Date 2023/7/6
 */
public class S3UploadPolicySignatureGenerator {

    private String  accessKeyId;
    private String  secretAccessKey;
    private String  bucketName;
    private Encoder encoder;

    public S3UploadPolicySignatureGenerator(String accessKeyId, String secretAccessKey, String bucketName) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucketName = bucketName;
        this.encoder = Base64.getEncoder();
    }

    private static String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(formatter);
    }

    public static String formatDate1(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return date.format(formatter);
    }

    public static String formatDate2(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        return date.format(formatter);
    }

    public static void main(String[] args) {
        String s = formatDate1(LocalDateTime.now());
        System.out.println(s);
    }

    public String generatePolicy(String path) {

        String policy = "{\n" +
            "  \"expiration\": \"" + formatDate1(LocalDateTime.now().plusMinutes(10)) + "\",\n" +
            "  \"conditions\": [\n" +
            "    {\"bucket\": \"" + bucketName + "\"},\n" +
            "    [\"starts-with\", \"$key\", \"" + path + "\"],\n" +
            "    {\"acl\": \"private\"},\n" +
            "    [\"content-length-range\", 0, 1048576000]\n" +
            "  ]\n" +
            "}";
        return encoder.encodeToString(policy.getBytes(StandardCharsets.UTF_8));
    }

    public String generateSignature0(String policy) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] signatureKey = getSignatureKey(secretAccessKey, formatDate(LocalDateTime.now()), policy);
        return Hex.encodeHexString(signatureKey);
    }

    private String getSigningKey(String date, String secretAccessKey)
        throws NoSuchAlgorithmException, InvalidKeyException {
        String kDate = hmacSha256("AWS4" + secretAccessKey, date);
        String kRegion = hmacSha256(kDate, Regions.US_EAST_1.getName());
        String kService = hmacSha256(kRegion, "s3");
        String kSigning = hmacSha256(kService, "aws4_request");
        return kSigning;
    }

    private String getSignature(byte[] signingKey, String canonicalRequest)
        throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] signatureBytes = hmacSHA256(signingKey, canonicalRequest);
        String signature = Hex.encodeHexString(signatureBytes);
        return signature;
    }

    private byte[] getSignatureKey(String secretKey, String dateStamp, String stringToSign)
        throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] kSecret = ("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(kSecret, dateStamp);
        byte[] kRegion = hmacSHA256(kDate, Regions.US_EAST_1.getName());
        byte[] kService = hmacSHA256(kRegion, "s3");
        byte[] kSigning = hmacSHA256(kService, "aws4_request");
        byte[] bytes = hmacSHA256(kSigning, stringToSign);
        return bytes;
    }

    private byte[] hmacSHA256(byte[] key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec signingKey = new SecretKeySpec(key, algorithm);
        mac.init(signingKey);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = mac.doFinal(dataBytes);
        return signatureBytes;
    }

    private String hmacSha256(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(result);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String getCredential() {
        return accessKeyId + "/" + formatDate(LocalDateTime.now()) + "/" + Regions.US_EAST_1.getName()
            + "/" + "s3" + "/aws4_request";
    }

}

