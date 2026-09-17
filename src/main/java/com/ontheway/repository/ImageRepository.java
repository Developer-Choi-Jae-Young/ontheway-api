package com.ontheway.repository;

import com.ontheway.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 배송완료 증빙 사진. 테이블은 {@code file}, 엔티티는 {@code Image} 다.
 *
 * <p>{@code existsByOrderId} 는 <b>COMPLETION_REQUESTED 전이의 전제 조건</b>을 확인하는 데 쓴다.
 * 사진이 없으면 전이를 막는 판정 자체는 서비스가 한다 (2-7).
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}
