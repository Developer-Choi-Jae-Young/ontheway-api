package com.ontheway.repository;

import com.ontheway.entity.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 의뢰 요청 — <b>모든 요청이 1행씩 남는 스파인</b>이라 이용내역(7) 조회도 여기서 시작한다
 * (ERD_REVIEW 1-6). {@code DeliveryOrder} 에서 시작하면 매칭대기중 건이 빠져 UNION 이 필요해진다.
 *
 * <p>상태로 필터하는 메서드는 {@link #findCancelledHistory} 하나뿐이다. 상태는 컬럼이 아니라
 * {@code rejectedAt} 과 {@code DeliveryOrder} 존재로 합성되며, 합성은
 * {@code Request.getStatus()} 한 곳뿐이다 (2-7). 대신 조회 메서드가
 * {@code left join fetch r.order} 로 그 판정에 필요한 행을 미리 붙여준다.
 */
public interface RequestRepository extends JpaRepository<Request, Long> {

    /** 2.6 같은 물품을 같은 경로에 중복 등록 방지. {@code uk_request_delivery_product} 의 사전 검사다. */
    boolean existsByDeliveryIdAndProductId(Long deliveryId, Long productId);

    /**
     * 2.4 경로 수정 가능 여부 — <b>요청이 한 건이라도 들어왔으면 수정할 수 없다.</b>
     * 명세 2.4 의 "의뢰 요청이 오면 수정 불가"를 문구 그대로 따른다.
     *
     * <p>{@code rejectedAt} 을 보지 않는 게 핵심이다. 전달자가 들어온 요청을 전부 거절해도
     * 그 경로는 <b>영구히 수정 불가</b>다 — 의뢰자들이 이미 그 조건을 보고 요청을 걸었으므로
     * 뒤늦게 조건을 바꾸면 판단의 근거가 달라진다. 조건을 바꾸려면 새 경로를 등록한다.
     * (삭제는 2.5 기준으로 요청 유무와 무관하게 작성자면 가능하다.)
     */
    boolean existsByDeliveryId(Long deliveryId);

    // ── 2.3 경로 상세 ────────────────────────────────────────
    // 뷰가 게시자 / 피게시자 / 제3자 셋으로 갈리고, 각자 볼 수 있는 요청의 범위가 다르다.
    // 제3자는 요청을 아예 못 보므로 조회 메서드가 없다 — 경로 본문만 읽으면 된다.

    /**
     * 게시자(전달자) 뷰 — 이 경로에 들어온 <b>모든 의뢰자의</b> 요청. 거절·종료된 건 뺀다.
     *
     * <p>{@code product.author} 까지 끌어오는 이유는 의뢰자를 {@code Request} 에 복사해 두지
     * 않기 때문이고(1-6), {@code order} 를 함께 끌어오는 이유는 각 행의 상태를 찍어야 하기
     * 때문이다. 빼면 요청 N건마다 SELECT 가 더 나간다.
     */
    @Query("""
            select r from Request r
              join fetch r.product p
              join fetch p.author
              left join fetch r.order
             where r.delivery.id = :deliveryId
               and r.rejectedAt is null
             order by r.id asc
            """)
    List<Request> findAllByDeliveryIdWithProductAndAuthor(@Param("deliveryId") Long deliveryId);

    /**
     * 피게시자(의뢰자) 뷰 — 이 경로에 걸린 <b>내 요청만</b>. 화면이 이 요청의 상태에 따라
     * 갈리므로(2.3 피게시자 1)~7)) 그 판정에 필요한 만큼만 가져온다.
     *
     * <p><b>위 메서드로 대신하면 안 된다.</b> 그건 남의 요청까지 다 읽어오는데, 2.3 은
     * 의뢰자 목록을 게시자만 볼 수 있다고 못 박고 있다. 서비스에서 걸러내는 건 이미 읽은
     * 뒤라 권한 경계가 아니다.
     *
     * <p>반환이 {@code List} 인 이유: {@code uk_request_delivery_product} 는 같은 물품의
     * 중복 등록만 막는다. 한 사람이 <b>다른 물품 여러 개</b>를 같은 경로에 거는 건 막혀 있지
     * 않으므로 1건이라고 단정할 수 없다.
     *
     * <p>거절된 요청도 그대로 준다 — 의뢰자에게 거절을 어떻게 알릴지가 아직 미결(B17)이라
     * 레포지토리가 먼저 정해버리지 않는다. 거를지 말지는 서비스가 정한다.
     */
    @Query("""
            select r from Request r
              join fetch r.product p
              left join fetch r.order
             where r.delivery.id = :deliveryId
               and p.author.id = :requesterId
             order by r.id asc
            """)
    List<Request> findMyRequestsOnDelivery(@Param("deliveryId") Long deliveryId,
                                           @Param("requesterId") Long requesterId);

    /**
     * 2.2 / 6.1 목록의 {@code requestCount}. 경로 여러 건의 요청 수를 <b>한 번에</b> 센다 —
     * 목록을 돌며 경로마다 세면 20건짜리 화면에 SELECT 가 20번 더 나간다.
     *
     * <p>요청이 0건인 경로는 결과에 없다. 서비스가 맵으로 묶고 없는 키는 0 으로 본다.
     */
    @Query("""
            select r.delivery.id as deliveryId, count(r) as requestCount
              from Request r
             where r.delivery.id in :deliveryIds
               and r.rejectedAt is null
             group by r.delivery.id
            """)
    List<DeliveryRequestCount> countActiveByDeliveryIds(
            @Param("deliveryIds") Collection<Long> deliveryIds);

    // ── 7. 이용내역 ───────────────────────────────────────────
    // 네 화면이 where 절 한 줄만 다르다. 공통 조각은 RequestQueries 에 있다.
    // 삭제된 게시글도 그대로 보여준다 — 소프트 삭제를 택한 이유가 이것이다 (1-6).
    //
    // Pageable 은 PageRequest.of(page, size) 로 넘긴다. 정렬은 쿼리에 박혀 있으니
    // Sort 를 담지 않는다. 반환이 Slice 라 count 쿼리가 나가지 않는다.

    /**
     * {@code /history/list} 전체 — 의뢰·전달을 한 목록에 섞는다.
     *
     * <p><b>두 쿼리를 메모리에서 합치지 않는 이유</b>: 각각 N건씩 받아 합친 뒤 자르면 잘린
     * 쪽의 다음 위치를 알 수 없어 페이지가 겹치거나 빠진다. 스파인이 {@code Request}
     * 하나라서 한 쿼리로 끝난다.
     *
     * <p>행마다 {@code BoardType} 은 {@code product.author} 가 나인지로 정한다 —
     * 나면 REQUEST(의뢰), 아니면 DELIVERY(전달).
     */
    @Query(RequestQueries.HISTORY_SELECT
            + " where " + RequestQueries.IS_PARTY
            + RequestQueries.ORDER_LATEST)
    Slice<Request> findHistory(@Param("userId") Long userId, Pageable pageable);

    /** {@code /history/request/list} 의뢰내역 — 내가 물품을 올려서 보낸 요청들. */
    @Query(RequestQueries.HISTORY_SELECT
            + " where pa.id = :userId"
            + RequestQueries.ORDER_LATEST)
    Slice<Request> findRequestedHistory(@Param("userId") Long userId, Pageable pageable);

    /** {@code /history/delivery/list} 전달내역 — 내 경로에 들어온 요청들. */
    @Query(RequestQueries.HISTORY_SELECT
            + " where da.id = :userId"
            + RequestQueries.ORDER_LATEST)
    Slice<Request> findDeliveredHistory(@Param("userId") Long userId, Pageable pageable);

    /**
     * {@code /history/cancel/list} 취소·실패 목록 (4.5 / 7).
     *
     * <p>사유는 여기서 안 읽는다 — {@code HistoryListResponseDto} 에 사유 필드가 없다.
     * 상세에서 필요해지면 {@code FailedAndCancelledRepository#findByOrderId} 를 쓴다.
     *
     * <p>{@code left join fetch} 한 {@code o} 를 where 에서 걸러 사실상 inner join 이 된다 —
     * 여기서는 거래가 있는 행만 원하므로 의도한 동작이다.
     */
    @Query(RequestQueries.HISTORY_SELECT
            + " where " + RequestQueries.IS_PARTY
            + " and o.status in (com.ontheway.enums.DeliveryStatus.CANCELED,"
            + " com.ontheway.enums.DeliveryStatus.FAILED)"
            + RequestQueries.ORDER_LATEST)
    Slice<Request> findCancelledHistory(@Param("userId") Long userId, Pageable pageable);

    // ── 수락 시 연쇄 종료 (§4) ────────────────────────────────
    // 요청을 하나씩 로딩해 바꾸면 N번 UPDATE 가 나가므로 벌크로 처리한다.
    // 벌크는 영속성 컨텍스트를 우회하므로 두 플래그가 반드시 필요하다:
    //   flushAutomatically  — 아직 안 나간 변경을 먼저 내보낸다
    //   clearAutomatically  — 낡아버린 1차 캐시를 비운다
    // 호출 순서는 ① DeliveryOrder INSERT → ② 아래 벌크 로 고정한다 (2-5 d).
    //
    // ⚠️ clearAutomatically 는 컨텍스트를 통째로 비운다. 이 호출 뒤에 들고 있던 엔티티는
    //    준영속이라 필드를 바꿔도 반영되지 않는다 — 벌크는 트랜잭션 마지막에 둘 것.

    /** (A) 같은 경로에 들어온 나머지 요청을 종료한다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Request r
               set r.rejectedAt = :now
             where r.delivery.id = :deliveryId
               and r.id <> :acceptedRequestId
               and r.rejectedAt is null
            """)
    int rejectSiblingsByDelivery(@Param("deliveryId") Long deliveryId,
                                 @Param("acceptedRequestId") Long acceptedRequestId,
                                 @Param("now") LocalDateTime now);

    /** (B) 같은 물품이 다른 경로에 걸어둔 요청을 종료한다. 물품 게시글은 1회용이다 (B6). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Request r
               set r.rejectedAt = :now
             where r.product.id = :productId
               and r.id <> :acceptedRequestId
               and r.rejectedAt is null
            """)
    int rejectSiblingsByProduct(@Param("productId") Long productId,
                                @Param("acceptedRequestId") Long acceptedRequestId,
                                @Param("now") LocalDateTime now);
}
