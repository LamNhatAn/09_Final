package com.utetra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotBlank(message = "Vui lòng nhập email")
    @Size(max = 150, message = "Email quá dài")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(max = 72, message = "Mật khẩu quá dài")
    private String password;

    private String next;
}
