# NHẬT KÝ PROMPT AI – DỰ ÁN UTETra

## 1. Mục đích tài liệu

Tài liệu này tổng hợp các yêu cầu chính mà nhóm đã nhập cho trợ lý AI trong quá trình phát triển UTETra. Nội dung được chuẩn hóa và rút gọn để dễ kiểm tra, không sao chép nguyên văn toàn bộ hội thoại, nhưng vẫn giữ đầy đủ mục tiêu, phạm vi xử lý và kết quả kỹ thuật.

AI được sử dụng như công cụ hỗ trợ phân tích, đề xuất mã nguồn, tìm lỗi, viết kiểm thử và chuẩn hóa tài liệu. Nhóm chịu trách nhiệm lựa chọn giải pháp, chạy thử, kiểm tra kết quả và quyết định nội dung đưa vào repository.

## 2. Quy ước sử dụng

- Không yêu cầu AI tự quyết định yêu cầu nghiệp vụ quan trọng khi chưa có xác nhận của nhóm.
- Chỉ chỉnh sửa chức năng thuộc Day hoặc phạm vi được yêu cầu.
- Mã nguồn của Lâm Nhật An và Nguyễn Tiến Đạt được chia thành các commit riêng.
- Khóa bí mật chỉ lưu trong `application-local.properties`, không đưa vào GitHub.
- Kết quả phải được kiểm tra bằng test tự động, build và chạy thử giao diện.

## 3. Nhật ký yêu cầu đã sử dụng

