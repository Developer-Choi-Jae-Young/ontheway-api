package com.ontheway.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 전달자 이동 경로 게시글.
 *
 * <p><b>상태 컬럼을 두지 않는다.</b> 2.1·2.4 의 "게시 상태"는 전부 유도된다 —
 * 공개 종료는 DeliveryOrder 존재, "시작시간 지난 글 제외"는 날짜 비교,
 * "요청 들어오면 수정 불가"는 Request 존재로 판정한다 (ERD_REVIEW 1-1).
 *
 * <p>배송 날짜와 시간이 분리돼 있어 시간 쪽은 {@code LocalTime} 이다. 물품 게시글과 다른 이유는
 * 1-9 (3) 참조.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery", indexes = {
        // 2.2 목록 정렬 + 필터
        @Index(name = "idx_delivery_feed", columnList = "delivery_date, id"),
        // 2.1 최근 게시물 불러오기 / 6.1 내 게시글 / 7 전달내역
        @Index(name = "idx_delivery_author", columnList = "author_id, deleted_at, id")
})
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 전달자. Request 에 복사해 두지 않고 여기서 조인으로 얻는다 (1-6). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_delivery_author"))
    private User author;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "departure_address", nullable = false, length = 255)),
            @AttributeOverride(name = "latitude", column = @Column(name = "departure_latitude", nullable = false, precision = 10, scale = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "departure_longitude", nullable = false, precision = 10, scale = 7))
    })
    private Location departure;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "destination_address", nullable = false, length = 255)),
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude", nullable = false, precision = 10, scale = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude", nullable = false, precision = 10, scale = 7))
    })
    private Location destination;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @Column(nullable = false)
    private LocalTime plannedStartTime;

    @Column(nullable = false)
    private LocalTime plannedEndTime;

    /** 2.2 / 6.1 희망금액 필터의 기준값. 필터는 상한 하나라 {@code desiredPrice <= 입력값} 이다. */
    @Column(nullable = false)
    private Integer desiredPrice;

    @Column(length = 500)
    private String additionalInfo;

    private LocalDateTime deletedAt;

    /**
     * 등록·수정에서 함께 쓰는 본문. 같은 타입의 값이 여러 개라({@code Location} 2개,
     * {@code LocalTime} 2개) 위치 인자로 넘기면 출발지·도착지가 뒤바뀌어도 컴파일이 통과한다.
     */
    @Builder
    public record Content(
            Location departure, Location destination,
            LocalDate deliveryDate, LocalTime plannedStartTime, LocalTime plannedEndTime,
            Integer desiredPrice, String additionalInfo) {
    }

    @Builder
    public Delivery(User author, Content content) {
        this.author = author;
        apply(content);
    }

    /** 2.4 수정. 가능 여부(= Request 미존재) 판정은 서비스가 한다. */
    public void update(Content content) {
        apply(content);
    }

    private void apply(Content content) {
        this.departure = content.departure();
        this.destination = content.destination();
        this.deliveryDate = content.deliveryDate();
        this.plannedStartTime = content.plannedStartTime();
        this.plannedEndTime = content.plannedEndTime();
        this.desiredPrice = content.desiredPrice();
        this.additionalInfo = content.additionalInfo();
    }

    /** 2.5 삭제. 이용내역에서 계속 참조되므로 소프트 삭제다. */
    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }
}
