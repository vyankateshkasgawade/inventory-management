package com.application.service.impl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class ImageUploadServiceTest {

    private ImageUploadService imageUploadService;

    // JUnit 5 @TempDir automatically creates and cleans up a temporary directory
    @TempDir
    Path tempUploadDir; // This will be our mock upload directory

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this); // Initialize mocks

        imageUploadService = new ImageUploadService();
        // Manually inject the uploadDir property for the test
        // ReflectionTestUtils.setField(imageUploadService, "uploadDir", tempUploadDir.toString());
        // Or directly set it if you make the field non-final and visible (e.g., protected or package-private for testing)
        // For simplicity in testing, we'll assign it here. In a real app, Spring injects it.
        // Since @Value is typically handled by Spring, for pure unit testing without a Spring context,
        // you might set it via reflection or a setter if available. For this case, we'll just set the field directly.
        // Making `uploadDir` non-final and having a setter is a common pattern for testability.
        // If not, we'd need to use ReflectionTestUtils from spring-test.
        // For this example, assuming `uploadDir` can be set after construction for testing purposes.
        try {
            // Use reflection to set the private field @Value
            java.lang.reflect.Field uploadDirField = ImageUploadService.class.getDeclaredField("uploadDir");
            uploadDirField.setAccessible(true);
            uploadDirField.set(imageUploadService, tempUploadDir.toString());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set uploadDir field via reflection: " + e.getMessage());
        }

        // Ensure the directory exists before tests, though the service method also creates it.
        // This is good for setup if multiple tests rely on it.
        if (!Files.exists(tempUploadDir)) {
            Files.createDirectories(tempUploadDir);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // @TempDir handles cleanup automatically, so usually not needed here unless complex cleanup.
    }

    @Test
    void uploadImage_ShouldThrowIllegalArgumentException_WhenFileIsEmpty() {
        // Given
        when(mockFile.isEmpty()).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> imageUploadService.uploadImage(mockFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot upload empty file.");

        // Verify that no further interactions with file system or file were made
        verify(mockFile, times(1)).isEmpty();
        verifyNoMoreInteractions(mockFile);
    }

    @Test
    void uploadImage_ShouldUploadFileSuccessfully_WithPngExtension() throws IOException {
        // Given
        String originalFilename = "my_image.png";
        byte[] fileContent = "This is a test image content.".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // When
        String returnedPath = imageUploadService.uploadImage(mockFile);

        // Then
        assertNotNull(returnedPath);
        assertTrue(returnedPath.startsWith("/images/"));
        assertTrue(returnedPath.endsWith(".png"));

        // Extract the unique filename from the returned path
        String uniqueFilename = returnedPath.substring(returnedPath.lastIndexOf('/') + 1);
        Path expectedFilePath = tempUploadDir.resolve(uniqueFilename);

        // Verify the file was actually created in the temporary directory
        assertTrue(Files.exists(expectedFilePath));
        assertThat(Files.readAllBytes(expectedFilePath)).isEqualTo(fileContent);

        verify(mockFile, times(1)).isEmpty();
        verify(mockFile, times(1)).getOriginalFilename();
        verify(mockFile, times(1)).getInputStream();
        // ensure getInputStream is closed if service closes it, or handled by try-with-resources.
        // For Files.copy, the stream is typically closed by Files.copy.
    }
    
    @Test
    void uploadImage_ShouldUploadFileSuccessfully_WithJpgExtension() throws IOException {
        // Given
        String originalFilename = "another_pic.jpg";
        byte[] fileContent = "JPG content here".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // When
        String returnedPath = imageUploadService.uploadImage(mockFile);

        // Then
        assertNotNull(returnedPath);
        assertTrue(returnedPath.startsWith("/images/"));
        assertTrue(returnedPath.endsWith(".jpg"));

        String uniqueFilename = returnedPath.substring(returnedPath.lastIndexOf('/') + 1);
        Path expectedFilePath = tempUploadDir.resolve(uniqueFilename);

        assertTrue(Files.exists(expectedFilePath));
        assertThat(Files.readAllBytes(expectedFilePath)).isEqualTo(fileContent);
    }

    @Test
    void uploadImage_ShouldUploadFileSuccessfully_WithoutExtension() throws IOException {
        // Given
        String originalFilename = "no_extension_file";
        byte[] fileContent = "Some text content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // When
        String returnedPath = imageUploadService.uploadImage(mockFile);

        // Then
        assertNotNull(returnedPath);
        assertTrue(returnedPath.startsWith("/images/"));
        assertFalse(returnedPath.contains(".")); // No extension in the returned path

        String uniqueFilename = returnedPath.substring(returnedPath.lastIndexOf('/') + 1);
        Path expectedFilePath = tempUploadDir.resolve(uniqueFilename);

        assertTrue(Files.exists(expectedFilePath));
        assertThat(Files.readAllBytes(expectedFilePath)).isEqualTo(fileContent);
    }

    @Test
    void uploadImage_ShouldHandleIOExceptionDuringFileOperations() throws IOException {
        // Given
        String originalFilename = "failing_image.png";
        byte[] fileContent = "Content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        // Simulate IOException when getting input stream
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulated IO error during getInputStream"));

        // When / Then
        assertThatThrownBy(() -> imageUploadService.uploadImage(mockFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Simulated IO error during getInputStream");

        // Verify interactions
        verify(mockFile, times(1)).isEmpty();
        verify(mockFile, times(1)).getOriginalFilename();
        verify(mockFile, times(1)).getInputStream();
        // Ensure no file was created as an error occurred early
        assertThat(Files.list(tempUploadDir)).isEmpty();
    }
    
    @Test
    void uploadImage_ShouldCreateDirectoryIfNotFound() throws IOException {
        // Delete the temp directory before the test to ensure the service creates it
        Files.walk(tempUploadDir)
             .sorted(java.util.Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(java.io.File::delete);
        
        // Assert directory is gone
        assertFalse(Files.exists(tempUploadDir));

        // Given
        String originalFilename = "new_dir_image.txt";
        byte[] fileContent = "Directory test content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // When
        String returnedPath = imageUploadService.uploadImage(mockFile);

        // Then
        assertTrue(Files.exists(tempUploadDir)); // Directory should now exist
        assertTrue(returnedPath.startsWith("/images/"));
        
        String uniqueFilename = returnedPath.substring(returnedPath.lastIndexOf('/') + 1);
        Path expectedFilePath = tempUploadDir.resolve(uniqueFilename);
        assertTrue(Files.exists(expectedFilePath));
        assertThat(Files.readAllBytes(expectedFilePath)).isEqualTo(fileContent);
    }
}
