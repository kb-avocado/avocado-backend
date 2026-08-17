-- ============================================================
-- 소비 리포트 유형 마스터
-- ============================================================
CREATE TABLE spending_report_types
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    code        VARCHAR(50) NOT NULL UNIQUE COMMENT '소비 리포트 유형 코드',

    name        VARCHAR(50) NOT NULL COMMENT '소비 리포트 유형 이름',

    description VARCHAR(255) NULL
        COMMENT '소비 리포트 유형 설명',

    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시'
);


-- ============================================================
-- 아이별 월간 소비 리포트
-- ============================================================
CREATE TABLE child_spending_reports
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    child_id       BIGINT   NOT NULL COMMENT '아이 회원 ID',

    report_type_id BIGINT   NOT NULL COMMENT '판정된 소비 리포트 유형 ID',

    report_year    INT      NOT NULL COMMENT '리포트 대상 연도',

    report_month   INT      NOT NULL COMMENT '리포트 대상 월',

    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '리포트 생성 일시',

    CONSTRAINT fk_spending_report_child
        FOREIGN KEY (child_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_spending_report_type
        FOREIGN KEY (report_type_id)
            REFERENCES spending_report_types (id)
            ON DELETE RESTRICT,

    -- 한 아이는 한 달에 하나의 소비 리포트만 가질 수 있다.
    CONSTRAINT uk_child_report_month
        UNIQUE (child_id, report_year, report_month),

    -- 월은 1 ~ 12 범위만 허용한다.
    CONSTRAINT chk_report_month
        CHECK (report_month BETWEEN 1 AND 12)
);