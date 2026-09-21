package com.ontheway.infra.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class R2FileUploader {
    private final S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrl;

    /** 저장된 객체의 키와 공개 URL. 키는 나중에 지울 때 쓴다. */
    public record StoredFile(String key, String url) {
    }

    public String upload(MultipartFile file) throws IOException {
        return put(file, UUID.randomUUID() + getExtension(file.getOriginalFilename())).url();
    }

    /** 확장자(점 없이)를 호출하는 쪽이 정한다. 파일 형식 검증을 끝낸 쪽이 부른다. */
    public StoredFile upload(MultipartFile file, String extension) throws IOException {
        return put(file, UUID.randomUUID() + "." + extension);
    }

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    private StoredFile put(MultipartFile file, String key) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return new StoredFile(key, publicUrl + "/" + key);
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
