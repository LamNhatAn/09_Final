# UTETra - Website chuỗi trà sữa

Spring Boot 3 (WAR) + JSP/JSTL + SiteMesh 3 + Bootstrap 5 + Spring Data JPA + SQL Server + WebSocket/STOMP.
Thanh toán: COD và VNPay (sandbox). Thông báo realtime bằng WebSocket. Ảnh sản phẩm lưu trên Cloudinary.

## Yêu cầu cài đặt

- JDK 17 (đặt `JAVA_HOME` trỏ tới JDK 17)
- SQL Server 2019 trở lên, bật TCP/IP cổng 1433, chế độ xác thực **SQL Server and Windows (Mixed Mode)**
- Không cần cài Maven (đã có Maven Wrapper `mvnw`)

## Cài đặt lần đầu

1. **Tạo database và dữ liệu mẫu** (chạy ở thư mục gốc dự án):

   ```powershell
   sqlcmd -S localhost -E -C -f 65001 -i database\UTETra_Final.sql
   ```

   `UTETra_Final.sql` là file nộp database thống nhất, chứa cả cấu trúc và dữ liệu mẫu. `-E` là xác thực Windows, `-f 65001` để đọc đúng tiếng Việt (UTF-8). Chạy lại file này sẽ xóa và tạo lại toàn bộ bảng.
   Nếu dùng SSMS: mở file bằng *File > Open > File*, chọn encoding UTF-8 rồi bấm Execute.

2. **Tạo tài khoản SQL cho ứng dụng** (chạy 1 lần, bằng sqlcmd/SSMS với quyền quản trị):

   ```sql
   CREATE LOGIN utetra_app WITH PASSWORD = 'MatKhauCuaBan', CHECK_POLICY = OFF;
   USE utetra;
   CREATE USER utetra_app FOR LOGIN utetra_app;
   ALTER ROLE db_owner ADD MEMBER utetra_app;
   ```

3. **Tạo file cấu hình bí mật:** sao chép `application-local.properties.example` thành `application-local.properties` (cùng thư mục gốc), điền mật khẩu DB và đặt `JWT_SECRET` là một chuỗi ngẫu nhiên dài ít nhất 32 ký tự (thiếu thì ứng dụng không khởi động). File này đã nằm trong `.gitignore`, **không commit**.

4. **Cloudinary (lưu ảnh sản phẩm):** đăng nhập https://console.cloudinary.com, vào **Dashboard → API Keys**, rồi điền 3 giá trị vào `application-local.properties`:

   ```properties
   cloudinary.cloud-name=TEN_CLOUD_CUA_BAN
   cloudinary.api-key=API_KEY
   cloudinary.api-secret=API_SECRET
   ```

   Mỗi thành viên tự điền trên máy mình, **không gửi `api-secret` cho ai và không commit**. Nếu để trống, web vẫn chạy bình thường, chỉ không upload được ảnh (form sản phẩm sẽ báo "Chưa cấu hình Cloudinary").

5. **VNPay sandbox:** lấy `TmnCode` và `HashSecret` trong tài khoản/email sandbox rồi thêm vào `application-local.properties`:

   ```properties
   vnpay.tmn-code=MA_WEBSITE_SANDBOX
   vnpay.hash-secret=KHOA_BI_MAT_SANDBOX
   vnpay.return-url=http://localhost:8080/payments/vnpay/return
   ```

   Không commit `HashSecret`. Khi triển khai thật, đăng ký IPN HTTPS công khai trỏ tới `/payments/vnpay/ipn`. Nếu chưa cấu hình, lựa chọn VNPay trên checkout sẽ bị khóa nhưng COD vẫn hoạt động.

