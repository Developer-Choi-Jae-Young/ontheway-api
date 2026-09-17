package com.ontheway.repository;

import com.ontheway.entity.Delivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

/**
 * 동적 조건이 붙는 경로 목록 두 개. QueryDSL 없이 {@code EntityManager} 로 JPQL 을 조립한다.
 *
 * 전체 피드와 내 게시글의 전달 탭은 필터와 정렬이 같고 범위만 다르다. 앞은 "공개 중인 남의 글",
 * 뒤는 "내 글 전부"다. 그래서 필터 조립을 공유한다.
 *
 * 반환은 {@link Slice} 다. 무한 스크롤에는 총 개수가 필요 없는데 {@code Page} 를 쓰면
 * {@code count} 쿼리가 매번 한 번 더 나간다. {@code Slice} 는 {@code size + 1} 건을 읽어서
 * 넘치는 게 있는지로 {@code hasNext()} 를 정한다.
 */
public interface DeliveryRepositoryCustom {

    /**
     * 경로 목록 피드. 공개 중인 남의 경로만 나온다.
     *
     * 요청 수는 여기서 안 센다. 화면의 {@code requestCount} 는 결과의 id 를 모아
     * {@link RequestRepository#countActiveByDeliveryIds(java.util.Collection)} 로 한 번에
     * 받는다. 목록을 돌며 경로마다 세면 그게 N+1 이다.
     *
     * @param now      출발 시각이 지났는지 재는 기준. 테스트에서 경계를 고정할 수 있게
     *                 {@code now()} 를 안에서 부르지 않고 받는다
     * @param pageable {@code PageRequest.of(page, size)}. 정렬은 쿼리에 박혀 있으니
     *                 {@code Sort} 는 담지 않는다. 담아도 무시된다
     */
    Slice<Delivery> searchOpenRoutes(RouteSearchCond cond, LocalDateTime now, Pageable pageable);

    /**
     * 내 게시글의 전달 탭. 내가 쓴 경로 전부를 같은 필터와 정렬로 본다.
     *
     * 피드와 달리 공개 종료나 출발 시각 조건이 없다. 내 글은 끝난 것도 보여야 한다.
     * {@code requestCount} 는 피드와 같은 방법으로 채운다.
     */
    Slice<Delivery> searchMyRoutes(Long authorId, RouteSearchCond cond, Pageable pageable);
}
