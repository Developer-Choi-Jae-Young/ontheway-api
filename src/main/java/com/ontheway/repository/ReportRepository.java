package com.ontheway.repository;

import com.ontheway.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 신고 접수. 4.3.
 *
 * <p>신고 대상은 {@code entityType + entityId} 다형성 참조라 DB FK 가 없다 —
 * <b>대상이 실제로 존재하는지는 서비스가 확인한다</b> (1-2).
 *
 * <p><b>조회 메서드가 하나도 없는 게 맞다.</b> MVP 에 신고를 처리하는 단계 자체가 없고(B8),
 * {@code ReportController} 도 등록 두 개뿐이다. 신고 누적 집계나 "내 신고 목록"을 읽을 화면이
 * 생기면 그때 추가한다 — 화면 없는 메서드를 미리 두면 인덱스와 페이징을 검증할 길이 없다.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
}
