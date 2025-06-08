package com.application.service.impl;
import com.application.dto.MediaFileResponseDTO; // Import the DTO
import com.application.entity.MediaFile;
import com.application.repository.MediaFileRepository;
import com.application.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MediaServiceImpl implements MediaService {

    private final Path fileStorageLocation;
    private final MediaFileRepository mediaFileRepository;

    public MediaServiceImpl(@Value("${file.upload-dir}") String uploadDir, MediaFileRepository mediaFileRepository) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.mediaFileRepository = mediaFileRepository;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public MediaFileResponseDTO storeFile(MultipartFile file) { // <-- CORRECTED RETURN TYPE TO DTO
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/media/view/")
                    .path(fileName)
                    .toUriString();

            MediaFile mediaFile = new MediaFile(
                    fileName,
                    originalFileName,
                    file.getContentType(),
                    file.getSize(),
                    targetLocation.toString(),
                    fileDownloadUri
            );

            MediaFile savedMediaFile = mediaFileRepository.save(mediaFile);
            // Convert entity to DTO before returning
            return convertToDto(savedMediaFile); // <-- Ensure this conversion happens and returns the DTO

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    @Override
    public List<MediaFileResponseDTO> getAllMediaFiles() {
        return mediaFileRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MediaFileResponseDTO> getMediaFileById(String id) {
        return mediaFileRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public void deleteMediaFile(String id) {
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(id);
        if (mediaFileOptional.isPresent()) {
            MediaFile mediaFile = mediaFileOptional.get();
            try {
                Files.deleteIfExists(Paths.get(mediaFile.getFilePath()));
                mediaFileRepository.deleteById(id);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete file from system: " + mediaFile.getFileName(), e);
            }
        } else {
            throw new RuntimeException("Media file not found with ID: " + id);
        }
    }

    // Helper method to convert MediaFile entity to MediaFileResponseDTO
    private MediaFileResponseDTO convertToDto(MediaFile mediaFile) {
        return new MediaFileResponseDTO(
                mediaFile.getId(),
                mediaFile.getOriginalFileName(),
                mediaFile.getFileType(),
                mediaFile.getFileSize(),
                mediaFile.getFileUrl(),
                mediaFile.getUploadDate()
        );
    }
}