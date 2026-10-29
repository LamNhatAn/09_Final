package com.utetra.dto;

import com.utetra.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserForm {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Pattern(regexp = "^$|^[0-9+(). -]{8,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @NotNull(message = "Vui lòng chọn vai trò")
    private Role role = Role.STAFF;

    private Long branchId;

    private boolean active = true;

    @Size(max = 72, message = "Mật khẩu tối đa 72 ký tự")
    private String password;
}
