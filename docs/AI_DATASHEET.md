# DATASHEET SỬ DỤNG AI – DỰ ÁN UTETra

## 1. Thông tin chung

| Thuộc tính | Nội dung |
|---|---|
| Tên dự án | UTETra – Website quản lý và bán hàng cho chuỗi trà sữa |
| Nhóm | Nhóm 9 |
| Thành viên | Lâm Nhật An; Nguyễn Tiến Đạt |
| Ngày hoàn thiện hồ sơ | 30/10/2026 |
| Hình thức sử dụng AI | Trợ lý hỗ trợ phát triển phần mềm |
| AI có chạy trong website | Không |
| Chức năng ra quyết định tự động bằng AI | Không |
| Dataset huấn luyện hoặc tinh chỉnh riêng | Không |
| Hình ảnh do AI tạo | Không |
| Nhật ký yêu cầu | `docs/prompts.md` |

## 2. Tuyên bố phạm vi

UTETra **không tích hợp mô hình AI vào sản phẩm vận hành**. Website không gọi API AI, không có chatbot AI, không thực hiện dự đoán, phân loại, gợi ý bằng mô hình học máy hoặc ra quyết định tự động đối với khách hàng.

Các chức năng tìm kiếm, gợi ý món cùng danh mục, tính giá, phân quyền, xử lý đơn, OTP, thanh toán và thông báo realtime được thực hiện bằng quy tắc nghiệp vụ, truy vấn SQL và dịch vụ hạ tầng thông thường.

AI chỉ được sử dụng trong giai đoạn phát triển để hỗ trợ nhóm phân tích yêu cầu, đề xuất mã, tìm lỗi, viết kiểm thử, chuẩn hóa tài liệu và hướng dẫn thao tác Git.

## 3. Công cụ và mục đích sử dụng

| Hoạt động | Mức độ hỗ trợ của AI | Quyết định cuối cùng |
|---|---|---|
| Phân tích yêu cầu và lập kế hoạch theo Day | Đề xuất cấu trúc và thứ tự thực hiện | Nhóm xác nhận phạm vi |
| Thiết kế kiến trúc và database | Đề xuất entity, quan hệ, ràng buộc và luồng dữ liệu | Nhóm rà soát và chạy SQL |
| Sinh/chỉnh sửa mã nguồn | Đề xuất Java, JSP, CSS, JavaScript và cấu hình | Nhóm chạy test và duyệt commit |
| Tìm và sửa lỗi | Phân tích log, ảnh chụp và hành vi tái hiện | Nhóm chạy lại trên môi trường local |
| Kiểm thử | Đề xuất unit/regression test và tình huống biên | Maven/Surefire thực thi |
| Viết tài liệu | Chuẩn hóa README, hướng dẫn Day, prompt log và hồ sơ nộp | Nhóm kiểm tra nội dung |
| Git/GitHub | Đề xuất cách chia commit, tác giả và quy trình push/merge | Thành viên thực hiện hoặc cho phép thực hiện |

## 4. Dữ liệu cung cấp cho trợ lý AI

### 4.1. Dữ liệu kỹ thuật

- Source code trong workspace của dự án.
- Cấu trúc database, dữ liệu mẫu và thông báo lỗi build/runtime.
- Ảnh chụp giao diện, terminal và quy trình xử lý đơn.
- Yêu cầu nghiệp vụ do nhóm mô tả.
- Đường dẫn tài liệu công khai của Cloudinary, VNPay và website tham khảo giao diện.
- Video tham khảo cục bộ do nhóm chỉ định cho quy trình nhân viên.

### 4.2. Dữ liệu cá nhân và thông tin nhạy cảm

- Dữ liệu sử dụng để phát triển chủ yếu là tài khoản và đơn hàng mẫu.
- Nhóm không cung cấp dataset khách hàng thật để huấn luyện hoặc tinh chỉnh AI.
- Một số thông tin cấu hình dịch vụ thử nghiệm được cung cấp để hướng dẫn cấu hình local. Các giá trị bí mật không được ghi lại trong tài liệu này và không được commit.
- `application-local.properties` được đưa vào `.gitignore`; repository chỉ chứa file cấu hình mẫu.
- Trước khi triển khai thực tế, nhóm cần đổi mọi khóa từng chia sẻ trong môi trường hỗ trợ và sử dụng secret manager hoặc biến môi trường.

## 5. Đầu ra do AI hỗ trợ

