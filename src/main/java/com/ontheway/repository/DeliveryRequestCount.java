package com.ontheway.repository;

/**
 * 경로 한 건에 붙어 있는, 아직 거절되지 않은 요청 수. 경로 목록의 {@code requestCount} 를 채운다.
 *
 * 주의: 요청이 0건인 경로는 아예 결과에 안 나온다. 서비스가 맵으로 묶을 때 없는 키는 0 으로 본다.
 */
public interface DeliveryRequestCount {

    Long getDeliveryId();

    long getRequestCount();
}
