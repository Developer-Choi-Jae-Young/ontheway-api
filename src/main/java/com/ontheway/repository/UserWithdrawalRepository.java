package com.ontheway.repository;

import com.ontheway.entity.UserWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 탈퇴 사유. 여러 개를 고르면 한 사용자에 여러 행이 들어간다.
 * {@code User.withdraw()} 와 이 행 저장은 같은 트랜잭션에서 한다.
 *
 * 지금은 쓰기 전용이다. 모아둔 사유를 다시 읽는 화면이 없어서, 통계 화면이 생기면 그때 넣는다.
 */
public interface UserWithdrawalRepository extends JpaRepository<UserWithdrawal, Long> {
}
