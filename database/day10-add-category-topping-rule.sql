USE utetra;
GO

IF COL_LENGTH(N'dbo.categories', N'allow_toppings') IS NULL
BEGIN
    ALTER TABLE categories
        ADD allow_toppings BIT NOT NULL
            CONSTRAINT df_categories_allow_toppings DEFAULT 0 WITH VALUES;
END;
GO

UPDATE categories
SET allow_toppings = CASE WHEN name = N'Trà sữa' THEN 1 ELSE 0 END;
GO

PRINT N'day10-add-category-topping-rule.sql: da san sang.';
GO
