package com.ontheway.repository;

import com.ontheway.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 신고 접수. 지금은 저장만 하고 조회 메서드가 없는데, 접수된 신고를 읽는 화면이 아직 없어서다.
 * 누적 집계나 "내 신고 목록"이 생기면 그때 추가한다.
 *
 * 주의: 신고 대상이 {@code entityType + entityId} 라 DB FK 가 없다. 대상이 실제로 있는지는
 * 서비스가 확인한다.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
}
