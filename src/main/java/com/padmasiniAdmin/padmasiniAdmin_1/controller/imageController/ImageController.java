package com.padmasiniAdmin.padmasiniAdmin_1.controller.imageController;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final Region region;
    private final String bucketName;
    private final DefaultCredentialsProvider credentialsProvider;
    private final S3Client s3Client;

    public ImageController(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.bucket.name:trilokinnovations-test-admin}") String bucketName
    ) {
        this.region = Region.of(awsRegion);
        this.bucketName = bucketName;
        this.credentialsProvider = DefaultCredentialsProvider.create();
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    // ✅ Presigned URL
    @GetMapping("/presigned-url-image")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam String fileType,
            @RequestParam String folderPath) {

        try (S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build()) {

            String key = folderPath + "/" + System.currentTimeMillis() + "-" + fileName;

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(fileType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(objectRequest)
                    .signatureDuration(Duration.ofMinutes(30))
                    .build();

            URL uploadUrl = presigner.presignPutObject(presignRequest).url();

            Map<String, String> response = new HashMap<>();
            response.put("uploadUrl", uploadUrl.toString());
            response.put("fileUrl", "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key);

            System.out.println("✅ Presigned URL generated: " + uploadUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not generate presigned URL: " + e.getMessage()));
        }
    }

    // ✅ Direct file upload (for your frontend POST /api/image/upload)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderName") String folderName) {

        try {
            String key = folderName + "/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            String fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;

            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
