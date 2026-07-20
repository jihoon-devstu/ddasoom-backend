package com.paw.ddasoom.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * [cross-field 규칙을 enum이 스스로 보유]
 * "ETC면 상세 사유(content) 필수"라는 규칙을 서비스 if문에 흩어놓지 않고 enum 상수에 부착
 * 덕분에 사유가 추가돼도 enum 한 줄 확장으로 끝나고, 검증 로직은 손대지 않아도 됨
 */
@Getter
@RequiredArgsConstructor
public enum ReportReason {
  SPAM(false),                // 스팸/광고
  ABUSE(false),              // 욕설/비방
  INAPPROPRIATE(false),      // 부적절한 내용
  FRAUD(false),              // 사기/허위
  ETC(true);                 // 기타 — 상세 내용(content) 필수

  private final boolean contentRequired; // true면 접수 시 content 필수 (ReportService.createReport에서 검증)
}
