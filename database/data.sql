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
