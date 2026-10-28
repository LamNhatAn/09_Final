/* Them bang gop y ma khong xoa du lieu hien tai. Chay mot lan tren database utetra. */
USE utetra;
GO

IF OBJECT_ID(N'dbo.feedbacks', N'U') IS NULL
BEGIN
    CREATE TABLE feedbacks (
        id               BIGINT IDENTITY(1,1) PRIMARY KEY,
        full_name        NVARCHAR(100)  NOT NULL,
        email            NVARCHAR(150)  NOT NULL,
        phone            NVARCHAR(20)   NULL,
        branch_id        BIGINT         NULL,
        topic            NVARCHAR(30)   NOT NULL,
        rating           INT            NOT NULL,
        content          NVARCHAR(2000) NOT NULL,
        privacy_consent  BIT            NOT NULL,
        created_at       DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT fk_feedback_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
        CONSTRAINT ck_feedback_topic CHECK (topic IN ('COMPLAINT','PRAISE','SUGGESTION','WEBSITE','DRINK','SERVICE','OTHER')),
        CONSTRAINT ck_feedback_rating CHECK (rating BETWEEN 1 AND 5),
        CONSTRAINT ck_feedback_consent CHECK (privacy_consent = 1)
    );
    CREATE INDEX ix_feedback_created ON feedbacks(created_at DESC);
END;
GO

PRINT N'day9-add-feedbacks.sql: bang feedbacks da san sang.';
GO
