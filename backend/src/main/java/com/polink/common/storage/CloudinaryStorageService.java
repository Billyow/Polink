package com.polink.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.polink.common.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(
            @Value("${app.storage.cloudinary.cloud-name}") String cloudName,
            @Value("${app.storage.cloudinary.api-key}") String apiKey,
            @Value("${app.storage.cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String storeImage(MultipartFile file, String subFolder) {
        ImageValidationHelper.validateAndResolveExtension(file);
        String sanitizedSubFolder = (subFolder == null || subFolder.isBlank())
                ? "polink/general"
                : "polink/" + subFolder.replaceAll("[^a-zA-Z0-9_-]", "");

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", sanitizedSubFolder, "resource_type", "image")
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new DomainException("No se obtuvo URL de la imagen subida", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return secureUrl.toString();
        } catch (IOException ex) {
            throw new DomainException("Error al subir la imagen a Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
