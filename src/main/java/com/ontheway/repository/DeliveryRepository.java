package com.ontheway.repository;

import com.ontheway.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 전달자 경로 게시글.
 *
 * 목록 두 개(전체 피드, 내 게시글의 전달 탭)는 동적 조건이 붙어
 * {@link DeliveryRepositoryCustom} 에 따로 있다.
 *
 * 여기 메서드에는 게시 상태 조건이 없다. Delivery 에 상태 컬럼이 없어서 공개가 끝났는지는
 * DeliveryOrder 가 있는지로 본다.
 */
public interface DeliveryRepository extends JpaRepository<Delivery, Long>, DeliveryRepositoryCustom {

    Optional<Delivery> findByIdAndDeletedAtIsNull(Long id);

    /** 최근 게시물 불러오기. 직전에 쓴 글 한 건을 등록 양식에 채워준다. 삭제글은 뺀다. */
    Optional<Delivery> findTopByAuthorIdAndDeletedAtIsNullOrderByIdDesc(Long authorId);
}
