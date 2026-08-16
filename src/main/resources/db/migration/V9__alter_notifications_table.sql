-- ============================================================
-- V9: notifications 테이블 구조 변경
--
-- 변경 사항
-- 1. user_id -> receiver_id 컬럼명 변경
-- 2. notify_type -> type 컬럼명 변경 및 길이 확장
-- 3. content -> message 컬럼명 변경 및 길이 확장
-- 4. 알림 타입 CHECK 제약 제거
-- 5. 기존 FK를 receiver_id 기준으로 재생성
-- 6. reference_id 컬럼 제거
-- 7. 알림 목록 조회용 인덱스 추가
-- 8. 읽지 않은 알림 조회용 인덱스 추가
-- ============================================================


-- 기존 알림 타입 CHECK 제약을 제거한다.
-- 알림 타입은 애플리케이션의 NotificationType enum에서 관리한다.
ALTER TABLE notifications
DROP
CHECK chk_notification_type;


-- 기존 사용자 FK를 제거한다.
-- user_id 컬럼명을 receiver_id로 변경하기 전에 먼저 제거해야 한다.
ALTER TABLE notifications
DROP
FOREIGN KEY fk_notification_user;


-- 알림 수신 사용자 컬럼명을 변경한다.
ALTER TABLE notifications
    CHANGE COLUMN user_id receiver_id BIGINT NOT NULL
    COMMENT '알림 수신 사용자 ID';


-- 알림 타입 컬럼명을 변경하고 길이를 확장한다.
ALTER TABLE notifications
    CHANGE COLUMN notify_type type VARCHAR (50) NOT NULL
    COMMENT '알림 유형';


-- 알림 내용 컬럼명을 변경하고 최대 길이를 확장한다.
ALTER TABLE notifications
    CHANGE COLUMN content message VARCHAR (500) NOT NULL
    COMMENT '알림 내용';


-- 더 이상 사용하지 않는 참조 ID 컬럼을 제거한다.
ALTER TABLE notifications
DROP
COLUMN reference_id;


-- 변경된 receiver_id를 기준으로 사용자 FK를 다시 생성한다.
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES users (id)
            ON DELETE CASCADE;