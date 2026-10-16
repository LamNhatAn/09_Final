/* =====================================================================
   UTETra - schema.sql  (SQL Server)
   Tao database + 16 bang. Chay lai file nay se XOA va TAO LAI toan bo bang.
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

/* ---------- Danh muc / San pham / Size / Topping ---------- */
CREATE TABLE categories (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(100) NOT NULL,
    description  NVARCHAR(500) NULL,
    active       BIT           NOT NULL DEFAULT 1,
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
GO

PRINT N'schema.sql: da tao xong 16 bang.';
GO
