package com.utetra.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Kiem tra va upload anh san pham len Cloudinary.
 * Khoa Cloudinary (cloud-name, api-key, api-secret) doc tu application-local.properties, khong co trong Git.
 */
@Service
@Slf4j
public class ImageStorageService {

    public static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final String FOLDER = "utetra/products";

    // null khi chua cau hinh: ung dung van chay, chi la khong upload duoc
    private final Cloudinary cloudinary;

    public ImageStorageService(@Value("${cloudinary.cloud-name:}") String cloudName,
                               @Value("${cloudinary.api-key:}") String apiKey,
                               @Value("${cloudinary.api-secret:}") String apiSecret) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            this.cloudinary = null;
            log.warn("Chua cau hinh Cloudinary: se khong upload duoc anh san pham");
        } else {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName.trim(),
                    "api_key", apiKey.trim(),
                    "api_secret", apiSecret.trim(),
                    "secure", true));
        }
    }

    public boolean isConfigured() {
        return cloudinary != null;
    }

    /**
     * Kiem tra file anh: toi da 2MB va dung dinh dang JPG/PNG/WEBP.
     * Doc "magic bytes" (byte dau file) thay vi tin ten file hay Content-Type do client gui.
     */
    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageException("Chưa chọn ảnh");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ImageException("Ảnh quá lớn (tối đa 2 MB)");
        }
        byte[] head;
        try (InputStream in = file.getInputStream()) {
            head = in.readNBytes(12);
        } catch (IOException e) {
            throw new ImageException("Không đọc được file ảnh", e);
        }
        if (!isJpeg(head) && !isPng(head) && !isWebp(head)) {
            throw new ImageException("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP");
        }
    }

    /** Upload anh (da validate) len Cloudinary; thu nho ve toi da 1200x1200 de tiet kiem dung luong. */
    public StoredImage upload(MultipartFile file) {
        validate(file);
        if (!isConfigured()) {
            throw new ImageException("Chưa cấu hình Cloudinary (xem README, mục Cloudinary)");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", FOLDER,
                    "resource_type", "image",
                    "transformation", new Transformation<>().width(1200).height(1200).crop("limit")));
            Object url = result.get("secure_url");
            Object publicId = result.get("public_id");
            if (url == null || publicId == null) {
                throw new ImageException("Cloudinary không trả về địa chỉ ảnh");
            }
            return new StoredImage(url.toString(), publicId.toString());
        } catch (ImageException e) {
            throw e;
        } catch (Exception e) {
            log.error("Upload Cloudinary that bai", e);
            throw new ImageException("Không tải được ảnh lên Cloudinary, kiểm tra khóa cấu hình và thử lại", e);
        }
    }

    /** Xoa anh tren Cloudinary; loi chi duoc ghi log vi khong nen lam hong thao tac chinh cua admin. */
    public void deleteQuietly(String publicId) {
        if (publicId == null || publicId.isBlank() || !isConfigured()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (Exception e) {
            log.warn("Khong xoa duoc anh {} tren Cloudinary: {}", publicId, e.getMessage());
        }
    }

    private static boolean isJpeg(byte[] b) {
        return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] b) {
        return b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A;
    }

    private static boolean isWebp(byte[] b) {
        return b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
