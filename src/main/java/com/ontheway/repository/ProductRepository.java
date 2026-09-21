package com.ontheway.repository;

import com.ontheway.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 의뢰 물품 게시글.
 *
 * 목록 조회가 전부 작성자 기준이다. 물품을 모아 보는 화면이 "내 게시글 > 의뢰 목록" 하나뿐이고,
 * 전달자가 남의 의뢰글을 훑는 게시판은 없다.
 *
 * 수정/삭제가 가능한지는 여기서 안 본다. 수락된 거래에 들어갔는지를
 * {@link DeliveryOrderRepository#existsByRequest_Product_Id(Long)} 가 답하고, 결론은 서비스가 낸다.
 *
 * 주의: 물품은 수락돼야 잠기는데 경로는 요청이 오기만 해도 잠긴다. 기준이 서로 다르다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 물품 행 잠금. 수락과 물품 수정·삭제가 쓴다.
     *
     * 수락: 같은 물품이 서로 다른 경로에서 동시에 수락되는 걸 막는다. 경로 쪽 잠금은 경로별이라 이 경쟁을
     * 못 잡는다. 데드락을 피하려고 항상 {@link DeliveryRepository#findByIdForUpdate} 로 경로를 먼저 잠근 뒤에 부른다.
     *
     * 수정·삭제: 수락과 같은 행을 잠근 뒤에 수락 여부를 확인해야 방금 수락된 물품이 바뀌지 않는다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    /**
     * 내 게시글의 의뢰 목록. 필터는 물품명 검색어 하나다.
     *
     * {@code keyword} 는 사용자가 친 그대로 넘기면 된다. {@code null} 이나 공백이면 조건이
     * 안 붙고, {@code like} 패턴 조립과 이스케이프는 여기서 한다.
     */
    default Slice<Product> searchMyProducts(Long authorId, String keyword, Pageable pageable) {
        return findMyProductsByNamePattern(authorId, LikePatterns.contains(keyword), pageable);
    }

    /**
     * {@link #searchMyProducts} 가 쓰는 실제 쿼리. {@code namePattern} 은 이스케이프까지 끝난
     * {@code like} 패턴이라 이 메서드를 직접 부르지는 않는다.
     *
     * 작성자는 페치하지 않는다. 목록에 일련번호, 물품명, 배송비만 찍히고 작성자는 로그인한
     * 본인이라 쓸 일이 없다.
     */
    @Query("select p from Product p"
            + " where p.author.id = :authorId"
            + "   and p.deletedAt is null"
            // escape 문자를 리터럴로 박으면 LikePatterns.ESCAPE 를 바꿨을 때 여기만 안 따라오고,
            // 예외도 없이 검색 결과만 틀어진다. 그래서 같은 상수를 끼워 넣는다
            + "   and (:namePattern is null"
            + "        or p.itemName like :namePattern escape '" + LikePatterns.ESCAPE + "')"
            + " order by p.id desc")
    Slice<Product> findMyProductsByNamePattern(@Param("authorId") Long authorId,
                                               @Param("namePattern") String namePattern,
                                               Pageable pageable);

    /**
     * 물품 ID 목록에 대응하는 '사용자별 등록 순번(일련번호)'을 단일 쿼리로 일괄 조회
     * 
     * [N+1 방지] 한 페이지 물품들의 '사용자별 등록 순번'을 IN 절로 묶어 1번에 조회
     * 서브쿼리 카운트 시 deletedAt을 검사하지 않아, 물품 삭제 시에도 기존 일련번호가 밀리지 않음
     */
    @Query("""
            select p.id as productId,
                   (select count(q) from Product q
                     where q.author.id = p.author.id and q.id <= p.id) as serialNumber
              from Product p
             where p.id in :ids
            """)
    List<ProductSerialNumber> findSerialNumbers(@Param("ids") Collection<Long> ids);
}
