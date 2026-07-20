package com.paw.ddasoom.report.dto.response;

import java.time.LocalDateTime;

import com.paw.ddasoom.report.domain.Report;
import com.paw.ddasoom.report.domain.ReportReason;
import com.paw.ddasoom.report.domain.ReportStatus;
import com.paw.ddasoom.report.domain.ReportTargetType;

import lombok.Builder;
import lombok.Getter;

/*
 * [신고 상세 응답]
 * 정적 팩토리 from()에서 조립 — 엔티티(Report)를 컨트롤러 밖으로 노출하지 않기 위한 경계
 * targetReportCount는 엔티티 필드가 아니라 서비스가 별도 집계해 넘겨주는 값
 */
@Getter
@Builder
public class ReportDetailResponse {

  private Long reportId;
  private ReportTargetType targetType;
  private Long targetId;
  private ReportReason reason;
  private String content;
  private ReportStatus status;
  private String reporterNickname;
  private String processorNickname;
  private LocalDateTime processedAt;
  private long targetReportCount; // 대상 누적 신고 건수
  private LocalDateTime createdAt;

  public static ReportDetailResponse from(Report report, long targetReportCount) {
    return ReportDetailResponse.builder()
      .reportId(report.getReportId())
      .targetType(report.getTargetType())
      .targetId(report.getTargetId())
      .reason(report.getReason())
      .content(report.getContent())
      .status(report.getStatus())
      .reporterNickname(report.getReporter().getNickname())
      .processorNickname(report.getProcessor() != null ? report.getProcessor().getNickname() : null)
      .processedAt(report.getProcessedAt())
      .targetReportCount(targetReportCount)
      .createdAt(report.getCreatedAt())
      .build();
  }

}
