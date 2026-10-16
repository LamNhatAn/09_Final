/* =====================================================================
   UTETra - bo sung rang buoc ten duy nhat cho database da tao truoc do.
   Script khong xoa du lieu. Neu dang co ten trung, script dung va yeu cau
   sua du lieu trung truoc khi tao UNIQUE constraint.
   Chay: sqlcmd -S localhost -E -C -f 65001 -i database\day5-add-unique-names.sql
   ===================================================================== */
USE utetra;
GO

IF EXISTS (
    SELECT name
    FROM categories
    GROUP BY name
    HAVING COUNT(*) > 1
)
    THROW 51000, N'categories đang có tên trùng. Hãy đổi hoặc xóa bản ghi trùng rồi chạy lại script.', 1;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = N'uq_categories_name'
      AND parent_object_id = OBJECT_ID(N'dbo.categories')
)
    ALTER TABLE categories
        ADD CONSTRAINT uq_categories_name UNIQUE (name);
GO

IF EXISTS (
    SELECT name
    FROM toppings
    GROUP BY name
    HAVING COUNT(*) > 1
)
    THROW 51001, N'toppings đang có tên trùng. Hãy đổi hoặc xóa bản ghi trùng rồi chạy lại script.', 1;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = N'uq_toppings_name'
      AND parent_object_id = OBJECT_ID(N'dbo.toppings')
)
    ALTER TABLE toppings
        ADD CONSTRAINT uq_toppings_name UNIQUE (name);
GO

SELECT name AS constraint_name, OBJECT_NAME(parent_object_id) AS table_name
FROM sys.key_constraints
WHERE name IN (N'uq_categories_name', N'uq_toppings_name');
GO
