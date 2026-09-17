package com.ontheway.repository;

/**
 * 4.2 / 5.1 만족도. 엔티티를 통째로 끌어오지 않고 집계 두 값만 받는 프로젝션이다.
 *
 * <p><b>후기가 한 건도 없으면 {@code averageRating} 이 null 이다</b>({@code avg()} 의 성질).
 * 화면에 "-"로 찍을지 0.0으로 찍을지는 서비스가 정한다.
 *
 * <p>타입이 {@code BigDecimal} 이 아니라 {@code Double} 인 이유: JPQL {@code avg()} 의 반환형은
 * 명세상 {@code Double} 이다. 컬럼이 {@code DECIMAL(2,1)} 인 것과 별개다.
 */
public interface RatingSummary {

    Double getAverageRating();

    Long getReviewCount();
}
