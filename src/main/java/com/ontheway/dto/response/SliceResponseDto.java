package com.ontheway.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SliceResponseDto<T> {
    private List<T> content;
    private boolean hasNext;
}
