package com.utetra.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AddCartItemForm {

    @NotNull(message = "Sản phẩm không hợp lệ")
    private Long productId;

    @NotNull(message = "Vui lòng chọn kích cỡ")
    private Long sizeId;

    private List<Long> toppingIds = new ArrayList<>();

    @NotNull(message = "Vui lòng chọn mức đường")
    private Integer sugarPercent = 100;

    @NotNull(message = "Vui lòng chọn mức đá")
    private Integer icePercent = 100;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    @Max(value = 20, message = "Mỗi món tối đa 20 ly")
    private int quantity = 1;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String note;
}
