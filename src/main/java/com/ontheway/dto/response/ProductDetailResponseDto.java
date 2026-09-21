package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponseDto {
    @Schema(description = "물품 ID")
    private Long productId;
    @Schema(description = "물품명")
    private String productName;
    @Schema(description = "사용자 이미지")
    private String userImage;
    @Schema(description = "사용자 이름")
    private String userName;
    @Schema(description = "물품 수령지(주소)")
    private String productDeliveryAddress;
    // 수정 화면이 이 응답으로 폼을 채운다. 주소를 안 바꾸는 사용자도 수정 요청에 좌표를 실어야 해서 돌려준다
    @Schema(description = "물품 수령지 위도. 수정 요청의 productDeliveryLatitude 와 같은 값")
    private BigDecimal productDeliveryLatitude;
    @Schema(description = "물품 수령지 경도. 수정 요청의 productDeliveryLongitude 와 같은 값")
    private BigDecimal productDeliveryLongitude;
    @Schema(description = "배송 목적지")
    private String deliveryDestination;
    @Schema(description = "배송 목적지 위도. 수정 요청의 endLatitude 와 같은 값")
    private BigDecimal endLatitude;
    @Schema(description = "배송 목적지 경도. 수정 요청의 endLongitude 와 같은 값")
    private BigDecimal endLongitude;
    @Schema(description = "물품 정보")
    private String productInfo;
    @Schema(description = "배송료")
    private Integer deliveryFee;
    @Schema(description = "물건 수령 시간")
    private String receivingTime;
    @Schema(description = "희망배도착시간")
    private String desiredDeliveryTime;
    @Schema(description = "결제 방식")
    private String paymentType;
    @Schema(description = "게시 등록일")
    private LocalDateTime createdAt;
}
