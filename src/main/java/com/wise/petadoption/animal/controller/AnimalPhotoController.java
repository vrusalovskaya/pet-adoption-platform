package com.wise.petadoption.animal.controller;

import com.wise.petadoption.animal.service.AnimalPhotoService;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class AnimalPhotoController {

    private final AnimalPhotoService animalPhotoService;

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> upload(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        animalPhotoService.replacePhoto(id, file);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable Long id) {
        StoredImageStream image = animalPhotoService.download(id);
        StreamingResponseBody body = outputStream -> {
            try (image) {
                image.stream().transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .contentLength(image.sizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(
                                        image.originalFilename(),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                                .toString()
                )
                .body(body);
    }

    @DeleteMapping("/{id}/photo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        animalPhotoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
