package com.medilab.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs; // Added
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinIOService {

    private final MinioClient minioClient;

    // Default bucket name from config, can be overridden
    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    @Value("${minio.url-expiry-hours:168}")
    private int urlExpiryHours;

    @Value("${minio.public-endpoint}")
    private String publicEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * Upload a PDF file to MinIO
     *
     * @param fileName The name of the file
     * @param pdfBytes The PDF content as bytes
     * @return The object name in MinIO
     */
    public String uploadPdf(String bucketName, String fileName, byte[] pdfBytes) {
        try {
            // Use provided bucket or default
            String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : defaultBucketName;
            String objectName = "reports/" + fileName;

            // Ensure bucket exists
            boolean bucketExists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(targetBucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(targetBucket).build());
                log.info("Created new MinIO bucket: {}", targetBucket);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(pdfBytes), pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build());

            log.info("Uploaded PDF to MinIO bucket {}: {}", targetBucket, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading PDF to MinIO", e);
            throw new RuntimeException("Failed to upload PDF to MinIO", e);
        }
    }

    /**
     * Generate a presigned URL for accessing a file
     * Uses public endpoint to ensure URLs are accessible from browsers
     *
     * @param objectName The object name in MinIO
     * @return The presigned URL valid for configured hours
     */
    public String getPresignedUrl(String bucketName, String objectName) {
        try {
            String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : defaultBucketName;

            // Create a separate client with public endpoint for URL generation
            MinioClient publicClient = MinioClient.builder()
                    .endpoint(publicEndpoint)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1")
                    .build();

            String url = publicClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(targetBucket)
                            .object(objectName)
                            .expiry(urlExpiryHours, TimeUnit.HOURS)
                            .build());

            log.debug("Generated presigned URL for: {}", objectName);
            return url;
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Delete a file from MinIO
     *
     * @param objectName The object name in MinIO
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : defaultBucketName;

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(objectName)
                            .build());
            log.info("Deleted file from MinIO bucket {}: {}", targetBucket, objectName);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", objectName, e);
            // We don't throw exception here to avoid failing the main transaction
        }
    }
}
