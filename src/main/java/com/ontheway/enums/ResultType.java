package com.ontheway.enums;

/**
 * 거래가 끝난 방식. FailedAndCancelled 한 테이블에 둘이 같이 들어가서 이 값으로 구분한다.
 *
 * 취소는 의뢰자와 전달자 양쪽이, 실패는 전달자만 할 수 있다.
 */
public enum ResultType {
    FAILED,
    CANCELED
}
