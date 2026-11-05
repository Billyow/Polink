package com.polink.common.storage;

import com.polink.common.exception.DomainException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

public final class ImageValidationHelper {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSION_BY_MIME = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private ImageValidationHelper() {
    }

    public static String validateAndResolveExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainException("Debe adjuntar un archivo de imagen válido");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new DomainException("La imagen no puede superar los 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new DomainException("Formato de imagen no soportado. Solo se permiten JPEG, PNG o WEBP");
        }
        return EXTENSION_BY_MIME.get(contentType.toLowerCase());
    }
}
