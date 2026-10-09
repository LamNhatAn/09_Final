# Day 2 - Dựng khung dự án

## Luồng xử lý một request (vì sao layout tự được áp dụng)

```mermaid
sequenceDiagram
    actor B as Trình duyệt
    participant F as Spring Security Filter
    participant S as SiteMesh Filter
    participant C as HomeController
    participant R as Repository (JPA)
    participant D as SQL Server
    participant V as home.jsp (nội dung)
    participant L as main.jsp (layout)

    B->>F: GET /
    F->>S: cho qua (Day 2: permitAll)
    S->>C: chuyển request, bắt đệm (buffer) phản hồi HTML
    C->>R: count() các bảng
    R->>D: SELECT COUNT(*)
    D-->>R: số liệu
    C-->>V: trả view "home" + model
    V-->>S: HTML nội dung (title, body)
    S->>L: gộp title/body vào layout
    L-->>B: HTML hoàn chỉnh (navbar + nội dung + footer)
```

## Chọn layout theo đường dẫn (`SiteMeshConfig`)

| Đường dẫn | Layout |
|---|---|
| `/admin/*` | `WEB-INF/decorators/admin.jsp` |
| `/staff/*` | `WEB-INF/decorators/staff.jsp` |
| còn lại | `WEB-INF/decorators/main.jsp` |

Trang con (ví dụ `home.jsp`) chỉ chứa `<title>` và nội dung `<body>`. SiteMesh chèn chúng vào `<sitemesh:write property='title'/>` và `<sitemesh:write property='body'/>` của layout.

## Những điểm hay bị hỏi

- **Vì sao đóng gói WAR?** Spring Boot JAR không hỗ trợ JSP tốt; WAR chạy được cả bằng `java -jar` lẫn trên Tomcat ngoài (nhờ `SpringBootServletInitializer`).
- **`ddl-auto=validate`:** Hibernate chỉ kiểm tra entity khớp bảng do `schema.sql` tạo, không tự sửa DB. Lệch thì ứng dụng báo lỗi ngay lúc khởi động.
- **`use_nationalized_character_data=true`:** chuỗi Java ánh xạ sang `NVARCHAR`, lưu được tiếng Việt có dấu.
- **Bí mật nằm ngoài Git:** `application-local.properties` (đã `.gitignore`) chứa mật khẩu DB; `application.properties` chỉ có placeholder `${DB_PASSWORD:}`.
- **Bootstrap qua WebJar:** file CSS/JS nằm trong thư viện Maven nên chạy được khi không có Internet.
