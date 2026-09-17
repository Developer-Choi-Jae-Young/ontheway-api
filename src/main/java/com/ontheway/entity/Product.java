package com.ontheway.entity;

import com.ontheway.enums.PaymentType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 의뢰 물품 게시글. 경로에 종속되지 않는 독립 엔티티로, 여러 경로에 반복 등록할 수 있다.
 *
 * <p><b>상태 컬럼을 두지 않는다.</b> 물품 1건이 여러 경로에 걸려 있을 수 있어 물품 자체에
 * 상태를 두면 표현이 불가능하다. "이미 거래에 쓰였는가"는 DeliveryOrder 존재 여부로 유도한다
 * (ERD_REVIEW 1-1).
 *
 * <p>수령·도착 시각이 {@code LocalDateTime} 인 이유: 물품 게시글은 경로가 정해지기 전에
 * 작성되므로 상속받을 날짜가 없다. 시각만 두면 "9/20 15시까지"를 표현할 수 없다 (1-9).
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product", indexes =
        // 6.2 내 게시글 - 의뢰 탭 / 7 의뢰내역의 진입점 (1-6)
        @Index(name = "idx_product_author", columnList = "author_id, deleted_at, id"))
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 의뢰자. Request 에 복사해 두지 않고 여기서 조인으로 얻는다 (1-6). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_product_author"))
    private User author;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(length = 500)
    private String itemInfo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address", nullable = false, length = 255)),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude", nullable = false, precision = 10, scale = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude", nullable = false, precision = 10, scale = 7))
    })
    private Location pickup;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "destination_address", nullable = false, length = 255)),
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude", nullable = false, precision = 10, scale = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude", nullable = false, precision = 10, scale = 7))
    })
    private Location destination;

    @Column(nullable = false)
    private LocalDateTime pickupTime;

    @Column(nullable = false)
    private LocalDateTime desiredArrivalTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PaymentType paymentType;

    /**
     * 거래 금액의 원본. 수락되면 물품 수정이 막히고(2.9) 그 물품은 재사용되지 않으므로
     * 값이 영구히 불변이다 — DeliveryOrder 에 스냅샷을 두지 않는다 (1-1).
     */
    @Column(nullable = false)
    private Integer deliveryFee;

    /**
     * 2.6 허용물품·책임 동의 시각. 동의해야만 등록되므로 <b>동의 여부 불리언은 두지 않는다</b> —
     * {@code NOT NULL} 인 채로 항상 {@code true} 인 컬럼이 되기 때문이다. {@code User} 의
     * 약관 동의와 같은 판단이다 (ERD_REVIEW 1-11).
     */
    @Column(nullable = false)
    private LocalDateTime termsAgreedAt;

    private LocalDateTime deletedAt;

    /**
     * 등록·수정에서 함께 쓰는 본문. 같은 타입의 값이 여러 개라({@code Location} 2개,
     * {@code LocalDateTime} 2개) 위치 인자로 넘기면 뒤바꿔도 컴파일이 통과한다.
     */
    @Builder
    public record Content(
            String itemName, String itemInfo,
            Location pickup, Location destination,
            LocalDateTime pickupTime, LocalDateTime desiredArrivalTime,
            PaymentType paymentType, Integer deliveryFee) {
    }

    @Builder
    public Product(User author, Content content, LocalDateTime termsAgreedAt) {
        this.author = author;
        this.termsAgreedAt = termsAgreedAt;
        apply(content);
    }

    /** 2.9 수정. 가능 여부(= DeliveryOrder 미존재) 판정은 서비스가 한다 (ERD_REVIEW 2-7). */
    public void update(Content content) {
        apply(content);
    }

    private void apply(Content content) {
        this.itemName = content.itemName();
        this.itemInfo = content.itemInfo();
        this.pickup = content.pickup();
        this.destination = content.destination();
        this.pickupTime = content.pickupTime();
        this.desiredArrivalTime = content.desiredArrivalTime();
        this.paymentType = content.paymentType();
        this.deliveryFee = content.deliveryFee();
    }

    /** 2.10 삭제. 이용내역에서 계속 참조되므로 소프트 삭제다. */
    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }
}
