package com.eviden.cine.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public String uploadImage(MultipartFile file, int width, int height) {
        try {
            File convFile = convertToFile(file);

            Map<String, Object> options = ObjectUtils.asMap(
                    "transformation", new Transformation<>().width(width).height(height).crop("fill")
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(convFile, options);
            boolean deleted = convFile.delete();

            if (!deleted) {
                logger.warn("No se pudo eliminar el archivo temporal: {}", convFile.getAbsolutePath());
            }

            logger.debug("Resultado de Cloudinary: {}", uploadResult);

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                logger.warn("No se obtuvo 'secure_url' en la respuesta de Cloudinary.");
            }

            return secureUrl;
        } catch (IOException e) {
            logger.error("Error al subir la imagen", e);
            return null;
        }
    }

    private File convertToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String prefix = "upload_";
        String suffix = ".tmp";

        if (originalFilename != null && originalFilename.contains(".")) {
            int dotIndex = originalFilename.lastIndexOf(".");
            prefix = originalFilename.substring(0, dotIndex);
            suffix = originalFilename.substring(dotIndex);
        }

        // Crear un directorio temporal específico para tu app si no existe
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "cine_uploads");
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio temporal seguro.");
        }

        // Crear el archivo en el directorio seguro
        File convFile = File.createTempFile(prefix, suffix, tempDir);
        file.transferTo(convFile);

        if (!convFile.exists()) {
            throw new IOException("El archivo no se pudo crear.");
        }

        return convFile;
    }


    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Imagen eliminada de Cloudinary: {}", publicId);
        } catch (IOException e) {
            logger.error("Error al eliminar la imagen con publicId '{}'", publicId, e);
            throw new RuntimeException("Error al eliminar la imagen", e);
        }
    }
}
