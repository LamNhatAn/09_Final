package com.utetra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordForm {
    @NotBlank
    private String email;

    @NotBlank(message = "Vui lòng nhập mã OTP")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã OTP phải gồm 6 chữ số")
    private String code;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).*$", message = "Mật khẩu phải có cả chữ và số")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;
}
