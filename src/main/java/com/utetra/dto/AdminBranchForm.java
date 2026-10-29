package com.utetra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class AdminBranchForm {

    @NotBlank(message = "Vui lòng nhập tên chi nhánh")
    @Size(max = 150, message = "Tên tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập địa chỉ")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @Pattern(regexp = "^$|^[0-9+(). -]{8,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private LocalTime openTime;

    private LocalTime closeTime;

    private boolean active = true;
}
