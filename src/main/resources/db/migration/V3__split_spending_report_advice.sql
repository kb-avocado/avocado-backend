ALTER TABLE child_spending_reports
    CHANGE COLUMN advice child_advice VARCHAR (500) NULL
        COMMENT '아이의 월간 소비 패턴에 따른 아이용 조언',
    ADD COLUMN parent_advice VARCHAR (500) NULL
        COMMENT '아이의 월간 소비 패턴에 따른 부모용 조언'
        AFTER child_advice;