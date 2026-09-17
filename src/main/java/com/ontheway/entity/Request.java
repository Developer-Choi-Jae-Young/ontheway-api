package com.ontheway.entity;

import com.ontheway.enums.DeliveryStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 의뢰 요청 — 물품 게시글을 경로 게시글에 답글로 붙인 것. <b>모든 요청이 1행씩 남는 스파인이다.</b>
 *
 * <p>status 컬럼을 두지 않는다. 매칭대기중·거절은 {@code rejectedAt} 과 {@link DeliveryOrder}
 * 존재 여부로 표현하므로 상태값이 두 테이블에 흩어지지 않는다 (ERD_REVIEW 1-1).
 *
 * <pre>
 * rejectedAt != null              → 거절 / 연쇄 종료
 * rejectedAt == null && order 없음 → 매칭대기중
 * order 있음                       → order.getStatus()
 * </pre>
 *
 * <p>수락 이후에도 삭제하지 않는다 — DeliveryOrder 가 이 행을 참조한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "request",
        uniqueConstraints = @UniqueConstraint(name = "uk_request_delivery_product",
                columnNames = {"delivery_id", "product_id"}),
        indexes = {
                // 2.3 경로 상세의 의뢰자 목록 / 2.4 수정 가능 여부 / 7 전달내역
                @Index(name = "idx_request_delivery", columnList = "delivery_id, rejected_at"),
                // 7 의뢰내역 — product(author) 에서 타고 들어온다 (1-6)
                @Index(name = "idx_request_product", columnList = "product_id")
        })
public class Request extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 전달자는 {@code delivery.author} 다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_request_delivery"))
    private Delivery delivery;

    /** 의뢰자는 {@code product.author} 다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_request_product"))
    private Product product;

    /**
     * 전달자의 개별 거절과 다른 건 성사에 의한 자동 종료를 함께 표현한다. 둘을 구분하는 사유 컬럼은
     * 두지 않는다 — 의뢰자에게 거절을 알리는 화면이 명세에 없어 구분해 보여줄 곳이 없다 (B17).
     */
    private LocalDateTime rejectedAt;

    /**
     * 수락 시에만 생기는 확장. 1:0..1 이다.
     *
     * <p>주의: {@code mappedBy} 인 @OneToOne 은 null 여부를 알아야 해서 LAZY 가 실제로는
     * 동작하지 않고 추가 SELECT 가 나간다. 목록 조회에서는 {@code left join fetch r.order} 로
     * 함께 가져올 것 (ERD_REVIEW 1-6).
     */
    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
    private DeliveryOrder order;

    @Builder
    public Request(Delivery delivery, Product product) {
        this.delivery = delivery;
        this.product = product;
    }

    /** 상태 합성은 여기 한 곳으로 고정한다. 레포지토리는 status 로 필터하지 않는다 (2-7). */
    public DeliveryStatus getStatus() {
        if (rejectedAt != null) {
            return DeliveryStatus.REJECTED;
        }
        return order == null ? DeliveryStatus.MATCHING_WAITING : order.getStatus();
    }

    public boolean isMatchingWaiting() {
        return rejectedAt == null && order == null;
    }

    /** 개별 거절과 연쇄 종료 양쪽에 쓰인다. 연쇄는 벌크 UPDATE 로 처리한다 (2-5 d). */
    public void reject(LocalDateTime now) {
        this.rejectedAt = now;
    }

    // 당사자는 저장하지 않고 게시글에서 얻는다 (1-6). 대신 프록시를 2단계 타므로
    // 목록 조회에서는 join fetch 로 product/delivery 와 그 author 까지 올려둘 것 (2-4).

    public User getRequester() {
        return product.getAuthor();
    }

    public User getDeliverer() {
        return delivery.getAuthor();
    }
}
