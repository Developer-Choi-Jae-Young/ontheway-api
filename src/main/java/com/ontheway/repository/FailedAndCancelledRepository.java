package com.ontheway.repository;

import com.ontheway.entity.FailedAndCancelled;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 취소(4.4) · 실패(4.6) 기록. 거래당 1건이다.
 *
 * <p>저장은 <b>거래 상태 전이와 같은 트랜잭션</b>에서 한다 —
 * {@code order.cancel()} 다음에 {@code FailedAndCancelled.canceledBy(...)} 를 저장한다.
 *
 * <p>사유는 4.6 기준으로 양쪽 당사자에게 노출되므로 조회 메서드를 둔다.
 */
public interface FailedAndCancelledRepository extends JpaRepository<FailedAndCancelled, Long> {

    Optional<FailedAndCancelled> findByOrderId(Long orderId);
}
