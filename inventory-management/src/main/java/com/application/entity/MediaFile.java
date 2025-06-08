package com.application.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import lombok.Data; 
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Document(collection = "mediaFiles") 
public class MediaFile {

    @Id
    private String id; // MongoDB uses String IDs by default
    private String fileName;
    private String originalFileName; // To preserve original name
    private String fileType; // e.g., image/jpeg, video/mp4
    private long fileSize; // in bytes
    private String filePath; // Path on the server where the file is stored
    private String fileUrl; // URL to access the file (e.g., http://localhost:8080/api/media/view/{id})
    private LocalDateTime uploadDate;

    // You still need to manually define the specific constructor for setting uploadDate and other fields
    // as @AllArgsConstructor will create a constructor for ALL fields including 'id' and 'uploadDate'
    // and you want 'uploadDate' to be automatically set.
    // If you need the specific constructor logic from before, you can keep it or use @Builder with @NoArgsConstructor

    // Manual constructor for specific initialization, complementing Lombok's @NoArgsConstructor
    public MediaFile(String fileName, String originalFileName, String fileType, long fileSize, String filePath, String fileUrl) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.uploadDate = LocalDateTime.now(); // Manually set uploadDate here
    }
}