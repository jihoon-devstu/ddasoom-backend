package com.paw.ddasoom.report.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.ddasoom.member.domain.Member;
import com.paw.ddasoom.report.domain.Report;
import com.paw.ddasoom.report.domain.ReportStatus;
import com.paw.ddasoom.report.domain.ReportTargetType;

public interface ReportRepository extends JpaRepository<Report, Long> {

  // 상세 조회 — 응답에 신고자·처리자 닉네임이 둘 다 필요하므로 한 번에 fetch (N+1 차단)
  @EntityGraph(attributePaths = {"reporter", "processor"})
  Optional<Report> findByReportIdAndDeletedAtIsNull(Long reportId);
  
  // 중복 신고 선 검증(DB UNIQUE: 최후 방어선)
  boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, ReportTargetType targetType, Long targetId); 

  // 대상 누적 신고 건수 (상세 응답용 — 제재 판단 근거)
  // status 조건을 걸지 않아 PENDING+APPROVED+REJECTED가 모두 집계됨.
  // 반려(허위 판정)는 제재 근거에서 빠져야 하므로 status 조건 추가 검토 필요.
  // 조회 빈도(상세당 1회)와 데이터 규모상 status 전용 인덱스는 과설계 — idx_report_target으로 충분
  long countByTargetTypeAndTargetIdAndDeletedAtIsNull(ReportTargetType targetType, Long targetId);

  // 목록 — 필터 조합 4종 (qna 방식: null 분기 + 파생 쿼리. 필터 3개째부터 QueryDSL 전환)
  @EntityGraph(attributePaths = "reporter")
  Page<Report> findAllByDeletedAtIsNull(Pageable pageable);

  @EntityGraph(attributePaths = "reporter")
  Page<Report> findAllByStatusAndDeletedAtIsNull(ReportStatus status, Pageable pageable);

  @EntityGraph(attributePaths = "reporter")
  Page<Report> findAllByTargetTypeAndDeletedAtIsNull(ReportTargetType targetType, Pageable pageable);

  @EntityGraph(attributePaths = "reporter")
  Page<Report> findAllByStatusAndTargetTypeAndDeletedAtIsNull(ReportStatus status, ReportTargetType targetType, Pageable pageable);
}