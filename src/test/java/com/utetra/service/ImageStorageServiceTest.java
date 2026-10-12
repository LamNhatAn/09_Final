package com.utetra.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageStorageServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 1};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P'};

    private static MockMultipartFile file(byte[] content, String contentType) {
        return new MockMultipartFile("image", "x.bin", contentType, content);
    }

    @Test
    void acceptsJpegPngWebp() {
        assertDoesNotThrow(() -> ImageStorageService.validate(file(JPEG, "image/jpeg")));
        assertDoesNotThrow(() -> ImageStorageService.validate(file(PNG, "image/png")));
        assertDoesNotThrow(() -> ImageStorageService.validate(file(WEBP, "image/webp")));
    }

    @Test
    void rejectsFakeImageByContentBytesNotByDeclaredType() {
        byte[] script = "<script>alert(1)</script>".getBytes();
        ImageException e = assertThrows(ImageException.class,
                () -> ImageStorageService.validate(file(script, "image/jpeg")));
        assertEquals("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP", e.getMessage());
    }

    @Test
    void rejectsGifAndTooShortFiles() {
        assertThrows(ImageException.class, () -> ImageStorageService.validate(file("GIF89a......".getBytes(), "image/gif")));
        assertThrows(ImageException.class, () -> ImageStorageService.validate(file(new byte[]{(byte) 0xFF}, "image/jpeg")));
    }

    @Test
    void rejectsEmptyAndNull() {
        assertThrows(ImageException.class, () -> ImageStorageService.validate(file(new byte[0], "image/png")));
        assertThrows(ImageException.class, () -> ImageStorageService.validate(null));
    }

    @Test
    void rejectsOver2Mb() {
        byte[] big = new byte[(int) ImageStorageService.MAX_BYTES + 1];
        System.arraycopy(JPEG, 0, big, 0, JPEG.length);
        ImageException e = assertThrows(ImageException.class,
                () -> ImageStorageService.validate(file(big, "image/jpeg")));
        assertEquals("Ảnh quá lớn (tối đa 2 MB)", e.getMessage());
    }

    @Test
    void notConfiguredWithoutKeysAndUploadFailsClearly() {
        ImageStorageService service = new ImageStorageService("", "", "");
        assertFalse(service.isConfigured());
        ImageException e = assertThrows(ImageException.class, () -> service.upload(file(PNG, "image/png")));
        assertEquals("Chưa cấu hình Cloudinary (xem README, mục Cloudinary)", e.getMessage());
        assertDoesNotThrow(() -> service.deleteQuietly("utetra/products/abc"));
    }
}
