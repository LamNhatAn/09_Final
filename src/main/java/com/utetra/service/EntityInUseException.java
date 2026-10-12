package com.utetra.service;

/** Khong xoa duoc vi du lieu dang duoc noi khac su dung (vd: san pham dang nam trong gio hang). */
public class EntityInUseException extends RuntimeException {

    public EntityInUseException(String message) {
        super(message);
    }
}
