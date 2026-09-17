package com.ontheway.repository;

import com.ontheway.entity.Delivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

/**
 * 동적 조건이 붙는 경로 목록 두 개. QueryDSL 없이 {@code EntityManager} + JPQL 조립으로 간다
 * (ERD_REVIEW 2-5 c).
 *
 * <p>2.2(전체 피드)와 6.1(내 게시글 전달 탭)은 <b>필터·정렬이 같고 범위만 다르다</b> —
 * 2.2 는 "공개 중인 남의 글", 6.1 은 "내 글 전부"다. 그래서 필터 조립을 공유한다.
 *
 * <p><b>반환이 {@link Slice} 인 이유</b>: 무한 스크롤에는 총 개수가 필요 없다.
 * {@code Page} 를 쓰면 매번 {@code count} 쿼리가 한 번 더 나가는데 화면이 쓰지도 않는다.
 * {@code Slice} 는 {@code size + 1} 건을 읽어 초과분의 존재로 {@code hasNext()} 를 판정한다.
 */
public interface DeliveryRepositoryCustom {

    /**
     * 2.2 경로 목록 — 공개 중인 남의 경로.
     *
     * <p><b>요청 수는 여기서 안 센다.</b> 화면의 {@code requestCount} 는 결과의 id 를 모아
     * {@link RequestRepository#countActiveByDeliveryIds(java.util.Collection)} 로 한 번에
     * 받는다 — 목록을 돌며 경로마다 세면 그대로 N+1 이다.
     *
     * @param now      "시작시간이 지난 글 제외"의 기준 시각. 테스트에서 경계를 고정할 수 있도록
     *                 레포지토리가 직접 {@code now()} 를 부르지 않고 받는다
     * @param pageable {@code PageRequest.of(page, size)}. <b>정렬은 쿼리에 박혀 있으므로
     *                 {@code Sort} 를 담지 않는다</b> — 담아도 무시된다
     */
    Slice<Delivery> searchOpenRoutes(RouteSearchCond cond, LocalDateTime now, Pageable pageable);

    /**
     * 6.1 내 게시글 - 전달 탭. 내가 쓴 경로 전부를 같은 필터·정렬로 본다.
     *
     * <p>2.2 와 달리 <b>공개 종료·시작시간 조건이 없다</b> — 내 글은 끝난 것도 봐야 한다.
     * {@code requestCount} 는 2.2 와 같은 방법으로 채운다.
     */
    Slice<Delivery> searchMyRoutes(Long authorId, RouteSearchCond cond, Pageable pageable);
}
