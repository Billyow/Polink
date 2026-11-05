package com.polink.common.storage;

import com.polink.common.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements StorageService {

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${app.storage.local-dir:./uploads}") String localDir) {
        this.rootLocation = Paths.get(localDir).toAbsolutePath().normalize();
    }

    @Override
    public String storeImage(MultipartFile file, String subFolder) {
        String extension = ImageValidationHelper.validateAndResolveExtension(file);
        String sanitizedSubFolder = (subFolder == null || subFolder.isBlank())
                ? "general"
                : subFolder.replaceAll("[^a-zA-Z0-9_-]", "");
        String fileName = UUID.randomUUID() + extension;

        try {
            Path targetDirectory = rootLocation.resolve(sanitizedSubFolder).normalize();
            if (!targetDirectory.startsWith(rootLocation)) {
                throw new DomainException("Ruta de almacenamiento no permitida");
            }
            Files.createDirectories(targetDirectory);
            Path destinationFile = targetDirectory.resolve(fileName).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + sanitizedSubFolder + "/" + fileName;
        } catch (IOException ex) {
            throw new DomainException("Error al almacenar el archivo de imagen", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
