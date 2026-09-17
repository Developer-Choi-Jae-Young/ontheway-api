package com.ontheway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 생성 시각만 갖는 엔티티의 공통 상위 타입.
 *
 * <p>기록성 테이블(Image, FailedAndCancelled, Report, UserWithdrawal)은 수정되지 않으므로
 * updatedAt 을 두지 않는다. 수정이 있는 엔티티는 {@link BaseTimeEntity} 를 상속한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseCreatedEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
