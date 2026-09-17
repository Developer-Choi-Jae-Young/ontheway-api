package com.ontheway.repository;

/**
 * 후기의 <b>피평가자</b>를 유도하는 규칙. {@code Review} 에 피평가자 컬럼이 없어서(1-5)
 * 거래를 타고 올라가 계산해야 하는데, 그 계산이 흩어지면 2.2 목록에 뜨는 만족도와
 * 5.1 프로필 만족도가 서로 다른 값이 된다. 규칙은 이 파일에만 둔다.
 *
 * <p>별칭에 {@code rv} 접두사를 붙인 이유는 이 조각이 <b>바깥 쿼리 안에 끼워지기</b> 때문이다.
 * 2.2 목록은 {@code Delivery d} 를 쓰고 있어서 접두사가 없으면 {@code d} 가 겹친다.
 *
 * <p><b>{@link #RECEIVED_BY_USER} 와 {@link #AVG_RATING_OF_ROUTE_AUTHOR} 는 같은 규칙의
 * 두 표현이다. 하나를 고치면 반드시 다른 하나도 고친다.</b> 한 문자열로 합칠 수 없는 이유는
 * {@code @Query} 의 값이 컴파일 상수여야 해서 런타임 조립이 안 되는데, 목록 쪽은 피평가자를
 * 파라미터가 아니라 <b>바깥 별칭에 상관(correlate)</b>시켜야 하기 때문이다.
 */
final class ReviewQueries {

    /** 후기 → 거래 → 게시글. 집계·서브쿼리용(페치 없음). */
    static final String TARGET_JOINS = """
              join rv.order rvo
              join rvo.request rvrq
              join rvrq.product rvp
              join rvrq.delivery rvd
            """;

    /** 목록용. 당사자 2인까지 함께 올려 {@code getTarget()} 이 추가 SELECT 없이 돌게 한다. */
    static final String TARGET_JOINS_FETCH = """
              join fetch rv.order rvo
              join fetch rvo.request rvrq
              join fetch rvrq.product rvp
              join fetch rvp.author
              join fetch rvrq.delivery rvd
              join fetch rvd.author
            """;

    /**
     * 피평가자가 {@code :userId} 인 후기를 고르는 술어.
     * 거래 당사자 2인 중 작성자가 아닌 쪽이 피평가자이므로, 당사자이면서 본인이 쓴 건 아닌 후기다.
     *
     * <p>{@link #TARGET_JOINS} 또는 {@link #TARGET_JOINS_FETCH} 와 함께 써야 한다.
     */
    static final String RECEIVED_BY_USER = """
            (rvp.author.id = :userId or rvd.author.id = :userId)
               and rv.reviewer.id <> :userId
            """;

    /**
     * 피평가자가 <b>바깥 쿼리의 {@code d.author}</b> 인 후기들의 평균 평점. 2.2 목록의
     * 만족도 하한 필터가 쓴다. 바깥에 {@code Delivery d} 별칭이 있어야 성립한다.
     *
     * <p>후기가 한 건도 없으면 {@code null} 이라 어떤 하한과 비교해도 거짓이다 —
     * <b>신규 전달자는 만족도 필터를 켜는 순간 목록에서 빠진다.</b>
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
