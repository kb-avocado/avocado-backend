-- ============================================================
-- V8__add_spending_report_summary_columns.sql
-- child_spending_reports 월간 소비 리포트 집계 컬럼 추가
-- ============================================================

ALTER TABLE child_spending_reports
    ADD COLUMN total_spent BIGINT NOT NULL DEFAULT 0
    COMMENT '해당 월 소비 총금액'
        AFTER report_month,

    ADD COLUMN transaction_count INT NOT NULL DEFAULT 0
        COMMENT '해당 월 결제 건수'
        AFTER total_spent,

    ADD COLUMN top_spots JSON NULL
        COMMENT '해당 월 가맹점 소비 TOP 5 JSON 배열'
        AFTER transaction_count,

    ADD COLUMN total_saved BIGINT NOT NULL DEFAULT 0
        COMMENT '해당 월 저금통 저축 합계'
        AFTER top_spots,

    ADD COLUMN allowance_received BIGINT NOT NULL DEFAULT 0
        COMMENT '해당 월 순수령 용돈'
        AFTER total_saved,

    ADD COLUMN saving_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00
        COMMENT '해당 월 저축률(%)'
        AFTER allowance_received,

    ADD COLUMN updated_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
    COMMENT '리포트 마지막 갱신 일시'
                   AFTER created_at;


-- ============================================================
-- 소비 리포트 집계값 유효성 검증
-- ============================================================

ALTER TABLE child_spending_reports
    ADD CONSTRAINT chk_spending_report_total_spent
        CHECK (
            total_spent >= 0
            ),

    ADD CONSTRAINT chk_spending_report_transaction_count
        CHECK (
            transaction_count >= 0
        ),

    ADD CONSTRAINT chk_spending_report_total_saved
        CHECK (
            total_saved >= 0
        ),

    ADD CONSTRAINT chk_spending_report_allowance_received
        CHECK (
            allowance_received >= 0
        ),

    ADD CONSTRAINT chk_spending_report_saving_rate
        CHECK (
            saving_rate BETWEEN 0 AND 100
        );