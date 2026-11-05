package com.polink.common.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeImageShouldWriteFileAndReturnNormalizedPublicPath() {
        LocalFileStorageService storageService = new LocalFileStorageService(tempDir.toString());
        byte[] content = new byte[]{10, 20, 30, 40};
        MockMultipartFile file = new MockMultipartFile("image", "certificate.png", "image/png", content);

        String storedUrl = storageService.storeImage(file, "certificates");

        assertThat(storedUrl).startsWith("/uploads/certificates/").endsWith(".png");
        String fileName = storedUrl.substring("/uploads/certificates/".length());
        Path expectedPath = tempDir.resolve("certificates").resolve(fileName);
        assertThat(Files.exists(expectedPath)).isTrue();
    }

    @Test
    void storeImageShouldSanitizeSubFolderAgainstTraversalCharacters() {
        LocalFileStorageService storageService = new LocalFileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("image", "offer.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String storedUrl = storageService.storeImage(file, "../../offers_test");

        assertThat(storedUrl).startsWith("/uploads/offers_test/").endsWith(".jpg");
    }
}
