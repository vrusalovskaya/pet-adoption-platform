package com.wise.petadoption.shared.storage.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.wise.petadoption.shared.storage.exception.ImageNotFoundException;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class GridFsImageStorageIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    private static GridFsImageStorage storage;

    @BeforeAll
    static void setUp() {
        MongoClient mongoClient = MongoClients.create(MONGO.getReplicaSetUrl());
        SimpleMongoClientDatabaseFactory factory =
                new SimpleMongoClientDatabaseFactory(mongoClient, "test");
        MongoTemplate mongoTemplate = new MongoTemplate(factory);
        GridFsTemplate gridFsTemplate = new GridFsTemplate(factory, mongoTemplate.getConverter());

        storage = new GridFsImageStorage(gridFsTemplate, gridFsTemplate);
    }

    @Test
    void saveAndLoad_StoredImage_RoundTripsContent() throws Exception {
        byte[] content = "gridfs-image-bytes".getBytes(StandardCharsets.UTF_8);

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
    void delete_ExistingFile_RemovesIt() throws Exception {
        byte[] content = "to-delete".getBytes(StandardCharsets.UTF_8);
        StoredImage stored = storage.save(
                new ByteArrayInputStream(content), content.length, "image/png", "del.png");

        storage.delete(stored.key());

        assertThatThrownBy(() -> storage.load(stored.key()))
                .isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    void load_InvalidObjectId_ThrowsImageNotFoundException() {
        assertThatThrownBy(() -> storage.load("not-a-valid-object-id"))
                .isInstanceOf(ImageNotFoundException.class);
    }
}
