package com.ontheway.enums;

import lombok.Getter;

/**
 * 배송 상태. 명세 §4 상태 흐름.
 *
 * <p>MATCHING_WAITING / REJECTED 는 컬럼에 저장되지 않는다.
 * Request.rejectedAt 과 DeliveryOrder 존재 여부로 유도한다 (ERD_REVIEW 1-1).
 */
@Getter
public enum DeliveryStatus {

    MATCHING_WAITING("매칭대기중", false),

    PICKING_UP("픽업중", true),                    // 수락 직후
    DELIVERY_WAITING("배송대기중", true),           // 이후 취소 불가
    DELIVERING("배송중", true),                    // 스케줄러 자동 전이
    COMPLETION_REQUESTED("배송완료 확인요청", true), // 증빙 사진 없으면 전이 불가
    COMPLETED("배송완료", true),                   // 의뢰자 수락 or 72h 자동
    FAILED("배송실패", true),
    CANCELED("취소", true),

    REJECTED("거절", false);

    private final String label;

    /** delivery_order.status 에 실제로 저장되는 값인지 */
    private final boolean stored;

    DeliveryStatus(String label, boolean stored) {
        this.label = label;
        this.stored = stored;
    }
}
