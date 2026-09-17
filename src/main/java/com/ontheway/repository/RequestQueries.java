package com.ontheway.repository;

/**
 * 이용내역 네 화면({@code /history/list}, {@code /history/request/list},
 * {@code /history/delivery/list}, {@code /history/cancel/list})의 공통 조각.
 *
 * 네 쿼리가 where 절 한 줄만 다르고 나머지는 같다. 조인을 각자 적어두면 한 화면에서만
 * 빠뜨려서 거기서만 N+1 이 나는데, 목록이라 눈에 잘 안 띈다. 그래서 여기 모아뒀다.
 */
final class RequestQueries {

    /**
     * 이용내역 공통 select. 삭제된 게시글도 그대로 보여줘야 해서 {@code deletedAt} 조건이 없다.
     *
     * {@code order} 를 {@code left join fetch} 하는 건 {@code Request.getStatus()} 가 그 행이
     * 있는지로 상태를 정하기 때문이다. 빼면 목록 N건마다 SELECT 가 더 나간다.
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
     * 내가 이 거래의 당사자인지. 의뢰자는 {@code product.author}, 전달자는
     * {@code delivery.author} 다.
     */
    static final String IS_PARTY = "(pa.id = :userId or da.id = :userId)";

    /**
     * 최신순 정렬. 페이징은 {@code Pageable} 이 붙여주므로 여기엔 없다.
     *
     * 정렬을 쿼리에 박아둔 건, {@code Pageable} 에 {@code Sort} 넣는 걸 깜빡하면 순서가 DB
     * 마음대로가 되고 그러면 페이지 사이에 항목이 겹치거나 빠지기 때문이다.
     */
    static final String ORDER_LATEST = " order by r.id desc";

    private RequestQueries() {
    }
}
