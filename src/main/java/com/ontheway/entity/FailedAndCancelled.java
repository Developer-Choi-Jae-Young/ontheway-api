package com.ontheway.entity;

import com.ontheway.enums.ResultType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 취소(4.4) · 실패(4.6) 기록. 성사된 거래에만 생기고 거래당 1건이다 —
 * 두 사건 모두 종료 상태이고 시점도 겹치지 않는다(취소는 픽업완료 전, 실패는 후).
 *
 * <p>{@code actor} 는 명세에 근거가 있다. 4.4 의 입력 항목이
 * {@code 거래식별정보 / 요청자 / 취소사유} 이고 권한 규칙이 "의뢰자 또는 전달자가 취소를
 * 제출한다"이므로, 양쪽 다 가능한 이상 누구였는지를 남겨야 구분된다 (ERD_REVIEW 1-10).
 * 실패는 전달자로 고정되지만 한 테이블이라 컬럼은 하나로 쓴다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "failed_and_cancelled",
        uniqueConstraints = @UniqueConstraint(name = "uk_fnc_order", columnNames = "order_id"))
public class FailedAndCancelled extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_fnc_order"))
    private DeliveryOrder order;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ResultType resultType;

    /** 취소·실패를 실행한 사람. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_fnc_actor"))
    private User actor;

    /** 4.6 실패 사유는 게시자·피게시자 양쪽에 노출된다. */
    @Column(length = 500)
    private String reason;

    private FailedAndCancelled(DeliveryOrder order, ResultType resultType, User actor, String reason) {
        this.order = order;
        this.resultType = resultType;
        this.actor = actor;
        this.reason = reason;
    }

    // 아래 둘은 "이렇게 끝났다"는 기록만 만든다. 거래의 상태 전이는 하지 않는다 —
    // 전이는 서비스가 order.cancel() / order.fail() 로 직접 한다 (ERD_REVIEW 2-7).
    //
    //     order.cancel();
    //     repository.save(FailedAndCancelled.canceledBy(order, actor, reason));

    /** 4.4 취소. 의뢰자·전달자 모두 가능하며 픽업 완료 이후에는 불가(서비스가 검사). */
    public static FailedAndCancelled canceledBy(DeliveryOrder order, User actor, String reason) {
        return new FailedAndCancelled(order, ResultType.CANCELED, actor, reason);
    }

    /** 4.6 배송 실패. 전달자만 가능하며 배송대기중·배송중에서만 호출된다. */
    public static FailedAndCancelled failedBy(DeliveryOrder order, User actor, String reason) {
        return new FailedAndCancelled(order, ResultType.FAILED, actor, reason);
    }
}
