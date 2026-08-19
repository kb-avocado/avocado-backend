-- ============================================================
-- V5__add_account_deposit_transaction_type.sql
--
-- 아이 선불지갑에서 서비스에 등록된 부모 계좌로 송금할 때
-- 계좌 입금 거래를 기록할 수 있도록 DEPOSIT 유형을 추가한다.
-- ============================================================


-- ==========================================
-- 부모 연동 계좌 거래 유형
-- ==========================================

-- 기존 계좌 거래 유형 CHECK 제약조건을 제거한다.
ALTER TABLE account_histories
DROP
CHECK chk_account_history_type;


-- 계좌 거래 유형에 DEPOSIT을 추가한다.
ALTER TABLE account_histories
    ADD CONSTRAINT chk_account_history_type
        CHECK (
            transaction_type IN (
                                 'WITHDRAWAL',
                                 'REFUND',
                                 'DEPOSIT'
                )
            );


-- transaction_type 컬럼 설명에 DEPOSIT을 반영한다.
ALTER TABLE account_histories
    MODIFY COLUMN transaction_type VARCHAR (30) NOT NULL
    COMMENT '계좌 거래 유형: WITHDRAWAL, REFUND, DEPOSIT';