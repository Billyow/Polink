package com.polink.common.storage;

import com.polink.common.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageValidationHelperTest {

    @Test
    void validateAndResolveExtensionShouldAcceptJpegPngAndWebp() {
        MockMultipartFile jpeg = new MockMultipartFile("file", "cert.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile png = new MockMultipartFile("file", "cert.png", "image/png", new byte[]{1, 2, 3});
        MockMultipartFile webp = new MockMultipartFile("file", "cert.webp", "image/webp", new byte[]{1, 2, 3});

        assertThat(ImageValidationHelper.validateAndResolveExtension(jpeg)).isEqualTo(".jpg");
        assertThat(ImageValidationHelper.validateAndResolveExtension(png)).isEqualTo(".png");
        assertThat(ImageValidationHelper.validateAndResolveExtension(webp)).isEqualTo(".webp");
    }

    @Test
    void validateAndResolveExtensionShouldRejectNullOrEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> ImageValidationHelper.validateAndResolveExtension(null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("archivo de imagen válido");

        assertThatThrownBy(() -> ImageValidationHelper.validateAndResolveExtension(empty))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("archivo de imagen válido");
    }

    @Test
    void validateAndResolveExtensionShouldRejectFileExceeding5MB() {
        byte[] oversized = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", oversized);

        assertThatThrownBy(() -> ImageValidationHelper.validateAndResolveExtension(file))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    void validateAndResolveExtensionShouldRejectUnsupportedMimeType() {
        MockMultipartFile pdf = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> ImageValidationHelper.validateAndResolveExtension(pdf))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Formato de imagen no soportado");
    }
}
