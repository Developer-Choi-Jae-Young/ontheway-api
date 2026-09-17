package com.ontheway.enums;

/**
 * 신고 대상의 종류. Report 의 entity_id 가 어느 테이블의 id 인지를 이 값으로 안다.
 * FK 제약이 없으니 대상이 실제로 있는지는 서비스가 확인한다.
 */
public enum ReportEntityType {
    USER,
    PRODUCT,
    DELIVERY
}