| Mã | Giai đoạn | Prompt của nhóm (tóm tắt) | Kết quả/đầu ra chính |
|---|---|---|---|
| P01 | Khảo sát dự án | Đọc toàn bộ workspace, tóm tắt kiến trúc, chức năng đã làm, phần còn thiếu và mức độ hiểu dự án. | Tổng quan Spring Boot, JSP, SQL Server, phân quyền và kế hoạch tiếp tục. |
| P02 | Tổ chức nhóm | Ghi nhớ nhóm có hai thành viên; mỗi Day chia phần An/Đạt, dùng commit riêng và hướng dẫn push bằng một máy. | Quy ước phân công backend–frontend, tác giả commit và quy trình GitHub. |
| P03 | Lập kế hoạch | Phân tích đề tài website chuỗi trà sữa, đề xuất công nghệ và kế hoạch thực hiện theo từng Day. | Kế hoạch phát triển, kiến trúc ba lớp, mô hình dữ liệu và phạm vi chức năng. |
| P04 | Day 1 | Xây dựng use case, ERD, kiến trúc, package, luồng đặt hàng, thanh toán và thông báo. | `docs/01-analysis-design.md`. |
| P05 | Day 2 | Tạo Spring Boot WAR dùng JSP/JSTL, SiteMesh, Bootstrap, JPA, SQL Server; tạo entity, repository, layout và health check. | Khung dự án, cấu hình chạy, database ban đầu và README. |
| P06 | Day 2 – sửa lỗi | Kiểm tra lỗi SiteMesh/JSP do đường dẫn decorator bị lặp và sửa cấu hình. | Sửa `SiteMeshConfig`, trang JSP hiển thị đúng layout. |
| P07 | Day 3 | Làm đăng ký, đăng nhập, đăng xuất, JWT cookie HttpOnly, BCrypt, CSRF và phân quyền USER/STAFF/ADMIN. | Security, controller xác thực, giao diện auth và kiểm thử JWT. |
| P08 | Day 3 – sửa lỗi | Sửa chuyển hướng sai tới JSP nội bộ và lỗi đăng xuất 403 sau khi bật Spring Security. | Cho phép dispatcher nội bộ, cấu hình security context và CSRF phù hợp. |
| P09 | Day 4 | Xây dựng trang chủ, thực đơn, tìm kiếm không dấu, lọc/sắp xếp/phân trang, chi tiết sản phẩm và chi nhánh. | Catalog backend, JSP khách hàng và tài liệu kiểm tra. |
| P10 | Day 4 – sửa lỗi | Sửa tìm kiếm tiếng Việt không dấu, định dạng tiền Việt Nam và layout cho trang lỗi. | Truy vấn collation, locale `vi-VN`, trang 403/404 thống nhất. |
| P11 | Day 5 | Hoàn thiện CRUD sản phẩm, danh mục, topping; upload ảnh Cloudinary; kiểm tra định dạng/kích thước ảnh và ràng buộc xóa. | Chức năng quản trị catalog và tài liệu Day 5. |
| P12 | Day 5 – cấu hình | Giải thích `data.sql`, hướng dẫn lấy Cloud name/API key/API secret và xử lý lỗi upload Cloudinary. | Cấu hình local, không commit khóa bí mật; sửa thông báo upload. |
| P13 | Day 6 | Làm giỏ hàng: size, đường, đá, topping, số lượng; checkout, chọn chi nhánh, giao hàng và COD. | Transaction tạo đơn, snapshot giá/món, xóa giỏ và kiểm thử. |
| P14 | Day 6 – Git | Hướng dẫn chia danh sách file và commit riêng của An/Đạt; giải thích cảnh báo LF/CRLF và tạo pull request. | Hai commit có tên/email riêng, quy trình merge rõ ràng. |
| P15 | Day 7 | Cho khách theo dõi/hủy đơn; cho admin truy cập, xác nhận và cập nhật trạng thái đơn. | Trang đơn khách hàng, quản trị đơn và lịch sử trạng thái. |
| P16 | Day 8 | Xây dựng quy trình nhân viên theo chi nhánh: nhận đơn, pha chế, giao hàng, hoàn tất; sửa build và dữ liệu nhân viên Gò Vấp. | Staff workflow, giới hạn quyền theo chi nhánh và tài khoản mẫu. |
| P17 | Day 9 | Tích hợp COD/VNPay sandbox, ký HMACSHA512, Return/IPN, kiểm tra chữ ký/số tiền, chống callback lặp. | Dịch vụ VNPay, trang kết quả, chức năng thanh toán lại và test. |
| P18 | Day 9 – thanh toán | Cấu hình thông tin sandbox được cung cấp; giải thích QR/test card và lý do không thể thanh toán tiền thật bằng sandbox. | Cấu hình local, hướng dẫn kiểm thử an toàn và ghi rõ giới hạn sandbox. |
| P19 | Day 9 – liên hệ | Thêm trang liên hệ, số điện thoại, email và form đánh giá; tham khảo cách trình bày của Gong Cha. | Trang contact, lưu feedback và trang admin xem phản hồi. |
| P20 | Day 9 – menu | Trình bày danh mục thức uống bằng tên và hình đại diện tương tự menu Gong Cha. | Thẻ danh mục hình ảnh và lọc thực đơn. |
| P21 | Day 10 | Làm thông báo realtime bằng WebSocket/STOMP; lưu lịch sử, số chưa đọc, chống trùng và phân quyền kết nối. | Notification backend, JavaScript realtime và kiểm thử bảo mật WebSocket. |
| P22 | Day 10 – quy trình staff | Bỏ hộp thoại xác nhận bất tiện; tách đơn mới, đang pha chế và đang giao thành các bước/bảng rõ ràng. | Giao diện xử lý đơn theo từng giai đoạn, thông tin khách dễ đọc. |
| P23 | Day 10 – OTP đăng ký | Yêu cầu tài khoản mới xác minh OTP email trước khi được tạo; hướng dẫn Gmail App Password. | OTP có thời hạn, giới hạn nhập, gửi lại và cấu hình SMTP local. |
| P24 | Day 10 – admin | Hoàn thiện dashboard, quản lý chi nhánh, người dùng/nhân viên; loại bỏ mục trùng hoặc chưa sử dụng. | Thống kê quản trị và các trang CRUD cần thiết. |
| P25 | Day 10 – tài khoản | Thêm quên mật khẩu qua OTP email với điều kiện mật khẩu giống đăng ký. | Luồng yêu cầu OTP, xác minh và đặt lại mật khẩu an toàn. |
| P26 | Day 10 – giao hàng | Bắt buộc địa chỉ giao hàng đủ rõ khi khách chọn giao tận nơi. | Validation phía server và thông báo hướng dẫn nhập địa chỉ. |
| P27 | Day 10 – thông báo khách | Chỉ thông báo thân thiện khi cửa hàng nhận đơn và khi đơn đã tới. | Giới hạn sự kiện thông báo khách, nội dung dễ hiểu và có mã đơn. |
| P28 | Day 10 – tồn món | Cho STAFF ngừng/mở bán món; món hết vẫn hiển thị trên web với nhãn “Đã hết”. | Trạng thái sold-out, chặn thêm giỏ/checkout và giao diện tương ứng. |
| P29 | Day 10 – topping | Chỉ cho chọn topping đối với sản phẩm thuộc danh mục Trà sữa. | Cờ danh mục, kiểm tra backend và ẩn topping ở sản phẩm không hợp lệ. |
| P30 | Day 10 – sửa giỏ hàng | Sửa lỗi một ly có topping bị hiển thị thành nhiều dòng và làm tổng tiền tăng sai. | Loại bỏ join-fetch gây nhân bản, khử trùng DTO và thêm regression test. |
| P31 | Hoàn thiện | Rà soát toàn bộ source, SQL, chức năng còn thiếu, tài liệu AI, khóa bí mật; chạy test và push bản cuối. | 117/117 test đạt, đóng gói WAR, hồ sơ nộp và hai commit thành viên. |
| P32 | Hồ sơ database | Hợp nhất mọi SQL thành một file duy nhất và đổi tên thành `database/nhom9_Database.sql`. | Một file gồm 20 bảng, khóa/index và dữ liệu mẫu; cập nhật toàn bộ tài liệu. |
| P33 | Hồ sơ AI | Viết lại `prompts.md` và `AI_DATASHEET.md` ngắn gọn, đầy đủ, minh bạch và có tính học thuật. | Nhật ký prompt chuẩn hóa và datasheet AI cuối kỳ. |

## 4. Phương pháp kiểm chứng đầu ra AI

1. Đối chiếu thay đổi với yêu cầu nghiệp vụ của từng Day.
2. Kiểm tra quyền truy cập theo vai trò USER, STAFF và ADMIN.
3. Kiểm tra database SQL, khóa ngoại, dữ liệu mẫu và khả năng khởi động ứng dụng.
4. Chạy `.\mvnw.cmd clean test`; bản cuối đạt 117/117 kiểm thử.
5. Chạy `.\mvnw.cmd package` để xác nhận đóng gói WAR thành công.
6. Chạy thử các luồng chính trên trình duyệt và chỉ commit sau khi người dùng xác nhận.
7. Quét Git để bảo đảm không commit mật khẩu, App Password hoặc API secret thật.

## 5. Phân công trách nhiệm

- **Lâm Nhật An:** phân tích nghiệp vụ, backend, database, bảo mật, tích hợp dịch vụ, kiểm thử và quản lý nhánh Git.
- **Nguyễn Tiến Đạt:** JSP/CSS/JavaScript, trải nghiệm người dùng, giao diện quản trị/nhân viên và tài liệu kiểm tra.
- **Trợ lý AI:** hỗ trợ đề xuất và rà soát; không thay thế trách nhiệm kỹ thuật, học thuật hoặc quyết định cuối cùng của hai thành viên.
