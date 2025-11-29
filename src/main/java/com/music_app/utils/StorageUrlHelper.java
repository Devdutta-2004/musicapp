package com.music_app.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageUrlHelper {

    @Value("${storage.base-url}")
    private String storageBaseUrl;

    public String toPublicUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) return null;

        // Already a full URL
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }

        String base = storageBaseUrl.endsWith("/") ? storageBaseUrl : storageBaseUrl + "/";
        String sanitized = filePath.startsWith("/") ? filePath.substring(1) : filePath;

        return base + sanitized;
    }
}
