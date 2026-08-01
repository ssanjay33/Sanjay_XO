package com.xo.eventmanagement.service;

import com.xo.eventmanagement.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${xo.app.uploadDir:uploads/event-images}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    /**
     * Saves an uploaded image to disk and returns the public URL path
     * (served via the /uploads/** static resource mapping).
     */
    public String storeEventImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPG, PNG, WEBP or GIF images are allowed");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("Image must be smaller than 5MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String filename = UUID.randomUUID().toString() + extension;

            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath);

            return "/uploads/event-images/" + filename;
        } catch (IOException e) {
            throw new BadRequestException("Failed to store the image. Please try again.");
        }
    }
}
