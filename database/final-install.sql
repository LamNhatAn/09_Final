-- UTETra - bộ cài database cuối kỳ dành cho sqlcmd.
-- Chạy từ thư mục gốc dự án:
-- sqlcmd -S localhost -E -C -f 65001 -i database\final-install.sql
-- LƯU Ý: schema.sql xóa và tạo lại toàn bộ bảng hiện có.

:r database\schema.sql
:r database\data.sql
