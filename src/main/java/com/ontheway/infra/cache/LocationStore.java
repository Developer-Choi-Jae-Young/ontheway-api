package com.ontheway.infra.cache;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 배송중인 전달자의 실시간 GPS 좌표. DeliveryOrder ID를 키로 쓴다.
 */
public interface LocationStore {

    void save(Long orderId, BigDecimal latitude, BigDecimal longitude, LocalDateTime updatedAt);

    Optional<Location> find(Long orderId);

    record Location(BigDecimal latitude, BigDecimal longitude, LocalDateTime updatedAt) {
    }
}
