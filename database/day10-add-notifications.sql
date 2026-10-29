/* Them bang thong bao ma khong xoa du lieu hien tai. Chay mot lan tren database utetra. */
USE utetra;
GO

IF OBJECT_ID(N'dbo.notifications', N'U') IS NULL
BEGIN
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
END;
GO

PRINT N'day10-add-notifications.sql: bang notifications da san sang.';
GO
