package com.ontheway.repository;

import com.ontheway.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 의뢰 물품 게시글.
 *
 * <p><b>전역 물품 피드는 없다.</b> 물품 게시글을 모아 보는 화면은 6.6 "내 게시글 &gt; 의뢰 목록"
 * 하나뿐이고, 전달자가 의뢰글을 탐색하는 게시판은 존재하지 않는다 (SPEC_REVIEW 2-3).
 * 그래서 목록 조회가 전부 작성자 스코프다.
 *
 * <p>수정·삭제 가능 여부는 여기서 판정하지 않는다 — "이미 거래에 쓰였는가"는
 * {@link DeliveryOrderRepository#existsByRequest_Product_Id(Long)} 이고, 두 결과로 결론을
 * 내는 건 서비스다 (ERD_REVIEW 2-7).
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 6.6 내 게시글 - 의뢰 목록. 명세의 필터는 <b>물품명 검색어</b> 하나다.
     *
     * <p>이 메서드를 쓴다. {@code keyword} 는 <b>날것 그대로</b> 넘기면 되고, {@code null} 이나
     * 공백이면 조건이 붙지 않는다 — {@code like} 패턴 조립과 이스케이프는 여기서 처리한다.
     */
    default Slice<Product> searchMyProducts(Long authorId, String keyword, Pageable pageable) {
        return findMyProductsByNamePattern(authorId, LikePatterns.contains(keyword), pageable);
    }

    /**
     * {@link #searchMyProducts} 가 쓰는 실제 쿼리. {@code namePattern} 은 이미 이스케이프된
     * {@code like} 패턴이라 직접 부르지 않는다.
     *
     * <p>목록 표시 항목이 일련번호·물품명·배송비뿐이라(6.6) 작성자를 페치하지 않는다 —
     * 어차피 로그인한 본인이다.
     */
    @Query("select p from Product p"
            + " where p.author.id = :authorId"
            + "   and p.deletedAt is null"
            // escape 문자를 리터럴로 박으면 LikePatterns.ESCAPE 를 바꿨을 때 여기만 어긋나고,
            // 예외 없이 검색 결과만 틀려진다. 그래서 같은 상수를 끼워 넣는다
            + "   and (:namePattern is null"
            + "        or p.itemName like :namePattern escape '" + LikePatterns.ESCAPE + "')"
            + " order by p.id desc")
    Slice<Product> findMyProductsByNamePattern(@Param("authorId") Long authorId,
                                               @Param("namePattern") String namePattern,
                                               Pageable pageable);
}
