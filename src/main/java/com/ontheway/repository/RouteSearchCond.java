package com.ontheway.repository;

import java.math.BigDecimal;

/**
 * 경로 목록(2.2 / 6.1)의 동적 필터 조건. <b>null 인 항목은 조건을 붙이지 않는다.</b>
 *
 * <p>웹 DTO({@code DeliveryListRequestDto})를 그대로 받지 않는 이유는 레포지토리가
 * 컨트롤러 계층에 묶이지 않게 하기 위해서다. 변환은 서비스가 한다.
 *
 * @param startAddress 출발지 — 접두 일치({@code like '값%'})
 * @param endAddress   도착지 — 접두 일치
 * @param hopePrice    희망금액 <b>상한</b>. 이 값 이하인 경로만 남긴다
 *                     ({@code desiredPrice <= hopePrice}) — 의뢰자가 낼 수 있는 금액으로
 *                     거르는 필터다. 명세 2.2 의 필터 항목은 값 하나이며,
 *                     비고의 "희망금액 → 레인지"는 확정이 아니라 미결 메모다
 * @param minRating    전달자 만족도 하한. <b>후기가 한 건도 없는 전달자는 제외된다</b>
 *                     (평균이 null 이라 비교가 성립하지 않는다)
 */
public record RouteSearchCond(
        String startAddress,
        String endAddress,
        Integer hopePrice,
        BigDecimal minRating) {

    public static RouteSearchCond none() {
        return new RouteSearchCond(null, null, null, null);
    }
}
