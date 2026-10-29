-- UTETra - Database cuối kỳ (cấu trúc + dữ liệu mẫu trong một file)
-- Chạy bằng: sqlcmd -S localhost -E -C -f 65001 -i database\UTETra_Final.sql
-- LƯU Ý: phần schema bên dưới xóa và tạo lại toàn bộ bảng hiện có.

-- ================================================================
-- PHẦN 1: CẤU TRÚC DATABASE
-- ================================================================

/* =====================================================================
   UTETra - schema.sql  (SQL Server)
   Tao database + 20 bang. Chay lai file nay se XOA va TAO LAI toan bo bang.
   Chay:  sqlcmd -S localhost -E -C -f 65001 -i database\schema.sql
   ===================================================================== */
IF DB_ID(N'utetra') IS NULL
    CREATE DATABASE utetra COLLATE Vietnamese_100_CI_AS;
GO

USE utetra;
GO

-- Bat buoc cho index loc (filtered index) o bang payments; sqlcmd mac dinh tat tuy chon nay
SET QUOTED_IDENTIFIER ON;
GO

/* ---------- Xoa bang cu (theo thu tu phu thuoc) ---------- */
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS chat_conversations;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS registration_otps;
DROP TABLE IF EXISTS password_reset_otps;
DROP TABLE IF EXISTS feedbacks;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_status_history;
DROP TABLE IF EXISTS order_item_toppings;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_item_toppings;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS toppings;
DROP TABLE IF EXISTS sizes;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS branches;
GO

/* ---------- Chi nhanh ---------- */
CREATE TABLE branches (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(150) NOT NULL,
    address     NVARCHAR(255) NOT NULL,
    phone       NVARCHAR(20)  NULL,
    open_time   TIME          NULL,
    close_time  TIME          NULL,
    active      BIT           NOT NULL DEFAULT 1
);

/* ---------- Nguoi dung (USER / STAFF / ADMIN) ---------- */
CREATE TABLE users (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name      NVARCHAR(100) NOT NULL,
    email          NVARCHAR(150) NOT NULL,
    password_hash  NVARCHAR(100) NOT NULL,
    phone          NVARCHAR(20)  NULL,
    address        NVARCHAR(255) NULL,
    role           NVARCHAR(20)  NOT NULL DEFAULT 'USER',
    branch_id      BIGINT        NULL,          -- chi co gia tri voi STAFF
    active         BIT           NOT NULL DEFAULT 1,
    created_at     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('USER','STAFF','ADMIN')),
    CONSTRAINT fk_users_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

/* ---------- OTP xac minh email khi dang ky ---------- */
CREATE TABLE registration_otps (
    id                   BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(150) NOT NULL,
    full_name            NVARCHAR(100) NOT NULL,
    phone                NVARCHAR(20)  NULL,
    password_hash        NVARCHAR(100) NOT NULL,
    otp_hash             NVARCHAR(100) NOT NULL,
    attempts             INT           NOT NULL DEFAULT 0,
    expires_at           DATETIME2     NOT NULL,
    resend_available_at  DATETIME2     NOT NULL,
    created_at           DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    updated_at           DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT uq_registration_otps_email UNIQUE (email),
    CONSTRAINT ck_registration_otps_attempts CHECK (attempts >= 0)
);

/* ---------- OTP dat lai mat khau ---------- */
CREATE TABLE password_reset_otps (
    id                   BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(150) NOT NULL,
    otp_hash             NVARCHAR(100) NOT NULL,
    attempts             INT           NOT NULL DEFAULT 0,
    expires_at           DATETIME2     NOT NULL,
    resend_available_at  DATETIME2     NOT NULL,
    created_at           DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    updated_at           DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT uq_password_reset_otps_email UNIQUE (email),
    CONSTRAINT ck_password_reset_otps_attempts CHECK (attempts >= 0)
);
CREATE INDEX ix_registration_otps_expires ON registration_otps(expires_at);
CREATE INDEX ix_password_reset_otps_expires ON password_reset_otps(expires_at);

