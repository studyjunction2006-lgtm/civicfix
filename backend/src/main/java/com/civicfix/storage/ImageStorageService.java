package com.civicfix.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String uploadImage(MultipartFile file, String folder);
    void deleteImage(String imageUrl);
}
