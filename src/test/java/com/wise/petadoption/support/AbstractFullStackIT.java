package com.wise.petadoption.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
public abstract class AbstractFullStackIT {

    protected static final String MINIO_BUCKET = "animals-e2e";

    @ServiceConnection
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @ServiceConnection
    protected static final MongoDBContainer MONGO =
            new MongoDBContainer("mongo:7");

    protected static final MinIOContainer MINIO =
            new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z");

    static {
        POSTGRES.start();
        MONGO.start();
        MINIO.start();
    }

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.type", () -> "minio");
        registry.add("minio.url", MINIO::getS3URL);
        registry.add("minio.access-key", MINIO::getUserName);
        registry.add("minio.secret-key", MINIO::getPassword);
        registry.add("minio.bucket-name", () -> MINIO_BUCKET);
    }
}
