# UTETra - Phân tích & Thiết kế (Day 1)

Website chuỗi trà sữa UTETra. Công nghệ: Spring Boot 3 (WAR) + JSP/JSTL + SiteMesh 3 + Bootstrap 5 + JPA + SQL Server + JWT (HttpOnly cookie) + WebSocket (STOMP) + Cloudinary + VNPay sandbox.

> Phạm vi thanh toán: **COD** và **VNPay (sandbox)**. Không làm MoMo.

## 1. Vai trò (Actor)

| Vai trò | Mô tả |
|---|---|
| GUEST | Chưa đăng nhập: xem menu, chi tiết sản phẩm, chi nhánh, đăng ký, đăng nhập |
| USER | Khách hàng: giỏ hàng, đặt hàng, thanh toán, theo dõi đơn, chat hỗ trợ |
| STAFF | Nhân viên của **1 chi nhánh**: xử lý đơn của chi nhánh mình, chat với khách |
| ADMIN | Quản trị toàn hệ thống: menu, chi nhánh, tài khoản, mọi đơn hàng, doanh thu |

### Ma trận phân quyền

| Chức năng | GUEST | USER | STAFF | ADMIN |
|---|:-:|:-:|:-:|:-:|
| Xem menu / chi tiết / chi nhánh | ✔ | ✔ | ✔ | ✔ |
| Đăng ký / đăng nhập | ✔ | | | |
| Hồ sơ cá nhân | | ✔ | ✔ | ✔ |
| Giỏ hàng, đặt hàng, thanh toán | | ✔ | | |
| Xem / hủy đơn của mình | | ✔ | | |
| Chat hỗ trợ (phía khách) | | ✔ | | |
| Xem và đổi trạng thái đơn **chi nhánh mình** | | | ✔ | |
| Chat hỗ trợ (phía nhân viên) | | | ✔ | ✔ |
| Xem đơn **mọi chi nhánh** | | | | ✔ |
| CRUD danh mục / sản phẩm / topping (+ upload Cloudinary) | | | | ✔ |
| CRUD chi nhánh, quản lý user/staff (gán chi nhánh) | | | | ✔ |
| Dashboard doanh thu | | | | ✔ |

## 2. Use case (sơ đồ)

```mermaid
flowchart LR
    G([GUEST]) --> UC1[Xem menu / tìm kiếm]
    G --> UC2[Xem chi nhánh]
    G --> UC3[Đăng ký / Đăng nhập]

    U([USER]) --> UC1
    U --> UC4[Quản lý giỏ hàng]
    U --> UC5[Đặt hàng COD]
    U --> UC6[Thanh toán VNPay]
    U --> UC7[Theo dõi / hủy đơn]
    U --> UC8[Chat hỗ trợ]
    U --> UC9[Cập nhật hồ sơ]

    S([STAFF]) --> UC10[Nhận thông báo đơn mới realtime]
    S --> UC11[Xử lý đơn chi nhánh]
    S --> UC12[Chat với khách]

    A([ADMIN]) --> UC13[Quản lý menu + upload ảnh Cloudinary]
    A --> UC14[Quản lý chi nhánh]
    A --> UC15[Quản lý user / staff]
    A --> UC16[Xem tất cả đơn hàng]
    A --> UC17[Dashboard doanh thu]
```

## 3. Kiến trúc hệ thống

```mermaid
flowchart TB
    Browser["Trình duyệt (JSP + Bootstrap + JS)"]
    subgraph SB["Spring Boot (WAR)"]
        Filter["Security Filter<br/>(JWT từ HttpOnly cookie)"]
        Ctrl["Controller (MVC + REST)"]
        Svc["Service (nghiệp vụ)"]
        Repo["Repository (Spring Data JPA)"]
        WS["WebSocket / STOMP broker"]
        Deco["SiteMesh decorator<br/>(main / staff / admin)"]
    end
    DB[("SQL Server")]
    CLD["Cloudinary<br/>(lưu ảnh sản phẩm)"]
    VNP["VNPay Sandbox"]

    Browser -->|HTTP| Filter --> Ctrl --> Svc --> Repo --> DB
    Ctrl --> Deco -->|HTML| Browser
    Browser <-->|"WebSocket /ws"| WS
    Svc --> WS
    Svc -->|upload ảnh| CLD
    Svc -->|tạo URL thanh toán| VNP
    VNP -->|"Return URL / IPN"| Ctrl
```

