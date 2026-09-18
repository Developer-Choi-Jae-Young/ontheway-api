package com.ontheway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 탈퇴 사유. 체크박스로 받아 여러 개일 수 있어서 컬럼이 아니라 행으로 쌓는다.
 * {@code unique(user, reason)} 은 같은 사유를 두 번 고르는 것만 막는다.
 *
 * 사유 목록이 아직 안 정해져서 {@code String} 이다. 정해지면 enum 으로 바꾸면 되고
 * 컬럼 타입은 그대로다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_withdrawal",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_withdrawal",
                columnNames = {"user_id", "reason"}))
public class UserWithdrawal extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_user_withdrawal_user"))
    private User user;

    @Column(nullable = false, length = 40)
    private String reason;

    @Builder
    public UserWithdrawal(User user, String reason) {
        this.user = user;
        this.reason = reason;
    }
}
