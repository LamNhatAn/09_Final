USE utetra;
GO

IF COL_LENGTH(N'dbo.products', N'sold_out') IS NULL
BEGIN
    ALTER TABLE products
        ADD sold_out BIT NOT NULL
            CONSTRAINT df_products_sold_out DEFAULT 0 WITH VALUES;
END;
GO

PRINT N'day10-add-product-sold-out.sql: da san sang.';
GO
