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
 * 탈퇴 사유. 명세 5.6 이 "드롭다운 체크박스로 탈퇴 사유를 표시하여 제출한다"라
 * 복수 선택일 수 있어 컬럼이 아닌 행으로 둔다 (ERD_REVIEW 1-11).
 *
 * <p>{@code unique(user, reason)} 은 같은 사유를 두 번 고르는 것만 막는다. 단일 선택이면
 * 사용자당 1행, 복수 선택이면 N행이 되므로 양쪽 다 이 스키마로 커버된다.
 *
 * <p>사유 목록이 명세에 없어 아직 enum 으로 만들지 않았다. 확정되면 {@code String} 을
 * enum + {@code @Enumerated(STRING)} 으로 바꾸면 되고 컬럼 타입은 그대로다.
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
