package com.civicfix.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Primary
public class CloudinaryStorageService implements ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final LocalStorageService localStorageService;
    private Cloudinary cloudinary;
    private final boolean isCloudinaryConfigured;

    public CloudinaryStorageService(
            LocalStorageService localStorageService,
            @Value("${civicfix.storage.cloudinary.cloud-name:}") String cloudName,
            @Value("${civicfix.storage.cloudinary.api-key:}") String apiKey,
            @Value("${civicfix.storage.cloudinary.api-secret:}") String apiSecret) {

        this.localStorageService = localStorageService;

        if (StringUtils.hasText(cloudName) && StringUtils.hasText(apiKey) && StringUtils.hasText(apiSecret)) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            this.isCloudinaryConfigured = true;
            log.info("Cloudinary storage provider initialized successfully for cloud: {}", cloudName);
        } else {
            this.isCloudinaryConfigured = false;
            log.info("Cloudinary credentials not provided. Using LocalStorageService as active storage provider.");
        }
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (!isCloudinaryConfigured) {
            return localStorageService.uploadImage(file, folder);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", StringUtils.hasText(folder) ? "civicfix/" + folder : "civicfix",
                            "resource_type", "image"
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.warn("Cloudinary upload failed, falling back to local storage: {}", e.getMessage());
            return localStorageService.uploadImage(file, folder);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (!isCloudinaryConfigured) {
            localStorageService.deleteImage(imageUrl);
        }
    }
}
