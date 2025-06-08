
package com.application.service;

import com.application.dto.MediaFileResponseDTO; // <--- Make sure this is imported
import com.application.entity.MediaFile; // This might still be needed if other methods use it internally, though less likely now
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface MediaService {

    // THIS IS THE CRITICAL LINE THAT NEEDS TO BE CORRECTED
    MediaFileResponseDTO storeFile(MultipartFile file); // <--- ENSURE this method returns MediaFileResponseDTO

    Resource loadFileAsResource(String fileName);

    List<MediaFileResponseDTO> getAllMediaFiles();

    Optional<MediaFileResponseDTO> getMediaFileById(String id);

    void deleteMediaFile(String id);
}