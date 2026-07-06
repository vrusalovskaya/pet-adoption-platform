package com.wise.petadoption.shared.storage.minio;

import com.wise.petadoption.shared.storage.exception.ImageNotFoundException;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class MinioImageStorageIT {

    @Container
    static final MinIOContainer MINIO =
            new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z");
    private static final String BUCKET = "it-bucket";
    private static MinioImageStorage storage;

    @BeforeAll
    static void setUp() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(MINIO.getS3URL())
                .credentials(MINIO.getUserName(), MINIO.getPassword())
                .build();
        client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());

        MinioProperties properties = new MinioProperties();
        properties.setUrl(MINIO.getS3URL());
        properties.setAccessKey(MINIO.getUserName());
        properties.setSecretKey(MINIO.getPassword());
        properties.setBucketName(BUCKET);

        storage = new MinioImageStorage(client, properties);
    }

    @Test
    void saveAndLoad_StoredImage_RoundTripsContentAndMetadata() throws Exception {
        byte[] content = "minio-image-bytes".getBytes(StandardCharsets.UTF_8);

        StoredImage stored = storage.save(
                new ByteArrayInputStream(content), content.length, "image/png", "rex.png");
        StoredImageStream loaded = storage.load(stored.key());

        try (loaded) {
            assertThat(loaded.contentType()).isEqualTo("image/png");
            assertThat(loaded.originalFilename()).isEqualTo("rex.png");
            assertThat(loaded.stream().readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void delete_ExistingObject_RemovesIt() throws Exception {
        byte[] content = "to-delete".getBytes(StandardCharsets.UTF_8);
        StoredImage stored = storage.save(
                new ByteArrayInputStream(content), content.length, "image/png", "del.png");

        storage.delete(stored.key());

        assertThatThrownBy(() -> storage.load(stored.key()))
                .isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    void load_UnknownKey_ThrowsImageNotFoundException() {
        assertThatThrownBy(() -> storage.load("non-existent-key"))
                .isInstanceOf(ImageNotFoundException.class);
    }
}
