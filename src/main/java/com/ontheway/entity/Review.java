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
 * 후기·만족도. 배송완료 건에만 작성할 수 있다 — 취소·실패 건은 평점 작성 자체가 불가하다(B9).
 *
 * <p><b>피평가자(target) 컬럼을 두지 않는다.</b> 거래를 알면 당사자 2인이 결정되고 그중
 * reviewer 가 아닌 쪽이 target 이므로, 같은 사실을 두 곳에 저장해 어긋나게 두지 않고 유도한다.
 * 명세 4.1 데이터 항목과 의도적으로 다른 선택이다 (ERD_REVIEW 1-5).
 *
 * <p>전제: requester 와 deliverer 가 같은 사람이면 이 유도가 성립하지 않는다.
 * <b>요청 등록 시 자기 경로에 자기 물품을 붙이지 못하도록 서비스에서 막아야 한다.</b>
 *
 * <p>제목 컬럼도 없다 — 명세 4.1 데이터 항목에 제목이 없다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review",
        // 4.1 동일 거래·동일 상대 중복 평가 불가
        uniqueConstraints = @UniqueConstraint(name = "uk_review_order_reviewer",
                columnNames = {"order_id", "reviewer_id"}),
        // 5.4 보낸 후기
        indexes = @Index(name = "idx_review_reviewer", columnList = "reviewer_id, id"))
// getTarget() 이 order → request → product/delivery → author 까지 4단계를 탄다.
// 그냥 조회하면 후기 1건마다 SELECT 가 3~4회 더 나가므로, 목록·상세 쿼리는
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

    /** 거래 당사자 2인까지 함께 가져오는 페치 플랜. {@link #getTarget()} 을 쓰는 쿼리에 붙인다. */
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

    /** 0.0 ~ 5.0, 0.5 단위. 평균 집계를 하므로 문자열이 아니라 DECIMAL 이다. */
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
     * 피평가자 = 거래 당사자 2인 중 작성자가 아닌 쪽.
     *
     * <p><b>공짜가 아니다.</b> 프록시를 4단계 타므로 페치 플랜 없이 부르면 SELECT 가 3~4회
     * 더 나가고, 트랜잭션 밖이면 {@code LazyInitializationException} 이다.
     * 조회할 때 {@link #WITH_PARTIES} 를 붙일 것.
     */
    public User getTarget() {
        Request request = order.getRequest();
        User requester = request.getRequester();
        return reviewer.getId().equals(requester.getId())
                ? request.getDeliverer()
                : requester;
    }
}