Giải thích ngắn (để trả lời khi bị hỏi):
- **Mô hình 3 lớp**: Controller nhận request, Service chứa nghiệp vụ, Repository làm việc với DB.
- **SiteMesh** bọc mỗi trang JSP vào layout chung: giao diện khách (`main`), nhân viên (`staff`), quản trị (`admin`), nên không lặp header/footer.
- **JWT trong HttpOnly cookie**: JS không đọc được token nên khó bị XSS đánh cắp; hợp với web render server (JSP).
- **WebSocket/STOMP**: server chủ động đẩy dữ liệu (đơn mới, đổi trạng thái, tin nhắn) mà client không cần F5.
- **Cloudinary**: ảnh nằm trên cloud, DB chỉ lưu URL và `public_id` (để xóa/ghi đè ảnh).

## 4. Sơ đồ ERD (SQL Server)

```mermaid
erDiagram
    BRANCHES ||--o{ USERS : "nhân viên thuộc"
    BRANCHES ||--o{ ORDERS : "nhận đơn"
    USERS ||--o{ ORDERS : "đặt"
    USERS ||--o| CARTS : "có"
    CARTS ||--o{ CART_ITEMS : "gồm"
    CATEGORIES ||--o{ PRODUCTS : "chứa"
    PRODUCTS ||--o{ CART_ITEMS : "được thêm"
    SIZES ||--o{ CART_ITEMS : "chọn"
    CART_ITEMS }o--o{ TOPPINGS : "cart_item_toppings"
    ORDERS ||--|{ ORDER_ITEMS : "gồm"
    ORDER_ITEMS ||--o{ ORDER_ITEM_TOPPINGS : "gồm"
    ORDERS ||--o{ ORDER_STATUS_HISTORY : "lịch sử"
    ORDERS ||--o{ PAYMENTS : "thanh toán"
    USERS ||--o{ CHAT_CONVERSATIONS : "khách"
    BRANCHES ||--o{ CHAT_CONVERSATIONS : "chi nhánh"
    CHAT_CONVERSATIONS ||--o{ CHAT_MESSAGES : "gồm"

    BRANCHES {
        bigint id PK
        nvarchar name
        nvarchar address
        varchar phone
        time open_time
        time close_time
        bit active
    }
    USERS {
        bigint id PK
        nvarchar full_name
        varchar email UK
        varchar password_hash
        varchar phone
        nvarchar address
        varchar role "USER/STAFF/ADMIN"
        bigint branch_id FK "chỉ STAFF"
        bit active
        datetime2 created_at
    }
    CATEGORIES {
        bigint id PK
        nvarchar name
        nvarchar description
        bit active
    }
    PRODUCTS {
        bigint id PK
        bigint category_id FK
        nvarchar name
        nvarchar description
        decimal base_price
        varchar image_url
        varchar image_public_id
        bit available
        datetime2 created_at
    }
    SIZES {
        bigint id PK
        varchar name "S/M/L"
        decimal extra_price
    }
    TOPPINGS {
        bigint id PK
        nvarchar name
        decimal price
        bit available
    }
    CARTS {
        bigint id PK
        bigint user_id FK
    }
    CART_ITEMS {
        bigint id PK
        bigint cart_id FK
        bigint product_id FK
        bigint size_id FK
        int sugar_percent
        int ice_percent
        int quantity
        nvarchar note
    }
    ORDERS {
        bigint id PK
        varchar code UK
        bigint user_id FK
        bigint branch_id FK
        varchar status
        varchar payment_method "COD/VNPAY"
        varchar payment_status "UNPAID/PAID/FAILED"
        decimal total_amount
        nvarchar receiver_name
        varchar receiver_phone
        nvarchar delivery_address
        nvarchar note
        datetime2 created_at
        datetime2 updated_at
    }
    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id
        nvarchar product_name "snapshot"
        varchar size_name "snapshot"
        decimal unit_price "snapshot"
        int sugar_percent
        int ice_percent
        int quantity
        decimal line_total
    }
    ORDER_ITEM_TOPPINGS {
        bigint id PK
        bigint order_item_id FK
        nvarchar topping_name "snapshot"
        decimal price "snapshot"
    }
    ORDER_STATUS_HISTORY {
        bigint id PK
        bigint order_id FK
        varchar status
        bigint changed_by
        datetime2 changed_at
    }
    PAYMENTS {
        bigint id PK
        bigint order_id FK
        varchar method
        decimal amount
        varchar status
        varchar vnp_txn_ref UK
        varchar vnp_transaction_no
        varchar vnp_response_code
        datetime2 paid_at
    }
    CHAT_CONVERSATIONS {
        bigint id PK
        bigint customer_id FK
        bigint branch_id FK
        varchar status "OPEN/CLOSED"
        datetime2 created_at
    }
    CHAT_MESSAGES {
        bigint id PK
        bigint conversation_id FK
        bigint sender_id FK
        nvarchar content
        bit is_read
        datetime2 sent_at
    }
```

