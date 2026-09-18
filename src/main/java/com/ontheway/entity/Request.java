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
 * 의뢰 요청. 물품 게시글을 경로 게시글에 답글처럼 붙인 것이고, 수락됐든 아니든 요청 하나당
 * 한 행이 남는다.
 *
 * 상태는 컬럼이 아니라 {@code rejectedAt} 과 {@link DeliveryOrder} 존재로 정해진다.
 *
 * rejectedAt != null              -> 거절 / 연쇄 종료
 * rejectedAt == null && order 없음 -> 매칭대기중
 * order 있음                       -> order.getStatus()
 *
 * 수락된 뒤에도 지우지 않는다. DeliveryOrder 가 이 행을 참조한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "request",
        uniqueConstraints = @UniqueConstraint(name = "uk_request_delivery_product",
                columnNames = {"delivery_id", "product_id"}),
        indexes = {
                // 경로 상세의 의뢰자 목록, 경로 수정 가능 여부, 전달내역
                @Index(name = "idx_request_delivery", columnList = "delivery_id, rejected_at"),
                // 의뢰내역. 의뢰자를 product.author 로 타고 들어온다
                @Index(name = "idx_request_product", columnList = "product_id")
        })
public class Request extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 전달자는 이 경로의 작성자다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_request_delivery"))
    private Delivery delivery;

    /** 의뢰자는 이 물품의 작성자다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_request_product"))
    private Product product;

    /** 전달자가 거절했거나 다른 요청이 성사돼 자동 종료된 시각. 둘을 구분하지는 않는다. */
    private LocalDateTime rejectedAt;

    /**
     * 수락됐을 때만 생기는 확장.
     *
     * 주의: {@code mappedBy} 쪽 @OneToOne 은 null 인지 알아야 해서 LAZY 가 먹지 않고, 건드릴
     * 때마다 SELECT 가 따로 나간다. 목록 조회에는 {@code left join fetch r.order} 를 같이 걸 것.
     */
    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
    private DeliveryOrder order;

    @Builder
    public Request(Delivery delivery, Product product) {
        this.delivery = delivery;
        this.product = product;
    }

    /** 상태를 만드는 곳은 여기 하나다. 레포지토리는 상태로 필터하지 않는다. */
    public DeliveryStatus getStatus() {
        if (rejectedAt != null) {
            return DeliveryStatus.REJECTED;
        }
        return order == null ? DeliveryStatus.MATCHING_WAITING : order.getStatus();
    }

    public boolean isMatchingWaiting() {
        return rejectedAt == null && order == null;
    }

    /** 거절 처리. 수락에 딸린 연쇄 종료는 벌크 UPDATE 로 따로 돈다. */
    public void reject(LocalDateTime now) {
        this.rejectedAt = now;
    }

    // 당사자는 게시글에서 가져온다. 프록시를 두 단계 타므로 목록 조회에서는 join fetch 로
    // product/delivery 와 그 author 까지 올려두고 쓸 것.

    public User getRequester() {
        return product.getAuthor();
    }

    public User getDeliverer() {
        return delivery.getAuthor();
    }
}
