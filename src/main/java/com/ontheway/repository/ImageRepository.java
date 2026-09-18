package com.ontheway.repository;

import com.ontheway.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 배송완료 증빙 사진. 테이블은 {@code file}, 엔티티는 {@code Image} 다.
 *
 * {@code existsByOrderId} 는 확인요청으로 넘어가도 되는지 볼 때 쓴다. 사진이 없을 때 막는 건
 * 서비스가 한다.
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}
