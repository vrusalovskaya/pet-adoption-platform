package com.wise.petadoption.shared.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(MinioStorageCondition.class)
@RequiredArgsConstructor
public class MinioBucketInitializer {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    public void init() throws Exception {

        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(properties.getBucketName()).build());

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(properties.getBucketName()).build());
        }
    }
}