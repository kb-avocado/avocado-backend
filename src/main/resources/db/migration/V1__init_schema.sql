-- ============================================================
-- MySQL 8.0.16 이상
-- 금액 단위: 원(KRW)
--
-- 프로젝트 정책
-- 1. 부분 환불 없이 전체 취소만 지원
-- 2. 원거래와 취소 거래는 동일한 trace_id 사용
-- 3. QR 결제 토큰과 리프레시 토큰은 Redis에서 관리
-- ============================================================


-- ==========================================
-- 1. 사용자 도메인
-- ==========================================

CREATE TABLE users
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY
        COMMENT '회원 고유 식별자',

    email       VARCHAR(254) NOT NULL UNIQUE COMMENT '로그인 이메일',

    password    VARCHAR(255) NOT NULL COMMENT '단방향 해시된 비밀번호',

    name        VARCHAR(50)  NOT NULL COMMENT '회원 이름',

    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '휴대전화 번호',

    birth       DATE         NOT NULL COMMENT '생년월일',

    role        VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT '계정 권한: USER, ADMIN',

    user_type   VARCHAR(10) COMMENT '일반 회원 유형: PARENT, CHILD. 관리자는 NULL',

    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태: PENDING, ACTIVE, SUSPENDED, DELETED',

    invite_code VARCHAR(20) UNIQUE COMMENT '부모 회원의 가족 초대 코드. 아이와 관리자는 NULL',

    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at  DATETIME     NOT NULL
                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK (
            role IN (
                     'USER',
                     'ADMIN'
                )
            ),

    CONSTRAINT chk_user_type
        CHECK (
            user_type IS NULL
                OR user_type IN (
                                 'PARENT',
                                 'CHILD'
                )
            ),

    CONSTRAINT chk_user_status
        CHECK (
            status IN (
                       'PENDING',
                       'ACTIVE',
                       'SUSPENDED',
                       'DELETED'
                )
            ),

    CONSTRAINT chk_user_role_type
        CHECK (
            (role = 'USER' AND user_type IS NOT NULL)
                OR
            (role = 'ADMIN' AND user_type IS NULL)
            ),

    CONSTRAINT chk_user_invite_code
        CHECK (
            (
                role = 'USER'
                    AND user_type = 'PARENT'
                    AND invite_code IS NOT NULL
                )
                OR
            (
                role = 'USER'
                    AND user_type = 'CHILD'
                    AND invite_code IS NULL
                )
                OR
            (
                role = 'ADMIN'
                    AND invite_code IS NULL
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='서비스 사용자 및 관리자 계정 마스터';


CREATE TABLE family_relations
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,

    parent_id     BIGINT      NOT NULL COMMENT '부모 회원 ID',

    child_id      BIGINT      NOT NULL COMMENT '아이 회원 ID',

    relation_type VARCHAR(20) NULL
        COMMENT '가족 관계 유형',

    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'PENDING, ACTIVE, REJECTED, TERMINATED',

    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at    DATETIME    NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_relation_parent
        FOREIGN KEY (parent_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_relation_child
        FOREIGN KEY (child_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_parent_child
        UNIQUE (
                parent_id,
                child_id
            ),

    CONSTRAINT chk_family_relation_different_user
        CHECK (
            parent_id <> child_id
            ),

    CONSTRAINT chk_family_relation_status
        CHECK (
            status IN (
                       'PENDING',
                       'ACTIVE',
                       'REJECTED',
                       'TERMINATED'
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='부모와 아이의 가족 관계 매핑';


-- ==========================================
-- 2. 가맹점 도메인
-- ==========================================

CREATE TABLE merchants
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,

    name                    VARCHAR(100) NOT NULL COMMENT '가맹점 이름',

    business_no             VARCHAR(20) NULL UNIQUE
        COMMENT '사업자등록번호',

    address                 VARCHAR(255) NOT NULL COMMENT '가맹점 주소',

    category                VARCHAR(50)  NOT NULL COMMENT '가맹점 업종',

    is_restricted_for_child BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '아이 결제 제한 업종 여부',

    status                  VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, SUSPENDED, CLOSED',

    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at              DATETIME     NOT NULL
                                                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_merchant_restricted
        CHECK (
            is_restricted_for_child IN (0, 1)
            ),

    CONSTRAINT chk_merchant_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'SUSPENDED',
                       'CLOSED'
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='가맹점 마스터';


-- ==========================================
-- 3. 부모 연동 계좌 도메인
-- ==========================================

CREATE TABLE accounts
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id        BIGINT      NOT NULL COMMENT '계좌를 연동한 부모 회원 ID',

    bank_code      CHAR(3)     NOT NULL COMMENT '금융기관 코드',

    account_number VARCHAR(20) NOT NULL COMMENT '실제 은행 계좌번호',

    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, SUSPENDED, DISCONNECTED',

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at     DATETIME    NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_account_bank_number
        UNIQUE (
                bank_code,
                account_number
            ),

    CONSTRAINT chk_account_bank_code
        CHECK (
            bank_code REGEXP '^[0-9]{3}$'
) ,

    CONSTRAINT chk_account_status
        CHECK (
            status IN (
                'ACTIVE',
                'SUSPENDED',
                'DISCONNECTED'
            )
        )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='부모의 오픈뱅킹 연동 계좌';


CREATE TABLE account_histories
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,

    account_id       BIGINT      NOT NULL COMMENT '거래가 발생한 부모 계좌 ID',

    trace_id         VARCHAR(50) NOT NULL COMMENT '계좌 거래와 지갑 거래를 연결하는 추적 ID',

    transaction_type VARCHAR(30) NOT NULL COMMENT '계좌 거래 유형: WITHDRAWAL, REFUND',

    amount           BIGINT      NOT NULL COMMENT '거래 금액',

    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED',

    failure_code     VARCHAR(30) NULL
        COMMENT '거래 실패 코드',

    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at       DATETIME    NOT NULL
                                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_history_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_account_history_account
        UNIQUE (
                id,
                account_id
            ),

    CONSTRAINT uk_account_history_trace_type
        UNIQUE (
                trace_id,
                account_id,
                transaction_type
            ),

    CONSTRAINT chk_account_history_type
        CHECK (
            transaction_type IN (
                                 'WITHDRAWAL',
                                 'REFUND'
                )
            ),

    CONSTRAINT chk_account_history_amount
        CHECK (
            amount > 0
            ),

    CONSTRAINT chk_account_history_status
        CHECK (
            status IN (
                       'PENDING',
                       'SUCCESS',
                       'FAILED'
                )
            ),

    CONSTRAINT chk_account_history_failure
        CHECK (
            (
                status = 'FAILED'
                    AND failure_code IS NOT NULL
                )
                OR
            (
                status <> 'FAILED'
                    AND failure_code IS NULL
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='부모 계좌 거래 이벤트 이력';


CREATE TABLE account_ledgers
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    history_id     BIGINT      NOT NULL COMMENT '연결된 계좌 거래 이력 ID',

    account_id     BIGINT      NOT NULL COMMENT '잔액이 변동된 계좌 ID',

    ledger_type    VARCHAR(10) NOT NULL COMMENT '원장 유형: IN, OUT',

    amount         BIGINT      NOT NULL COMMENT '잔액 변동 금액',

    balance_before BIGINT      NOT NULL COMMENT '거래 전 계좌 잔액',

    balance_after  BIGINT      NOT NULL COMMENT '거래 후 계좌 잔액',

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_account_ledger_history
        UNIQUE (history_id),

    CONSTRAINT fk_account_ledger_history_account
        FOREIGN KEY (
                     history_id,
                     account_id
            )
            REFERENCES account_histories (
                                          id,
                                          account_id
                )
            ON DELETE RESTRICT,

    CONSTRAINT chk_account_ledger_type
        CHECK (
            ledger_type IN (
                            'IN',
                            'OUT'
                )
            ),

    CONSTRAINT chk_account_ledger_amount
        CHECK (
            amount > 0
            ),

    CONSTRAINT chk_account_ledger_balance
        CHECK (
            balance_before >= 0
                AND balance_after >= 0
            ),

    CONSTRAINT chk_account_ledger_calculation
        CHECK (
            (
                ledger_type = 'IN'
                    AND balance_after = balance_before + amount
                )
                OR
            (
                ledger_type = 'OUT'
                    AND balance_after = balance_before - amount
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='부모 계좌 잔액 변동 원장';


-- ==========================================
-- 4. 아이 선불지갑 도메인
-- ==========================================

CREATE TABLE wallets
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,

    child_id      BIGINT      NOT NULL UNIQUE COMMENT '지갑을 소유한 아이 회원 ID',

    wallet_number VARCHAR(50) NOT NULL UNIQUE COMMENT '서비스 내부 지갑 계좌번호',

    balance       BIGINT      NOT NULL DEFAULT 0 COMMENT '현재 사용 가능한 선불지갑 잔액',

    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, SUSPENDED, CLOSED',

    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at    DATETIME    NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_child
        FOREIGN KEY (child_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_wallet_balance
        CHECK (
            balance >= 0
            ),

    CONSTRAINT chk_wallet_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'SUSPENDED',
                       'CLOSED'
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='아이 선불 전자지갑';


-- ==========================================
-- 4-1. 지갑별 정책
-- ==========================================

CREATE TABLE wallet_policies
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY
        COMMENT '지갑 정책 고유 식별자',

    wallet_id                 BIGINT   NOT NULL UNIQUE COMMENT '정책이 적용되는 지갑 ID',

    monthly_tx_limit          BIGINT   NOT NULL DEFAULT 500000 COMMENT '월 결제 및 송금 누적 한도',

    daily_tx_limit            BIGINT   NOT NULL DEFAULT 50000 COMMENT '일 결제 및 송금 누적 한도',

    is_parent_blocked         BOOLEAN  NOT NULL DEFAULT FALSE COMMENT '부모에 의한 지갑 사용 차단 여부',

    is_online_payment_allowed BOOLEAN  NOT NULL DEFAULT TRUE COMMENT '온라인 결제 허용 여부',

    created_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at                DATETIME NOT NULL
                                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_policy_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallets (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_wallet_policy_limits
        CHECK (
            daily_tx_limit > 0
                AND monthly_tx_limit > 0
                AND daily_tx_limit <= monthly_tx_limit
            ),

    CONSTRAINT chk_wallet_policy_parent_blocked
        CHECK (
            is_parent_blocked IN (0, 1)
            ),

    CONSTRAINT chk_wallet_policy_online_payment
        CHECK (
            is_online_payment_allowed IN (0, 1)
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='아이 선불지갑 개별 정책';


-- ==========================================
-- 4-2. 지갑 거래 이벤트 이력
-- ==========================================

CREATE TABLE wallet_histories
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,

    wallet_id              BIGINT      NOT NULL COMMENT '거래 이력의 소유 지갑 ID',

    trace_id               VARCHAR(50) NOT NULL COMMENT '동일 업무 흐름을 연결하는 추적 ID',

    transaction_type       VARCHAR(30) NOT NULL COMMENT '
        CHARGE,
        CHARGE_CANCEL,
        PAYMENT,
        PAYMENT_CANCEL,
        TRANSFER_OUT,
        TRANSFER_IN,
        TRANSFER_CANCEL_OUT,
        TRANSFER_CANCEL_IN,
        PIGGY_BANK_DEPOSIT,
        PIGGY_BANK_WITHDRAWAL',

    amount                 BIGINT      NOT NULL COMMENT '거래 금액',

    counterparty_wallet_id BIGINT NULL
        COMMENT '내부 송금 상대방 지갑 ID',

    counterparty_name      VARCHAR(50) NULL
        COMMENT '거래 당시 상대방 이름 스냅샷',

    target_bank_code       CHAR(3) NULL
        COMMENT '타행 송금 대상 은행 코드',

    target_account_number  VARCHAR(20) NULL
        COMMENT '타행 송금 대상 계좌번호',

    merchant_id            BIGINT NULL
        COMMENT '결제 또는 결제 취소 대상 가맹점 ID',

    memo                   VARCHAR(100) NULL
        COMMENT '거래 메모',

    status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED',

    failure_code           VARCHAR(30) NULL
        COMMENT '거래 실패 코드',

    created_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at             DATETIME    NOT NULL
                                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_history_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallets (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_wallet_history_counterparty
        FOREIGN KEY (counterparty_wallet_id)
            REFERENCES wallets (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_wallet_history_merchant
        FOREIGN KEY (merchant_id)
            REFERENCES merchants (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_wallet_history_wallet
        UNIQUE (
                id,
                wallet_id
            ),

    CONSTRAINT uk_wallet_history_trace_type
        UNIQUE (
                trace_id,
                wallet_id,
                transaction_type
            ),

    CONSTRAINT chk_wallet_history_type
        CHECK (
            transaction_type IN (
                                 'CHARGE',
                                 'CHARGE_CANCEL',
                                 'PAYMENT',
                                 'PAYMENT_CANCEL',
                                 'TRANSFER_OUT',
                                 'TRANSFER_IN',
                                 'TRANSFER_CANCEL_OUT',
                                 'TRANSFER_CANCEL_IN',
                                 'PIGGY_BANK_DEPOSIT',
                                 'PIGGY_BANK_WITHDRAWAL'
                )
            ),

    CONSTRAINT chk_wallet_history_amount
        CHECK (
            amount > 0
            ),

    CONSTRAINT chk_wallet_history_status
        CHECK (
            status IN (
                       'PENDING',
                       'SUCCESS',
                       'FAILED'
                )
            ),

    CONSTRAINT chk_wallet_history_failure
        CHECK (
            (
                status = 'FAILED'
                    AND failure_code IS NOT NULL
                )
                OR
            (
                status <> 'FAILED'
                    AND failure_code IS NULL
                )
            ),

    CONSTRAINT chk_wallet_history_not_self_transfer
        CHECK (
            counterparty_wallet_id IS NULL
                OR counterparty_wallet_id <> wallet_id
            ),

    CONSTRAINT chk_wallet_history_bank_code
        CHECK (
            target_bank_code IS NULL
                OR target_bank_code REGEXP '^[0-9]{3}$'
) ,

    CONSTRAINT chk_wallet_history_merchant_relation
        CHECK (
            (
                transaction_type IN (
                    'PAYMENT',
                    'PAYMENT_CANCEL'
                )
                AND merchant_id IS NOT NULL
            )
            OR
            (
                transaction_type NOT IN (
                    'PAYMENT',
                    'PAYMENT_CANCEL'
                )
                AND merchant_id IS NULL
            )
        ),

    CONSTRAINT chk_wallet_history_transfer_out_target
        CHECK (
            transaction_type NOT IN (
                'TRANSFER_OUT',
                'TRANSFER_CANCEL_OUT'
            )
            OR
            (
                counterparty_wallet_id IS NOT NULL
                AND target_bank_code IS NULL
                AND target_account_number IS NULL
            )
            OR
            (
                counterparty_wallet_id IS NULL
                AND target_bank_code IS NOT NULL
                AND target_account_number IS NOT NULL
                AND counterparty_name IS NOT NULL
            )
        ),

    CONSTRAINT chk_wallet_history_transfer_in_target
        CHECK (
            transaction_type NOT IN (
                'TRANSFER_IN',
                'TRANSFER_CANCEL_IN'
            )
            OR
            (
                counterparty_wallet_id IS NOT NULL
                AND target_bank_code IS NULL
                AND target_account_number IS NULL
            )
        ),

    CONSTRAINT chk_wallet_history_non_transfer_target
        CHECK (
            transaction_type IN (
                'TRANSFER_OUT',
                'TRANSFER_IN',
                'TRANSFER_CANCEL_OUT',
                'TRANSFER_CANCEL_IN'
            )
            OR
            (
                counterparty_wallet_id IS NULL
                AND counterparty_name IS NULL
                AND target_bank_code IS NULL
                AND target_account_number IS NULL
            )
        )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='아이 지갑 거래 이벤트 이력';


-- ==========================================
-- 4-3. 지갑 잔액 변동 원장
-- ==========================================

CREATE TABLE wallet_ledgers
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    history_id     BIGINT      NOT NULL COMMENT '연결된 지갑 거래 이력 ID',

    wallet_id      BIGINT      NOT NULL COMMENT '잔액이 변경된 지갑 ID',

    ledger_type    VARCHAR(10) NOT NULL COMMENT '원장 유형: IN, OUT',

    amount         BIGINT      NOT NULL COMMENT '잔액 변동 금액',

    balance_before BIGINT      NOT NULL COMMENT '거래 전 지갑 잔액',

    balance_after  BIGINT      NOT NULL COMMENT '거래 후 지갑 잔액',

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_wallet_ledger_history
        UNIQUE (history_id),

    CONSTRAINT fk_wallet_ledger_history_wallet
        FOREIGN KEY (
                     history_id,
                     wallet_id
            )
            REFERENCES wallet_histories (
                                         id,
                                         wallet_id
                )
            ON DELETE RESTRICT,

    CONSTRAINT chk_wallet_ledger_type
        CHECK (
            ledger_type IN (
                            'IN',
                            'OUT'
                )
            ),

    CONSTRAINT chk_wallet_ledger_amount
        CHECK (
            amount > 0
            ),

    CONSTRAINT chk_wallet_ledger_balance
        CHECK (
            balance_before >= 0
                AND balance_after >= 0
            ),

    CONSTRAINT chk_wallet_ledger_calculation
        CHECK (
            (
                ledger_type = 'IN'
                    AND balance_after = balance_before + amount
                )
                OR
            (
                ledger_type = 'OUT'
                    AND balance_after = balance_before - amount
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='아이 지갑 잔액 변동 원장';


-- ==========================================
-- 5. 저금통 도메인
-- ==========================================

CREATE TABLE piggy_banks
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,

    wallet_id          BIGINT      NOT NULL COMMENT '저금통을 소유한 지갑 ID',

    name               VARCHAR(50) NOT NULL COMMENT '저금통 이름',

    target_amount      BIGINT      NOT NULL COMMENT '저금통 목표 금액',

    balance            BIGINT      NOT NULL DEFAULT 0 COMMENT '현재 저금통 잔액',

    bonus_type         VARCHAR(10) NOT NULL DEFAULT 'NONE' COMMENT '보너스 유형: NONE, FIXED, RATE',

    bonus_value        BIGINT      NOT NULL DEFAULT 0 COMMENT '정액 보너스 금액 또는 정수 비율',

    is_favorite        BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '즐겨찾기 여부',

    status             VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태: ACTIVE(진행중), PENDING_ACHIEVE(목표달성 7일 대기중), ACHIEVE(최종달성), CANCEL(해지/취소)',

    first_deposited_at DATETIME NULL
        COMMENT '최초 입금 일시',

    target_reached_at  DATETIME NULL
        COMMENT '목표 금액 도달 일시 (필수 유지 기간 7일 시작점)',

    achieved_at        DATETIME NULL
        COMMENT '필수 유지 기간 7일을 모두 만족하여 최종 목표 달성된 일시',

    canceled_at        DATETIME NULL
        COMMENT '저금통 해지/취소 일시',

    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at         DATETIME    NOT NULL
                                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_piggy_bank_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallets (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_piggy_target_amount
        CHECK (
            target_amount > 0
            ),

    CONSTRAINT chk_piggy_balance
        CHECK (
            balance >= 0
            ),

    CONSTRAINT chk_piggy_favorite
        CHECK (
            is_favorite IN (0, 1)
            ),

    CONSTRAINT chk_piggy_bonus
        CHECK (
            (
                bonus_type = 'NONE'
                    AND bonus_value = 0
                )
                OR
            (
                bonus_type = 'FIXED'
                    AND bonus_value > 0
                )
                OR
            (
                bonus_type = 'RATE'
                    AND bonus_value BETWEEN 1 AND 100
                )
            ),

    CONSTRAINT chk_piggy_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'PENDING_ACHIEVE',
                       'ACHIEVE',
                       'CANCEL'
                )
            ),

    -- PENDING_ACHIEVE 상태일 때 목표 도달 시간 필수 검증
    CONSTRAINT chk_piggy_pending_achieve_at
        CHECK (
            (
                status = 'PENDING_ACHIEVE'
                    AND target_reached_at IS NOT NULL
                )
                OR
            status <> 'PENDING_ACHIEVE'
            ),

    -- ACHIEVE 상태일 때 목표 도달 시간과 최종 달성 시간 필수 검증
    CONSTRAINT chk_piggy_achieved_at
        CHECK (
            (
                status = 'ACHIEVE'
                    AND target_reached_at IS NOT NULL
                    AND achieved_at IS NOT NULL
                )
                OR
            (
                status <> 'ACHIEVE'
                    AND achieved_at IS NULL
                )
            ),

    -- CANCEL 상태일 때 해지 시간 필수 검증
    CONSTRAINT chk_piggy_canceled_at
        CHECK (
            (
                status = 'CANCEL'
                    AND canceled_at IS NOT NULL
                )
                OR
            (
                status <> 'CANCEL'
                    AND canceled_at IS NULL
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='아이 저금통 마스터';


CREATE TABLE piggy_bank_histories
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    piggy_bank_id  BIGINT      NOT NULL COMMENT '잔액이 변경된 저금통 ID',

    trace_id       VARCHAR(50) NOT NULL COMMENT '지갑 거래 이력과 연결하는 추적 ID',

    history_type   VARCHAR(20) NOT NULL COMMENT '저금통 거래 유형: DEPOSIT, WITHDRAWAL',

    amount         BIGINT      NOT NULL COMMENT '저금통 잔액 변동 금액',

    balance_before BIGINT      NOT NULL COMMENT '거래 전 저금통 잔액',

    balance_after  BIGINT      NOT NULL COMMENT '거래 후 저금통 잔액',

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_piggy_history_master
        FOREIGN KEY (piggy_bank_id)
            REFERENCES piggy_banks (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_piggy_history_trace_type
        UNIQUE (
                piggy_bank_id,
                trace_id,
                history_type
            ),

    CONSTRAINT chk_piggy_history_type
        CHECK (
            history_type IN (
                             'DEPOSIT',
                             'WITHDRAWAL'
                )
            ),

    CONSTRAINT chk_piggy_history_amount
        CHECK (
            amount > 0
            ),

    CONSTRAINT chk_piggy_history_balance
        CHECK (
            balance_before >= 0
                AND balance_after >= 0
            ),

    CONSTRAINT chk_piggy_history_calculation
        CHECK (
            (
                history_type = 'DEPOSIT'
                    AND balance_after = balance_before + amount
                )
                OR
            (
                history_type = 'WITHDRAWAL'
                    AND balance_after = balance_before - amount
                )
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='저금통 거래 이력 및 잔액 원장';


CREATE TABLE piggy_bank_cheer_messages
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,

    piggy_bank_id BIGINT       NOT NULL COMMENT '응원 메시지를 등록한 저금통 ID',

    parent_id     BIGINT       NOT NULL COMMENT '응원 메시지를 작성한 부모 ID',

    content       VARCHAR(255) NOT NULL COMMENT '응원 메시지 내용',

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at    DATETIME     NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cheer_piggy_bank
        FOREIGN KEY (piggy_bank_id)
            REFERENCES piggy_banks (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_cheer_parent
        FOREIGN KEY (parent_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='부모의 저금통 응원 메시지';


-- ==========================================
-- 6. 경제 신문 및 교육 도메인
-- ==========================================

CREATE TABLE news_articles
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,

    title           VARCHAR(255) NOT NULL COMMENT '기사 제목',

    subtitle        TEXT NULL
        COMMENT '기사 부제 또는 요약',

    link            TEXT         NOT NULL COMMENT '기사 원문 링크',

    today_challenge VARCHAR(500) NOT NULL COMMENT '오늘의 경제 학습 문제',

    published_at    DATETIME     NOT NULL COMMENT '기사 발행 일시',

    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '서비스 등록 일시'
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='경제 신문 기사 마스터';


CREATE TABLE news_activities
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,

    child_id     BIGINT   NOT NULL COMMENT '경제 활동을 수행한 아이 ID',

    article_id   BIGINT   NOT NULL COMMENT '대상 경제 기사 ID',

    viewed_at    DATETIME NULL
        COMMENT '기사 조회 일시',

    child_answer TEXT NULL
        COMMENT '오늘의 문제에 대한 아이 답변',

    completed_at DATETIME NULL
        COMMENT '활동 완료 일시, NULL이면 미완료',

    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at   DATETIME NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_news_activity_child
        FOREIGN KEY (child_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_news_activity_article
        FOREIGN KEY (article_id)
            REFERENCES news_articles (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_child_article
        UNIQUE (
                child_id,
                article_id
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='아이 경제 신문 활동 이력';


-- ==========================================
-- 7. 알림 도메인
-- ==========================================

CREATE TABLE notifications
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id      BIGINT       NOT NULL COMMENT '알림을 받을 회원 ID',

    notify_type  VARCHAR(30)  NOT NULL COMMENT '알림 유형: WALLET, PIGGY_BANK, NEWS, SYSTEM',

    title        VARCHAR(100) NOT NULL COMMENT '알림 제목',

    content      VARCHAR(255) NOT NULL COMMENT '알림 상세 내용',

    is_read      BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '알림 열람 여부',

    reference_id BIGINT NULL
        COMMENT '이동 대상 도메인의 ID',

    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_notification_type
        CHECK (
            notify_type IN (
                            'WALLET',
                            'PIGGY_BANK',
                            'NEWS',
                            'SYSTEM'
                )
            ),

    CONSTRAINT chk_notification_read
        CHECK (
            is_read IN (0, 1)
            )
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='사용자 앱 내 알림 이력';


-- ==========================================
-- 8. 성능 최적화 및 조회 인덱스
-- ==========================================

-- 계좌 거래 추적
CREATE INDEX idx_account_history_trace
    ON account_histories (
                          trace_id
        );

-- 계좌 최근 거래 조회
CREATE INDEX idx_account_history_time
    ON account_histories (
                          account_id,
                          created_at DESC,
                          id DESC
        );

-- 계좌 원장 조회
CREATE INDEX idx_account_ledger_time
    ON account_ledgers (
                        account_id,
                        created_at DESC,
                        id DESC
        );


-- 지갑 거래 추적
CREATE INDEX idx_wallet_history_trace
    ON wallet_histories (
                         trace_id
        );

-- 지갑 최근 거래 조회
CREATE INDEX idx_wallet_history_time
    ON wallet_histories (
                         wallet_id,
                         created_at DESC,
                         id DESC
        );

-- 지갑 원장 조회
CREATE INDEX idx_wallet_ledger_time
    ON wallet_ledgers (
                       wallet_id,
                       created_at DESC,
                       id DESC
        );

-- 결제 및 송금 일·월 누적 한도 조회
CREATE INDEX idx_wallet_history_limit
    ON wallet_histories (
                         wallet_id,
                         transaction_type,
                         status,
                         created_at
        );

-- 내부 송금 상대방 조회
CREATE INDEX idx_wallet_history_counterparty
    ON wallet_histories (
                         counterparty_wallet_id,
                         created_at DESC
        );

-- 타행 송금 내역 조회
CREATE INDEX idx_wallet_history_external
    ON wallet_histories (
                         target_bank_code,
                         target_account_number,
                         created_at DESC
        );


-- 지갑별 저금통 상태 조회
CREATE INDEX idx_piggy_bank_status
    ON piggy_banks (
                    wallet_id,
                    status
        );

-- 즐겨찾기 저금통 조회
CREATE INDEX idx_piggy_bank_favorite
    ON piggy_banks (
                    wallet_id,
                    is_favorite,
                    updated_at DESC
        );

-- 저금통 거래 추적
CREATE INDEX idx_piggy_history_trace
    ON piggy_bank_histories (
                             trace_id
        );

-- 저금통 최근 거래 조회
CREATE INDEX idx_piggy_history_time
    ON piggy_bank_histories (
                             piggy_bank_id,
                             created_at DESC,
                             id DESC
        );


-- 아이 경제 활동 조회
CREATE INDEX idx_activity_child_time
    ON news_activities (
                        child_id,
                        viewed_at DESC
        );

-- 완료한 경제 활동 조회
CREATE INDEX idx_activity_child_completed
    ON news_activities (
                        child_id,
                        completed_at DESC
        );


-- 사용자 최근 알림 조회
CREATE INDEX idx_notification_user_time
    ON notifications (
                      user_id,
                      created_at DESC,
                      id DESC
        );

-- 사용자 미열람 알림 조회
CREATE INDEX idx_notification_user_read
    ON notifications (
                      user_id,
                      is_read,
                      created_at DESC
        );