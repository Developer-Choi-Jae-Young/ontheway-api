package com.ontheway.infra.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 배송이 끝나면 같이 버려도 되는 값이라 DB 대신 캐시에 둔다.
 * 임의로 정한 가정: 폴링이 2분 이상 끊기면(전달자 앱 종료 등) 오래된 좌표를 보여주느니 없는 걸로 본다.
 */
@Component
public class CaffeineLocationStore implements LocationStore {

    private final Cache<Long, Location> locationCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Override
    public void save(Long orderId, BigDecimal latitude, BigDecimal longitude, LocalDateTime updatedAt) {
        locationCache.put(orderId, new Location(latitude, longitude, updatedAt));
    }

    @Override
    public Optional<Location> find(Long orderId) {
        return Optional.ofNullable(locationCache.getIfPresent(orderId));
    }
}
