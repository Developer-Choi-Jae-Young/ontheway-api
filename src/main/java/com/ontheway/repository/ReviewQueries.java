package com.ontheway.repository;

/**
 * 후기에서 평가 대상을 찾아내는 JPQL 조각들. Review 에 대상 컬럼이 없어서 거래를 타고
 * 올라가 계산해야 하고, 그 계산이 흩어지면 경로 목록의 만족도와 프로필의 만족도가 서로 다른
 * 값이 된다. 그래서 여기 모아뒀다.
 *
 * 별칭에 {@code rv} 를 붙인 건 이 조각들이 바깥 쿼리 안에 끼워지기 때문이다. 경로 목록이
 * {@code Delivery d} 를 쓰고 있어서 접두사가 없으면 {@code d} 가 겹친다.
 *
 * 주의: {@link #RECEIVED_BY_USER} 와 {@link #AVG_RATING_OF_ROUTE_AUTHOR} 는 같은 규칙을 두 번
 * 쓴 것이다. 하나를 고치면 다른 하나도 같이 고쳐야 한다. 합치지 못하는 건 {@code @Query} 값이
 * 컴파일 상수여야 해서 런타임 조립이 안 되는데, 목록 쪽은 대상을 파라미터가 아니라 바깥
 * 별칭에 걸어야 하기 때문이다.
 */
final class ReviewQueries {

    /** 후기에서 거래를 거쳐 게시글까지. 집계와 서브쿼리에 쓰고 페치는 하지 않는다. */
    static final String TARGET_JOINS = """
              join rv.order rvo
              join rvo.request rvrq
              join rvrq.product rvp
              join rvrq.delivery rvd
            """;

    /** 목록용. 당사자 둘까지 같이 올려서 {@code getTarget()} 이 SELECT 없이 돌게 한다. */
    static final String TARGET_JOINS_FETCH = """
              join fetch rv.order rvo
              join fetch rvo.request rvrq
              join fetch rvrq.product rvp
              join fetch rvp.author
              join fetch rvrq.delivery rvd
              join fetch rvd.author
            """;

    /**
     * {@code :userId} 가 평가받은 후기를 고르는 조건. 당사자이면서 본인이 쓰지는 않은 후기다.
     *
     * {@link #TARGET_JOINS} 나 {@link #TARGET_JOINS_FETCH} 와 같이 써야 한다.
     */
    static final String RECEIVED_BY_USER = """
            (rvp.author.id = :userId or rvd.author.id = :userId)
               and rv.reviewer.id <> :userId
            """;

    /**
     * 바깥 쿼리의 {@code d.author} 가 받은 후기들의 평균 평점. 경로 목록의 만족도 필터가 쓴다.
     * 바깥에 {@code Delivery d} 별칭이 있어야 돌아간다.
     *
     * 주의: 후기가 한 건도 없으면 {@code null} 이라 어떤 값과 비교해도 거짓이다. 후기가 없는
     * 전달자는 만족도 필터를 켜는 순간 목록에서 사라진다.
     */
    static final String AVG_RATING_OF_ROUTE_AUTHOR = """
            (select avg(rv.rating) from Review rv
            """
            + TARGET_JOINS
            + """
              where (rvp.author.id = d.author.id or rvd.author.id = d.author.id)
                and rv.reviewer.id <> d.author.id)
            """;

    private ReviewQueries() {
    }
}
