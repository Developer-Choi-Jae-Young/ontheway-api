package com.ontheway.repository;

import com.ontheway.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 성사된 배송 거래.
 *
 * <p>경로·물품 스냅샷 컬럼을 두지 않기로 했으므로(1-1) 아래 두 {@code exists} 는
 * {@code request} 를 타고 들어간다. <b>이 둘이 연쇄 규칙 (A)(B)를 지키는 유일한 수단이다</b> —
 * DB UNIQUE 로는 강제하지 못하고, 확인과 INSERT 사이의 창은 감수하기로 했다 (2-5 d).
 *
 * <p>둘 다 상태를 보지 않는다. 취소·실패해도 행은 남고, 그러면 그 경로·물품은 다시 쓸 수 없다 —
 * <b>재활용 불가가 의도된 동작이다.</b> 새로 등록해야 한다.
 */
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    /**
     * (A) 이 경로에 이미 성사된 배송이 있는가. 경로 1건 = 배송 1건 (B5).
     *
     * <p>이름의 밑줄은 오타가 아니라 <b>연관관계 경로</b>다({@code request.delivery.id}).
     * {@code DeliveryOrder} 에 {@code delivery} 필드가 없으니 이름만 보고는 어디를 타는지
     * 알 수 없어서, 경로를 이름에 그대로 드러낸다.
     */
    boolean existsByRequest_Delivery_Id(Long deliveryId);

    /**
     * (B) 이 물품이 이미 거래에 쓰였는가. 물품 게시글은 1회용이다 (B6).
     *
     * <p>수락 전 중복 검증, 2.9/2.10 물품 수정·삭제 가능 여부, 2.6 사용 이력 검증(1-8)이
     * 전부 이 한 메서드를 쓴다. <b>"쓰였으니 수정 불가"라는 결론은 서비스가 낸다</b> (2-7).
     */
    boolean existsByRequest_Product_Id(Long productId);

    Optional<DeliveryOrder> findByRequestId(Long requestId);

    /**
     * 4.x 배송 상세·상태 변경. 당사자 2인까지 한 번에 올려둔다.
     *
     * <p>{@code getDeliveryFee()} / {@code getRequester()} 같은 파생 getter 가 프록시를
     * 여러 단계 타므로, 이걸 안 쓰면 화면 한 장에 SELECT 가 서너 번 더 나간다.
     */
    @Query("""
            select o from DeliveryOrder o
              join fetch o.request rq
              join fetch rq.product p
              join fetch p.author
              join fetch rq.delivery d
              join fetch d.author
             where o.id = :id
            """)
    Optional<DeliveryOrder> findByIdWithParties(@Param("id") Long id);

    // ── 스케줄러 ──────────────────────────────────────────────
    // 둘 다 상태를 쿼리에 박는다. 파라미터로 받으면 "다른 상태로도 부를 수 있는 메서드"처럼
    // 보이지만 실제로는 이 값 말고는 의미가 없다 — 대상 상태가 곧 스케줄러의 정의다.

    /**
     * ① 예정 배송시각이 도달한 건 → 배송중.
     * 경로의 날짜와 시각이 분리돼 있어 비교가 두 단계다.
     */
    @Query("""
            select o from DeliveryOrder o
              join fetch o.request rq
              join fetch rq.delivery d
             where o.status = com.ontheway.enums.DeliveryStatus.DELIVERY_WAITING
               and (d.deliveryDate < :today
                    or (d.deliveryDate = :today and d.plannedStartTime <= :nowTime))
            """)
    List<DeliveryOrder> findDueForDelivering(@Param("today") LocalDate today,
                                             @Param("nowTime") LocalTime nowTime);

    /**
     * ② 확인요청 후 72시간이 지난 건 → 배송완료.
     * {@code idx_order_completion_req(status, completion_requested_at)} 를 탄다.
     */
    @Query("""
            select o from DeliveryOrder o
             where o.status = com.ontheway.enums.DeliveryStatus.COMPLETION_REQUESTED
               and o.completionRequestedAt < :deadline
            """)
    List<DeliveryOrder> findDueForCompletion(@Param("deadline") LocalDateTime deadline);
}
