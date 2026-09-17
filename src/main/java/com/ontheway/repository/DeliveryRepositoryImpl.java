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
 * ⚠️ <b>클래스명은 반드시 {@code DeliveryRepository} + {@code Impl} 이어야 한다.</b>
 * 어기면 스프링이 조용히 무시하고 빈 생성 실패로 이어진다 (ERD_REVIEW 2-5 c).
 *
 * <p>조건 조각을 {@code List} 에 모았다가 한 번에 {@code and} 로 잇는다. 문자열에 직접
 * {@code " and "} 를 붙여 나가면 조건이 하나도 안 붙는 경우와 첫 조건의 접두사를 매번 따져야 한다.
 */
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepositoryCustom {

    /**
     * 아직 성사된 거래가 없는 글 = "공개 종료" 판정. {@code Delivery.status} 컬럼 대신
     * anti-join 으로 유도한다 (1-1). 취소·실패한 거래도 행은 남으므로 그 경로는 다시 열리지
     * 않는다 — 재활용 불가가 의도다.
     */
    private static final String NOT_MATCHED_YET = """
            not exists (
                   select 1 from DeliveryOrder o
                     join o.request rq
                    where rq.delivery = d)""";

    /** 가까운 배송예정일 순 → 동일 일자는 최신순 (2.2 / 6.1 공통). */
    private static final String ORDER_BY = " order by d.deliveryDate asc, d.id desc";

    private final EntityManager em;

    @Override
    public Slice<Delivery> searchOpenRoutes(RouteSearchCond cond, LocalDateTime now, Pageable pageable) {
        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        where.add("d.deletedAt is null");
        where.add(NOT_MATCHED_YET);
        // 예정 시작시각이 지나지 않은 글. 날짜와 시각이 분리돼 있어 두 단계로 비교한다
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

    /** 2.2 와 6.1 이 공유하는 동적 필터. 값이 들어온 것만 붙인다. */
    private static void addFilters(RouteSearchCond cond, List<String> where, Map<String, Object> params) {
        if (StringUtils.hasText(cond.startAddress())) {
            where.add("d.departure.address like :startAddress escape '" + LikePatterns.ESCAPE + "'");
            params.put("startAddress", LikePatterns.startsWith(cond.startAddress()));
        }
        if (StringUtils.hasText(cond.endAddress())) {
            where.add("d.destination.address like :endAddress escape '" + LikePatterns.ESCAPE + "'");
            params.put("endAddress", LikePatterns.startsWith(cond.endAddress()));
        }
        // 희망금액은 상한 하나다. 의뢰자가 낼 수 있는 금액 이하인 경로만 남긴다
        if (cond.hopePrice() != null) {
            where.add("d.desiredPrice <= :hopePrice");
            params.put("hopePrice", cond.hopePrice());
        }
        // 만족도 하한 — User 에 평점 캐시 컬럼을 두지 않으므로 Review 를 그 자리에서 집계한다 (2-5 e).
        // 피평가자 유도 규칙은 ReviewQueries 한 곳에만 둔다.
        if (cond.minRating() != null) {
            where.add(ReviewQueries.AVG_RATING_OF_ROUTE_AUTHOR + " >= :minRating");
            params.put("minRating", cond.minRating());
        }
    }

    /**
     * 오프셋 페이징. {@code size + 1} 건을 읽어 초과분의 존재로 {@code hasNext} 를 판정하므로
     * {@code count} 쿼리가 나가지 않는다.
     */
    private Slice<Delivery> slice(List<String> where, Map<String, Object> params, Pageable pageable) {
        // Unpaged 를 그냥 두면 getPageSize() 가 UnsupportedOperationException 을 던진다.
        // 무슨 일인지 알 수 없는 예외 대신 "여긴 페이징이 필수다"라고 말해준다
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
