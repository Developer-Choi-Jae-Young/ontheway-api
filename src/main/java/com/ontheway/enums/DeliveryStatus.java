package com.ontheway.enums;

import lombok.Getter;

/**
 * 배송 상태.
 *
 * MATCHING_WAITING 과 REJECTED 는 컬럼에 저장되지 않는다. Request 가 rejectedAt 과
 * DeliveryOrder 존재를 보고 그때 만들어낸다. {@code stored} 가 그 구분이다.
 */
@Getter
public enum DeliveryStatus {

    MATCHING_WAITING("매칭대기중", false),

    PICKING_UP("픽업중", true),                    // 수락 직후
    DELIVERY_WAITING("배송대기중", true),           // 여기부터 취소 불가
    DELIVERING("배송중", true),                    // 스케줄러가 자동으로 넘긴다
    COMPLETION_REQUESTED("배송완료 확인요청", true), // 증빙 사진이 있어야 넘어간다
    COMPLETED("배송완료", true),                   // 의뢰자 확인 또는 72시간 경과
    FAILED("배송실패", true),
    CANCELED("취소", true),

    REJECTED("거절", false);

    private final String label;

    /** delivery_order.status 에 실제로 들어가는 값인지 */
    private final boolean stored;

    DeliveryStatus(String label, boolean stored) {
        this.label = label;
        this.stored = stored;
    }
}
