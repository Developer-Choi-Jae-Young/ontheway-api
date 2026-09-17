package com.ontheway.repository;

/**
 * 7 이용내역 네 화면({@code /history/list}, {@code /history/request/list},
 * {@code /history/delivery/list}, {@code /history/cancel/list})의 공통 조각.
 *
 * <p>네 쿼리가 <b>where 절 한 줄만 다르고</b> 나머지는 같다. 페치 계획이 흩어지면 한 화면만
 * 조인을 빠뜨려서 거기서만 N+1 이 나는데, 목록이라 티가 잘 안 난다. 그래서 한 곳에 둔다.
 */
final class RequestQueries {

    /**
     * 이용내역 공통 select. 삭제된 게시글도 그대로 보여주므로 {@code deletedAt} 조건은 없다 —
     * 소프트 삭제를 택한 이유가 이것이다 (1-6).
     *
     * <p>{@code order} 를 {@code left join fetch} 하는 이유는 {@code Request.getStatus()} 가
     * 그 행의 존재로 상태를 합성하기 때문이다. 빼면 목록 N건마다 SELECT 가 더 나간다.
     */
    static final String HISTORY_SELECT = """
            select r from Request r
              join fetch r.product p
              join fetch p.author pa
              join fetch r.delivery d
              join fetch d.author da
              left join fetch r.order o
            """;

    /**
     * 내가 이 거래의 당사자인가. 의뢰자는 {@code product.author}, 전달자는
     * {@code delivery.author} 다 — {@code Request} 에 당사자를 복사해 두지 않는다 (1-6).
     */
    static final String IS_PARTY = "(pa.id = :userId or da.id = :userId)";

    /**
     * 최신순 정렬. 페이징은 {@code Pageable} 이 붙여주므로 여기엔 없다.
     *
     * <p>정렬을 쿼리에 박아 두는 이유는 {@code Pageable} 에 {@code Sort} 를 넣는 걸 깜빡하면
     * 순서가 DB 마음대로가 되고, 그러면 페이지 사이에 항목이 겹치거나 빠지기 때문이다.
     */
    static final String ORDER_LATEST = " order by r.id desc";

    private RequestQueries() {
    }
}
