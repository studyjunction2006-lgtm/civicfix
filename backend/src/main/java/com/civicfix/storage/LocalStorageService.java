package com.civicfix.storage;

import com.civicfix.common.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalStorageService implements ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/octet-stream"
    );

    @Value("${civicfix.storage.upload-dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Initialized local image storage directory at: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("Could not create local upload directory", e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty image file");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg"
        );

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file type: ." + extension + ". Allowed image types: JPG, JPEG, PNG, WEBP.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported MIME type: " + contentType);
        }

        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            Path targetFolder = this.rootLocation;
            if (StringUtils.hasText(folder)) {
                targetFolder = this.rootLocation.resolve(folder).normalize();
                Files.createDirectories(targetFolder);
            }

            Path destinationFile = targetFolder.resolve(storedFilename).normalize();

            // Prevent path traversal attacks
            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new BadRequestException("Cannot store file outside current directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            if (StringUtils.hasText(folder)) {
                return "/api/v1/uploads/" + folder + "/" + storedFilename;
            } else {
                return "/api/v1/uploads/" + storedFilename;
            }

        } catch (IOException e) {
            log.error("Failed to store image", e);
            throw new BadRequestException("Failed to store uploaded image: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/api/v1/uploads/")) {
            return;
        }
        try {
            String relative = imageUrl.replace("/api/v1/uploads/", "");
            Path file = this.rootLocation.resolve(relative).normalize();
            if (file.startsWith(this.rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            log.warn("Failed to delete local image: {}", imageUrl);
        }
    }
}
