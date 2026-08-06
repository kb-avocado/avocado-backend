-- ============================================================
-- V3: 상태 정책, 전화번호 형식 및 저금통 보너스 지급일 변경
-- 대상 DBMS: MySQL 8.0.16 이상
--
-- 변경 사항
-- 1. users.phone의 기존 하이픈 제거
-- 2. users.phone을 010으로 시작하는 11자리 숫자로 제한
-- 3. users.status 기본값 ACTIVE -> PENDING
-- 4. family_relations.status 기본값 ACTIVE -> PENDING
-- 5. family_relations 상태값 재정의
--    PENDING, APPROVED, REJECTED, CANCELED, ACTIVE
-- 6. 기존 TERMINATED 상태를 CANCELED로 변경
-- 7. piggy_banks.bonus_paid_at 컬럼 추가
-- ============================================================


-- ============================================================
-- 1. 기존 사용자 전화번호 정규화
--
-- 예시:
-- 010-2000-0202 -> 01020000202
-- ============================================================

UPDATE users
SET phone = REPLACE(phone, '-', '')
WHERE phone LIKE '%-%';


-- ============================================================
-- 2. 전화번호 형식 CHECK 추가
--
-- 010으로 시작하는 하이픈 없는 11자리 숫자만 허용한다.
-- ============================================================

ALTER TABLE users
    ADD CONSTRAINT `chk_user_phone`
        CHECK (
            phone REGEXP '^010[0-9]{8}$'
    );


-- ============================================================
-- 3. 사용자 상태 기본값 변경
--
-- 기존 사용자 상태는 변경하지 않는다.
-- 이후 status를 생략한 신규 사용자만 PENDING으로 저장된다.
-- ============================================================

ALTER TABLE users
    MODIFY COLUMN status VARCHAR (20) NOT NULL DEFAULT 'PENDING'
    COMMENT '회원 상태: PENDING, ACTIVE, SUSPENDED, DELETED';


-- ============================================================
-- 4. 기존 가족 관계 상태 CHECK 제거
--
-- 기존 CHECK는 APPROVED와 CANCELED를 허용하지 않으므로
-- 상태 데이터를 변경하기 전에 제거한다.
-- ============================================================

ALTER TABLE family_relations
DROP
CHECK `chk_family_relation_status`;


-- ============================================================
-- 5. 기존 가족 관계 상태 데이터 변환
--
-- 더 이상 사용하지 않는 TERMINATED를 CANCELED로 변환한다.
-- ============================================================

UPDATE family_relations
SET status     = 'CANCELED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'TERMINATED';


-- ============================================================
-- 6. 가족 관계 상태 기본값 및 설명 변경
-- ============================================================

ALTER TABLE family_relations
    MODIFY COLUMN status VARCHAR (20) NOT NULL DEFAULT 'PENDING'
    COMMENT '가족 관계 상태: PENDING, APPROVED, REJECTED, CANCELED, ACTIVE';


-- ============================================================
-- 7. 새로운 가족 관계 상태 CHECK 등록
-- ============================================================

ALTER TABLE family_relations
    ADD CONSTRAINT `chk_family_relation_status`
        CHECK (
            status IN (
                       'PENDING',
                       'APPROVED',
                       'REJECTED',
                       'CANCELED',
                       'ACTIVE'
                )
            );


-- ============================================================
-- 8. 저금통 보너스 지급 일시 컬럼 추가
-- ============================================================

ALTER TABLE piggy_banks
    ADD COLUMN bonus_paid_at DATETIME NULL
        COMMENT '목표 달성 보너스가 실제 지급된 일시'
        AFTER achieved_at;


-- ============================================================
-- 9. 저금통 보너스 지급 일시 CHECK 등록
--
-- bonus_paid_at이 존재하는 경우:
-- 1. 저금통 상태가 ACHIEVE여야 한다.
-- 2. 보너스 유형이 NONE이 아니어야 한다.
-- 3. 최종 달성 일시가 존재해야 한다.
-- 4. 지급 일시는 최종 달성 일시보다 빠를 수 없다.
--
-- 기존 V2 데이터에는 실제 보너스 지급 원장이 없으므로
-- bonus_paid_at은 NULL로 유지한다.
-- ============================================================

ALTER TABLE piggy_banks
    ADD CONSTRAINT `chk_piggy_bonus_paid_at`
        CHECK (
            bonus_paid_at IS NULL
                OR (
                status = 'ACHIEVE'
                    AND bonus_type <> 'NONE'
                    AND achieved_at IS NOT NULL
                    AND bonus_paid_at >= achieved_at
                )
            );