package com.ontheway.repository;

/**
 * 만족도 집계. 엔티티를 통째로 끌어오지 않고 값 두 개만 받는 프로젝션이다.
 *
 * 주의: 후기가 한 건도 없으면 {@code averageRating} 이 null 이다. 화면에 "-"로 찍을지
 * 0.0 으로 찍을지는 서비스가 정한다.
 *
 * 타입이 {@code Double} 인 건 JPQL {@code avg()} 의 반환형이 그래서다. 컬럼이
 * {@code DECIMAL(2,1)} 인 것과는 별개다.
 */
public interface RatingSummary {

    Double getAverageRating();

    Long getReviewCount();
}
