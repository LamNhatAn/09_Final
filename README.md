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

3. **Tạo file cấu hình bí mật:** sao chép `application-local.properties.example` thành `application-local.properties` (cùng thư mục gốc) và điền mật khẩu. File này đã nằm trong `.gitignore`, **không commit**.

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
| http://localhost:8080/ | Trang chủ (layout khách), hiện số chi nhánh 3, danh mục 4, sản phẩm 12, người dùng 5 |
| http://localhost:8080/staff/dashboard | Trang tạm, layout nhân viên (thanh điều hướng xanh) |
| http://localhost:8080/admin/dashboard | Trang tạm, layout quản trị (thanh bên tối) |
| http://localhost:8080/health | JSON `{"status":"UP","database":"utetra",...}` |

## Tài khoản mẫu (mật khẩu chung: `Utetra@123`)

| Email | Vai trò | Chi nhánh |
|---|---|---|
| admin@utetra.vn | ADMIN | |
| staff1@utetra.vn | STAFF | UTETra Võ Văn Ngân |
| staff2@utetra.vn | STAFF | UTETra Quận 1 |
| user1@utetra.vn | USER | |
| user2@utetra.vn | USER | |

Chức năng đăng nhập sẽ làm ở Day 3.

## Cấu trúc thư mục

```
UTETra/
├── pom.xml, mvnw, mvnw.cmd
├── application-local.properties.example   (mẫu cấu hình bí mật)
├── database/                              (schema.sql, data.sql)
├── docs/                                  (thiết kế, nhật ký prompt)
└── src/main/
    ├── java/com/utetra/
    │   ├── config/        SecurityConfig, SiteMeshConfig
    │   ├── entity/        16 entity + enums/
    │   ├── repository/    16 repository
    │   └── controller/    web/ admin/ staff/
    ├── resources/         application.properties, static/css
    └── webapp/WEB-INF/
        ├── decorators/    main.jsp, staff.jsp, admin.jsp (SiteMesh)
        └── views/         home.jsp, admin/, staff/
```

Xem thiết kế chi tiết tại [docs/01-analysis-design.md](docs/01-analysis-design.md).
