USE utetra;
GO

IF OBJECT_ID(N'dbo.password_reset_otps', N'U') IS NULL
BEGIN
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
    CREATE INDEX ix_password_reset_otps_expires ON password_reset_otps(expires_at);
END;
GO

PRINT N'day10-add-password-reset-otps.sql: da san sang.';
GO
