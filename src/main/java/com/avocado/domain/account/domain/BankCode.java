package com.avocado.domain.account.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 계좌 및 선불지갑 송금에서 사용하는 은행 식별 코드를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum BankCode {

    AVOCADO("999", "아보카도"),

    KDB("002", "산업은행"),

    IBK("003", "기업은행"),

    KB("004", "국민은행"),

    SUHYUP_BANK("007", "수협은행"),

    NH("011", "농협은행"),

    LOCAL_NONGHYUP("012", "지역농축협"),

    WOORI("020", "우리은행"),

    SC("023", "SC제일은행"),

    CITI("027", "한국씨티은행"),

    IM_BANK("031", "iM뱅크"),

    BUSAN("032", "부산은행"),

    GWANGJU("034", "광주은행"),

    JEJU("035", "제주은행"),

    JEONBUK("037", "전북은행"),

    GYEONGNAM("039", "경남은행"),

    SAEMAUL("045", "새마을금고"),

    SHINHYUP("048", "신협"),

    SAVINGS_BANK("050", "저축은행"),

    FORESTRY("064", "산림조합"),

    POST_OFFICE("071", "우체국"),

    HANA("081", "하나은행"),

    SHINHAN("088", "신한은행"),

    KBANK("089", "케이뱅크"),

    KAKAO_BANK("090", "카카오뱅크"),

    TOSS_BANK("092", "토스뱅크");

    private final String code;

    private final String description;
}
