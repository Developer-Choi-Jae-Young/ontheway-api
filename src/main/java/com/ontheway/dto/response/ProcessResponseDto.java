package com.ontheway.dto.response;

import com.ontheway.enums.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessResponseDto {
    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
    //처리 후 배송 상태가 응답으로 제공되어야 API가 어떤 결과를 만들었는지 알 수 있음
    @Schema(description = "처리 후 배송 상태")
    private DeliveryStatus deliveryStatus;
}
