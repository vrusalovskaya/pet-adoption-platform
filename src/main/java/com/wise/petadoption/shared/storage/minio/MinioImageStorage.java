package com.wise.petadoption.shared.storage.minio;

import com.wise.petadoption.shared.storage.exception.ImageNotFoundException;
import com.wise.petadoption.shared.storage.exception.ImageStorageException;
import com.wise.petadoption.shared.storage.model.StorageType;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import com.wise.petadoption.shared.storage.contract.ImageStorage;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@Conditional(MinioStorageCondition.class)
@RequiredArgsConstructor
public class MinioImageStorage implements ImageStorage {

    private final MinioClient client;
    private final MinioProperties props;

    @Override
    public StoredImage save(InputStream content, long size,
                            String contentType, String originalFilename) {
        String key = UUID.randomUUID().toString();
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucketName())
                    .object(key)
                    .stream(content, size, 10L * 1024 * 1024)   // partSize 10MB
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .userMetadata(Map.of("original-filename",
                            URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)))
                    .build());
        } catch (Exception e) {
            throw new ImageStorageException("MinIO putObject failed", e);
        }
        return new StoredImage(key, contentType, originalFilename, size);
    }

    @Override
    public StoredImageStream load(String key) {
        try {
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder()
                    .bucket(props.getBucketName()).object(key).build());
            GetObjectResponse obj = client.getObject(GetObjectArgs.builder()
                    .bucket(props.getBucketName()).object(key).build());

            Set<String> filenames = stat.userMetadata().get("original-filename");

            String filename = filenames.isEmpty()
                    ? key
                    : URLDecoder.decode(
                    filenames.iterator().next(),
                    StandardCharsets.UTF_8);

            return new StoredImageStream(obj, stat.contentType(), filename, stat.size());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ImageNotFoundException(key);
            }
            throw new ImageStorageException("MinIO getObject failed", e);
        } catch (Exception e) {
            throw new ImageStorageException("MinIO getObject failed", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.getBucketName()).object(key).build());
        } catch (Exception e) {
            throw new ImageStorageException("MinIO removeObject failed", e);
        }
    }

    @Override public StorageType type() { return StorageType.MINIO; }
}