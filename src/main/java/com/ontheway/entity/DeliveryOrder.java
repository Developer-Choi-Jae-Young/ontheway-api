package com.ontheway.entity;

import com.ontheway.enums.DeliveryStatus;
import jakarta.persistence.Column;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 성사된 배송 거래. 수락 시점에만 생성되는 {@link Request} 의 1:0..1 확장이다.
 *
 * <p><b>경로·물품 스냅샷을 두지 않는다.</b> 두면 연쇄 규칙 (A)(B)를 UNIQUE 로 강제할 수 있지만,
 * 컬럼 2개를 아끼고 동시 수락 레이스를 감수하기로 했다. 규칙은 수락 트랜잭션 앞의
 * {@code existsByRequest_Delivery_Id}/{@code existsByRequest_Product_Id} 검증으로 지킨다
 * (ERD_REVIEW 2-5 d).
 *
 * <p>{@code uk_order_request} 는 <b>같은 요청을 두 번 수락하는 것</b>(더블 클릭·재시도)만 막는다.
 *
 * <p>거래 금액 스냅샷도 없다 — {@code request.product.deliveryFee} 가 수락 이후 불변이다 (1-1).
 *
 * <p>수락 시각 컬럼도 없다. 이 행은 수락 시점에만 생기므로 {@code created_at} 과 같은 값이다 —
 * 취소·실패 시각을 {@code failed_and_cancelled.created_at} 으로 대체한 것과 같은 판단이다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery_order",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_request", columnNames = "request_id"),
        // 스케줄러 ②: 확인요청 후 72시간 경과 건 조회
        indexes = @Index(name = "idx_order_completion_req",
                columnList = "status, completion_requested_at"))
public class DeliveryOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_order_request"))
    private Request request;

    /** 픽업중 이후의 값만 저장된다. 매칭대기중·거절은 Request 쪽에서 유도한다. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)   // H2가 네이티브 enum 타입을 쓰지 않도록 고정 (1-9)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status;

    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveryStartedAt;
    private LocalDateTime completionRequestedAt;
    private LocalDateTime completedAt;

    private DeliveryOrder(Request request) {
        this.request = request;
        this.status = DeliveryStatus.PICKING_UP;   // 수락 직후 상태는 픽업중
    }

    /** 전달자 수락. 호출 전에 서비스가 경로·물품 중복을 검증한다 (2-5 d). */
    public static DeliveryOrder accept(Request request) {
        return new DeliveryOrder(request);
    }

    /** 수락 시각. 이 행이 수락 시점에만 생기므로 생성 시각이 곧 매칭 시각이다. */
    public LocalDateTime getMatchedAt() {
        return getCreatedAt();
    }

    // ── 상태 전이 ──────────────────────────────────────────────
    // 전이 가능 여부(선행 상태·권한·증빙 사진 유무)는 서비스가 판단한다 (ERD_REVIEW 2-7).

    /** 픽업 완료 버튼 → 배송대기중. 이후 취소 불가. */
    public void pickUp(LocalDateTime now) {
        this.status = DeliveryStatus.DELIVERY_WAITING;
        this.pickedUpAt = now;
    }

    /** 예정 배송시간 도달 → 배송중. 스케줄러가 호출한다. */
    public void startDelivery(LocalDateTime now) {
        this.status = DeliveryStatus.DELIVERING;
        this.deliveryStartedAt = now;
    }

    /** 증빙 사진 등록 후 확인요청. Image 행이 없으면 서비스가 거부한다. */
    public void requestCompletion(LocalDateTime now) {
        this.status = DeliveryStatus.COMPLETION_REQUESTED;
        this.completionRequestedAt = now;
    }

    /** 의뢰자 수락 또는 72시간 경과 → 배송완료. 후기 작성이 가능해지는 유일한 상태다. */
    public void complete(LocalDateTime now) {
        this.status = DeliveryStatus.COMPLETED;
        this.completedAt = now;
    }

    /** 4.6 배송 실패. 기록은 FailedAndCancelled 에 남는다. */
    public void fail() {
        this.status = DeliveryStatus.FAILED;
    }

    /** 4.4 매칭 배송 취소. 픽업 완료 이후에는 호출할 수 없다(서비스가 검사). */
    public void cancel() {
        this.status = DeliveryStatus.CANCELED;
    }

    public boolean isCompleted() {
        return status == DeliveryStatus.COMPLETED;
    }

    // ── request 경유 접근 ─────────────────────────────────────
    // 아래 셋은 공짜가 아니다. request 가 LAZY 프록시라 한 번에 SELECT 가 1~2회 더 나가고,
    // 목록에서 N건을 돌면 N배가 된다. 조회 쿼리에 join fetch 를 걸어두고 쓸 것 (2-4).

    public Delivery getDelivery() {
        return request.getDelivery();
    }

    public Product getProduct() {
        return request.getProduct();
    }

    /** 거래 금액. 스냅샷이 아니라 물품 게시글의 값을 그대로 읽는다 (B3). */
    public Integer getDeliveryFee() {
        return request.getProduct().getDeliveryFee();
    }
}
