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
 * 의뢰 물품 게시글.
 *
 * 경로와 독립적이라 같은 물품을 여러 경로에 동시에 걸어둘 수 있다. 그래서 물품에는 상태
 * 컬럼이 없고, 이미 수락된 거래에 들어갔는지는 DeliveryOrder 가 있는지로 판단한다.
 *
 * 수령/도착 시각은 날짜까지 받는다. 경로가 정해지기 전에 작성돼 기준 날짜가 없다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product", indexes =
        // 내 의뢰 목록, 이용내역의 진입점
        @Index(name = "idx_product_author", columnList = "author_id, deleted_at, id"))
public class Product extends BaseTimeEntity {

    // 컬럼 크기. 입력 검증(ProductService)도 이 값을 그대로 쓴다
    public static final int NAME_MAX_LENGTH = 100;
    public static final int INFO_MAX_LENGTH = 500;
    public static final int ADDRESS_MAX_LENGTH = 255;
    public static final int COORDINATE_SCALE = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 의뢰자. 요청의 의뢰자도 이 값을 가져다 쓴다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_product_author"))
    private User author;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String itemName;

    @Column(length = INFO_MAX_LENGTH)
    private String itemInfo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address", nullable = false, length = ADDRESS_MAX_LENGTH)),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude", nullable = false, precision = 10, scale = COORDINATE_SCALE)),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude", nullable = false, precision = 10, scale = COORDINATE_SCALE))
    })
    private Location pickup;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "destination_address", nullable = false, length = ADDRESS_MAX_LENGTH)),
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude", nullable = false, precision = 10, scale = COORDINATE_SCALE)),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude", nullable = false, precision = 10, scale = COORDINATE_SCALE))
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

    /** 거래 금액. 수락되면 물품 수정이 막히므로 이후로는 바뀌지 않는다. */
    @Column(nullable = false)
    private Integer deliveryFee;

    /** 허용물품/책임 동의 시각. 동의해야만 등록되므로 동의 여부는 따로 담지 않는다. */
    @Column(nullable = false)
    private LocalDateTime termsAgreedAt;

    private LocalDateTime deletedAt;

    /**
     * 등록과 수정이 함께 쓰는 본문.
     *
     * 같은 타입이 두 개씩 있어서({@code Location}, {@code LocalDateTime}) 위치 인자로 받으면
     * 순서를 바꿔 넘겨도 컴파일이 통과한다. 이름을 붙여 받으려고 record 로 묶었다.
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

    /** 수정. 수정할 수 있는 상태인지는 서비스가 먼저 확인한다. */
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

    /** 삭제. 이용내역이 계속 참조하므로 소프트 삭제다. */
    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }
}
