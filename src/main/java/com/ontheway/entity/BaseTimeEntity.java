package com.ontheway.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/** 생성/수정 시각을 함께 갖는 엔티티의 공통 상위 타입. */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity {

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
