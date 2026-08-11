-- ============================================================
-- V7__add_advice_to_child_spending_reports.sql
-- 아이별 월간 소비 리포트에 조언 컬럼 추가
-- ============================================================

ALTER TABLE child_spending_reports
    ADD COLUMN advice VARCHAR(500) NULL
        COMMENT '아이의 월간 소비 패턴에 따른 조언'
        AFTER report_month;