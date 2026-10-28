package com.utetra.dto;

/** Danh mục kèm ảnh của một sản phẩm đại diện để hiển thị trên trang thực đơn. */
public record MenuCategoryCard(Long id, String name, String imageUrl) {

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
