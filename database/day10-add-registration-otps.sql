USE utetra;
GO

IF OBJECT_ID(N'dbo.registration_otps', N'U') IS NULL
BEGIN
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
    CREATE INDEX ix_registration_otps_expires ON registration_otps(expires_at);
END;
GO

PRINT N'day10-add-registration-otps.sql: da san sang.';
GO
