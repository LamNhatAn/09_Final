# HỒ SƠ NỘP CUỐI KỲ – UTETra

## 1. Thành phần nộp

- **Source code:** toàn bộ repository này, không bao gồm file cấu hình bí mật `application-local.properties`.
- **Database SQL:** `database/schema.sql` và `database/data.sql`. Hai file này đã chứa cấu trúc và dữ liệu cuối cùng; các file `day*.sql` được giữ lại để nâng cấp database cũ.
- **Datasheet AI:** `docs/AI_DATASHEET.md`.
- **GitHub:** <https://github.com/LamNhatAn/09_Final>
- **Cấu hình mẫu:** `application-local.properties.example`. Người chấm sao chép thành `application-local.properties` và tự điền khóa DB, JWT, Cloudinary, VNPay sandbox và email SMTP.

Không đưa mật khẩu, API Secret, HashSecret, App Password hoặc JWT secret thật lên GitHub/Drive.

## 2. Chức năng đã hoàn thành

### Khách chưa đăng nhập

- Trang chủ, danh mục menu có hình ảnh, thực đơn, tìm kiếm tiếng Việt có/không dấu, lọc danh mục, phân trang và chi tiết sản phẩm.
- Xem danh sách chi nhánh, giờ hoạt động và liên kết bản đồ.
- Trang liên hệ có số điện thoại, email và form gửi đánh giá trải nghiệm.
- Đăng ký tài khoản bằng OTP 6 số gửi qua email; có thời hạn, giới hạn số lần nhập và chức năng gửi lại.
- Quên mật khẩu bằng OTP email; mật khẩu mới phải đạt cùng điều kiện bảo mật như khi đăng ký.

### Khách hàng

- Đăng nhập bảo mật bằng JWT trong cookie HttpOnly; mật khẩu được mã hóa BCrypt và phân quyền USER/STAFF/ADMIN.
- Chọn size, mức đường, mức đá và topping. Topping chỉ được phép với danh mục **Trà sữa** và được kiểm tra lại ở backend.
- Giỏ hàng gộp đúng món có cùng cấu hình, cập nhật số lượng, xóa món và tính tổng chính xác; topping không làm phát sinh thêm ly.
- Món tạm hết vẫn hiển thị nhãn **Đã hết**, nhưng không thể thêm mới hoặc thanh toán.
- Checkout yêu cầu họ tên, số điện thoại và địa chỉ giao hàng rõ ràng; chọn chi nhánh và COD hoặc VNPay sandbox.
- Theo dõi danh sách/chi tiết/lịch sử trạng thái đơn và hủy đơn còn chờ xác nhận.
- Nhận thông báo thân thiện khi cửa hàng đã nhận đơn và khi đơn đã tới; xem số chưa đọc và đánh dấu đã đọc.

### Nhân viên

- Chỉ xem và xử lý đơn của chi nhánh được phân công.
- Quy trình tách rõ: đơn mới cần nhận → đang pha chế → pha chế xong/đang giao → hoàn tất.
- Xem rõ món, tùy chọn, tổng tiền, thông tin người nhận và liên hệ khách khi đơn có vấn đề.
- Quản lý tình trạng món: tạm ngừng bán hoặc mở bán lại.
- Nhận thông báo realtime khi có đơn mới thuộc chi nhánh.

### Quản trị viên

- Dashboard tổng hợp doanh thu, số đơn, khách hàng, nhân viên, chi nhánh và các đơn gần nhất.
- Quản lý sản phẩm, danh mục, topping; upload ảnh Cloudinary; ẩn/hiện/xóa theo ràng buộc dữ liệu.
- Quy định danh mục nào được chọn topping.
- Quản lý toàn bộ đơn hàng, lọc/tìm kiếm, xem chi tiết và cập nhật đúng luồng trạng thái.
- Quản lý chi nhánh: thêm, sửa, kích hoạt/ngừng hoạt động.
- Quản lý người dùng/nhân viên: tìm kiếm, tạo, sửa, phân vai trò, gán chi nhánh và khóa/mở khóa.
- Xem phản hồi/đánh giá của khách hàng.
- Trung tâm thông báo realtime, lưu lịch sử và trạng thái đã đọc.

### Nền tảng và chất lượng

- Spring Boot 3, JSP/JSTL, SiteMesh, Bootstrap, Spring Data JPA, SQL Server.
- WebSocket/STOMP cho thông báo realtime; thông báo vẫn được lưu trong database.
- Kiểm tra chữ ký, số tiền, mã giao dịch và chống xử lý lặp callback VNPay.
- Có kiểm thử tự động cho nghiệp vụ xác thực, giỏ hàng, checkout, đơn hàng, quản trị, OTP, thông báo, VNPay và phân quyền WebSocket.
- Có trang lỗi, validation phía server, CSRF và kiểm soát quyền truy cập theo vai trò.

## 3. Giới hạn cần ghi đúng khi báo cáo

- VNPay đang dùng **môi trường sandbox**, không phải thanh toán tiền thật.
- Cloudinary, gửi OTP email và VNPay chỉ hoạt động sau khi người chạy tự điền khóa hợp lệ vào file cấu hình local.
- Hệ thống không tích hợp mô hình AI, chatbot AI hoặc dữ liệu huấn luyện AI.
- Chức năng chat trực tiếp không nằm trong bản nộp cuối; hệ thống chỉ có thông báo realtime.

## 4. Cài đặt database sạch

Chạy tại thư mục gốc dự án:

```powershell
sqlcmd -S localhost -E -C -f 65001 -i database\schema.sql
sqlcmd -S localhost -E -C -f 65001 -i database\data.sql
```

`schema.sql` sẽ xóa và tạo lại các bảng, vì vậy chỉ dùng cách này khi cài mới hoặc chấp nhận xóa dữ liệu hiện có.

## 5. Kiểm tra trước khi nộp

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd package
```

Sau đó chạy `java -jar target\utetra.war` hoặc `.\mvnw.cmd spring-boot:run` và kiểm tra các URL trong `README.md`.

## 6. Thành viên và phần việc

- **Lâm Nhật An:** backend, database, bảo mật, nghiệp vụ giỏ hàng/đơn hàng/thanh toán/OTP/thông báo và kiểm thử.
- **Nguyễn Tiến Đạt:** giao diện JSP/CSS/JavaScript, trải nghiệm khách hàng/nhân viên/quản trị và tài liệu kiểm tra.

Lịch sử Git giữ commit riêng của từng thành viên để thể hiện phần đóng góp.