Quyết định thiết kế quan trọng:
1. **Role là cột `role` trong `users`** (không tách bảng): đủ cho 3 vai trò cố định, đơn giản hơn.
2. **`order_items` lưu snapshot** tên, giá, size, topping: sau này đổi giá hoặc xóa món thì đơn cũ không bị sai.
3. **`users.branch_id`** chỉ có giá trị với STAFF, dùng để lọc đơn theo chi nhánh.
4. **`payments` tách khỏi `orders`**: một đơn có thể thử thanh toán VNPay nhiều lần. `vnp_txn_ref` là mã tham chiếu gửi sang VNPay (duy nhất mỗi lần).
5. **`order_status_history`** phục vụ việc khách theo dõi tiến trình.

## 5. Luồng trạng thái đơn hàng

```mermaid
stateDiagram-v2
    [*] --> PENDING: Khách đặt (COD) / thanh toán VNPay thành công
    PENDING --> CONFIRMED: Staff xác nhận
    CONFIRMED --> PREPARING: Pha chế
    PREPARING --> DELIVERING: Giao / sẵn sàng
    DELIVERING --> COMPLETED: Hoàn thành
    PENDING --> CANCELLED: Khách hủy / Staff từ chối
    CONFIRMED --> CANCELLED: Staff hủy
    COMPLETED --> [*]
    CANCELLED --> [*]
```

Quy tắc:
- Khách chỉ được hủy khi đơn còn `PENDING`.
- COD: `payment_status = UNPAID`, chuyển `PAID` khi `COMPLETED`.
- VNPay: đơn tạo với `payment_status = UNPAID`; **chỉ thông báo cho staff khi VNPay xác nhận thành công** (IPN/return hợp lệ) để tránh đơn chưa trả tiền.

## 6. Activity diagram: Đặt hàng (checkout)

```mermaid
flowchart TD
    A([Bắt đầu]) --> B[Khách xem giỏ hàng]
    B --> C{Đã đăng nhập?}
    C -- Không --> D[Chuyển trang đăng nhập] --> B
    C -- Có --> E[Chọn chi nhánh + nhập thông tin nhận hàng]
    E --> F[Chọn phương thức thanh toán]
    F --> G{COD hay VNPay?}
    G -- COD --> H[Tạo đơn: PENDING, UNPAID]
    H --> I[Xóa giỏ hàng]
    I --> J[WebSocket: báo staff chi nhánh + admin]
    J --> K[Trang xác nhận đặt hàng]
    G -- VNPay --> L[Tạo đơn + payment, ký URL VNPay]
    L --> M[Chuyển sang cổng VNPay]
    M --> N[Khách thanh toán]
    N --> O[VNPay gọi IPN / redirect Return URL]
    O --> P{Chữ ký + số tiền hợp lệ và mã 00?}
    P -- Có --> Q[payment PAID, order PENDING, xóa giỏ]
    Q --> J
    P -- Không --> R[payment FAILED, giữ giỏ, báo lỗi]
    R --> B
    K --> Z([Kết thúc])
```

## 7. Sequence: Thanh toán VNPay

```mermaid
sequenceDiagram
    actor U as Khách
    participant W as UTETra (Spring)
    participant V as VNPay Sandbox
    participant S as Staff (WebSocket)

    U->>W: POST /checkout (VNPAY)
    W->>W: Tạo order + payment (UNPAID)
    W->>W: Ký HMAC-SHA512 → paymentUrl
    W-->>U: Redirect paymentUrl
    U->>V: Nhập thẻ test NCB + OTP
    V-->>W: IPN (server-to-server)
    W->>W: Kiểm tra chữ ký, số tiền, mã 00
    W->>W: payment = PAID
    W-->>S: Push đơn mới /topic/branch/{id}/orders
    V-->>U: Redirect Return URL
    U->>W: GET /payment/vnpay-return
    W-->>U: Trang kết quả
```

## 8. Sequence: Realtime + Chat

