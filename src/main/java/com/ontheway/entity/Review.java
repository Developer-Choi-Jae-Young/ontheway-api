package com.ontheway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 후기와 만족도. 배송완료된 거래에만 달 수 있다.
 *
 * 누구를 평가한 건지는 컬럼으로 두지 않고 {@link #getTarget()} 이 거래에서 계산한다.
 * 거래를 알면 당사자가 둘로 정해지고, 그중 작성자가 아닌 쪽이 평가 대상이다.
 *
 * 주의: 이 계산은 의뢰자와 전달자가 서로 다른 사람이라는 전제에 기대고 있다.
 * 자기 경로에 자기 물품을 거는 건 요청 단계에서 서비스가 막아야 한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review",
        // 한 거래에 같은 사람이 두 번 평가하지 못하게 막는다
        uniqueConstraints = @UniqueConstraint(name = "uk_review_order_reviewer",
                columnNames = {"order_id", "reviewer_id"}),
        // 내가 쓴 후기 목록의 진입점
        indexes = @Index(name = "idx_review_reviewer", columnList = "reviewer_id, id"))
// getTarget() 이 order -> request -> product/delivery -> author 까지 네 단계를 탄다.
// 그냥 조회하면 후기 한 건마다 SELECT 가 서너 번 더 나가므로, 목록과 상세 쿼리에는
// @EntityGraph(Review.WITH_PARTIES) 를 붙여 한 번에 가져온다.
@NamedEntityGraph(name = Review.WITH_PARTIES,
        attributeNodes = {
                @NamedAttributeNode("reviewer"),
                @NamedAttributeNode(value = "order", subgraph = "order")
        },
        subgraphs = {
                @NamedSubgraph(name = "order",
                        attributeNodes = @NamedAttributeNode(value = "request", subgraph = "request")),
                @NamedSubgraph(name = "request", attributeNodes = {
                        @NamedAttributeNode(value = "product", subgraph = "productAuthor"),
                        @NamedAttributeNode(value = "delivery", subgraph = "deliveryAuthor")
                }),
                @NamedSubgraph(name = "productAuthor", attributeNodes = @NamedAttributeNode("author")),
                @NamedSubgraph(name = "deliveryAuthor", attributeNodes = @NamedAttributeNode("author"))
        })
public class Review extends BaseTimeEntity {

    /** 거래 당사자 둘까지 같이 끌어오는 페치 플랜. {@link #getTarget()} 을 쓰는 쿼리에 붙인다. */
    public static final String WITH_PARTIES = "Review.withParties";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_review_order"))
    private DeliveryOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_review_reviewer"))
    private User reviewer;

    /** 0.0 ~ 5.0, 0.5 단위. 평균을 내야 해서 DECIMAL 이다. */
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(length = 1000)
    private String content;

    @Builder
    public Review(DeliveryOrder order, User reviewer, BigDecimal rating, String content) {
        this.order = order;
        this.reviewer = reviewer;
        this.rating = rating;
        this.content = content;
    }

    /**
     * 평가 대상. 거래 당사자 둘 중 작성자가 아닌 쪽이다.
     *
     * 주의: 프록시를 네 단계 탄다. {@link #WITH_PARTIES} 없이 부르면 SELECT 가 서너 번 더
     * 나가고, 트랜잭션 밖이면 {@code LazyInitializationException} 이 난다.
     */
    public User getTarget() {
        Request request = order.getRequest();
        User requester = request.getRequester();
        return reviewer.getId().equals(requester.getId())
                ? request.getDeliverer()
                : requester;
    }
}
