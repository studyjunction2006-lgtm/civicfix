package com.civicfix.storage;

import com.civicfix.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@Tag(name = "Image Storage", description = "Endpoints for uploading issue photographs and resolution proof")
public class FileUploadController {

    private final ImageStorageService storageService;

    public FileUploadController(ImageStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image file (returns secure URL)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "issues") String folder) {

        String imageUrl = storageService.uploadImage(file, folder);
        return ResponseEntity.ok(ApiResponse.ok("Image uploaded successfully", Map.of("url", imageUrl)));
    }
}
