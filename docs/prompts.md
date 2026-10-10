# Nhật ký prompt AI (UTETra)

Yêu cầu của đề: liệt kê các prompt đã dùng để AI sinh code/hình. Ghi thêm mỗi ngày.

| Ngày | Mục đích | Prompt (tóm tắt) | Kết quả |
|---|---|---|---|
| 0 | Lập kế hoạch | Đóng vai senior web developer và mentor, đọc đề tài UTETra, tóm tắt yêu cầu, hỏi điểm chưa rõ, đề xuất tech stack và kế hoạch 14 ngày | Kế hoạch 14 ngày, tech stack |
| 0 | Chốt phạm vi | Dùng Cloudinary, SQL Server, thêm vai trò Staff, WebSocket (thông báo đơn + chat), thanh toán COD + VNPay | Phạm vi cuối |
| 0 | Hướng dẫn tài khoản test | Hướng dẫn đăng ký VNPay sandbox, tìm hiểu MoMo test | Có tài khoản VNPay sandbox; bỏ MoMo vì không dùng được trên web |
| 1 | Phân tích, thiết kế | Bắt đầu Day 1: use case, ERD, kiến trúc, cấu trúc package, luồng thanh toán và chat | `docs/01-analysis-design.md` |
| 2 | Dựng khung dự án | Day 2: tạo project Spring Boot WAR (JSP, SiteMesh, Bootstrap, JPA, SQL Server), 16 entity và repository theo ERD, `schema.sql`/`data.sql`, 3 layout main/staff/admin, trang `/health`, README | Project `UTETra/` chạy được; `database/schema.sql`, `database/data.sql` |
| 2 | Sửa lỗi SiteMesh | Trang JSP trả 400 vì đường dẫn layout bị lặp tiền tố `/WEB-INF/decorators//...`; chỉ khai báo tên file layout | `SiteMeshConfig.java` |
| 3 | Đăng ký, đăng nhập, JWT, phân quyền | Day 3: đăng ký/đăng nhập/đăng xuất, JWT trong cookie HttpOnly (jjwt), phân quyền USER/STAFF/ADMIN, trang 403, CSRF, cập nhật navbar 3 layout, unit test JwtService | `security/`, `SecurityConfig.java`, `AuthController.java`, `views/auth/`, `docs/03-auth.md` |
| 3 | Sửa lỗi forward JSP | Sau khi bật phân quyền, mọi trang bị chuyển về `/login?next=/WEB-INF/views/...` vì Spring Security 6 kiểm tra cả dispatcher FORWARD; thêm `dispatcherTypeMatchers(FORWARD, INCLUDE, ERROR).permitAll()` | `SecurityConfig.java` |
