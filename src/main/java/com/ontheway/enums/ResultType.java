package com.ontheway.enums;

/**
 * 거래가 끝난 방식. FailedAndCancelled 한 테이블에 두 사건이 함께 들어가므로 이 값으로 구분한다.
 *
 * <p>취소(4.4)는 의뢰자·전달자 양쪽이, 실패(4.6)는 전달자만 할 수 있다.
 */
public enum ResultType {
    FAILED,
    CANCELED
}
