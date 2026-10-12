package com.utetra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/** Form them/sua san pham (admin). Anh duoc kiem tra rieng trong ImageStorageService.validate. */
@Getter
@Setter
public class ProductForm {

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    @NotBlank(message = "Vui lòng nhập tên sản phẩm")
    @Size(max = 150, message = "Tên tối đa 150 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull(message = "Vui lòng nhập giá")
    @DecimalMin(value = "1000", message = "Giá tối thiểu 1.000 đ")
    @Digits(integer = 9, fraction = 0, message = "Giá phải là số nguyên (VNĐ), tối đa 9 chữ số")
    private BigDecimal basePrice;

    private boolean available = true;

    private boolean removeImage;

    private MultipartFile image;
}
