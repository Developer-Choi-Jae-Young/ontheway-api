package com.ontheway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자. 의뢰자·전달자 역할 구분 컬럼을 두지 않는다 — 어떤 글을 썼는지로만 구분한다.
 *
 * <p>탈퇴는 {@code deletedAt} 하나로 표현하며 별도 status 컬럼이 없다. 탈퇴 사유는
 * {@link UserWithdrawal} 에 남는다 (ERD_REVIEW 1-11).
 *
 * <p>테이블명 {@code user} 는 PostgreSQL·H2 모두 예약어라 백틱으로 감쌌다. Hibernate 가
 * 방언에 맞는 인용부호로 바꿔준다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "`user`", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_account_id", columnNames = "account_id"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 아이디. 명세 5.2 에서 수정 불가로 명시돼 있다. */
    @Column(nullable = false, updatable = false, length = 30)
    private String accountId;

    /**
     * BCrypt 해시 자체는 60자지만 Spring Security 권장인 {@code DelegatingPasswordEncoder} 는
     * {@code {bcrypt}} 접두사를 붙여 68자를 낸다. 알고리즘을 바꿔도 안 걸리게 여유를 둔다.
     *
     * <p>명세 1.1 의 "8~15자"는 평문 규칙이라 DTO 에 건다.
     */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    /** 명세의 "최대 7자"는 DTO 에서 강제한다. 여기는 상한선만 잡는다. */
    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 5.2 아바타. URL 한 칸만 둔다 — 원본명·크기 같은 메타데이터를 보관할 이유가 없고,
     * 교체하면 이전 값은 버려지므로 이력도 필요 없다. 증빙 사진({@link Image})을 별도
     * 엔티티로 뺀 것과 판단이 다른 이유다.
     *
     * <p>미설정이면 {@code null} 이고, 기본 아바타는 프론트가 정한다.
     */
    @Column(length = 500)
    private String profileImageUrl;

    @Column(nullable = false)
    private LocalDate birthDate;

    /**
     * 약관 동의 시각. 지금은 전 약관을 필수로 전제하므로 동의 여부를 따로 담지 않는다
     * — 동의해야만 가입되기 때문이다. 선택 약관이 정해지면 항목당
     * {@code xxxAgreedAt}(nullable) 한 칸씩 추가한다 (ERD_REVIEW 1-11).
     */
    @Column(nullable = false)
    private LocalDateTime termsAgreedAt;

    /** 소프트 삭제 = 탈퇴. 전역 필터를 걸지 않고 조회마다 개별로 거른다 (ERD_REVIEW 2-2). */
    private LocalDateTime deletedAt;

    @Builder
    public User(String accountId, String password, String email, boolean emailVerified,
                String nickname, String name, LocalDate birthDate, LocalDateTime termsAgreedAt) {
        this.accountId = accountId;
        this.password = password;
        this.email = email;
        this.emailVerified = emailVerified;
        this.nickname = nickname;
        this.name = name;
        this.birthDate = birthDate;
        this.termsAgreedAt = termsAgreedAt;
    }

    public boolean isWithdrawn() {
        return deletedAt != null;
    }

    /** 5.6 회원탈퇴. 사유는 {@link UserWithdrawal} 을 같은 트랜잭션에서 함께 저장한다. */
    public void withdraw(LocalDateTime now) {
        this.deletedAt = now;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeEmail(String email) {
        this.email = email;
        this.emailVerified = true;
    }

    public void changeBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /** 5.2 아바타 변경. {@code null} 을 넣으면 기본 아바타로 되돌아간다. */
    public void changeProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
