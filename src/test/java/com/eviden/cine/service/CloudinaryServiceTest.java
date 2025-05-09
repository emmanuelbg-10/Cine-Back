package com.eviden.cine.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {

    private CloudinaryService cloudinaryService;

    @Mock
    private Cloudinary cloudinaryMock;

    @Mock
    private Uploader uploaderMock;

    @BeforeEach
    public void setUp() {
        // Se crea la instancia con URL dummy para evitar fallo en constructor real
        cloudinaryService = new CloudinaryService("cloudinary://key:secret@cloud");

        // Se inyecta el mock manualmente
        ReflectionTestUtils.setField(cloudinaryService, "cloudinary", cloudinaryMock);
        when(cloudinaryMock.uploader()).thenReturn(uploaderMock);
    }

    @Test
    public void testUploadImage() throws Exception {
        byte[] imageBytes = new byte[]{1, 2, 3};
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/v12345/test.jpg";
        uploadResult.put("secure_url", expectedUrl);
        when(uploaderMock.upload(any(File.class), anyMap())).thenReturn(uploadResult);

        String secureUrl = cloudinaryService.uploadImage(file, 200, 300);

        assertNotNull(secureUrl);
        assertEquals(expectedUrl, secureUrl);
        verify(uploaderMock).upload(any(File.class), anyMap());
    }

    @Test
    public void testUploadImageError() throws Exception {
        byte[] imageBytes = new byte[]{4, 5, 6};
        MultipartFile file = new MockMultipartFile("file", "error.jpg", "image/jpeg", imageBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        when(uploaderMock.upload(any(File.class), anyMap())).thenReturn(uploadResult);

        String secureUrl = cloudinaryService.uploadImage(file, 100, 100);

        assertNull(secureUrl);
    }

    @Test
    public void testDeleteImage() throws Exception {
        Map<String, Object> destroyResult = Collections.emptyMap();
        when(uploaderMock.destroy(eq("publicIdTest"), anyMap())).thenReturn(destroyResult);

        cloudinaryService.deleteImage("publicIdTest");

        verify(uploaderMock).destroy(eq("publicIdTest"), anyMap());
    }

    @Test
    public void testDeleteImageThrowsException() throws Exception {
        when(uploaderMock.destroy(eq("invalidId"), anyMap()))
                .thenThrow(new IOException("Error de Cloudinary")); // <-- Aquí el cambio

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                cloudinaryService.deleteImage("invalidId")
        );

        assertTrue(exception.getMessage().contains("Error al eliminar la imagen"));
    }

}