```mermaid
sequenceDiagram
    actor K as Khách
    participant W as Spring + STOMP
    actor S as Staff / Admin

    Note over K,S: Đơn mới
    K->>W: Đặt hàng
    W-->>S: /topic/branch/{id}/orders và /topic/admin/orders
    Note over K,S: Đổi trạng thái
    S->>W: Cập nhật trạng thái đơn
    W-->>K: /user/queue/order-status
    Note over K,S: Chat hỗ trợ
    K->>W: SEND /app/chat.send
    W->>W: Lưu chat_messages
    W-->>S: /topic/branch/{id}/chat
    S->>W: SEND /app/chat.send (trả lời)
    W-->>K: /user/queue/chat
```

Kênh WebSocket (endpoint `/ws`, SockJS):

| Kênh | Ai nhận | Nội dung |
|---|---|---|
| `/topic/admin/orders` | ADMIN | đơn mới mọi chi nhánh |
| `/topic/branch/{id}/orders` | STAFF chi nhánh `id` | đơn mới của chi nhánh |
| `/user/queue/order-status` | khách của đơn | đổi trạng thái đơn |
| `/topic/branch/{id}/chat` | STAFF chi nhánh `id` | tin nhắn từ khách |
| `/user/queue/chat` | khách / staff trong hội thoại | tin nhắn trả lời |

## 9. Cấu trúc package và thư mục (sẽ dựng ở Day 2)

```
UTETra/
├── pom.xml
├── application-local.properties.example   (mẫu cấu hình bí mật; file thật application-local.properties bị .gitignore)
├── database/schema.sql, data.sql
├── docs/                         (tài liệu, sơ đồ, prompts.md)
└── src/main/
    ├── java/com/utetra/
    │   ├── UtetraApplication.java
    │   ├── config/               (SecurityConfig, WebSocketConfig, CloudinaryConfig, SiteMesh)
    │   ├── security/             (JwtService, JwtAuthFilter, UserDetails)
    │   ├── entity/               (User, Branch, Category, Product, ...)
    │   ├── repository/
    │   ├── service/
    │   ├── dto/
    │   ├── controller/
    │   │   ├── web/              (home, menu, cart, checkout, order, auth, profile)
    │   │   ├── staff/            (đơn chi nhánh, chat)
    │   │   └── admin/            (menu, chi nhánh, user, dashboard)
    │   ├── payment/              (VnPayService, VnPayController)
    │   ├── websocket/            (OrderNotifier, ChatController)
    │   └── exception/
    ├── resources/
    │   ├── application.properties
    │   └── static/ (css, js, images)
    └── webapp/WEB-INF/
        ├── decorators/           (main.jsp, staff.jsp, admin.jsp)
        └── views/                (common, auth, user, staff, admin)
```

## 10. Danh sách chức năng và ngày thực hiện

| Ngày | Chức năng |
|---|---|
| 2 | Dựng khung, kết nối SQL Server, entity, `schema.sql`/`data.sql` |
| 3 | Đăng ký, đăng nhập, JWT cookie, phân quyền, trang 403 |
| 4 | Trang chủ, menu theo danh mục, tìm kiếm, phân trang, chi tiết, chi nhánh |
| 5 | Admin CRUD danh mục/sản phẩm/topping, upload Cloudinary |
| 6 | Giỏ hàng (size/topping/đường/đá), checkout COD |
| 7 | Quản lý đơn: lịch sử, chi tiết, hủy; staff/admin xử lý đơn, luồng trạng thái |
| 8 | Thanh toán VNPay sandbox (URL, return, IPN, kiểm tra chữ ký) |
| 9 | WebSocket: đơn mới, đổi trạng thái, chat khách ↔ staff |
| 10 | Admin: chi nhánh, user/staff, dashboard doanh thu, hồ sơ |
| 11 | Dự phòng (nửa đầu) + kiểm thử |
| 12 | Sửa lỗi, responsive, xuất `.sql` cuối |
| 13 | Gộp tài liệu `.docx` |
| 14 | Slide, kiểm tra cuối, nộp bài |

## 11. Chia việc 2 người (đề xuất)

| Người A (backend) | Người B (giao diện + tài liệu) |
|---|---|
| Entity, service, security, payment, WebSocket, DB, GitHub | Kiểm tra giao diện JSP, chụp ảnh màn hình, ghi test case, viết `.docx`, slide |

## 12. Thông tin cấu hình cần chuẩn bị (không commit vào Git)

- SQL Server: host, port 1433, tên DB `utetra`, user/password
- Cloudinary: `cloud_name`, `api_key`, `api_secret`
- VNPay: `vnp_TmnCode`, `vnp_HashSecret`
- JWT secret (tự sinh, chuỗi dài ≥ 32 ký tự)
