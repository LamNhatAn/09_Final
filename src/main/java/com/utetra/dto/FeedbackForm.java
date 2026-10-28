package com.utetra.dto;

import com.utetra.entity.enums.FeedbackTopic;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackForm {

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Pattern(regexp = "^$|^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ (vd: 0901234567)")
    private String phone;

    private Long branchId;

    @NotNull(message = "Vui lòng chọn vấn đề phản hồi")
    private FeedbackTopic topic;

    @NotNull(message = "Vui lòng đánh giá trải nghiệm")
    @Min(value = 1, message = "Đánh giá từ 1 đến 5 sao")
    @Max(value = 5, message = "Đánh giá từ 1 đến 5 sao")
    private Integer rating;

    @NotBlank(message = "Vui lòng nhập nội dung phản hồi")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự")
    private String content;

    @AssertTrue(message = "Bạn cần đồng ý để UTETra sử dụng thông tin cho mục đích xử lý phản hồi")
    private boolean privacyConsent;
}
