# Day 8 - Nhân viên xử lý đơn hàng theo chi nhánh

## 1. Chức năng

| URL | Chức năng |
|---|---|
| `/staff/orders` | STAFF xem, lọc trạng thái và tìm đơn của đúng chi nhánh được gán |
| `/staff/orders/{code}` | STAFF xem chi tiết, lịch sử và chuyển đơn sang bước kế tiếp |
| `POST /staff/orders/{code}/advance` | Cập nhật đúng một bước trong quy trình xử lý |
| `/admin/orders` | ADMIN xem toàn hệ thống, lọc theo chi nhánh/trạng thái và tìm mã đơn hoặc số điện thoại |
| `/admin/orders/{code}` | ADMIN xem chi tiết và chuyển đơn sang bước kế tiếp |

Trang `/staff/dashboard` chuyển thẳng tới `/staff/orders` để nhân viên bắt đầu xử lý đơn ngay sau khi đăng nhập.

## 2. Quy trình trạng thái

```text
PENDING → CONFIRMED → PREPARING → DELIVERING → COMPLETED
    └──────── USER chỉ được hủy tại PENDING ────────→ CANCELLED
```

- Mỗi lần bấm chỉ chuyển đúng một bước, server tự xác định trạng thái kế tiếp.
- `COMPLETED` và `CANCELLED` là trạng thái kết thúc, không thể xử lý tiếp.
- Mỗi thay đổi được ghi vào `order_status_history`, gồm trạng thái mới, thời gian và ID người thực hiện.
- Cập nhật dùng transaction và pessimistic write lock để hai yêu cầu đồng thời không thể bỏ qua bước hoặc ghi trùng.

## 3. Phân quyền chi nhánh

- `branchId` của STAFF luôn lấy từ JWT/người dùng đã xác thực, không nhận từ URL hay form.
- Cả danh sách, chi tiết và câu lệnh khóa đơn đều truy vấn kèm `branchId`.
- STAFF mở mã đơn của chi nhánh khác sẽ nhận 404 và không được biết nội dung đơn.
- ADMIN có thể xem mọi chi nhánh hoặc chọn một chi nhánh cụ thể trong bộ lọc.
- Tìm kiếm theo mã đơn/số điện thoại có escape ký tự wildcard để nội dung nhập không làm thay đổi ý nghĩa truy vấn `LIKE`.

## 4. Phân công (Day 8)

| Người | Phần việc | File chính |
|---|---|---|
| LamNhatAn | Quy trình trạng thái, truy vấn lọc/tìm kiếm, giới hạn dữ liệu theo chi nhánh, khóa đồng thời, service/controller và unit test | `OrderStatus`, `OrderRepository`, `AdminOrderService`, `StaffOrderService`, hai order controller, các test Day 8 |
| Nguyễn Tiến Đạt | Giao diện danh sách/chi tiết cho STAFF, bộ lọc và nút chuyển trạng thái của ADMIN, menu nhân viên, README và tài liệu | `views/staff/orders/*`, `views/admin/orders/*`, `decorators/staff.jsp`, `README.md`, `docs/*` |

## 5. Kiểm tra từng bước

Mật khẩu chung của tài khoản mẫu: `Utetra@123`.

Ba tài khoản nhân viên mẫu là `staff1@utetra.vn` (Võ Văn Ngân), `staff2@utetra.vn` (Quận 1) và `staff3@utetra.vn` (Gò Vấp).

1. Chạy `mvnw.cmd clean test`: phải có **71 test** thành công.
2. Đăng nhập `staff1@utetra.vn`: hệ thống mở `/staff/orders` và chỉ hiển thị đơn của chi nhánh Võ Văn Ngân.
3. Thử lọc trạng thái và tìm theo mã đơn/số điện thoại: kết quả phải đồng thời thỏa cả hai điều kiện.
4. Mở một đơn `PENDING`, lần lượt bấm nút chuyển trạng thái: `CONFIRMED`, `PREPARING`, `DELIVERING`, rồi `COMPLETED`.
5. Sau mỗi lần bấm, badge và lịch sử trạng thái phải có bước mới; khi hoàn thành thì nút biến mất.
6. Sao chép URL chi tiết ở bước 4, đăng nhập `staff2@utetra.vn` rồi mở URL: nhận 404 vì khác chi nhánh.
7. Mở một đơn `CANCELLED`: không có nút chuyển trạng thái.
8. Đăng nhập `admin@utetra.vn`, mở `/admin/orders`: lọc theo chi nhánh, trạng thái và tìm theo mã/số điện thoại.
9. ADMIN mở một đơn và chuyển trạng thái; khách hàng tải lại `/orders/{code}` phải thấy trạng thái và lịch sử mới.
10. USER mở `/staff/orders` hoặc `/admin/orders`: bị từ chối quyền truy cập.

## 6. Giải thích để trả lời khi bị hỏi

- **Vì sao không gửi trạng thái mới từ giao diện?** Server tự tính bước kế tiếp nên người dùng không thể sửa form để nhảy thẳng từ `PENDING` sang `COMPLETED`.
- **Vì sao truy vấn STAFF luôn kèm branchId?** Ẩn nút trên giao diện không phải là bảo mật; giới hạn phải được áp dụng ngay tại truy vấn dữ liệu.
- **Vì sao chi tiết sai chi nhánh trả 404 thay vì 403?** Không tiết lộ việc một mã đơn có tồn tại ở chi nhánh khác.
- **Vì sao vẫn cần pessimistic lock?** Hai nhân viên có thể bấm gần như cùng lúc; khóa giúp request sau đọc trạng thái mới nhất rồi chỉ tiến thêm đúng một bước.
- **ADMIN và STAFF khác nhau thế nào?** ADMIN quản lý toàn hệ thống và lọc được chi nhánh, còn STAFF chỉ làm việc với chi nhánh đã gán.

## 7. Chuẩn bị Day 9

Day 9 nên hoàn thiện thanh toán **VNPay sandbox**:

1. Chuẩn bị `TMN_CODE`, secret và URL sandbox dưới dạng biến cấu hình cục bộ, không commit khóa bí mật.
2. Cho khách chọn COD hoặc VNPay khi checkout; tạo URL thanh toán có chữ ký đúng chuẩn.
3. Xử lý return/IPN, kiểm tra chữ ký, số tiền, mã đơn và chống cập nhật lặp.
4. Chỉ xác nhận thanh toán khi VNPay báo thành công; lưu giao dịch thất bại/hủy để theo dõi.
5. Viết test tạo chữ ký, kiểm tra callback hợp lệ/sai và các lần callback trùng nhau.