6. **Email gửi OTP đăng ký:** với Gmail, bật xác minh 2 bước rồi tạo **App Password**; thêm vào `application-local.properties`:

   ```properties
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your_email@gmail.com
   MAIL_PASSWORD=APP_PASSWORD_16_KY_TU
   MAIL_FROM=
   ```

   `MAIL_PASSWORD` không phải mật khẩu đăng nhập Gmail và tuyệt đối không được commit. Nếu chưa cấu hình, website vẫn chạy nhưng form đăng ký sẽ báo không gửi được OTP.

   Với database đã tạo từ trước, chạy thêm một lần:

   ```powershell
   sqlcmd -S localhost -E -C -f 65001 -i database\day10-add-password-reset-otps.sql
   sqlcmd -S localhost -E -C -f 65001 -i database\day10-add-product-sold-out.sql
   sqlcmd -S localhost -E -C -f 65001 -i database\day10-add-category-topping-rule.sql
   ```

## Chạy ứng dụng

```powershell
.\mvnw.cmd spring-boot:run
```

Hoặc đóng gói rồi chạy WAR:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\utetra.war
```

Chạy lệnh ở thư mục gốc dự án để ứng dụng đọc được `application-local.properties`.

## Kiểm tra

| URL | Kết quả mong đợi |
|---|---|
| http://localhost:8080/ | Trang chủ: ô tìm kiếm, danh mục, 8 đồ uống mới nhất, 3 chi nhánh |
| http://localhost:8080/menu | Thực đơn 12 đồ uống, 8 sản phẩm/trang, có phân trang (trang 2 có 4 sản phẩm) |
| http://localhost:8080/contact | Thông tin điện thoại/email và form đánh giá website, thức uống, phục vụ |
| http://localhost:8080/menu?category=3 | Chỉ 3 món cà phê |
| http://localhost:8080/menu?q=tra sua | Tìm không dấu: ra 4 món trà sữa |
| http://localhost:8080/products/1 | Chi tiết Trà sữa trân châu đen: giá 35.000 đ, size S/M/L, 6 topping, món cùng loại |
| http://localhost:8080/products/5 | Trà trái cây: không hiển thị và không cho thêm topping |
| http://localhost:8080/products/999 | Trang 404 có thanh menu |
| http://localhost:8080/branches | 3 chi nhánh, giờ mở cửa, nhãn Đang mở/Đã đóng, link bản đồ |
| http://localhost:8080/login | Form đăng nhập |
| http://localhost:8080/forgot-password | Gửi OTP email và đặt lại mật khẩu khách hàng |
| http://localhost:8080/register | Form đăng ký; gửi OTP email trước khi tạo tài khoản |
| http://localhost:8080/register/verify | Nhập OTP 6 số, gửi lại mã sau 60 giây |
| http://localhost:8080/admin/dashboard | ADMIN: doanh thu, số đơn, khách hàng, nhân viên, chi nhánh và đơn mới nhất |
| http://localhost:8080/admin/branches | ADMIN: thêm/sửa/kích hoạt/ngừng hoạt động chi nhánh |
| http://localhost:8080/admin/users | ADMIN: tìm kiếm, tạo nhân viên, sửa, khóa/mở khóa tài khoản |
| http://localhost:8080/admin/products | ADMIN: danh sách 12 sản phẩm, thêm/sửa/ẩn/xóa, upload ảnh Cloudinary |
| http://localhost:8080/admin/categories | ADMIN: 4 danh mục kèm số sản phẩm, thêm/sửa/ẩn/xóa |
| http://localhost:8080/admin/toppings | ADMIN: 6 topping, thêm/sửa/ẩn/xóa |
| http://localhost:8080/cart | USER: xem, cập nhật hoặc xóa món trong giỏ hàng |
| http://localhost:8080/checkout | USER: chọn chi nhánh, thông tin giao hàng và thanh toán COD hoặc VNPay sandbox |
| http://localhost:8080/payments/vnpay/return | Kết quả VNPay có kiểm tra chữ ký, số tiền và mã giao dịch |
| http://localhost:8080/orders | USER: danh sách đơn của mình, phân trang và sắp mới nhất trước |
| http://localhost:8080/orders/{code} | USER: chi tiết, lịch sử trạng thái và hủy đơn còn chờ xác nhận |
| http://localhost:8080/admin/orders | ADMIN: lọc/tìm toàn bộ đơn, xem chi tiết và chuyển trạng thái đúng quy trình |
| http://localhost:8080/admin/feedback | ADMIN: xem ý kiến khách hàng mới nhất, số sao và thông tin liên hệ |
| http://localhost:8080/api/notifications | Tài khoản đã đăng nhập: JSON gồm thông báo mới nhất và số chưa đọc |
| http://localhost:8080/staff/orders | STAFF: chỉ xem, lọc, tìm và xử lý đơn thuộc chi nhánh được gán |
| http://localhost:8080/staff/products | STAFF: đánh dấu món tạm hết hoặc mở bán lại; món hết vẫn hiện trên menu khách |
| http://localhost:8080/staff/dashboard | STAFF/ADMIN: chuyển tới trang xử lý đơn; USER bị từ chối quyền |
| http://localhost:8080/health | JSON `{"status":"UP","database":"utetra",...}` |

Các bước kiểm tra đăng nhập/phân quyền từng bước: xem [docs/03-auth.md](docs/03-auth.md). Trang thực đơn: xem [docs/04-catalog.md](docs/04-catalog.md). Quản trị sản phẩm, danh mục, topping, upload ảnh: xem [docs/05-admin-catalog.md](docs/05-admin-catalog.md). Giỏ hàng và đặt hàng COD: xem [docs/06-cart-checkout.md](docs/06-cart-checkout.md). Theo dõi và hủy đơn của khách: xem [docs/07-customer-orders.md](docs/07-customer-orders.md). Nhân viên xử lý đơn theo chi nhánh: xem [docs/08-staff-order-workflow.md](docs/08-staff-order-workflow.md). Thanh toán VNPay sandbox: xem [docs/09-vnpay-sandbox.md](docs/09-vnpay-sandbox.md).

## Tài khoản mẫu (mật khẩu chung: `Utetra@123`)

| Email | Vai trò | Chi nhánh |
|---|---|---|
| admin@utetra.vn | ADMIN | |
| staff1@utetra.vn | STAFF | UTETra Võ Văn Ngân |
| staff2@utetra.vn | STAFF | UTETra Quận 1 |
| staff3@utetra.vn | STAFF | UTETra Gò Vấp |
| user1@utetra.vn | USER | |
| user2@utetra.vn | USER | |

Đăng nhập tại `/login`. Tài khoản đăng ký mới chỉ được tạo sau khi xác minh OTP email và luôn có vai trò USER; STAFF và ADMIN do quản trị tạo.

## Cấu trúc thư mục

```
UTETra/
├── pom.xml, mvnw, mvnw.cmd
├── application-local.properties.example   (mẫu cấu hình bí mật)
├── database/                              (schema.sql, data.sql)
├── docs/                                  (thiết kế, nhật ký prompt)
└── src/main/
    ├── java/com/utetra/
    │   ├── config/        SecurityConfig, SiteMeshConfig, LocaleConfig
    │   ├── security/      JwtService, JwtAuthenticationFilter, AuthCookieService, AppUserDetails(+Service)
    │   ├── dto/           Các form đăng nhập, quản trị, giỏ hàng, checkout và view model
    │   ├── service/       Auth, Catalog, Admin catalog, Cart, Checkout, CustomerOrder, ImageStorage
    │   ├── entity/        16 entity + enums/
    │   ├── repository/    16 repository
    │   └── controller/    web/ (Home, Menu, Product, Cart, Checkout, Order, Branch, Auth...) admin/ staff/
    ├── resources/         application.properties, messages.properties, static/css
    └── webapp/WEB-INF/
        ├── decorators/    main.jsp, staff.jsp, admin.jsp (SiteMesh)
        └── views/         home.jsp, menu.jsp, product-detail.jsp, branches.jsp, fragments/, auth/, error/, admin/ (products/, categories/, toppings/), staff/
```

Thông báo realtime và cách kiểm tra Day 10: xem [docs/10-realtime-notifications.md](docs/10-realtime-notifications.md).

Xem thiết kế chi tiết tại [docs/01-analysis-design.md](docs/01-analysis-design.md).

Danh mục hồ sơ nộp cuối kỳ, danh sách chức năng đầy đủ và hướng dẫn kiểm tra: xem [SUBMISSION.md](SUBMISSION.md).
