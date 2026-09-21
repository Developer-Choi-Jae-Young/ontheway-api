package com.ontheway.repository;

/**
 * 물품 한 건의 일련번호. 같은 작성자가 올린 물품 중 몇 번째로 등록됐는지를 의미.
 * 보안 상 노출되어도 문제가 없으며, 내용이 같은 두 게시글을 사용자가 쉽게 분간할 수도 있음
 *
 * 삭제된 물품도 센다. 그래서 물품을 지워도 다른 물품의 번호가 밀리지 않고, 한 번 쓴 번호가 다시
 * 나오지도 않는다(지운 자리는 빈 번호로 남는다). 
 * 물품 ID 는 전 사용자가 함께 쓰는 카운터라 따로 만듦
 */
public interface ProductSerialNumber {

    Long getProductId();

    long getSerialNumber();
}
