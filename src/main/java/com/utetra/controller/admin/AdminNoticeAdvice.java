package com.utetra.controller.admin;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Thong bao sau khi thao tac (them/sua/xoa) cua cac trang admin.
 * Controller redirect kem ?ok=<ma> hoac ?err=<ma>; o day doi ma thanh cau chu co dinh.
 * Chi cho phep ma co san de URL khong the chen noi dung tuy y vao trang. Khong dung session/flash.
 */
@ControllerAdvice(basePackages = "com.utetra.controller.admin")
public class AdminNoticeAdvice {

    private static final Map<String, String> OK = Map.of(
            "created", "Đã thêm thành công.",
            "updated", "Đã cập nhật thành công.",
            "deleted", "Đã xóa thành công.",
            "shown", "Đã chuyển sang trạng thái đang bán.",
            "hidden", "Đã ẩn (ngừng bán).");

    private static final Map<String, String> ERR = Map.of(
            "in_use", "Không thể xóa vì dữ liệu đang được sử dụng. Hãy dùng nút Ẩn thay cho Xóa.",
            "too_large", "Ảnh quá lớn (tối đa 2 MB).",
            "failed", "Có lỗi xảy ra, vui lòng thử lại.");

    @ModelAttribute("okMessage")
    public String okMessage(@RequestParam(name = "ok", required = false) String code) {
        return code == null ? null : OK.get(code);
    }

    @ModelAttribute("errMessage")
    public String errMessage(@RequestParam(name = "err", required = false) String code) {
        return code == null ? null : ERR.get(code);
    }
}
