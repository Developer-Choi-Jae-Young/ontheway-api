package com.ontheway.enums;

import lombok.Getter;

/**
 * 신고 유형. 명세 4.3 의 체크박스 목록을 그대로 옮겼다.
 *
 * <p>한 신고당 최대 3개까지 선택할 수 있으며, 그 제한은 DB가 아니라 서비스에서 막는다
 * (ERD_REVIEW 1-10). 게시글 신고와 사용자 신고의 선택지가 다르므로
 * {@link #appliesTo(ReportEntityType)} 로 진입점과 맞는지 검증한다.
 */
@Getter
public enum ReportCategory {

    // 게시글 신고
    PROHIBITED_ITEM("허용되지 않은 물품 또는 위험 물품을 요청합니다.", true, false),
    INFO_MISMATCH("물품 정보와 사진이 물품 내용과 다릅니다.", true, false),
    DUPLICATE_POSTING("같은 내용의 게시글을 반복해서 올렸습니다.", true, false),
    ADVERTISEMENT("광고·홍보 또는 서비스와 무관한 내용을 게시했습니다.", true, false),

    // 사용자 신고
    SCHEDULE_VIOLATION("약속한 픽업 또는 전달 시간을 지키지 않았습니다.", false, true),
    NO_RESPONSE("배송 과정에서 응답하지 않았습니다.", false, true),
    UNFAIR_PAYMENT_DEMAND("배송비 또는 금전 거래와 관련해 부당한 요구를 했습니다.", false, true),
    ITEM_DAMAGE("물품을 분실·파손하거나 훼손했습니다.", false, true),
    FALSE_IDENTITY("허위 신원 또는 거래 정보를 제공했습니다.", false, true),
    SAFETY_THREAT("안전을 위협하거나 불안감을 주는 행동을 했습니다.", false, true),

    // 양쪽 목록에 동일한 문구로 들어 있다
    ABUSIVE_LANGUAGE("욕설·협박 또는 부적절한 언행을 했습니다.", true, true);

    private final String label;
    private final boolean forPost;
    private final boolean forUser;

    ReportCategory(String label, boolean forPost, boolean forUser) {
        this.label = label;
        this.forPost = forPost;
        this.forUser = forUser;
    }

    public boolean appliesTo(ReportEntityType entityType) {
        return entityType == ReportEntityType.USER ? forUser : forPost;
    }
}
