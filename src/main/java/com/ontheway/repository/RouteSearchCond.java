package com.ontheway.repository;

import java.math.BigDecimal;

/**
 * 경로 목록 두 개가 같이 쓰는 검색 조건. null 인 항목은 조건을 안 붙인다.
 *
 * 웹 DTO 를 그대로 받지 않는 건 레포지토리가 컨트롤러에 묶이지 않게 하려는 것이고,
 * 변환은 서비스가 한다.
 *
 * @param startAddress 출발지. 앞에서부터 맞춘다({@code like '값%'})
 * @param endAddress   도착지. 앞에서부터 맞춘다
 * @param hopePrice    의뢰자가 낼 수 있는 금액. 경로의 희망금액이 이 값 이하인 것만 남는다
 * @param minRating    전달자 만족도 하한. 후기가 하나도 없는 전달자는 평균이 null 이라 빠진다
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
