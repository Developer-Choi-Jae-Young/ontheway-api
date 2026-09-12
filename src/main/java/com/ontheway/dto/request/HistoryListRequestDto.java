package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HistoryListRequestDto {
    @Schema(description = "페이지 번호")
    private int page;
    @Schema(description = "페이지당 항목수")
    private int size;
}
