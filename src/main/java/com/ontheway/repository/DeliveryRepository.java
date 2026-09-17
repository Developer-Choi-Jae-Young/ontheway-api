package com.ontheway.repository;

import com.ontheway.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 전달자 경로 게시글.
 *
 * <p>목록 두 개(2.2 전체 피드 / 6.1 내 게시글 전달 탭)는 동적 조건이 붙어
 * {@link DeliveryRepositoryCustom} 으로 빠져 있다.
 *
 * <p>여기에 "게시 상태" 조건은 없다 — {@code Delivery.status} 컬럼 자체를 두지 않고
 * 공개 종료는 {@code DeliveryOrder} 존재 여부로 판정하기 때문이다 (ERD_REVIEW 1-1).
 */
public interface DeliveryRepository extends JpaRepository<Delivery, Long>, DeliveryRepositoryCustom {

    Optional<Delivery> findByIdAndDeletedAtIsNull(Long id);

    /** 2.1 최근 게시물 불러오기 — 직전에 쓴 글 1건을 양식에 채워준다. 삭제글은 제외한다. */
    Optional<Delivery> findTopByAuthorIdAndDeletedAtIsNullOrderByIdDesc(Long authorId);
}
