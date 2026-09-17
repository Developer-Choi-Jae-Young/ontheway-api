package com.ontheway.repository;

import com.ontheway.entity.Delivery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link DeliveryRepositoryCustom} 구현. 조건 조각을 {@code List} 에 모았다가 한 번에
 * {@code and} 로 잇는다. 문자열에 {@code " and "} 를 직접 붙여 나가면 조건이 하나도 안 붙는
 * 경우와 첫 조건 앞을 매번 따져야 한다.
 *
 * 주의: 클래스명이 {@code DeliveryRepository} + {@code Impl} 이어야 한다. 이름이 어긋나면
 * 스프링이 이 구현을 조용히 무시하고 빈 생성이 실패한다.
 */
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepositoryCustom {

    /**
     * 아직 수락된 요청이 없는 글만 남기는 조건. 상태 컬럼 대신 anti-join 으로 본다.
     *
     * 요청이 들어와 있어도 수락 전이면 그대로 목록에 뜬다. 반대로 한 번 수락됐으면 그 거래가
     * 취소나 실패로 끝나도 행이 남아 다시 뜨지 않는다.
     */
    private static final String NOT_MATCHED_YET = """
            not exists (
                   select 1 from DeliveryOrder o
                     join o.request rq
                    where rq.delivery = d)""";

    /** 배송예정일이 가까운 순, 같은 날짜면 최신순. 두 목록이 같이 쓴다. */
    private static final String ORDER_BY = " order by d.deliveryDate asc, d.id desc";

    private final EntityManager em;

    @Override
    public Slice<Delivery> searchOpenRoutes(RouteSearchCond cond, LocalDateTime now, Pageable pageable) {
        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        where.add("d.deletedAt is null");
        where.add(NOT_MATCHED_YET);
        // 출발 시각이 아직 안 지난 글. 날짜와 시각이 나뉘어 있어 두 단계로 비교한다
        where.add("(d.deliveryDate > :today"
                + " or (d.deliveryDate = :today and d.plannedStartTime > :nowTime))");
        params.put("today", now.toLocalDate());
        params.put("nowTime", now.toLocalTime());

        addFilters(cond, where, params);
        return slice(where, params, pageable);
    }

    @Override
    public Slice<Delivery> searchMyRoutes(Long authorId, RouteSearchCond cond, Pageable pageable) {
        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        where.add("d.deletedAt is null");
        where.add("d.author.id = :authorId");
        params.put("authorId", authorId);

        addFilters(cond, where, params);
        return slice(where, params, pageable);
    }

    /** 두 목록이 같이 쓰는 검색 조건. 값이 들어온 것만 붙인다. */
    private static void addFilters(RouteSearchCond cond, List<String> where, Map<String, Object> params) {
        if (StringUtils.hasText(cond.startAddress())) {
            where.add("d.departure.address like :startAddress escape '" + LikePatterns.ESCAPE + "'");
            params.put("startAddress", LikePatterns.startsWith(cond.startAddress()));
        }
        if (StringUtils.hasText(cond.endAddress())) {
            where.add("d.destination.address like :endAddress escape '" + LikePatterns.ESCAPE + "'");
            params.put("endAddress", LikePatterns.startsWith(cond.endAddress()));
        }
        // 의뢰자가 낼 수 있는 금액. 경로의 희망금액이 그 이하인 것만 남긴다
        if (cond.hopePrice() != null) {
            where.add("d.desiredPrice <= :hopePrice");
            params.put("hopePrice", cond.hopePrice());
        }
        // 만족도 하한. 평점 캐시 컬럼이 없어서 Review 를 이 자리에서 집계한다
        if (cond.minRating() != null) {
            where.add(ReviewQueries.AVG_RATING_OF_ROUTE_AUTHOR + " >= :minRating");
            params.put("minRating", cond.minRating());
        }
    }

    /**
     * 오프셋 페이징. {@code size + 1} 건을 읽어서 넘치는 게 있는지로 {@code hasNext} 를
     * 정하므로 {@code count} 쿼리가 나가지 않는다.
     */
    private Slice<Delivery> slice(List<String> where, Map<String, Object> params, Pageable pageable) {
        // Unpaged 를 그냥 두면 getPageSize() 가 UnsupportedOperationException 을 던진다.
        // 영문 모를 예외 대신 뭘 해야 하는지 알려준다
        if (pageable.isUnpaged()) {
            throw new IllegalArgumentException(
                    "경로 목록은 반드시 페이징한다. PageRequest.of(page, size) 로 호출할 것");
        }

        String jpql = "select d from Delivery d join fetch d.author"
                + " where " + String.join("\n   and ", where)
                + "\n" + ORDER_BY;

        TypedQuery<Delivery> query = em.createQuery(jpql, Delivery.class);
        params.forEach(query::setParameter);

        int size = pageable.getPageSize();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(size + 1);

        List<Delivery> rows = query.getResultList();
        boolean hasNext = rows.size() > size;
        return new SliceImpl<>(hasNext ? new ArrayList<>(rows.subList(0, size)) : rows, pageable, hasNext);
    }
}
