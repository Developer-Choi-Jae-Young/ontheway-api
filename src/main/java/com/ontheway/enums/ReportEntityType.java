package com.ontheway.enums;

/**
 * 신고 대상의 종류. Report 의 다형성 참조에서 entity_id 가 어느 테이블을 가리키는지 정의한다
 * (ERD_REVIEW 1-2). DB FK 제약이 없으므로 유효성은 서비스에서 검증한다.
 */
public enum ReportEntityType {
    USER,
    PRODUCT,
    DELIVERY
}