AI đã hỗ trợ tạo hoặc chỉnh sửa:

- Tài liệu phân tích, thiết kế và hướng dẫn theo Day.
- Mã Java cho controller, service, repository, DTO, entity và cấu hình bảo mật.
- Giao diện JSP, CSS và JavaScript.
- SQL Server schema/dữ liệu mẫu, sau cùng hợp nhất thành `database/nhom9_Database.sql`.
- Kiểm thử tự động cho xác thực, catalog, giỏ hàng, checkout, đơn hàng, quản trị, OTP, thông báo và VNPay.
- Lệnh kiểm tra, build, commit, push và hợp nhất nhánh Git.

Đầu ra AI không được xem là đúng mặc định; chỉ những thay đổi đã được nhóm kiểm tra mới được giữ lại.

## 6. Kiểm soát chất lượng và giám sát con người

| Biện pháp | Cách thực hiện |
|---|---|
| Kiểm tra chức năng | Chạy thử trực tiếp theo vai trò khách, USER, STAFF và ADMIN |
| Kiểm thử tự động | `.\mvnw.cmd clean test`; bản cuối đạt 117/117 test |
| Kiểm tra đóng gói | `.\mvnw.cmd package` tạo WAR thành công |
| Kiểm tra database | Xác nhận file SQL thống nhất có 20 bảng, khóa/index và dữ liệu mẫu |
| Kiểm tra bảo mật | JWT HttpOnly, BCrypt, CSRF, phân quyền URL/WebSocket và validation phía server |
| Kiểm tra thanh toán | Chữ ký, merchant, số tiền, mã giao dịch và callback lặp trên VNPay sandbox |
| Kiểm tra bí mật | Không track file local; rà soát giá trị nhạy cảm trước khi push |
| Kiểm tra tác giả | Commit riêng cho Lâm Nhật An và Nguyễn Tiến Đạt |

## 7. Rủi ro và biện pháp giảm thiểu

| Rủi ro | Biện pháp |
|---|---|
| AI đề xuất mã sai hoặc thiếu trường hợp biên | Viết test, chạy build, tái hiện lỗi và kiểm tra thủ công |
| AI hiểu sai yêu cầu nghiệp vụ | Chia công việc theo Day và yêu cầu người dùng xác nhận sau mỗi chức năng |
| Lộ khóa bí mật qua source hoặc tài liệu | Dùng file local bị ignore, placeholder và quét repository |
| Tham chiếu thư viện/API lỗi thời | Dùng phiên bản cố định trong `pom.xml` và kiểm tra bằng build thực tế |
| Giao diện giống website tham khảo quá mức | Chỉ tham khảo bố cục/chức năng; tự triển khai nội dung và nhận diện UTETra |
| Phụ thuộc dịch vụ ngoài | Ghi rõ Cloudinary, SMTP và VNPay cần khóa hợp lệ; VNPay chỉ là sandbox |
| Người đọc hiểu nhầm hệ thống có AI vận hành | Tuyên bố rõ AI chỉ hỗ trợ phát triển, không nằm trong kiến trúc runtime |

## 8. Giới hạn và khả năng tái lập

- Nhật ký prompt là bản tóm tắt học thuật, không phải bản sao nguyên văn của toàn bộ hội thoại.
- Kết quả có thể thay đổi theo phiên bản công cụ AI, thư viện và môi trường local.
- Database nộp bài là bản cài mới; chạy lại sẽ xóa dữ liệu hiện có.
- OTP email, Cloudinary và VNPay phụ thuộc cấu hình của người chạy.
- VNPay sử dụng sandbox nên không đại diện cho giao dịch tiền thật.
- Việc tái lập được hỗ trợ bằng source code, `pom.xml`, Maven Wrapper, SQL thống nhất, README và lịch sử Git.

## 9. Trách nhiệm và kết luận

Lâm Nhật An và Nguyễn Tiến Đạt chịu trách nhiệm đối với mã nguồn, dữ liệu mẫu, tài liệu, kiểm thử và kết quả nộp bài. Trợ lý AI chỉ là công cụ hỗ trợ, không phải thành viên nhóm, tác giả học thuật độc lập hoặc chủ thể chịu trách nhiệm.

Việc sử dụng AI trong dự án được công bố minh bạch qua tài liệu này và `docs/prompts.md`. Không có mô hình AI, dataset huấn luyện, trọng số mô hình hoặc dữ liệu khách hàng được triển khai trong website UTETra.
