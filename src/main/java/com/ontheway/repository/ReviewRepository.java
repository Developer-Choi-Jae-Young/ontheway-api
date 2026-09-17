package com.ontheway.repository;

import com.ontheway.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 후기와 만족도.
 *
 * 평가 대상 컬럼이 없어서 "받은 후기"와 "만족도 집계"는 거래를 타고 올라가 계산한다.
 * 그 계산 규칙은 {@link ReviewQueries} 에만 둔다.
 *
 * 후기는 배송완료된 거래에만 달리는데, 그 확인은 서비스가 하므로 여기 상태 조건은 없다.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 같은 거래에 두 번 평가하는 걸 막는다. {@code uk_review_order_reviewer} 의 사전 검사다. */
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);

    /**
     * 프로필에 찍는 만족도. 캐시 컬럼 없이 그때그때 집계한다. 한 사람짜리 조회라 싸고,
     * 느려지면 나중에 캐시를 붙여 소급 계산하면 된다.
     */
    @Query("select avg(rv.rating) as averageRating, count(rv) as reviewCount from Review rv"
            + ReviewQueries.TARGET_JOINS
            + " where " + ReviewQueries.RECEIVED_BY_USER)
    RatingSummary findRatingSummary(@Param("userId") Long userId);

    /**
     * 내가 쓴 후기 목록. 화면에 평가 대상을 찍어야 해서 당사자까지 같이 가져온다.
     *
     * {@code Pageable} 에 {@code Sort} 는 담지 않는다. 정렬이 메서드 이름에 들어 있다.
     */
    @EntityGraph(Review.WITH_PARTIES)
    Slice<Review> findByReviewerIdOrderByIdDesc(Long reviewerId, Pageable pageable);

    /** 내가 받은 후기 목록. 내가 당사자인 거래의 후기 중 내가 쓰지 않은 것들이다. */
    @Query("select rv from Review rv join fetch rv.reviewer"
            + ReviewQueries.TARGET_JOINS_FETCH
            + " where " + ReviewQueries.RECEIVED_BY_USER
            + " order by rv.id desc")
    Slice<Review> findReceived(@Param("userId") Long userId, Pageable pageable);
}
