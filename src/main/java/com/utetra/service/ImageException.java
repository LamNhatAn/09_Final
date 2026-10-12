package com.utetra.service;

/** Anh khong hop le hoac khong luu duoc len Cloudinary; message hien thi duoc cho nguoi dung. */
public class ImageException extends RuntimeException {

    public ImageException(String message) {
        super(message);
    }

    public ImageException(String message, Throwable cause) {
        super(message, cause);
    }
}
