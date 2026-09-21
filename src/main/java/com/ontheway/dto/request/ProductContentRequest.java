package com.ontheway.dto.request;

import com.ontheway.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 물품 등록과 수정이 똑같이 받는 본문. 서비스가 두 DTO 를 한 메서드로 검증하려고 둔다.
 * 구현은 각 DTO 의 Lombok getter 가 한다.
 */
public interface ProductContentRequest {

    String getProductName();

    String getProductInfo();

    String getProductDeliveryAddress();

    BigDecimal getProductDeliveryLatitude();

    BigDecimal getProductDeliveryLongitude();

    String getEndAddress();

    BigDecimal getEndLatitude();

    BigDecimal getEndLongitude();

    LocalDateTime getReceivingTime();

    LocalDateTime getDesiredDeliveryTime();

    PaymentType getPaymentType();

    Integer getDeliveryFee();
}
