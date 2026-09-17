package com.ontheway.repository;

import com.ontheway.entity.UserWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 5.6 탈퇴 사유. 복수 선택이면 한 사용자에 여러 행이 들어간다 (ERD_REVIEW 1-11).
 *
 * <p>탈퇴 처리는 {@code User.withdraw()} 와 이 행 저장을 <b>같은 트랜잭션</b>에서 한다.
 *
 * <p>지금은 <b>쓰기 전용</b>이다. 모아둔 사유를 다시 읽는 화면이 명세에 없어 조회 메서드를 두지
 * 않는다. 통계 화면이 생기면 그때 추가한다.
 */
public interface UserWithdrawalRepository extends JpaRepository<UserWithdrawal, Long> {
}
