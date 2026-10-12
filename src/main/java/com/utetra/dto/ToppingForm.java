package com.utetra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ToppingForm {

    @NotBlank(message = "Vui lòng nhập tên topping")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;

    @NotNull(message = "Vui lòng nhập giá")
    @DecimalMin(value = "0", message = "Giá không được âm")
    @Digits(integer = 7, fraction = 0, message = "Giá phải là số nguyên (VNĐ), tối đa 7 chữ số")
    private BigDecimal price;

    private boolean available = true;
}
