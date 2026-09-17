package com.ontheway.repository;

/**
 * 경로 한 건에 붙어 있는 <b>살아 있는</b> 의뢰 요청 수.
 * 2.2 목록의 {@code requestCount} 를 채운다.
 *
 * <p>요청이 0건인 경로는 <b>결과에 나오지 않는다</b>({@code group by} 의 성질).
 * 서비스가 맵으로 묶을 때 없는 키는 0 으로 본다.
 */
public interface DeliveryRequestCount {

    Long getDeliveryId();

    long getRequestCount();
}
