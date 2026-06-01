package com.wise.petadoption.shared.storage.mongo;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.wise.petadoption.shared.storage.exception.ImageNotFoundException;
import com.wise.petadoption.shared.storage.exception.ImageStorageException;
import com.wise.petadoption.shared.storage.model.StorageType;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import com.wise.petadoption.shared.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class GridFsImageStorage implements ImageStorage {

    private final GridFsTemplate gridFs;
    private final GridFsOperations ops;

    @Override
    public StoredImage save(InputStream content, long size,
                            String contentType, String originalFilename) {
        ObjectId id = gridFs.store(content, originalFilename, contentType);
        return new StoredImage(id.toHexString(), contentType, originalFilename, size);
    }

    @Override
    public StoredImageStream load(String key) {
        GridFSFile file = getFile(key);
        GridFsResource r = ops.getResource(file);
        try {
            return new StoredImageStream(
                    r.getInputStream(), r.getContentType(),
                    r.getFilename(), r.contentLength());
        } catch (IOException e) {
            throw new ImageStorageException("GridFS read failed", e);
        }
    }

    @Override
    public void delete(String key) {
        GridFSFile file = getFile(key);
        gridFs.delete(query(where("_id").is(file.getObjectId())));
    }

    @Override
    public StorageType type() {
        return StorageType.MONGO;
    }

    private @NonNull GridFSFile getFile(String key) {
        ObjectId objectId = getObjectId(key);
        GridFSFile file = gridFs.findOne(query(where("_id").is(objectId)));
        if (file == null) throw new ImageNotFoundException(key);
        return file;
    }

    private static ObjectId getObjectId(String key) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(key);

        } catch (IllegalArgumentException ex) {
            throw new ImageNotFoundException(key);
        }
        return objectId;
    }
}
