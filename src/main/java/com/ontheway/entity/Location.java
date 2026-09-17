package com.ontheway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주소 + 좌표 한 쌍. 경로의 출발지·도착지, 물품의 수령지·목적지에 쓰인다.
 *
 * <p>좌표는 DECIMAL(10,7) — 소수 7자리는 약 1.1cm 이고 정수부 3자리라 경도 ±180 까지 담긴다.
 * DOUBLE 은 오차가 누적되므로 쓰지 않는다 (ERD_REVIEW 1-9).
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Builder
    public Location(String address, BigDecimal latitude, BigDecimal longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