/* ---------- Danh muc / San pham / Size / Topping ---------- */
CREATE TABLE categories (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(100) NOT NULL,
    description  NVARCHAR(500) NULL,
    active       BIT           NOT NULL DEFAULT 1,
    allow_toppings BIT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE products (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_id      BIGINT         NOT NULL,
    name             NVARCHAR(150)  NOT NULL,
    description      NVARCHAR(1000) NULL,
    base_price       DECIMAL(12,0)  NOT NULL,
    image_url        NVARCHAR(500)  NULL,     -- URL anh tren Cloudinary
    image_public_id  NVARCHAR(200)  NULL,     -- public_id de xoa / ghi de anh
    available        BIT            NOT NULL DEFAULT 1,
    sold_out         BIT            NOT NULL DEFAULT 0,
    created_at       DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT ck_products_price CHECK (base_price >= 0)
);
CREATE INDEX ix_products_category ON products(category_id);

CREATE TABLE sizes (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(20)  NOT NULL,
    extra_price  DECIMAL(12,0) NOT NULL DEFAULT 0
);

CREATE TABLE toppings (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    name       NVARCHAR(100) NOT NULL,
    price      DECIMAL(12,0) NOT NULL,
    available  BIT           NOT NULL DEFAULT 1,
    CONSTRAINT uq_toppings_name UNIQUE (name)
);

/* ---------- Gio hang ---------- */
CREATE TABLE carts (
    id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    CONSTRAINT uq_carts_user UNIQUE (user_id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id        BIGINT        NOT NULL,
    product_id     BIGINT        NOT NULL,
    size_id        BIGINT        NOT NULL,
    sugar_percent  INT           NOT NULL DEFAULT 100,
    ice_percent    INT           NOT NULL DEFAULT 100,
    quantity       INT           NOT NULL DEFAULT 1,
    note           NVARCHAR(255) NULL,
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_cart_items_size    FOREIGN KEY (size_id)    REFERENCES sizes(id),
    CONSTRAINT ck_cart_items_qty CHECK (quantity BETWEEN 1 AND 20),
    CONSTRAINT ck_cart_items_sugar CHECK (sugar_percent IN (0,30,50,70,100)),
    CONSTRAINT ck_cart_items_ice CHECK (ice_percent IN (0,30,50,70,100))
);
CREATE INDEX ix_cart_items_cart ON cart_items(cart_id);

CREATE TABLE cart_item_toppings (
    cart_item_id  BIGINT NOT NULL,
    topping_id    BIGINT NOT NULL,
    PRIMARY KEY (cart_item_id, topping_id),
    CONSTRAINT fk_cit_cart_item FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_cit_topping   FOREIGN KEY (topping_id)   REFERENCES toppings(id)
);

/* ---------- Don hang ---------- */
CREATE TABLE orders (
    id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    code              NVARCHAR(30)   NOT NULL,
    user_id           BIGINT         NOT NULL,
    branch_id         BIGINT         NOT NULL,
    status            NVARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    payment_method    NVARCHAR(20)   NOT NULL DEFAULT 'COD',
    payment_status    NVARCHAR(20)   NOT NULL DEFAULT 'UNPAID',
    total_amount      DECIMAL(12,0)  NOT NULL,
    receiver_name     NVARCHAR(100)  NOT NULL,
    receiver_phone    NVARCHAR(20)   NOT NULL,
    delivery_address  NVARCHAR(255)  NOT NULL,
    note              NVARCHAR(500)  NULL,
    created_at        DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at        DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT uq_orders_code UNIQUE (code),
    CONSTRAINT fk_orders_user   FOREIGN KEY (user_id)   REFERENCES users(id),
    CONSTRAINT fk_orders_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING','CONFIRMED','PREPARING','DELIVERING','COMPLETED','CANCELLED')),
    CONSTRAINT ck_orders_pay_method CHECK (payment_method IN ('COD','VNPAY')),
    CONSTRAINT ck_orders_pay_status CHECK (payment_status IN ('UNPAID','PAID','FAILED'))
);
CREATE INDEX ix_orders_user   ON orders(user_id);
CREATE INDEX ix_orders_branch ON orders(branch_id, status);

/* Chi tiet don luu snapshot ten/gia tai thoi diem dat */
CREATE TABLE order_items (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id       BIGINT        NOT NULL,
    product_id     BIGINT        NULL,
    product_name   NVARCHAR(150) NOT NULL,
    size_name      NVARCHAR(20)  NULL,
    unit_price     DECIMAL(12,0) NOT NULL,
    sugar_percent  INT           NOT NULL DEFAULT 100,
    ice_percent    INT           NOT NULL DEFAULT 100,
    quantity       INT           NOT NULL DEFAULT 1,
    note           NVARCHAR(255) NULL,
    line_total     DECIMAL(12,0) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT ck_order_items_qty CHECK (quantity BETWEEN 1 AND 20),
    CONSTRAINT ck_order_items_sugar CHECK (sugar_percent IN (0,30,50,70,100)),
    CONSTRAINT ck_order_items_ice CHECK (ice_percent IN (0,30,50,70,100)),
    CONSTRAINT ck_order_items_total CHECK (unit_price >= 0 AND line_total >= 0)
);
CREATE INDEX ix_order_items_order ON order_items(order_id);

CREATE TABLE order_item_toppings (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_item_id  BIGINT        NOT NULL,
    topping_name   NVARCHAR(100) NOT NULL,
    price          DECIMAL(12,0) NOT NULL,
    CONSTRAINT fk_oit_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE
);

CREATE TABLE order_status_history (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id    BIGINT       NOT NULL,
    status      NVARCHAR(20) NOT NULL,
    changed_by  BIGINT       NULL,
    changed_at  DATETIME2    NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
CREATE INDEX ix_osh_order ON order_status_history(order_id);

/* ---------- Thanh toan (COD / VNPay) ---------- */
CREATE TABLE payments (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id            BIGINT        NOT NULL,
    method              NVARCHAR(20)  NOT NULL,
    amount              DECIMAL(12,0) NOT NULL,
    status              NVARCHAR(20)  NOT NULL DEFAULT 'UNPAID',
    vnp_txn_ref         NVARCHAR(50)  NULL,
    vnp_transaction_no  NVARCHAR(50)  NULL,
    vnp_response_code   NVARCHAR(10)  NULL,
    paid_at             DATETIME2     NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT ck_payments_method CHECK (method IN ('COD','VNPAY')),
    CONSTRAINT ck_payments_status CHECK (status IN ('UNPAID','PAID','FAILED'))
);
CREATE INDEX ix_payments_order ON payments(order_id);
-- vnp_txn_ref duy nhat nhung cho phep nhieu NULL (thanh toan COD)
CREATE UNIQUE INDEX uq_payments_vnp_txn_ref ON payments(vnp_txn_ref) WHERE vnp_txn_ref IS NOT NULL;

/* ---------- Chat ho tro ---------- */
CREATE TABLE chat_conversations (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id  BIGINT       NOT NULL,
    branch_id    BIGINT       NOT NULL,
    status       NVARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at   DATETIME2    NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_conv_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_conv_branch   FOREIGN KEY (branch_id)   REFERENCES branches(id),
    CONSTRAINT ck_conv_status CHECK (status IN ('OPEN','CLOSED'))
);

CREATE TABLE chat_messages (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    conversation_id  BIGINT         NOT NULL,
    sender_id        BIGINT         NOT NULL,
    content          NVARCHAR(1000) NOT NULL,
    is_read          BIT            NOT NULL DEFAULT 0,
    sent_at          DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender       FOREIGN KEY (sender_id)       REFERENCES users(id)
);
CREATE INDEX ix_msg_conversation ON chat_messages(conversation_id, sent_at);

/* ---------- Lien he / gop y ---------- */
CREATE TABLE feedbacks (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name        NVARCHAR(100)  NOT NULL,
    email            NVARCHAR(150)  NOT NULL,
    phone            NVARCHAR(20)   NULL,
    branch_id        BIGINT         NULL,
    topic            NVARCHAR(30)   NOT NULL,
    rating           INT            NOT NULL,
    content          NVARCHAR(2000) NOT NULL,
    privacy_consent  BIT            NOT NULL,
    created_at       DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_feedback_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT ck_feedback_topic CHECK (topic IN ('COMPLAINT','PRAISE','SUGGESTION','WEBSITE','DRINK','SERVICE','OTHER')),
    CONSTRAINT ck_feedback_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_feedback_consent CHECK (privacy_consent = 1)
);
CREATE INDEX ix_feedback_created ON feedbacks(created_at DESC);

/* ---------- Thong bao realtime ---------- */
CREATE TABLE notifications (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT         NOT NULL,
    event_key   NVARCHAR(100)  NOT NULL,
    type        NVARCHAR(30)   NOT NULL,
    title       NVARCHAR(150)  NOT NULL,
    message     NVARCHAR(500)  NOT NULL,
    link        NVARCHAR(255)  NOT NULL,
    read_at     DATETIME2      NULL,
    created_at  DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_notifications_user_event UNIQUE (user_id, event_key),
    CONSTRAINT ck_notifications_type CHECK (type IN ('NEW_ORDER','ORDER_STATUS'))
);
CREATE INDEX ix_notifications_user_created ON notifications(user_id, created_at DESC);
CREATE INDEX ix_notifications_user_unread ON notifications(user_id, read_at);
GO

PRINT N'schema.sql: da tao xong 19 bang.';
GO

-- ================================================================
-- PHẦN 2: DỮ LIỆU MẪU
-- ================================================================

/* =====================================================================
   UTETra - data.sql  (du lieu mau)
   Chay SAU schema.sql:  sqlcmd -S localhost -E -C -f 65001 -i database\data.sql
   Mat khau MOI tai khoan mau: Utetra@123  (da ma hoa BCrypt)
   ===================================================================== */
USE utetra;
GO

/* ---------- Chi nhanh ---------- */
INSERT INTO branches (name, address, phone, open_time, close_time) VALUES
 (N'UTETra Võ Văn Ngân',   N'01 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức, TP.HCM', N'0281234001', '07:00', '22:00'),
 (N'UTETra Quận 1',        N'25 Nguyễn Huệ, P. Bến Nghé, Quận 1, TP.HCM',          N'0281234002', '07:30', '22:30'),
 (N'UTETra Gò Vấp',        N'120 Quang Trung, P. 10, Quận Gò Vấp, TP.HCM',          N'0281234003', '08:00', '22:00');

/* ---------- Tai khoan mau ---------- */
INSERT INTO users (full_name, email, password_hash, phone, address, role, branch_id) VALUES
 (N'Quản trị viên',    N'admin@utetra.vn',  N'$2a$10$on7yaqyxABStyDi.hT49hOtkp23DiMrBDnSe5/MS.pWw4Tmh.jnym', N'0900000001', N'TP.HCM', 'ADMIN', NULL),
 (N'Nhân viên Thủ Đức', N'staff1@utetra.vn', N'$2a$10$on7yaqyxABStyDi.hT49hOtkp23DiMrBDnSe5/MS.pWw4Tmh.jnym', N'0900000002', N'TP.HCM', 'STAFF', 1),
 (N'Nhân viên Quận 1',  N'staff2@utetra.vn', N'$2a$10$on7yaqyxABStyDi.hT49hOtkp23DiMrBDnSe5/MS.pWw4Tmh.jnym', N'0900000003', N'TP.HCM', 'STAFF', 2),
 (N'Nhân viên Gò Vấp',  N'staff3@utetra.vn', N'$2a$10$on7yaqyxABStyDi.hT49hOtkp23DiMrBDnSe5/MS.pWw4Tmh.jnym', N'0900000006', N'TP.HCM', 'STAFF', 3),
 (N'Nguyễn Văn An',     N'user1@utetra.vn',  N'$2a$10$on7yaqyxABStyDi.hT49hOtkp23DiMrBDnSe5/MS.pWw4Tmh.jnym', N'0900000004', N'12 Lê Văn Việt, TP. Thủ Đức', 'USER', NULL),
 (N'Trần Thị Bình',     N'user2@utetra.vn',  N'$2a$10$on7yaqyxABStyDi.hT49hOtkp23DiMrBDnSe5/MS.pWw4Tmh.jnym', N'0900000005', N'45 Phan Văn Trị, Gò Vấp',      'USER', NULL);

/* ---------- Danh muc ---------- */
INSERT INTO categories (name, description) VALUES
 (N'Trà sữa',      N'Trà sữa pha chế từ trà nguyên lá và sữa tươi'),
 (N'Trà trái cây', N'Trà kết hợp trái cây tươi mát'),
 (N'Cà phê',       N'Cà phê pha phin và pha máy'),
 (N'Đá xay',       N'Thức uống đá xay mát lạnh');

UPDATE categories SET allow_toppings = 1 WHERE name = N'Trà sữa';

/* ---------- San pham (anh se upload len Cloudinary o Day 5) ---------- */
INSERT INTO products (category_id, name, description, base_price) VALUES
 (1, N'Trà sữa trân châu đen',  N'Trà sữa truyền thống kèm trân châu đen dai ngon', 35000),
 (1, N'Trà sữa matcha',         N'Matcha Nhật Bản béo nhẹ, thơm dịu',               42000),
 (1, N'Trà sữa khoai môn',      N'Khoai môn nghiền mịn, béo ngậy',                  40000),
 (1, N'Hồng trà sữa',           N'Hồng trà đậm vị kết hợp sữa tươi',                32000),
 (2, N'Trà đào cam sả',         N'Đào miếng, cam tươi và sả thơm',                  38000),
 (2, N'Trà vải hoa hồng',       N'Vải thiều và hương hoa hồng thanh nhẹ',           39000),
 (2, N'Trà chanh mật ong',      N'Chanh tươi và mật ong nguyên chất',               30000),
 (3, N'Cà phê sữa đá',          N'Cà phê phin đậm đà với sữa đặc',                  29000),
 (3, N'Bạc xỉu',                N'Nhiều sữa, ít cà phê, vị ngọt béo',               32000),
 (3, N'Cà phê muối',            N'Cà phê kết hợp kem muối mặn nhẹ',                 35000),
 (4, N'Matcha đá xay',          N'Matcha đá xay phủ kem tươi',                      49000),
 (4, N'Cookie đá xay',          N'Bánh quy nghiền cùng kem sữa, phủ sốt socola',    52000);

/* ---------- Size ---------- */
INSERT INTO sizes (name, extra_price) VALUES
 (N'S', 0),
 (N'M', 5000),
 (N'L', 10000);

/* ---------- Topping ---------- */
INSERT INTO toppings (name, price) VALUES
 (N'Trân châu đen',    5000),
 (N'Trân châu trắng',  5000),
 (N'Thạch dừa',        5000),
 (N'Thạch trái cây',   5000),
 (N'Pudding trứng',    7000),
 (N'Kem cheese',      10000);
GO

SELECT N'branches' AS bang, COUNT(*) AS so_dong FROM branches
UNION ALL SELECT N'users', COUNT(*) FROM users
UNION ALL SELECT N'categories', COUNT(*) FROM categories
UNION ALL SELECT N'products', COUNT(*) FROM products
UNION ALL SELECT N'sizes', COUNT(*) FROM sizes
UNION ALL SELECT N'toppings', COUNT(*) FROM toppings;
GO
