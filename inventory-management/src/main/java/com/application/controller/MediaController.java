package com.application.controller;
import com.application.dto.MediaFileResponseDTO; // Import the DTO
import com.application.service.MediaService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "http://localhost:4200")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // UPLOAD File (Create)
    @PostMapping("/upload")
    public ResponseEntity<MediaFileResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) { // Returns DTO
        MediaFileResponseDTO storedFileDto = mediaService.storeFile(file);
        return new ResponseEntity<>(storedFileDto, HttpStatus.CREATED);
    }

    // GET All Files (Read)
    @GetMapping
    public ResponseEntity<List<MediaFileResponseDTO>> getAllMediaFiles() { // Returns List of DTOs
        List<MediaFileResponseDTO> mediaFiles = mediaService.getAllMediaFiles();
        return ResponseEntity.ok(mediaFiles);
    }

    // GET File by ID (Read - metadata)
    @GetMapping("/{id}")
    public ResponseEntity<MediaFileResponseDTO> getMediaFileById(@PathVariable String id) { // Returns DTO
        return mediaService.getMediaFileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // VIEW File Content (Read - actual file from file system) - no change needed here as it returns Resource
    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = mediaService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Log this: Could not determine file type.
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // DELETE File - no change needed here as it returns Void
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMediaFile(@PathVariable String id) {
        try {
            mediaService.deleteMediaFile(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}