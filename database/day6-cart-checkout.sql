/* =====================================================================
   UTETra - Day 6: bo sung rang buoc gio hang va snapshot ghi chu don hang.
   Script nang cap database cu, khong xoa du lieu.
   Chay: sqlcmd -S localhost -E -C -f 65001 -i database\day6-cart-checkout.sql
   ===================================================================== */
USE utetra;
GO

IF COL_LENGTH(N'dbo.order_items', N'note') IS NULL
    ALTER TABLE order_items ADD note NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_cart_items_sugar')
    ALTER TABLE cart_items ADD CONSTRAINT ck_cart_items_sugar
        CHECK (sugar_percent IN (0,30,50,70,100));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_cart_items_ice')
    ALTER TABLE cart_items ADD CONSTRAINT ck_cart_items_ice
        CHECK (ice_percent IN (0,30,50,70,100));
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_cart_items_qty')
    ALTER TABLE cart_items DROP CONSTRAINT ck_cart_items_qty;
GO
ALTER TABLE cart_items ADD CONSTRAINT ck_cart_items_qty CHECK (quantity BETWEEN 1 AND 20);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_order_items_sugar')
    ALTER TABLE order_items ADD CONSTRAINT ck_order_items_sugar
        CHECK (sugar_percent IN (0,30,50,70,100));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_order_items_ice')
    ALTER TABLE order_items ADD CONSTRAINT ck_order_items_ice
        CHECK (ice_percent IN (0,30,50,70,100));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_order_items_qty')
    ALTER TABLE order_items ADD CONSTRAINT ck_order_items_qty CHECK (quantity BETWEEN 1 AND 20);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_order_items_total')
    ALTER TABLE order_items ADD CONSTRAINT ck_order_items_total
        CHECK (unit_price >= 0 AND line_total >= 0);
GO

PRINT N'Day 6: da cap nhat rang buoc gio hang va order_items.note.';
GO
