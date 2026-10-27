# UTETra - Website chuỗi trà sữa

Spring Boot 3 (WAR) + JSP/JSTL + SiteMesh 3 + Bootstrap 5 + Spring Data JPA + SQL Server.
Thanh toán: COD và VNPay (sandbox). Thông báo realtime và chat bằng WebSocket. Ảnh sản phẩm lưu trên Cloudinary.

## Yêu cầu cài đặt

- JDK 17 (đặt `JAVA_HOME` trỏ tới JDK 17)
- SQL Server 2019 trở lên, bật TCP/IP cổng 1433, chế độ xác thực **SQL Server and Windows (Mixed Mode)**
- Không cần cài Maven (đã có Maven Wrapper `mvnw`)

## Cài đặt lần đầu

1. **Tạo database và dữ liệu mẫu** (chạy ở thư mục gốc dự án):

   ```powershell
   sqlcmd -S localhost -E -C -f 65001 -i database\schema.sql
   sqlcmd -S localhost -E -C -f 65001 -i database\data.sql
   ```

   `-E` là xác thực Windows, `-f 65001` để đọc đúng tiếng Việt (UTF-8). Chạy lại `schema.sql` sẽ xóa và tạo lại toàn bộ bảng.
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
| http://localhost:8080/menu?category=3 | Chỉ 3 món cà phê |
| http://localhost:8080/menu?q=tra sua | Tìm không dấu: ra 4 món trà sữa |
| http://localhost:8080/products/1 | Chi tiết Trà sữa trân châu đen: giá 35.000 đ, size S/M/L, 6 topping, món cùng loại |
| http://localhost:8080/products/999 | Trang 404 có thanh menu |
| http://localhost:8080/branches | 3 chi nhánh, giờ mở cửa, nhãn Đang mở/Đã đóng, link bản đồ |
| http://localhost:8080/login | Form đăng nhập |
| http://localhost:8080/register | Form đăng ký |
| http://localhost:8080/admin/dashboard | Chưa đăng nhập: chuyển về `/login`. USER/STAFF: trang 403. ADMIN: trang quản trị |
| http://localhost:8080/admin/products | ADMIN: danh sách 12 sản phẩm, thêm/sửa/ẩn/xóa, upload ảnh Cloudinary |
| http://localhost:8080/admin/categories | ADMIN: 4 danh mục kèm số sản phẩm, thêm/sửa/ẩn/xóa |
| http://localhost:8080/admin/toppings | ADMIN: 6 topping, thêm/sửa/ẩn/xóa |
| http://localhost:8080/cart | USER: xem, cập nhật hoặc xóa món trong giỏ hàng |
| http://localhost:8080/checkout | USER: chọn chi nhánh, nhập thông tin giao hàng và đặt đơn COD |
| http://localhost:8080/orders | USER: danh sách đơn của mình, phân trang và sắp mới nhất trước |
| http://localhost:8080/orders/{code} | USER: chi tiết, lịch sử trạng thái và hủy đơn còn chờ xác nhận |
| http://localhost:8080/admin/orders | ADMIN: lọc/tìm toàn bộ đơn, xem chi tiết và chuyển trạng thái đúng quy trình |
| http://localhost:8080/staff/orders | STAFF: chỉ xem, lọc, tìm và xử lý đơn thuộc chi nhánh được gán |
| http://localhost:8080/staff/dashboard | STAFF/ADMIN: chuyển tới trang xử lý đơn; USER bị từ chối quyền |
| http://localhost:8080/health | JSON `{"status":"UP","database":"utetra",...}` |

Các bước kiểm tra đăng nhập/phân quyền từng bước: xem [docs/03-auth.md](docs/03-auth.md). Trang thực đơn: xem [docs/04-catalog.md](docs/04-catalog.md). Quản trị sản phẩm, danh mục, topping, upload ảnh: xem [docs/05-admin-catalog.md](docs/05-admin-catalog.md). Giỏ hàng và đặt hàng COD: xem [docs/06-cart-checkout.md](docs/06-cart-checkout.md). Theo dõi và hủy đơn của khách: xem [docs/07-customer-orders.md](docs/07-customer-orders.md). Nhân viên xử lý đơn theo chi nhánh: xem [docs/08-staff-order-workflow.md](docs/08-staff-order-workflow.md).

## Tài khoản mẫu (mật khẩu chung: `Utetra@123`)

| Email | Vai trò | Chi nhánh |
|---|---|---|
| admin@utetra.vn | ADMIN | |
| staff1@utetra.vn | STAFF | UTETra Võ Văn Ngân |
| staff2@utetra.vn | STAFF | UTETra Quận 1 |
| staff3@utetra.vn | STAFF | UTETra Gò Vấp |
| user1@utetra.vn | USER | |
| user2@utetra.vn | USER | |

Đăng nhập tại `/login`. Tài khoản đăng ký mới luôn có vai trò USER; STAFF và ADMIN do quản trị tạo (Day 10).

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

Xem thiết kế chi tiết tại [docs/01-analysis-design.md](docs/01-analysis-design.md).
