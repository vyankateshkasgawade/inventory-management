package com.application.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class MediaFileResponseDTO {

 private String id;
 private String originalFileName; // The name the user uploaded
 private String fileType;
 private long fileSize; // in bytes
 private String fileUrl; // The URL to access the file
 private LocalDateTime uploadDate;
}