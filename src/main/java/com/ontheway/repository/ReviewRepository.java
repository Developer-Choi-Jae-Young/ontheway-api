package com.ontheway.repository;

import com.ontheway.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 후기·만족도.
 *
 * <p><b>피평가자 컬럼이 없다</b>(1-5). 그래서 "받은 후기"와 "만족도 집계"는 거래를 타고 올라가
 * 유도하는데, 그 유도 규칙은 {@link ReviewQueries} 한 곳에만 둔다.
 *
 * <p>후기는 {@code COMPLETED} 건에만 달린다(B9). 그 판정은 서비스가 하므로 여기 상태 조건은 없다.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 4.1 동일 거래·동일 상대 중복 평가 방지. {@code uk_review_order_reviewer} 의 사전 검사다. */
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);

    /**
     * 4.2 만족도 / 5.1 프로필. 평점 캐시 컬럼을 두지 않고 그때그때 집계한다 —
     * 1명짜리 조회라 싸고, 느려지면 나중에 캐시를 붙여 소급 계산할 수 있다 (2-5 e).
     */
    @Query("select avg(rv.rating) as averageRating, count(rv) as reviewCount from Review rv"
            + ReviewQueries.TARGET_JOINS
            + " where " + ReviewQueries.RECEIVED_BY_USER)
    RatingSummary findRatingSummary(@Param("userId") Long userId);

    /**
     * 5.4 나의 후기 - 보낸 토글. {@code getTarget()} 을 찍어야 하므로 당사자까지 함께 가져온다.
     *
     * <p>{@code Pageable} 에 {@code Sort} 를 담지 않는다 — 정렬은 메서드 이름에 있다.
     */
    @EntityGraph(Review.WITH_PARTIES)
    Slice<Review> findByReviewerIdOrderByIdDesc(Long reviewerId, Pageable pageable);

    /** 5.4 나의 후기 - 받은 토글. 내가 당사자인 거래의 후기 중 내가 쓰지 않은 것. */
    @Query("select rv from Review rv join fetch rv.reviewer"
            + ReviewQueries.TARGET_JOINS_FETCH
            + " where " + ReviewQueries.RECEIVED_BY_USER
            + " order by rv.id desc")
    Slice<Review> findReceived(@Param("userId") Long userId, Pageable pageable);
}
